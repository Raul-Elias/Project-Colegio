async function cargarAlumnos() {
  const res = await fetch("/api/alumnos");
  const datos = await res.json();
  document.getElementById("tablaAlumnos").innerHTML = datos.map(a =>
    `<tr><td>${a.id}</td><td>${a.nombres}</td><td>${a.apellido}</td><td>${a.grado}</td><td>${a.promedio}</td></tr>`
  ).join("");
}

async function cargarDocentes() {
  const res = await fetch("/api/docentes");
  const datos = await res.json();
  document.getElementById("tablaDocentes").innerHTML = datos.map(d =>
    `<tr><td>${d.id}</td><td>${d.nombres}</td><td>${d.especialidad}</td><td>${d.correo}</td><td>${d.telefono}</td></tr>`
  ).join("");
}

document.getElementById("formAlumno").addEventListener("submit", async (e) => {
  e.preventDefault();
  await fetch("/api/alumnos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      nombres: document.getElementById("aNombres").value,
      apellido: document.getElementById("aApellido").value,
      grado: document.getElementById("aGrado").value,
      promedio: parseFloat(document.getElementById("aPromedio").value)
    })
  });
  e.target.reset();
  cargarAlumnos();
});

document.getElementById("formDocente").addEventListener("submit", async (e) => {
  e.preventDefault();
  await fetch("/api/docentes", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      nombres: document.getElementById("dNombres").value,
      especialidad: document.getElementById("dEspecialidad").value,
      correo: document.getElementById("dCorreo").value,
      telefono: document.getElementById("dTelefono").value
    })
  });
  e.target.reset();
  cargarDocentes();
});

cargarAlumnos();
cargarDocentes();