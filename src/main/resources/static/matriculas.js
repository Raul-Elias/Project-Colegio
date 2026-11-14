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

const msg = document.getElementById("msgMatricula");
function mostrar(texto, esError) {
  msg.textContent = texto || "";
  msg.className = "mensaje " + (texto ? (esError ? "error" : "exito") : "");
}

// ---------- Listas para los <select> ----------
async function cargarOpciones() {
  const [a, s] = await Promise.all([pedir("/api/alumnos"), pedir("/api/secciones")]);
  const alumnos = a.ok && Array.isArray(a.datos) ? a.datos : [];
  const secciones = s.ok && Array.isArray(s.datos) ? s.datos : [];
  document.getElementById("mAlumno").innerHTML = '<option value="">Alumno</option>' +
    alumnos.map((x) => '<option value="' + x.id + '">' +
      esc(x.apellido + ", " + x.nombres + " (" + x.dni + ")") + "</option>").join("");
  const opcionesSeccion = secciones.map((x) => '<option value="' + x.id + '">' +
      esc(x.gradoNombre + " - Sección " + x.nombre) + "</option>").join("");
  document.getElementById("mSeccion").innerHTML = '<option value="">Grado y sección</option>' + opcionesSeccion;
  document.getElementById("tSeccion").innerHTML = '<option value="">Sección de destino</option>' + opcionesSeccion;
}

// ---------- Tabla de matrículas ----------
async function cargarMatriculas() {
  const anio = document.getElementById("fAnio").value.trim();
  const r = await pedir("/api/matriculas" + (anio ? "?anio=" + encodeURIComponent(anio) : ""));
  matriculas = r.ok && Array.isArray(r.datos) ? r.datos : [];
  dibujarMatriculas();
}

function compararMatriculas(a, b) {
  return compararGrados(a.gradoNombre, b.gradoNombre) || compararTexto(a.seccionNombre, b.seccionNombre) ||
    compararTexto(a.alumnoNombre, b.alumnoNombre);
}

function dibujarMatriculas() {
  const lista = ordenM.activo ? [...matriculas].sort(compararMatriculas) : matriculas;
  document.getElementById("tablaMatriculas").innerHTML = lista.map((m) =>
    "<tr><td>" + esc(m.anioEscolar) + "</td><td>" + esc(m.alumnoDni) + "</td><td>" +
    esc(m.alumnoNombre) + "</td><td>" + esc(m.gradoNombre) + "</td><td>" +
    esc(m.seccionNombre) + "</td><td>" + esc(m.estado) + "</td><td>" +
    esc(m.fechaMatricula) + "</td>" + botones(m) + "</tr>"
  ).join("");
}

let matriculas = [];
const ordenM = botonOrden("btnOrdenMatriculas", () => dibujarMatriculas());

function botones(m) {
  const b = (accion, texto, clase) =>
    '<button type="button" class="' + clase + '" data-accion="' + accion + '" data-id="' + m.id + '">' + texto + "</button> ";
  let html = '<td class="acciones">';
  if (m.estado === "ACTIVA") {
    html += b("trasladar", "Trasladar", "secundario") + b("retirar", "Retirar", "peligro");
  } else if (m.estado === "RETIRADA") {
    html += b("reactivar", "Reactivar", "secundario");
  }
  return html + b("historial", "Historial", "secundario") + "</td>";
}

// ---------- HU-09: retiro, traslado, reactivación e historial ----------
let matriculaTraslado = null;

const NOMBRES_ACCION = {
  MATRICULA: "Matrícula", RETIRO: "Retiro", TRASLADO: "Traslado", REACTIVACION: "Reactivación"
};

