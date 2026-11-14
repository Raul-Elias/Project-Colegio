// ---------- Sesión ----------
const correoSesion = sessionStorage.getItem("correo");
if (correoSesion) {
  document.getElementById("usuarioActual").textContent = correoSesion;
}
document.getElementById("btnSalir").addEventListener("click", () => {
  sessionStorage.clear();
  window.location.href = "login.html";
});

// ---------- Utilidades (mismas que app.js) ----------

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

// Igual que en app.js, pero con un aviso (alCambiar) para refrescar las demás tablas
function crearGestor(cfg) {
  const form = document.getElementById(cfg.form);
  const tbody = document.getElementById(cfg.tbody);
  const msg = document.getElementById(cfg.msg);
  const idOculto = document.getElementById(cfg.idCampo);
  const btnGuardar = document.getElementById(cfg.btnGuardar);
  const btnCancelar = document.getElementById(cfg.btnCancelar);
  let lista = [];
  const orden = botonOrden(cfg.btnOrden, () => dibujar());

  function mostrar(texto, esError) {
    msg.textContent = texto || "";
    msg.className = "mensaje " + (texto ? (esError ? "error" : "exito") : "");
  }

  function limpiar() {
    form.reset();
    idOculto.value = "";
    btnGuardar.textContent = "Guardar " + cfg.nombre;
    btnCancelar.hidden = true;
  }

  async function cargar() {
    const r = await pedir(cfg.url);
    lista = r.ok && Array.isArray(r.datos) ? r.datos : [];
    dibujar();
    if (cfg.alCargar) cfg.alCargar(lista);
  }

  // Dibuja la tabla; con el botón activo, ordena Inicial -> Secundaria, 1, 2, 3... y A, B, C...
  function dibujar() {
    const mostrados = orden.activo && cfg.comparar ? [...lista].sort(cfg.comparar) : lista;
    tbody.innerHTML = mostrados.map((item) =>
      "<tr>" +
      cfg.columnas.map((c) => "<td>" + esc(item[c]) + "</td>").join("") +
      '<td class="acciones">' +
      '<button type="button" class="secundario" data-accion="editar" data-id="' + item.id + '">Editar</button> ' +
      '<button type="button" class="peligro" data-accion="eliminar" data-id="' + item.id + '">Eliminar</button>' +
      "</td></tr>"
    ).join("");
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const cuerpo = {};
    cfg.campos.forEach((c) => {
      const valor = document.getElementById(c.id).value.trim();
      cuerpo[c.clave] = c.numero ? parseFloat(valor) : valor;
    });
    const id = idOculto.value;
    const r = await pedir(id ? cfg.url + "/" + id : cfg.url, {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cuerpo)
    });
    if (r.ok) {
      mostrar(id ? "Registro actualizado" : "Registro guardado", false);
      limpiar();
      cargar();
      if (cfg.alCambiar) cfg.alCambiar();
    } else {
      mostrar((r.datos && r.datos.mensaje) || "No se pudo guardar", true);
    }
  });

  tbody.addEventListener("click", async (e) => {
    const boton = e.target.closest("button[data-accion]");
    if (!boton) return;
    const item = lista.find((x) => String(x.id) === boton.dataset.id);
    if (!item) return;

    if (boton.dataset.accion === "editar") {
      cfg.campos.forEach((c) => {
        document.getElementById(c.id).value = item[c.clave] ?? "";
      });
      idOculto.value = item.id;
      btnGuardar.textContent = "Actualizar " + cfg.nombre;
      btnCancelar.hidden = false;
      mostrar("", false);
      form.scrollIntoView({ behavior: "smooth", block: "center" });
    } else {
      if (!confirm("¿Eliminar " + cfg.nombre + " " + cfg.etiqueta(item) + "?")) return;
      const r = await pedir(cfg.url + "/" + item.id, { method: "DELETE" });
      if (r.ok) {
        mostrar("Registro eliminado", false);
        if (idOculto.value === String(item.id)) limpiar();
        cargar();
        if (cfg.alCambiar) cfg.alCambiar();
      } else {
        mostrar((r.datos && r.datos.mensaje) || "No se pudo eliminar", true);
      }
    }
  });

  btnCancelar.addEventListener("click", () => {
    limpiar();
    mostrar("", false);
  });

  cargar();
  return { cargar };
}

// "PRIMARIA" -> "Primaria"
function nivelTexto(nivel) {
  const n = String(nivel || "");
  return n.charAt(0).toUpperCase() + n.slice(1).toLowerCase();
}

// ---------- Lista de grados dentro de los <select> ----------
function llenarSelectGrados(grados) {
  ["sGrado", "cGrado"].forEach((idSelect) => {
    const select = document.getElementById(idSelect);
    const actual = select.value;
    select.innerHTML = '<option value="">Grado</option>' +
      grados.map((g) => '<option value="' + g.id + '">' + esc(g.nombre + " (" + nivelTexto(g.nivel) + ")") + "</option>").join("");
    select.value = actual;
  });
}

// ---------- Gestores ----------
let gestorSecciones, gestorCursos;

crearGestor({
  nombre: "grado",
  url: "/api/grados",
  form: "formGrado",
  tbody: "tablaGrados",
  msg: "msgGrado",
  idCampo: "gId",
  btnGuardar: "btnGuardarGrado",
  btnCancelar: "btnCancelarGrado",
  columnas: ["id", "nombre", "nivel"],
  campos: [
    { id: "gNombre", clave: "nombre" },
    { id: "gNivel", clave: "nivel" }
  ],
  etiqueta: (g) => g.nombre + " (" + nivelTexto(g.nivel) + ")",
  btnOrden: "btnOrdenGrados",
  comparar: (a, b) => compararGrados(a.nombre + " (" + a.nivel + ")", b.nombre + " (" + b.nivel + ")"),
  alCargar: llenarSelectGrados,
  // si cambia el nombre de un grado, las otras tablas deben mostrarlo actualizado
  alCambiar: () => {
    if (gestorSecciones) gestorSecciones.cargar();
    if (gestorCursos) gestorCursos.cargar();
  }
});

gestorSecciones = crearGestor({
  nombre: "sección",
  url: "/api/secciones",
  form: "formSeccion",
  tbody: "tablaSecciones",
  msg: "msgSeccion",
  idCampo: "sId",
  btnGuardar: "btnGuardarSeccion",
  btnCancelar: "btnCancelarSeccion",
  columnas: ["id", "gradoNombre", "nombre"],
  campos: [
    { id: "sGrado", clave: "gradoId", numero: true },
    { id: "sNombre", clave: "nombre" }
  ],
  btnOrden: "btnOrdenSecciones",
  comparar: (a, b) => compararGrados(a.gradoNombre, b.gradoNombre) || compararTexto(a.nombre, b.nombre),
  etiqueta: (s) => s.gradoNombre + " " + s.nombre
});

gestorCursos = crearGestor({
  nombre: "curso",
  url: "/api/cursos",
  form: "formCurso",
  tbody: "tablaCursos",
  msg: "msgCurso",
  idCampo: "cId",
  btnGuardar: "btnGuardarCurso",
  btnCancelar: "btnCancelarCurso",
  columnas: ["id", "gradoNombre", "nombre"],
  campos: [
    { id: "cGrado", clave: "gradoId", numero: true },
    { id: "cNombre", clave: "nombre" }
  ],
  btnOrden: "btnOrdenCursos",
  comparar: (a, b) => compararGrados(a.gradoNombre, b.gradoNombre) || compararTexto(a.nombre, b.nombre),
  etiqueta: (c) => c.nombre + " - " + c.gradoNombre
});
