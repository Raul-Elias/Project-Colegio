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

const msg = document.getElementById("msgHorario");
function mostrar(texto, esError) {
  msg.textContent = texto || "";
  msg.className = "mensaje " + (texto ? (esError ? "error" : "exito") : "");
}

const $ = (id) => document.getElementById(id);
const DIAS = { LUNES: "Lunes", MARTES: "Martes", MIERCOLES: "Miércoles", JUEVES: "Jueves", VIERNES: "Viernes" };
const hora = (t) => String(t || "").slice(0, 5); // "08:00:00" -> "08:00"

let secciones = [], cursos = [], horarios = [];
const ordenH = botonOrden("btnOrdenHorarios", () => dibujarHorarios());

// Grado (Inicial -> Secundaria), sección (A, B, C...), luego día y hora
function compararHorarios(a, b) {
  const pa = partirSeccion(a.seccionNombre), pb = partirSeccion(b.seccionNombre);
  return compararGrados(pa.grado, pb.grado) || compararTexto(pa.seccion, pb.seccion) ||
    (Object.keys(DIAS).indexOf(a.diaSemana) - Object.keys(DIAS).indexOf(b.diaSemana)) ||
    compararTexto(hora(a.horaInicio), hora(b.horaInicio));
}

// ---------- Listas para los <select> ----------
function opciones(lista, textoVacio, etiqueta) {
  return '<option value="">' + textoVacio + "</option>" +
    lista.map((x) => '<option value="' + x.id + '">' + esc(etiqueta(x)) + "</option>").join("");
}

// El curso solo se puede elegir entre los del grado de la sección elegida
function llenarCursos() {
  const seccion = secciones.find((s) => String(s.id) === $("hSeccion").value);
  const actual = $("hCurso").value;
  const delGrado = seccion ? cursos.filter((c) => c.gradoId === seccion.gradoId) : [];
  $("hCurso").innerHTML = opciones(delGrado, seccion ? "Curso" : "Primero elija la sección", (c) => c.nombre);
  $("hCurso").value = actual;
}

async function cargarOpciones() {
  const [s, c, d] = await Promise.all([pedir("/api/secciones"), pedir("/api/cursos"), pedir("/api/docentes")]);
  secciones = s.ok && Array.isArray(s.datos) ? s.datos : [];
  cursos = c.ok && Array.isArray(c.datos) ? c.datos : [];
  const docentes = d.ok && Array.isArray(d.datos) ? d.datos : [];
  const etiquetaSeccion = (x) => x.gradoNombre + " - Sección " + x.nombre;
  $("hSeccion").innerHTML = opciones(secciones, "Sección", etiquetaSeccion);
  $("fSeccion").innerHTML = opciones(secciones, "Todas las secciones", etiquetaSeccion);
  $("hDocente").innerHTML = opciones(docentes, "Docente", (x) => x.nombres + " (" + x.especialidad + ")");
  $("fDocente").innerHTML = opciones(docentes, "Todos los docentes", (x) => x.nombres);
  llenarCursos();
}

// ---------- Tabla de horarios ----------
async function cargarHorarios() {
  const params = new URLSearchParams();
  if ($("fAnio").value.trim()) params.set("anio", $("fAnio").value.trim());
  if ($("fSeccion").value) params.set("seccionId", $("fSeccion").value);
  if ($("fDocente").value) params.set("docenteId", $("fDocente").value);
  const r = await pedir("/api/horarios" + (params.toString() ? "?" + params : ""));
  horarios = r.ok && Array.isArray(r.datos) ? r.datos : [];
  dibujarHorarios();
}

function dibujarHorarios() {
  const mostrados = ordenH.activo ? [...horarios].sort(compararHorarios) : horarios;
  $("tablaHorarios").innerHTML = mostrados.map((h) =>
    "<tr><td>" + esc(h.anioEscolar) + "</td><td>" + esc(DIAS[h.diaSemana] || h.diaSemana) + "</td><td>" +
    esc(hora(h.horaInicio) + " - " + hora(h.horaFin)) + "</td><td>" + esc(h.seccionNombre) + "</td><td>" +
    esc(h.cursoNombre) + "</td><td>" + esc(h.docenteNombre) + "</td>" +
    '<td class="acciones">' +
    '<button type="button" class="secundario" data-accion="editar" data-id="' + h.id + '">Editar</button> ' +
    '<button type="button" class="peligro" data-accion="eliminar" data-id="' + h.id + '">Eliminar</button>' +
    "</td></tr>"
  ).join("");
}

// ---------- Formulario ----------
function limpiar() {
  $("formHorario").reset();
  $("hId").value = "";
  $("hAnio").value = new Date().getFullYear();
  llenarCursos();
  $("btnGuardarHorario").textContent = "Guardar bloque";
  $("btnCancelarHorario").hidden = true;
}

$("hSeccion").addEventListener("change", llenarCursos);

$("formHorario").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = $("hId").value;
  const r = await pedir(id ? "/api/horarios/" + id : "/api/horarios", {
    method: id ? "PUT" : "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      anioEscolar: parseInt($("hAnio").value, 10),
      seccionId: parseInt($("hSeccion").value, 10),
      cursoId: parseInt($("hCurso").value, 10),
      docenteId: parseInt($("hDocente").value, 10),
      diaSemana: $("hDia").value,
      horaInicio: $("hInicio").value,
      horaFin: $("hFin").value
    })
  });
  if (r.ok) {
    mostrar(id ? "Bloque actualizado" : "Bloque guardado", false);
    limpiar();
    cargarHorarios();
  } else {
    // Si hay cruce, aquí aparece quién y cuándo choca
    mostrar((r.datos && r.datos.mensaje) || "No se pudo guardar", true);
  }
});

$("btnCancelarHorario").addEventListener("click", () => { limpiar(); mostrar("", false); });

$("tablaHorarios").addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-accion]");
  if (!boton) return;
  const h = horarios.find((x) => String(x.id) === boton.dataset.id);
  if (!h) return;

  if (boton.dataset.accion === "editar") {
    $("hId").value = h.id;
    $("hAnio").value = h.anioEscolar;
    $("hSeccion").value = h.seccionId;
    llenarCursos();
    $("hCurso").value = h.cursoId;
    $("hDocente").value = h.docenteId;
    $("hDia").value = h.diaSemana;
    $("hInicio").value = hora(h.horaInicio);
    $("hFin").value = hora(h.horaFin);
    $("btnGuardarHorario").textContent = "Actualizar bloque";
    $("btnCancelarHorario").hidden = false;
    mostrar("", false);
    $("formHorario").scrollIntoView({ behavior: "smooth", block: "center" });
  } else {
    if (!confirm("¿Eliminar " + h.cursoNombre + " de " + h.seccionNombre + " (" +
        (DIAS[h.diaSemana] || h.diaSemana) + " " + hora(h.horaInicio) + ")?")) return;
    const r = await pedir("/api/horarios/" + h.id, { method: "DELETE" });
    mostrar(r.ok ? "Bloque eliminado" : ((r.datos && r.datos.mensaje) || "No se pudo eliminar"), !r.ok);
    if (r.ok) {
      if ($("hId").value === String(h.id)) limpiar();
      cargarHorarios();
    }
  }
});

$("formFiltro").addEventListener("submit", (e) => { e.preventDefault(); cargarHorarios(); });
$("btnTodos").addEventListener("click", () => {
  $("fAnio").value = "";
  $("fSeccion").value = "";
  $("fDocente").value = "";
  cargarHorarios();
});

limpiar();
cargarOpciones();
cargarHorarios();
