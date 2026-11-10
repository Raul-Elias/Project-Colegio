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

function crearGestor(cfg) {
  const form = document.getElementById(cfg.form);
  const tbody = document.getElementById(cfg.tbody);
  const msg = document.getElementById(cfg.msg);
  const idOculto = document.getElementById(cfg.idCampo);
  const btnGuardar = document.getElementById(cfg.btnGuardar);
  const btnCancelar = document.getElementById(cfg.btnCancelar);
  let lista = [];

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
    tbody.innerHTML = lista.map((item) =>
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
}

crearGestor({
  nombre: "alumno",
  url: "/api/alumnos",
  form: "formAlumno",
  tbody: "tablaAlumnos",
  msg: "msgAlumno",
  idCampo: "aId",
  btnGuardar: "btnGuardarAlumno",
  btnCancelar: "btnCancelarAlumno",
  columnas: ["id", "dni", "nombres", "apellido", "grado", "promedio"],
  campos: [
    { id: "aDni", clave: "dni" },
    { id: "aNombres", clave: "nombres" },
    { id: "aApellido", clave: "apellido" },
    { id: "aGrado", clave: "grado" },
    { id: "aPromedio", clave: "promedio", numero: true }
  ],
  etiqueta: (a) => a.nombres + " " + a.apellido
});

crearGestor({
  nombre: "docente",
  url: "/api/docentes",
  form: "formDocente",
  tbody: "tablaDocentes",
  msg: "msgDocente",
  idCampo: "dId",
  btnGuardar: "btnGuardarDocente",
  btnCancelar: "btnCancelarDocente",
  columnas: ["id", "dni", "nombres", "especialidad", "correo", "telefono"],
  campos: [
    { id: "dDni", clave: "dni" },
    { id: "dNombres", clave: "nombres" },
    { id: "dEspecialidad", clave: "especialidad" },
    { id: "dCorreo", clave: "correo" },
    { id: "dTelefono", clave: "telefono" }
  ],
  etiqueta: (d) => d.nombres
});