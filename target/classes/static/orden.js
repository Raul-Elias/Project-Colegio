// Orden de grados para todas las tablas: Inicial -> Primaria -> Secundaria,
// luego por número de grado (1, 2, 3...) y luego por sección (A, B, C...).

const TEXTO_ORDENAR = "Ordenar: Inicial → Secundaria";
const TEXTO_QUITAR = "Quitar orden";

const NIVELES_ORDEN = ["INICIAL", "PRIMARIA", "SECUNDARIA"];
const ORDINALES = { primero: 1, primer: 1, segundo: 2, tercero: 3, tercer: 3, cuarto: 4, quinto: 5, sexto: 6 };

function sinTildes(t) {
  return String(t ?? "").normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

// "1° Grado (Primaria)" -> [1, 1]   (posición del nivel, número del grado)
function claveGrado(texto) {
  const limpio = sinTildes(texto);
  const parentesis = limpio.match(/\(([^)]*)\)/);
  const parteNivel = (parentesis ? parentesis[1] : limpio).toUpperCase();
  let nivel = NIVELES_ORDEN.findIndex((n) => parteNivel.includes(n));
  if (nivel < 0) nivel = NIVELES_ORDEN.length;

  const parteNombre = parentesis ? limpio.replace(parentesis[0], "") : limpio;
  let numero = 999;
  const digitos = parteNombre.match(/\d+/);
  if (digitos) {
    numero = parseInt(digitos[0], 10);
  } else {
    const palabra = parteNombre.toLowerCase().split(/[^a-z]+/).find((p) => ORDINALES[p]);
    if (palabra) numero = ORDINALES[palabra];
  }
  return [nivel, numero];
}

function compararGrados(a, b) {
  const ka = claveGrado(a), kb = claveGrado(b);
  return (ka[0] - kb[0]) || (ka[1] - kb[1]);
}

function compararTexto(a, b) {
  return String(a ?? "").localeCompare(String(b ?? ""), "es", { numeric: true, sensitivity: "base" });
}

// "1° Grado (Primaria) - Sección A" -> { grado: "1° Grado (Primaria)", seccion: "A" }
function partirSeccion(texto) {
  const partes = String(texto ?? "").split(" - Sección ");
  return { grado: partes[0], seccion: partes[1] || "" };
}

// Conecta el botón de la esquina de una tabla. estado.activo dice si se debe ordenar.
function botonOrden(idBoton, alCambiar) {
  const boton = document.getElementById(idBoton);
  const estado = { activo: false };
  boton.textContent = TEXTO_ORDENAR;
  boton.addEventListener("click", () => {
    estado.activo = !estado.activo;
    boton.textContent = estado.activo ? TEXTO_QUITAR : TEXTO_ORDENAR;
    alCambiar();
  });
  return estado;
}