async function verHistorial(m) {
  const r = await pedir("/api/matriculas/" + m.id + "/historial");
  const lista = r.ok && Array.isArray(r.datos) ? r.datos : [];
  document.getElementById("tituloHistorial").textContent =
    "Historial de " + m.alumnoNombre + " (" + m.anioEscolar + ")";
  document.getElementById("tablaHistorial").innerHTML = lista.map((h) =>
    "<tr><td>" + esc(String(h.fecha).replace("T", " ").slice(0, 16)) + "</td><td>" +
    esc(NOMBRES_ACCION[h.accion] || h.accion) + "</td><td>" + esc(h.seccionOrigen) + "</td><td>" +
    esc(h.seccionDestino) + "</td><td>" + esc(h.motivo) + "</td></tr>"
  ).join("");
  document.getElementById("panelHistorial").hidden = false;
  document.getElementById("panelHistorial").scrollIntoView({ behavior: "smooth" });
}

async function ejecutar(url, cuerpo, textoExito) {
  const r = await pedir(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(cuerpo)
  });
  mostrar(r.ok ? textoExito : ((r.datos && r.datos.mensaje) || "No se pudo completar la acción"), !r.ok);
  if (r.ok) {
    document.getElementById("panelHistorial").hidden = true;
    cargarMatriculas();
  }
  return r.ok;
}

document.getElementById("tablaMatriculas").addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-accion]");
  if (!boton) return;
  const m = matriculas.find((x) => String(x.id) === boton.dataset.id);
  if (!m) return;
  const base = "/api/matriculas/" + m.id;

  if (boton.dataset.accion === "historial") {
    verHistorial(m);
  } else if (boton.dataset.accion === "retirar") {
    const motivo = prompt("Motivo del retiro de " + m.alumnoNombre + ":");
    if (motivo === null) return;
    ejecutar(base + "/retirar", { motivo: motivo }, "Matrícula retirada");
  } else if (boton.dataset.accion === "reactivar") {
    if (!confirm("¿Reactivar la matrícula de " + m.alumnoNombre + " (" + m.anioEscolar + ")?")) return;
    ejecutar(base + "/reactivar", {}, "Matrícula reactivada");
  } else if (boton.dataset.accion === "trasladar") {
    matriculaTraslado = m;
    document.getElementById("infoTraslado").textContent =
      m.alumnoNombre + " - actualmente en " + m.gradoNombre + ", sección " + m.seccionNombre;
    document.getElementById("tSeccion").value = "";
    document.getElementById("tMotivo").value = "";
    document.getElementById("panelTraslado").hidden = false;
    document.getElementById("panelTraslado").scrollIntoView({ behavior: "smooth" });
  }
});

document.getElementById("formTraslado").addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!matriculaTraslado) return;
  const ok = await ejecutar("/api/matriculas/" + matriculaTraslado.id + "/trasladar", {
    seccionId: parseInt(document.getElementById("tSeccion").value, 10),
    motivo: document.getElementById("tMotivo").value
  }, "Matrícula trasladada");
  if (ok) {
    document.getElementById("panelTraslado").hidden = true;
    matriculaTraslado = null;
  }
});

document.getElementById("btnCancelarTraslado").addEventListener("click", () => {
  document.getElementById("panelTraslado").hidden = true;
  matriculaTraslado = null;
});

// ---------- Eventos ----------
document.getElementById("formMatricula").addEventListener("submit", async (e) => {
  e.preventDefault();
  const r = await pedir("/api/matriculas", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      alumnoId: parseInt(document.getElementById("mAlumno").value, 10),
      seccionId: parseInt(document.getElementById("mSeccion").value, 10),
      anioEscolar: parseInt(document.getElementById("mAnio").value, 10)
    })
  });
  if (r.ok) {
    mostrar("Alumno matriculado correctamente", false);
    document.getElementById("mAlumno").value = "";
    document.getElementById("mSeccion").value = "";
    cargarMatriculas();
  } else {
    mostrar((r.datos && r.datos.mensaje) || "No se pudo matricular", true);
  }
});

document.getElementById("formFiltro").addEventListener("submit", (e) => {
  e.preventDefault();
  cargarMatriculas();
});
document.getElementById("btnTodas").addEventListener("click", () => {
  document.getElementById("fAnio").value = "";
  cargarMatriculas();
});

document.getElementById("mAnio").value = new Date().getFullYear();
cargarOpciones();
cargarMatriculas();
