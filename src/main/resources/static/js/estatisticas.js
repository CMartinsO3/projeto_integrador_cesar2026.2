import { apiRequest } from "./api-client.js";
import { toast, formatNumber, formatTemperature } from "./ui.js";

function textCell(row, value) {
  const cell = document.createElement("td");
  cell.textContent = value;
  row.append(cell);
  return cell;
}
function kpi(container, label, value) {
  const article = document.createElement("article");
  article.className = "kpi teal";
  const labelNode = document.createElement("div");
  labelNode.className = "label";
  labelNode.textContent = label;
  const valueNode = document.createElement("div");
  valueNode.className = "value";
  valueNode.textContent = value;
  article.append(labelNode,valueNode);
  container.append(article);
}

async function loadEstatisticas() {
  const container = document.getElementById("estatisticas");
  if (!container) return;
  try {
    const [estoque,requisicoes,demanda,descarte,hospitals,tempos,risco,probabilidade,temperatura] = await Promise.all([
      apiRequest("/api/v1/estatisticas/estoque"),
      apiRequest("/api/v1/estatisticas/requisicoes"),
      apiRequest("/api/v1/estatisticas/demanda"),
      apiRequest("/api/v1/estatisticas/descarte"),
      apiRequest("/api/v1/estatisticas/hospitais"),
      apiRequest("/api/v1/estatisticas/tempos"),
      apiRequest("/api/v1/estatisticas/risco"),
      apiRequest("/api/v1/estatisticas/probabilidade"),
      apiRequest("/api/v1/estatisticas/temperatura")
    ]);
    const kpis = document.getElementById("kpiPrincipais");
    kpi(kpis,"Estoque disponível",formatNumber(estoque.totalDisponivel));
    kpi(kpis,"Requisições pendentes",formatNumber(requisicoes.totalPendentes));
    kpi(kpis,"Próximas do vencimento",formatNumber(estoque.bolsasQuaseVencidas));
    kpi(kpis,"Taxa de atendimento",requisicoes.taxaAtendimentoPercent == null ? "—" : `${formatNumber(requisicoes.taxaAtendimentoPercent,{maximumFractionDigits:1})}%`);

    const demandaBody = document.getElementById("demandaRows");
    const q = demanda.quantidadePorRequisicao;
    addRows(demandaBody,[
      ["Média",formatNumber(q.media)],
      ["Mediana",formatNumber(q.mediana)],
      ["Desvio padrão",formatNumber(q.desvioPadrao)],
      ["Máximo",formatNumber(q.maximo)]
    ]);

    const descarteBody = document.getElementById("descarteRows");
    descarte.porTipo.forEach(item => addRows(descarteBody,[[
      String(item.tipoSanguineo).replace("_POS","+").replace("_NEG","−"),
      formatNumber(item.coletadas),
      formatNumber(item.descartadasPorVencimento),
      item.taxaPercent == null ? "—" : `${formatNumber(item.taxaPercent,{maximumFractionDigits:1})}%`
    ]]));

    const hospitalBody = document.getElementById("hospitalRows");
    hospitals.forEach(hospital => addRows(hospitalBody,[[
      hospital.hospital,formatNumber(hospital.requisicoes),formatNumber(hospital.unidades),
      formatNumber(hospital.mediaUnidades),formatNumber(hospital.desvioPadraoUnidades)
    ]]));

    const riscoBody = document.getElementById("riscoRows");
    risco.tipos.forEach(item => addRows(riscoBody,[[
      String(item.tipoSanguineo).replace("_POS","+").replace("_NEG","−"),
      formatNumber(item.estoqueDisponivel),
      formatNumber(item.probRuptura,{style:"percent",maximumFractionDigits:1}),
      item.nivel,
      formatNumber(item.estoqueSegurancaSugerido)
    ]]));

    const tempoText = document.getElementById("tempoResumo");
    tempoText.textContent = `Entrega média: ${formatNumber(tempos.tempoEntregaMinutos?.media,{maximumFractionDigits:1})} min · Prazo médio: ${formatNumber(tempos.prazoAtendimentoHoras?.media,{maximumFractionDigits:1})} h`;
    const probText = document.getElementById("probabilidadeResumo");
    probText.textContent = `λ Poisson: ${formatNumber(probabilidade.poisson?.lambdaRequisicoesPorDia,{maximumFractionDigits:2})} requisições/dia · P(atendimento): ${formatNumber(probabilidade.binomial?.pAtendimento,{style:"percent",maximumFractionDigits:1})}`;
    const tempText = document.getElementById("temperaturaResumo");
    tempText.textContent = `Temperatura média simulada: ${formatTemperature(temperatura.temperatura?.media)} · Leituras: ${formatNumber(temperatura.leituras)}`;
  } catch (error) {
    toast(error.message,"error");
  }
}

function addRows(body, rows) {
  rows.forEach(values => {
    if (!Array.isArray(values)) values = [values];
    const row = document.createElement("tr");
    values.forEach(value => textCell(row,String(value)));
    body.append(row);
  });
}

loadEstatisticas();
