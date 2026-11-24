import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { handle } from "../src/index.js";
import { compativel } from "../src/guia.js";

const exemplo = JSON.parse(readFileSync(new URL("../exemplo-requisicao.json", import.meta.url)));
const clone = () => structuredClone(exemplo);

const post = (body, query = "") =>
  handle(new Request(`http://localhost/guia-transporte${query}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: typeof body === "string" ? body : JSON.stringify(body),
  }));

test("matriz ABO/Rh igual à do backend Java", () => {
  assert.equal(compativel("O_NEG", "AB_POS"), true);
  assert.equal(compativel("A_POS", "A_NEG"), false);
  assert.equal(compativel("AB_POS", "O_POS"), false);
  assert.equal(compativel("O_POS", "B_POS"), true);
});

test("requisição válida gera guia APTA (201) com bolsas em ordem FEFO", async () => {
  const res = await post(clone());
  assert.equal(res.status, 201);
  const guia = await res.json();
  assert.equal(guia.status, "APTA_PARA_TRANSPORTE");
  assert.match(guia.numeroGuia, /^HF-\d{8}-[0-9A-F]{6}$/);
  assert.deepEqual(guia.itens.map((i) => i.bolsaId), ["BOL-0042", "BOL-0007"]);
  assert.equal(guia.transporte.destino, "Hospital da Restauração");
});

test("bolsa incompatível bloqueia a guia (422)", async () => {
  const body = clone();
  body.bolsas[0].tipoSanguineo = "B_POS";
  const res = await post(body);
  assert.equal(res.status, 422);
  const guia = await res.json();
  assert.equal(guia.status, "BLOQUEADA");
  assert.ok(guia.pendencias.some((p) => p.includes("incompatível")));
});

test("rota acima do limite de cadeia fria bloqueia a guia", async () => {
  const body = clone();
  body.rota.tempoEstimadoMin = 150;
  const guia = await (await post(body)).json();
  assert.ok(guia.pendencias.some((p) => p.includes("cadeia fria")));
});

test("entrada inválida retorna 400 com detalhes", async () => {
  const res = await post({ requisicao: { tipoSanguineo: "X" } });
  assert.equal(res.status, 400);
  const corpo = await res.json();
  assert.ok(corpo.detalhes.length > 0);
});

test("JSON malformado retorna 400", async () => {
  assert.equal((await post("{oops")).status, 400);
});

test("GET na rota da guia retorna 405", async () => {
  const res = await handle(new Request("http://localhost/guia-transporte"));
  assert.equal(res.status, 405);
});

test("?formato=html devolve guia imprimível", async () => {
  const res = await post(clone(), "?formato=html");
  assert.match(res.headers.get("Content-Type"), /text\/html/);
  assert.match(await res.text(), /Guia de Transporte/);
});
