/**
 * HemoFlow — Função HTTP serverless (Cloudflare Workers)
 *
 *   POST /guia-transporte   -> recebe requisição + bolsas alocadas + rota e devolve a guia
 *   GET  /health            -> verificação simples
 *
 * Formato da resposta: JSON por padrão; HTML imprimível com ?formato=html
 * ou com o header "Accept: text/html".
 */
import { validarEntrada, gerarGuia } from "./guia.js";
import { renderizarHtml } from "./html.js";

const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
};

function json(dados, status = 200) {
  return new Response(JSON.stringify(dados, null, 2), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8", ...CORS },
  });
}

export async function handle(request) {
  const url = new URL(request.url);

  if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS });

  if (url.pathname === "/health" && request.method === "GET") {
    return json({ status: "UP", servico: "hemoflow-guia-transporte" });
  }

  if (url.pathname !== "/guia-transporte") {
    return json({ erro: "Rota não encontrada. Use POST /guia-transporte." }, 404);
  }
  if (request.method !== "POST") {
    return new Response(JSON.stringify({ erro: "Método não permitido. Use POST." }), {
      status: 405,
      headers: { "Content-Type": "application/json; charset=utf-8", Allow: "POST", ...CORS },
    });
  }

  let body;
  try {
    body = await request.json();
  } catch {
    return json({ erro: "JSON malformado." }, 400);
  }

  const erros = validarEntrada(body);
  if (erros.length) return json({ erro: "Requisição inválida.", detalhes: erros }, 400);

  const guia = gerarGuia(body);
  // 201 quando a guia foi emitida e está apta; 422 quando há regra de negócio violada
  const status = guia.status === "APTA_PARA_TRANSPORTE" ? 201 : 422;

  const querHtml = url.searchParams.get("formato") === "html"
    || (request.headers.get("Accept") ?? "").includes("text/html");
  if (querHtml) {
    return new Response(renderizarHtml(guia), {
      status,
      headers: { "Content-Type": "text/html; charset=utf-8", ...CORS },
    });
  }
  return json(guia, status);
}

export default {
  fetch: (request) => handle(request),
};
