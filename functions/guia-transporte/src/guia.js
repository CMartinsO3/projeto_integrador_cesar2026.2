/**
 * Regras de negocio da Guia de Transporte do HemoFlow.
 *
 * Espelha o dominio do backend Java:
 *  - TipoSanguineo / CompatibilidadeAboRh  (matriz 8x8 doador x receptor)
 *  - Hemocomponente (limite de cadeia fria em minutos)
 *  - FEFO: as bolsas saem ordenadas pela validade mais proxima
 *
 * Funcoes puras (sem I/O) para facilitar o teste unitario.
 */

export const TIPOS = ["A_POS", "A_NEG", "B_POS", "B_NEG", "AB_POS", "AB_NEG", "O_POS", "O_NEG"];

const ROTULO_TIPO = {
  A_POS: "A+", A_NEG: "A−", B_POS: "B+", B_NEG: "B−",
  AB_POS: "AB+", AB_NEG: "AB−", O_POS: "O+", O_NEG: "O−",
};

// MATRIZ[doador][receptor] — mesma ordem do enum Java TipoSanguineo
const MATRIZ = [
  //   A+     A-     B+     B-     AB+    AB-    O+     O-     <- receptor
  [true,  false, false, false, true,  false, false, false], // A+
  [true,  true,  false, false, true,  true,  false, false], // A-
  [false, false, true,  false, true,  false, false, false], // B+
  [false, false, true,  true,  true,  true,  false, false], // B-
  [false, false, false, false, true,  false, false, false], // AB+
  [false, false, false, false, true,  true,  false, false], // AB-
  [true,  false, true,  false, true,  false, true,  false], // O+
  [true,  true,  true,  true,  true,  true,  true,  true ], // O-
];

/** Espelha o enum Hemocomponente + faixa de temperatura de transporte (didatica). */
export const HEMOCOMPONENTES = {
  HEMACIAS:        { rotulo: "Hemácias",        limiteCadeiaFriaMin: 120, temperatura: "2 °C a 6 °C",     acondicionamento: "Caixa térmica refrigerada" },
  PLASMA:          { rotulo: "Plasma",          limiteCadeiaFriaMin: 120, temperatura: "≤ −18 °C",        acondicionamento: "Caixa térmica com gelo seco" },
  PLAQUETAS:       { rotulo: "Plaquetas",       limiteCadeiaFriaMin: 90,  temperatura: "20 °C a 24 °C",   acondicionamento: "Caixa térmica em temperatura ambiente controlada (sem gelo)" },
  CRIOPRECIPITADO: { rotulo: "Crioprecipitado", limiteCadeiaFriaMin: 120, temperatura: "≤ −18 °C",        acondicionamento: "Caixa térmica com gelo seco" },
};

export function compativel(doador, receptor) {
  const d = TIPOS.indexOf(doador);
  const r = TIPOS.indexOf(receptor);
  if (d < 0 || r < 0) return false;
  return MATRIZ[d][r];
}

/** Valida o formato do corpo da requisicao. Retorna lista de erros (vazia = ok). */
export function validarEntrada(body) {
  const erros = [];
  if (!body || typeof body !== "object") return ["Corpo JSON ausente ou inválido."];

  const req = body.requisicao;
  if (!req) erros.push("Campo 'requisicao' é obrigatório.");
  else {
    if (!req.hospital?.nome) erros.push("requisicao.hospital.nome é obrigatório.");
    if (!TIPOS.includes(req.tipoSanguineo)) erros.push(`requisicao.tipoSanguineo inválido. Use: ${TIPOS.join(", ")}.`);
    if (!HEMOCOMPONENTES[req.hemocomponente]) erros.push(`requisicao.hemocomponente inválido. Use: ${Object.keys(HEMOCOMPONENTES).join(", ")}.`);
    if (!Number.isInteger(req.quantidade) || req.quantidade < 1) erros.push("requisicao.quantidade deve ser inteiro >= 1.");
    if (!req.janelaEntrega || isNaN(Date.parse(req.janelaEntrega))) erros.push("requisicao.janelaEntrega deve ser data ISO-8601.");
  }

  if (!Array.isArray(body.bolsas) || body.bolsas.length === 0) erros.push("Campo 'bolsas' deve ser uma lista não vazia.");
  else body.bolsas.forEach((b, i) => {
    if (!b?.id) erros.push(`bolsas[${i}].id é obrigatório.`);
    if (!TIPOS.includes(b?.tipoSanguineo)) erros.push(`bolsas[${i}].tipoSanguineo inválido.`);
    if (!b?.validade || isNaN(Date.parse(b.validade))) erros.push(`bolsas[${i}].validade deve ser data ISO-8601.`);
  });

  const rota = body.rota;
  if (!rota) erros.push("Campo 'rota' é obrigatório.");
  else {
    if (!Array.isArray(rota.caminho) || rota.caminho.length < 2) erros.push("rota.caminho deve ter ao menos origem e destino.");
    if (typeof rota.tempoEstimadoMin !== "number" || rota.tempoEstimadoMin <= 0) erros.push("rota.tempoEstimadoMin deve ser número > 0.");
  }
  return erros;
}

