// Mascota "Tobi": camina por la pantalla, abre un chat con el Agente Tutor y se puede ocultar.
(function () {
  const dni = (sessionStorage.getItem("correo") || "").split("@")[0];
  const css = `
  #tobi{position:fixed;bottom:12px;left:20px;width:84px;z-index:9999;cursor:pointer;transition:left 3.5s ease-in-out;user-select:none}
  #tobi img{width:100%;display:block;animation:tobiBob 1.6s ease-in-out infinite;filter:drop-shadow(0 6px 6px rgba(0,0,0,.25));transition:transform .3s}
  #tobi.der img{transform:scaleX(-1)}
  @keyframes tobiBob{0%,100%{transform:translateY(0)}50%{transform:translateY(-6px)}}
  #tobi.der img{animation-name:tobiBobR}@keyframes tobiBobR{0%,100%{transform:scaleX(-1) translateY(0)}50%{transform:scaleX(-1) translateY(-6px)}}
  #tobiX{position:absolute;top:-6px;right:-6px;width:22px;height:22px;border-radius:50%;border:0;background:#0f2a4a;color:#fff;font-size:12px;cursor:pointer;opacity:.85}
  #tobiBack{position:fixed;bottom:12px;right:12px;z-index:9999;border:0;border-radius:20px;background:#0f2a4a;color:#fff;padding:8px 14px;font-size:13px;cursor:pointer;display:none}
  #tobiChat{position:fixed;bottom:110px;right:14px;width:min(330px,calc(100vw - 28px));max-height:60vh;z-index:9999;background:#fff;color:#1b2430;border-radius:16px;box-shadow:0 12px 36px rgba(0,0,0,.28);display:none;flex-direction:column;overflow:hidden;font:14px system-ui,sans-serif}
  #tobiChat header{background:#0f2a4a!important;color:#fff!important;padding:10px 14px;font-weight:600;position:static!important}
  #tobiMsgs{padding:10px;overflow:auto;flex:1;display:flex;flex-direction:column;gap:8px}
  .tm{padding:8px 11px;border-radius:12px;max-width:85%;line-height:1.35}.tm.b{background:#eef2f9;align-self:flex-start}.tm.u{background:#d62839;color:#fff;align-self:flex-end}
  #tobiChips{display:flex;flex-wrap:wrap;gap:6px;padding:0 10px 8px}#tobiChips button{border:1px solid #cdd6e6;background:#fff!important;color:#0f2a4a!important;border-radius:14px;padding:5px 10px;font-size:12px;cursor:pointer}#tobiChips button{border:1px solid #030b18;background:#fff;border-radius:14px;padding:5px 10px;font-size:12px;cursor:pointer}
  #tobiForm{display:flex;border-top:1px solid #030a16}#tobiIn{flex:1;border:0;padding:11px;font-size:14px;outline:0}#tobiForm button{border:0;background:#d62839;color:#fff;padding:0 16px;cursor:pointer}`;
  const st = document.createElement("style"); st.textContent = css; document.head.appendChild(st);
  document.body.insertAdjacentHTML("beforeend", `
  <div id="tobi" title="Soy Tobi, tu tutor"><button id="tobiX" title="Ocultar">✕</button><img src="img/mascota.png" alt="Tobi"></div>
  <button id="tobiBack">🐾 Mostrar a Tobi</button>
  <div id="tobiChat"><header>🐾 Tobi · tu tutor</header><div id="tobiMsgs"></div>
    <div id="tobiChips"><button>¿Cómo van mis notas?</button><button>Dame un plan de estudio</button><button>¿Cómo subo mi promedio?</button></div>
    <form id="tobiForm"><input id="tobiIn" placeholder="Escríbeme…" autocomplete="off"><button>Enviar</button></form></div>`);
  const $ = id => document.getElementById(id), t = $("tobi"), chat = $("tobiChat"), msgs = $("tobiMsgs");
  const add = (txt, c) => { const d = document.createElement("div"); d.className = "tm " + c; d.textContent = txt; msgs.appendChild(d); msgs.scrollTop = 9e9; };
  const ocultar = v => { localStorage.setItem("tobiOculto", v ? "1" : ""); t.style.display = v ? "none" : "block"; $("tobiBack").style.display = v ? "block" : "none"; if (v) chat.style.display = "none"; };
  ocultar(!!localStorage.getItem("tobiOculto"));
  $("tobiX").onclick = e => { e.stopPropagation(); ocultar(true); };
  $("tobiBack").onclick = () => ocultar(false);
  t.onclick = () => { chat.style.display = chat.style.display === "flex" ? "none" : "flex"; if (!msgs.children.length) add("¡Guau! Soy Tobi 🐶 Puedo explicarte tus notas y ayudarte a estudiar mejor.", "b"); };
  async function enviar(m) {
    add(m, "u");
    try {
      const r = await fetch("/api/agentes/tutor", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ dni, mensaje: m }) });
      add((await r.json()).respuesta, "b");
    } catch (e) { add("Uy, no pude conectarme. Intenta de nuevo.", "b"); }
  }
  $("tobiForm").onsubmit = e => { e.preventDefault(); const v = $("tobiIn").value.trim(); if (v) { $("tobiIn").value = ""; enviar(v); } };
  $("tobiChips").onclick = e => { if (e.target.tagName === "BUTTON") enviar(e.target.textContent); };
  // Caminata: se mueve a una posición al azar y mira hacia donde camina
  setInterval(() => {
    if (t.style.display === "none" || chat.style.display === "flex") return;
    const x = 10 + Math.random() * (innerWidth - 110), actual = parseFloat(t.style.left) || 20;
    t.classList.toggle("der", x > actual); t.style.left = x + "px";
  }, 6000);
})();
