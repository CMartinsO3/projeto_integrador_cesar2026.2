import { apiRequest } from "./api-client.js";
import { toast, formatDate, confirmAction, withBusy } from "./ui.js";

const container = document.getElementById("deliveries");
const statusRegion = document.getElementById("deliveryStatus");
let inFlight = false;
let timer = null;

const statusLabels = { ALOCADA:"Aguardando saída", EM_TRANSITO:"Em trânsito", ENTREGUE:"Entregue" };

async function updateStatus(requisicaoId, newStatus, button) {
  await withBusy(button, async () => {
    try {
      await apiRequest(`/api/v1/entregas/requisicoes/${requisicaoId}/status`, {
        method:"PATCH",
        body:JSON.stringify({ status:newStatus })
      });
      toast(newStatus === "ENTREGUE" ? "Entrega confirmada." : "Entrega iniciada.", "success");
      await loadDeliveries();
    } catch (error) {
      toast(error.message, "error");
    }
  });
}

async function advancePoint(entregaId, button) {
  await withBusy(button, async () => {
    try {
      await apiRequest(`/api/v1/entregas/${entregaId}/proximo-ponto`, { method:"PATCH" });
      toast("Próximo ponto atualizado.", "success");
      await loadDeliveries();
    } catch (error) {
      toast(error.message, "error");
    }
  });
}

async function loadDeliveries() {
  if (inFlight || document.hidden) return;
  inFlight = true;
  statusRegion.textContent = "Atualizando entregas...";
  try {
    const entries = await apiRequest("/api/v1/entregas");
    const grouped = new Map();
    entries.forEach(entry => {
      if (!grouped.has(entry.requisicaoId)) grouped.set(entry.requisicaoId, []);
      grouped.get(entry.requisicaoId).push(entry);
    });
    container.replaceChildren();
    if (!entries.length) {
      statusRegion.textContent = "Não há entregas alocadas ou em trânsito.";
      return;
    }
    grouped.forEach((deliveries, requisicaoId) => {
      const first = deliveries[0];
      const card = document.createElement("article");
      card.className = "card delivery-card";
      card.dataset.testid = `motorista-entrega-${requisicaoId}`;
      const kicker = document.createElement("p");
      kicker.className = "kicker";
      kicker.textContent = `Requisição #${requisicaoId} · ${statusLabels[first.statusRequisicao] || first.statusRequisicao}`;
      const title = document.createElement("h2");
      title.textContent = first.nomeHospital;
      card.append(kicker, title);
      const preview = document.createElement("p");
      preview.textContent = `Previsão: ${formatDate(first.previsaoChegada)}`;
      card.append(preview);
      if (first.alertaCadeiaFria) {
        const alert = document.createElement("p");
        alert.className = "notice notice-danger";
        alert.textContent = "Alerta: rota ultrapassa o limite didático de cadeia fria.";
        card.append(alert);
      }
      deliveries.forEach(delivery => {
        const item = document.createElement("section");
        item.className = "delivery-item";
        item.dataset.testid = `motorista-bolsa-${delivery.entregaId}`;
        const bagTitle = document.createElement("h3");
        bagTitle.textContent = `Bolsa #${delivery.bolsaId} · ${delivery.lote}`;
        const blood = document.createElement("p");
        blood.textContent = `${delivery.tipoSanguineo} · ${delivery.hemocomponente}`;
        const point = document.createElement("p");
        point.textContent = `Próximo ponto: ${delivery.rota[delivery.proximoNoIndice]?.nome || first.nomeHospital}`;
        item.append(bagTitle, blood, point);
        if (delivery.alertaTemperatura) {
          const alert = document.createElement("p");
          alert.className = "notice notice-danger";
          alert.textContent = `Alerta térmico simulado: ${delivery.ultimaTemperaturaCelsius} °C.`;
          item.append(alert);
        }
        if (first.statusRequisicao === "EM_TRANSITO" && delivery.proximoNoIndice < delivery.rota.length - 1) {
          const next = document.createElement("button");
          next.type = "button";
          next.className = "btn btn-ghost";
          next.textContent = "Marcar próximo ponto visitado";
          next.dataset.testid = `motorista-entrega-${delivery.entregaId}-proximo-ponto`;
          next.addEventListener("click", () => advancePoint(delivery.entregaId, next));
          item.append(next);
        }
        card.append(item);
      });
      const action = document.createElement("button");
      action.type = "button";
      action.className = "btn btn-primary";
      if (first.statusRequisicao === "ALOCADA") {
        action.textContent = "Iniciar entrega";
        action.dataset.testid = `motorista-entrega-${requisicaoId}-iniciar`;
        action.addEventListener("click", () => updateStatus(requisicaoId, "EM_TRANSITO", action));
      } else if (first.statusRequisicao === "EM_TRANSITO") {
        action.textContent = "Confirmar entrega";
        action.dataset.testid = `motorista-entrega-${requisicaoId}-confirmar`;
        action.addEventListener("click", () => updateStatus(requisicaoId, "ENTREGUE", action));
      }
      if (action.textContent) card.append(action);
      container.append(card);
    });
    statusRegion.textContent = `${grouped.size} entrega(s) em acompanhamento.`;
  } catch (error) {
    toast(error.message, "error");
  } finally {
    inFlight = false;
  }
}

async function validarSessao() {
  const session = await apiRequest("/api/v1/auth/sessao");
  if (!session.logado || !["MOTORISTA","ADMIN"].includes(session.role)) {
    window.location.href = "/login-motorista";
    return false;
  }
  return true;
}

document.getElementById("logoutLink")?.addEventListener("click", async event => {
  event.preventDefault();
  await apiRequest("/api/v1/auth/logout", { method:"POST" });
  window.location.href = "/login-motorista";
});
document.addEventListener("visibilitychange", () => {
  if (!document.hidden) loadDeliveries();
});

validarSessao().then(authorized => {
  if (authorized) {
    loadDeliveries();
    timer = window.setInterval(loadDeliveries, 15000);
  }
}).catch(error => toast(error.message,"error"));