/**
 * Gera a guia de transporte. Nao lanca erro por regra de negocio:
 * devolve a guia com status BLOQUEADA e a lista de pendencias.
 */
export function gerarGuia(body, agora = new Date()) {
  const { requisicao: req, bolsas, rota } = body;
  const comp = HEMOCOMPONENTES[req.hemocomponente];
  const pendencias = [];

  const saida = new Date(rota.saidaPrevista ?? agora);
  const chegada = new Date(saida.getTime() + rota.tempoEstimadoMin * 60_000);
  const janela = new Date(req.janelaEntrega);

  // FEFO: primeiro a vencer, primeiro a sair
  const itens = [...bolsas]
    .sort((a, b) => Date.parse(a.validade) - Date.parse(b.validade))
    .map((b, i) => {
      const okAbo = compativel(b.tipoSanguineo, req.tipoSanguineo);
      const okValidade = Date.parse(b.validade) > chegada.getTime();
      const okComponente = !b.hemocomponente || b.hemocomponente === req.hemocomponente;
      if (!okAbo) pendencias.push(`Bolsa ${b.id} (${ROTULO_TIPO[b.tipoSanguineo]}) incompatível com receptor ${ROTULO_TIPO[req.tipoSanguineo]}.`);
      if (!okValidade) pendencias.push(`Bolsa ${b.id} vence antes da chegada prevista.`);
      if (!okComponente) pendencias.push(`Bolsa ${b.id} é ${b.hemocomponente}, mas a requisição pede ${req.hemocomponente}.`);
      return {
        ordemFEFO: i + 1,
        bolsaId: b.id,
        tipoSanguineo: ROTULO_TIPO[b.tipoSanguineo],
        validade: b.validade,
        compativel: okAbo,
        validaNaEntrega: okValidade,
      };
    });

  if (bolsas.length !== req.quantidade) {
    pendencias.push(`Quantidade divergente: requisição pede ${req.quantidade}, foram enviadas ${bolsas.length} bolsas.`);
  }
  const folgaCadeiaFria = comp.limiteCadeiaFriaMin - rota.tempoEstimadoMin;
  if (folgaCadeiaFria < 0) {
    pendencias.push(`Tempo de rota (${rota.tempoEstimadoMin} min) excede o limite de cadeia fria de ${comp.rotulo} (${comp.limiteCadeiaFriaMin} min).`);
  }
  if (chegada > janela) {
    pendencias.push(`Chegada prevista (${chegada.toISOString()}) fica após a janela de entrega (${janela.toISOString()}).`);
  }

  const origem = rota.caminho[0];
  const destino = rota.caminho[rota.caminho.length - 1];

  return {
    numeroGuia: gerarNumero(agora, req, bolsas),
    emitidaEm: agora.toISOString(),
    status: pendencias.length === 0 ? "APTA_PARA_TRANSPORTE" : "BLOQUEADA",
    pendencias,
    requisicao: {
      id: req.id ?? null,
      hospital: req.hospital.nome,
      tipoSanguineoReceptor: ROTULO_TIPO[req.tipoSanguineo],
      hemocomponente: comp.rotulo,
      quantidade: req.quantidade,
      janelaEntrega: janela.toISOString(),
    },
    transporte: {
      origem,
      destino,
      caminho: rota.caminho,
      distanciaKm: rota.distanciaKm ?? null,
      saidaPrevista: saida.toISOString(),
      chegadaPrevista: chegada.toISOString(),
      tempoEstimadoMin: rota.tempoEstimadoMin,
    },
    cadeiaFria: {
      temperaturaExigida: comp.temperatura,
      acondicionamento: comp.acondicionamento,
      limiteMin: comp.limiteCadeiaFriaMin,
      folgaMin: folgaCadeiaFria,
    },
    itens,
    checklist: [
      "Conferir etiqueta e número de cada bolsa com esta guia",
      `Registrar temperatura da caixa na saída (faixa: ${comp.temperatura})`,
      "Lacrar a caixa térmica e anotar o número do lacre",
      "Registrar temperatura e horário no recebimento pelo hospital",
      "Coletar assinatura do responsável pelo recebimento",
    ],
  };
}

/** Numero legivel e deterministico: HF-AAAAMMDD-XXXXXX */
function gerarNumero(agora, req, bolsas) {
  const base = `${req.id ?? ""}|${req.hospital.nome}|${bolsas.map((b) => b.id).join(",")}|${agora.toISOString()}`;
  let h = 2166136261; // FNV-1a 32 bits
  for (let i = 0; i < base.length; i++) {
    h ^= base.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  const data = agora.toISOString().slice(0, 10).replaceAll("-", "");
  return `HF-${data}-${(h >>> 0).toString(16).toUpperCase().padStart(8, "0").slice(0, 6)}`;
}
