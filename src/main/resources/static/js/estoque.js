import { apiRequest } from "./api-client.js";
import { toast, formatDate, confirmAction, withBusy } from "./ui.js";

function bloodLabel(value) {
  return String(value || "").replace("_POS","+").replace("_NEG","−");
}

function componentLabel(value) {
  return ({
    HEMACIAS:"Hemácias", PLAQUETAS:"Plaquetas", PLASMA:"Plasma", CRIOPRECIPITADO:"Crioprecipitado"
  })[value] || value || "—";
}

function statusLabel(value) {
  return ({
    DISPONIVEL:"Disponível", ALOCADA:"Alocada", EM_TRANSITO:"Em trânsito",
    ENTREGUE:"Entregue", DESCARTADA:"Descartada"
  })[value] || value || "—";
}

async function carregarBolsas() {
  const tbody = document.getElementById("tabelaBolsas");
  if (!tbody) return;
  const statusSelect = document.getElementById("bolsaStatusFiltro");
  const typeSelect = document.getElementById("bolsaTipoFiltro");
  const status = statusSelect?.value || "";
  const type = typeSelect?.value || "";
  try {
    const bolsas = await apiRequest("/api/v1/bolsas" + (status ? `?status=${encodeURIComponent(status)}` : ""));
    tbody.replaceChildren();
    const filtered = bolsas.filter(bolsa => !type || bolsa.tipoSanguineo === type);
    if (!filtered.length) {
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.colSpan = 7;
      cell.className = "empty";
      cell.textContent = "Nenhuma bolsa encontrada com os filtros selecionados.";
      row.append(cell);
      tbody.append(row);
      return;
    }
    filtered.forEach(bolsa => {
      const row = document.createElement("tr");
      row.dataset.testid = `bolsas-linha-${bolsa.id}`;
      [
        bolsa.id,
        bloodLabel(bolsa.tipoSanguineo),
        componentLabel(bolsa.hemocomponente),
        bolsa.lote || "—",
        formatDate(bolsa.dataValidade, { dateStyle:"medium", timeStyle:undefined }),
        statusLabel(bolsa.status)
      ].forEach(value => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.append(cell);
      });
      const actionCell = document.createElement("td");
      if (bolsa.status === "DISPONIVEL") {
        const transfusion = document.createElement("button");
        transfusion.type = "button";
        transfusion.className = "btn btn-sm btn-teal";
        transfusion.textContent = "Transfusão";
        transfusion.dataset.testid = `bolsas-linha-${bolsa.id}-transfusao`;
        transfusion.addEventListener("click", () => darBaixa(bolsa.id, "TRANSFUSAO", transfusion));
        const discard = document.createElement("button");
        discard.type = "button";
        discard.className = "btn btn-sm btn-danger";
        discard.textContent = "Descartar";
        discard.dataset.testid = `bolsas-linha-${bolsa.id}-descartar`;
        discard.addEventListener("click", () => darBaixa(bolsa.id, "DESCARTE", discard));
        actionCell.append(transfusion, discard);
      } else {
        actionCell.textContent = "—";
      }
      row.append(actionCell);
      tbody.append(row);
    });
  } catch (error) {
    toast(error.message, "error");
  }
}

async function darBaixa(id, motivo, button) {
  const confirmed = await confirmAction({
    title: "Confirmar baixa",
    message: `Confirmar ${motivo === "TRANSFUSAO" ? "transfusão" : "descarte"} da bolsa #${id}?`,
    confirmText: motivo === "TRANSFUSAO" ? "Confirmar transfusão" : "Confirmar descarte"
  });
  if (!confirmed) return;
  await withBusy(button, async () => {
    try {
      await apiRequest(`/api/v1/bolsas/${id}/saida`, {
        method: "POST",
        body: JSON.stringify({ motivo })
      });
      toast("Baixa registrada.", "success");
      await carregarBolsas();
    } catch (error) {
      toast(error.message, "error");
    }
  });
}

const entradaForm = document.getElementById("formEntrada");
if (entradaForm) {
  entradaForm.addEventListener("submit", event => {
    event.preventDefault();
    const button = entradaForm.querySelector("[type=submit]");
    withBusy(button, async () => {
      try {
        const result = await apiRequest("/api/v1/bolsas/entrada", {
          method: "POST",
          body: JSON.stringify({
            tipoSanguineo: document.getElementById("tipoSanguineo").value,
            hemocomponente: document.getElementById("hemo").value,
            lote: document.getElementById("lote").value,
            dataColeta: document.getElementById("coleta").value,
            dataValidade: document.getElementById("validade").value,
            noRedeId: document.getElementById("noRedeId").value
          })
        });
        toast(`Bolsa #${result.id || ""} registrada. Lote: ${result.lote || ""}.`, "success");
        entradaForm.reset();
        await carregarBolsas();
      } catch (error) {
        toast(error.message, "error");
      }
    });
  });
}

document.querySelectorAll("[data-bolsa-reload]").forEach(button => {
  button.addEventListener("click", carregarBolsas);
});
["bolsaStatusFiltro","bolsaTipoFiltro"].forEach(id => {
  const control = document.getElementById(id);
  if (control) control.addEventListener("change", carregarBolsas);
});

apiRequest("/api/v1/nos-rede").then(nodes => {
  const select = document.getElementById("noRedeId");
  if (!select) return;
  select.replaceChildren(...nodes.map(node => new Option(`${node.codigo} — ${node.nome}`, node.id)));
}).catch(error => toast(error.message, "error"));

carregarBolsas();
