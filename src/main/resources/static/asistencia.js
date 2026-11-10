// ---------- Sesión ----------
const correoSesion = sessionStorage.getItem("correo");
if (correoSesion) {
  document.getElementById("usuarioActual").textContent = correoSesion;
}
document.getElementById("btnSalir").addEventListener("click", () => {
  sessionStorage.clear();
  window.location.href = "login.html";
});

// ---------- Utilidades ----------
function esc(texto) {
  return String(texto ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

async function pedir(url, opciones) {
  try {
    const res = await fetch(url, opciones);
    let datos = null;
    try { datos = await res.json(); } catch (e) { /* sin cuerpo */ }
    return { ok: res.ok, datos: datos };
  } catch (e) {
    return { ok: false, datos: { mensaje: "No se pudo conectar con el servidor" } };
  }
}

const $ = (id) => document.getElementById(id);

function mostrar(texto, esError) {
  const msg = $("msgAsistencia");
  msg.textContent = texto || "";
  msg.className = "mensaje " + (texto ? (esError ? "error" : "exito") : "");
}

const ESTADOS = [
  { valor: "PRESENTE", texto: "Presente" },
  { valor: "TARDANZA", texto: "Tardanza" },
  { valor: "FALTA", texto: "Falta" },
  { valor: "JUSTIFICADA", texto: "Justificada" }
];

// Fecha local en formato AAAA-MM-DD (sin pasar por UTC)
function aTexto(f) {
  return f.getFullYear() + "-" + String(f.getMonth() + 1).padStart(2, "0") + "-" + String(f.getDate()).padStart(2, "0");
}

// Hoy; si es fin de semana, el viernes anterior
function fechaInicial() {
  const f = new Date();
  if (f.getDay() === 6) f.setDate(f.getDate() - 1);
  if (f.getDay() === 0) f.setDate(f.getDate() - 2);
  return aTexto(f);
}

let filas = [];       // alumnos de la lista cargada
let seccionActual = null;
let seccionEtiqueta = "";
let fechaActual = null;

// ---------- Secciones ----------
async function cargarSecciones() {
  const r = await pedir("/api/secciones");
  const secciones = r.ok && Array.isArray(r.datos) ? r.datos : [];
  $("aSeccion").innerHTML = '<option value="">Sección</option>' +
    secciones.map((s) => '<option value="' + s.id + '">' + esc(s.gradoNombre + " - Sección " + s.nombre) + "</option>").join("");
}

// ---------- Lista de asistencia ----------
function dibujarLista() {
  $("tablaAsistencia").innerHTML = filas.map((f, i) =>
    "<tr><td>" + (i + 1) + "</td><td>" + esc(seccionEtiqueta) + "</td><td>" + esc(f.alumnoDni) + "</td><td>" + esc(f.alumnoNombre) + "</td>" +
    '<td class="estados">' + ESTADOS.map((e) =>
      '<label><input type="radio" name="est-' + f.matriculaId + '" value="' + e.valor + '"' +
      (f.estado === e.valor ? " checked" : "") + ' data-id="' + f.matriculaId + '"> ' + e.texto + "</label>"
    ).join("") + "</td>" +
    '<td><input class="obs" maxlength="200" placeholder="Opcional" data-obs="' + f.matriculaId + '" value="' + esc(f.observacion || "") + '"></td></tr>'
  ).join("");
  actualizarResumen();
}

function actualizarResumen() {
  const cuenta = { PRESENTE: 0, TARDANZA: 0, FALTA: 0, JUSTIFICADA: 0 };
  filas.forEach((f) => { if (cuenta[f.estado] !== undefined) cuenta[f.estado]++; });
  $("resumen").innerHTML = "<span>Total: <b>" + filas.length + "</b></span>" +
    ESTADOS.map((e) => "<span>" + e.texto + "s: <b>" + cuenta[e.valor] + "</b></span>").join("");
}

async function cargarLista() {
  const seccionId = $("aSeccion").value;
  const fecha = $("aFecha").value;
  if (!seccionId || !fecha) {
    mostrar("Elija la sección y la fecha", true);
    return;
  }
  const r = await pedir("/api/asistencias?seccionId=" + encodeURIComponent(seccionId) + "&fecha=" + encodeURIComponent(fecha));
  if (!r.ok) {
    $("zonaLista").hidden = true;
    mostrar((r.datos && r.datos.mensaje) || "No se pudo cargar la lista", true);
    return;
  }
  seccionActual = parseInt(seccionId, 10);
  seccionEtiqueta = $("aSeccion").selectedOptions[0].textContent;
  fechaActual = fecha;
  const lista = Array.isArray(r.datos) ? r.datos : [];
  const yaTomada = lista.some((f) => f.registrada);
  // Si aún no se tomó, todos empiezan como "Presente" (el docente solo marca las excepciones)
  filas = lista.map((f) => ({ ...f, estado: f.estado || "PRESENTE" }));

  if (filas.length === 0) {
    $("zonaLista").hidden = true;
    mostrar("La sección no tiene alumnos con matrícula activa en " + fecha.slice(0, 4), true);
    return;
  }
  mostrar("", false);
  $("estadoDia").textContent = yaTomada
    ? "La asistencia de este día ya fue registrada. Puede corregirla y guardar de nuevo."
    : "Aún no se ha tomado la asistencia de este día. Todos aparecen como presentes: marque las excepciones.";
  $("zonaLista").hidden = false;
  dibujarLista();
}

$("formCargar").addEventListener("submit", (e) => { e.preventDefault(); cargarLista(); });

$("tablaAsistencia").addEventListener("change", (e) => {
  const radio = e.target.closest("input[type=radio]");
  if (!radio) return;
  const fila = filas.find((f) => String(f.matriculaId) === radio.dataset.id);
  if (fila) fila.estado = radio.value;
  actualizarResumen();
});

$("tablaAsistencia").addEventListener("input", (e) => {
  const campo = e.target.closest("input[data-obs]");
  if (!campo) return;
  const fila = filas.find((f) => String(f.matriculaId) === campo.dataset.obs);
  if (fila) fila.observacion = campo.value;
});

$("btnTodosPresentes").addEventListener("click", () => {
  filas.forEach((f) => { f.estado = "PRESENTE"; });
  dibujarLista();
});

$("btnGuardar").addEventListener("click", async () => {
  // Si se cambió la sección o la fecha sin pulsar "Ver lista", se avisa para no guardar en el día equivocado
  if ($("aSeccion").value !== String(seccionActual) || $("aFecha").value !== fechaActual) {
    mostrar("Cambió la sección o la fecha: pulse \"Ver lista\" antes de guardar", true);
    return;
  }
  const r = await pedir("/api/asistencias", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      seccionId: seccionActual,
      fecha: fechaActual,
      registros: filas.map((f) => ({
        matriculaId: f.matriculaId,
        estado: f.estado,
        observacion: (f.observacion || "").trim()
      }))
    })
  });
  mostrar((r.datos && r.datos.mensaje) || (r.ok ? "Asistencia guardada" : "No se pudo guardar"), !r.ok);
  if (r.ok) cargarLista().then(() => mostrar("Asistencia guardada", false));
});

$("aFecha").max = aTexto(new Date());
$("aFecha").value = fechaInicial();
cargarSecciones();
