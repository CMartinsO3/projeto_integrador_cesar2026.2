/** Versão imprimível (HTML) da guia de transporte. */

const esc = (s) => String(s ?? "—")
  .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;");

const dataBR = (iso) => new Date(iso).toLocaleString("pt-BR", { timeZone: "America/Recife" });

export function renderizarHtml(g) {
  const apta = g.status === "APTA_PARA_TRANSPORTE";
  const linhas = g.itens.map((i) => `
      <tr>
        <td>${i.ordemFEFO}</td><td>${esc(i.bolsaId)}</td><td>${esc(i.tipoSanguineo)}</td>
        <td>${dataBR(i.validade)}</td>
        <td>${i.compativel ? "✔" : "✘"}</td><td>${i.validaNaEntrega ? "✔" : "✘"}</td>
      </tr>`).join("");
  const pend = g.pendencias.length
    ? `<section class="pend"><h2>Pendências</h2><ul>${g.pendencias.map((p) => `<li>${esc(p)}</li>`).join("")}</ul></section>`
    : "";

  return `<!doctype html>
<html lang="pt-BR"><head><meta charset="utf-8">
<title>Guia de Transporte ${esc(g.numeroGuia)}</title>
<style>
  body{font-family:system-ui,sans-serif;max-width:820px;margin:24px auto;padding:0 16px;color:#1a1a1a}
  header{display:flex;justify-content:space-between;align-items:center;border-bottom:3px solid #b3121f;padding-bottom:8px}
  h1{margin:0;font-size:1.4rem;color:#b3121f} h2{font-size:1rem;margin:20px 0 8px}
  .status{padding:6px 12px;border-radius:6px;font-weight:700;color:#fff;background:${apta ? "#1e7d32" : "#b3121f"}}
  dl{display:grid;grid-template-columns:200px 1fr;gap:4px 12px;margin:0} dt{color:#555}
  table{width:100%;border-collapse:collapse} th,td{border:1px solid #ccc;padding:6px;text-align:left}
  th{background:#f3f3f3} .pend{background:#fdecea;border:1px solid #b3121f;padding:4px 16px;border-radius:6px}
  .ass{display:flex;gap:32px;margin-top:40px} .ass div{flex:1;border-top:1px solid #000;padding-top:4px;text-align:center}
</style></head><body>
<header><div><h1>🩸 HemoFlow — Guia de Transporte</h1><small>Nº ${esc(g.numeroGuia)} · emitida em ${dataBR(g.emitidaEm)}</small></div>
<span class="status">${apta ? "APTA PARA TRANSPORTE" : "BLOQUEADA"}</span></header>
${pend}
<h2>Requisição</h2><dl>
  <dt>Hospital</dt><dd>${esc(g.requisicao.hospital)}</dd>
  <dt>Receptor</dt><dd>${esc(g.requisicao.tipoSanguineoReceptor)}</dd>
  <dt>Hemocomponente</dt><dd>${esc(g.requisicao.hemocomponente)} × ${g.requisicao.quantidade}</dd>
  <dt>Janela de entrega</dt><dd>${dataBR(g.requisicao.janelaEntrega)}</dd></dl>
<h2>Transporte</h2><dl>
  <dt>Rota</dt><dd>${g.transporte.caminho.map(esc).join(" → ")}</dd>
  <dt>Distância</dt><dd>${g.transporte.distanciaKm ?? "—"} km</dd>
  <dt>Saída / chegada previstas</dt><dd>${dataBR(g.transporte.saidaPrevista)} → ${dataBR(g.transporte.chegadaPrevista)} (${g.transporte.tempoEstimadoMin} min)</dd></dl>
<h2>Cadeia fria</h2><dl>
  <dt>Temperatura exigida</dt><dd>${esc(g.cadeiaFria.temperaturaExigida)}</dd>
  <dt>Acondicionamento</dt><dd>${esc(g.cadeiaFria.acondicionamento)}</dd>
  <dt>Limite / folga</dt><dd>${g.cadeiaFria.limiteMin} min / ${g.cadeiaFria.folgaMin} min</dd></dl>
<h2>Bolsas (ordem FEFO)</h2>
<table><thead><tr><th>#</th><th>Bolsa</th><th>Tipo</th><th>Validade</th><th>ABO/Rh</th><th>Válida na entrega</th></tr></thead><tbody>${linhas}</tbody></table>
<h2>Checklist</h2><ul>${g.checklist.map((c) => `<li>☐ ${esc(c)}</li>`).join("")}</ul>
<div class="ass"><div>Expedição (hemocentro)</div><div>Transportador</div><div>Recebimento (hospital)</div></div>
</body></html>`;
}
