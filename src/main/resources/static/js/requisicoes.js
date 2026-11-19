import { apiRequest } from "./api-client.js";
import { toast, formatDate, withBusy } from "./ui.js";

const bloodTypes = ["A_POS","A_NEG","B_POS","B_NEG","AB_POS","AB_NEG","O_POS","O_NEG"];
const components = ["HEMACIAS","PLASMA","PLAQUETAS","CRIOPRECIPITADO"];

function bloodLabel(value) { return String(value).replace("_POS","+").replace("_NEG","−"); }
function componentLabel(value) {
  return ({HEMACIAS:"Hemácias",PLASMA:"Plasma",PLAQUETAS:"Plaquetas",CRIOPRECIPITADO:"Crioprecipitado"})[value] || value;
}
function statusLabel(value) {
  return ({
    PENDENTE:"Pendente", AGUARDANDO_ESTOQUE:"Aguardando estoque", ALOCADA:"Alocada",
    EM_TRANSITO:"Em trânsito", ENTREGUE:"Entregue", CANCELADA:"Cancelada"
  })[value] || value;
}

const form = document.getElementById("requestForm");
const hospitalSelect = document.getElementById("hospitalId");
const filterSelect = document.getElementById("hospitalFilter");
const requestRows = document.getElementById("requestRows");
let currentRole = null;
let refreshTimer = null;

function setOptions(select, values, placeholder) {
  if (!select) return;
  select.replaceChildren(new Option(placeholder, ""));
  values.forEach(([label, value]) => select.add(new Option(label, value)));
}

async function loadHospitals() {
  const session = await apiRequest("/api/v1/auth/sessao");
  if (!session.logado || !["HOSPITAL","ADMIN"].includes(session.role)) {
    window.location.href = "/login-hospital";
    return;
  }
  currentRole = session.role;
  let hospitals;
  if (session.role === "HOSPITAL") {
    hospitals = [{ id: session.hospitalId, nome: session.hospitalNome, ativo:true }];
  } else {
    hospitals = (await apiRequest("/api/v1/hospitais")).filter(hospital => hospital.ativo);
  }
  const options = hospitals.map(hospital => [`${hospital.nome} (#${hospital.id})`, hospital.id]);
  setOptions(hospitalSelect, options, "Selecione um hospital");
  setOptions(filterSelect, options, "Selecione um hospital");
  if (session.role === "HOSPITAL") {
    hospitalSelect.value = String(session.hospitalId);
    hospitalSelect.disabled = true;
    filterSelect.value = String(session.hospitalId);
    await loadRequests(session.hospitalId);
  }
}

async function processRequest(requestId, button) {
  await withBusy(button, async () => {
    try {
      const result = await apiRequest(`/api/v1/requisicoes/${requestId}/processar`, { method:"POST" });
      const details = result.entregas.map(entrega =>
        `bolsa ${entrega.bolsa.lote} (${bloodLabel(entrega.bolsa.tipoSanguineo)}, vence ${String(entrega.bolsa.dataValidade).split("-").reverse().join("/")}) · ` +
        `${entrega.rota.map(no => no.codigo).join(" → ")} · ${entrega.tempoTotalMinutos} min` +
        (entrega.alertaCadeiaFria ? " · ALERTA cadeia fria" : "")
      ).join("; ");
      toast(`Requisição #${requestId} ${statusLabel(result.status)}: ${details}.`, "success");
    } catch (error) {
      toast(error.message, "error");
    }
    await loadRequests(filterSelect?.value);
  });
}

async function loadRequests(hospitalId) {
  if (!requestRows) return;
  if (!hospitalId) {
    requestRows.replaceChildren();
    return;
  }
  try {
    const requests = await apiRequest(`/api/v1/requisicoes?hospitalId=${encodeURIComponent(hospitalId)}`);
    requests.sort((a, b) => b.id - a.id);
    requestRows.replaceChildren();
    if (!requests.length) {
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.colSpan = 6;
      cell.className = "empty";
      cell.textContent = "Nenhuma requisição para este hospital.";
      row.append(cell);
      requestRows.append(row);
    } else {
      requests.forEach(request => {
        const row = document.createElement("tr");
        row.dataset.testid = `requisicoes-linha-${request.id}`;
        [
          `#${request.id}`,
          `${bloodLabel(request.tipoSanguineo)} · ${componentLabel(request.hemocomponente)}`,
          String(request.quantidade),
          formatDate(request.janelaEntrega),
          statusLabel(request.status)
        ].forEach(value => {
          const cell = document.createElement("td");
          cell.textContent = value;
          row.append(cell);
        });
        const actionCell = document.createElement("td");
        if (currentRole === "ADMIN" && ["PENDENTE","AGUARDANDO_ESTOQUE"].includes(request.status)) {
          const process = document.createElement("button");
          process.type = "button";
          process.className = "btn btn-sm btn-teal";
          process.textContent = "Processar";
          process.dataset.testid = `requisicoes-linha-${request.id}-processar`;
          process.addEventListener("click", () => processRequest(request.id, process));
          actionCell.append(process);
        } else {
          actionCell.textContent = "—";
        }
        row.append(actionCell);
        requestRows.append(row);
      });
    }
  } catch (error) {
    toast(error.message, "error");
  }
}

if (form) {
  const bloodSelect = document.getElementById("bloodType");
  const componentSelect = document.getElementById("component");
  setOptions(bloodSelect, bloodTypes.map(value => [bloodLabel(value), value]), "Selecione");
  setOptions(componentSelect, components.map(value => [componentLabel(value), value]), "Selecione");

  const windowInput = document.getElementById("deliveryWindow");
  const now = new Date(Date.now() + 4*60*60*1000);
  windowInput.min = new Date(Date.now() - new Date().getTimezoneOffset()*60000).toISOString().slice(0,16);
  windowInput.value = new Date(now.getTime() - now.getTimezoneOffset()*60000).toISOString().slice(0,16);

  form.addEventListener("submit", event => {
    event.preventDefault();
    const button = document.getElementById("submitRequest");
    withBusy(button, async () => {
      try {
        const created = await apiRequest("/api/v1/requisicoes", {
          method:"POST",
          body:JSON.stringify({
            hospitalId:Number(hospitalSelect.value),
            tipoSanguineo:bloodSelect.value,
            hemocomponente:componentSelect.value,
            quantidade:Number(document.getElementById("quantity").value),
            janelaEntrega:windowInput.value
          })
        });
        toast(`Requisição #${created.id} criada com status ${statusLabel(created.status)}.`, "success");
        await loadRequests(hospitalSelect.value);
      } catch (error) {
        toast(error.message, "error");
      }
    });
  });
  filterSelect?.addEventListener("change", () => loadRequests(filterSelect.value));
  document.getElementById("logoutLink")?.addEventListener("click", async event => {
    event.preventDefault();
    await apiRequest("/api/v1/auth/logout", { method:"POST" });
    window.location.href = "/login-hospital";
  });
  loadHospitals().catch(error => toast(error.message, "error"));
  // Atualiza o status das requisições sem precisar recarregar a página
  refreshTimer = window.setInterval(() => {
    if (!document.hidden && filterSelect?.value) loadRequests(filterSelect.value);
  }, 10000);
}
