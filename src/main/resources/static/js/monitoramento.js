import { apiRequest } from "./api-client.js";
import { toast, formatTemperature, formatDate, withBusy } from "./ui.js";

const temperatureBody = document.getElementById("temperatureRows");
const deviceBody = document.getElementById("deviceRows");
const rangeBody = document.getElementById("rangeRows");
let inFlight = false;
let intervalMs = 30000;
let timer = null;
let heartbeatTimer = null;
let errors = 0;
const knownAlerts = new Set();

function setTextRow(body, cells) {
  const row = document.createElement("tr");
  cells.forEach(value => {
    const cell = document.createElement("td");
    cell.textContent = value;
    row.append(cell);
  });
  body.append(row);
  return row;
}

async function loadPanel(generateReading = false) {
  if (inFlight || document.hidden) return;
  inFlight = true;
  try {
    if (generateReading) {
      await apiRequest("/api/v1/monitoramento/leituras/simular?forcarAlerta=false", { method:"POST" });
    }
    const panel = await apiRequest("/api/v1/monitoramento");
    intervalMs = Math.max(5000, (panel.intervaloLeituraSegundos || 30) * 1000);
    errors = 0;
    if (temperatureBody) {
      temperatureBody.replaceChildren();
      panel.leiturasRecentes.forEach(reading => {
        const row = setTextRow(temperatureBody, [
          `#${reading.bolsaId} · ${reading.lote}`,
          reading.hemocomponente,
          formatTemperature(reading.temperaturaCelsius),
          `${formatTemperature(reading.minimoConfiguradoCelsius)}–${formatTemperature(reading.maximoConfiguradoCelsius)}`,
          reading.alerta ? "Alerta térmico" : "Dentro da faixa fictícia",
          formatDate(reading.registradaEm)
        ]);
        row.dataset.testid = `monitoramento-leitura-${reading.id}`;
        if (reading.alerta) {
          row.className = "row-alert";
          row.dataset.alerta = "true";
          if (!knownAlerts.has(reading.id)) {
            knownAlerts.add(reading.id);
            toast(`Alerta térmico na bolsa #${reading.bolsaId}.`, "error");
          }
        }
      });
    }
    if (rangeBody) {
      rangeBody.replaceChildren();
      panel.faixasTemperatura.forEach(range => {
        setTextRow(rangeBody, [range.hemocomponente, `${range.minimoCelsius}–${range.maximoCelsius} °C · faixa fictícia`]);
      });
    }
    if (deviceBody) {
      deviceBody.replaceChildren();
      panel.dispositivos.forEach(device => {
        const row = setTextRow(deviceBody, [
          `${device.nome} (${device.codigo})`,
          device.status,
          formatDate(device.atualizadoEm)
        ]);
        row.dataset.testid = `monitoramento-dispositivo-${device.noRedeId}`;
        const action = document.createElement("td");
        const select = document.createElement("select");
        ["ONLINE","INSTAVEL","OFFLINE"].forEach(status => select.add(new Option(status, status)));
        select.value = device.status;
        select.dataset.testid = `monitoramento-dispositivo-${device.noRedeId}-status`;
        const button = document.createElement("button");
        button.type = "button";
        button.className = "btn btn-ghost btn-sm";
        button.textContent = "Atualizar";
        button.addEventListener("click", () => withBusy(button, async () => {
          try {
            await apiRequest(`/api/v1/nos-rede/${device.noRedeId}/status-conectividade`, {
              method:"PUT",
              body:JSON.stringify({ status:select.value })
            });
            toast("Conectividade atualizada.", "success");
            await loadPanel(false);
          } catch (error) {
            toast(error.message, "error");
          }
        }));
        action.append(select, button);
        row.append(action);
        deviceBody.append(row);
      });
    }
    document.getElementById("temperatureStatus").textContent =
      panel.leiturasRecentes.length
        ? `${panel.leiturasRecentes.length} bolsa(s) em trânsito · atualização automática.`
        : "Sem bolsas em trânsito.";
  } catch (error) {
    errors++;
    intervalMs = Math.min(intervalMs * 2, 120000);
    toast(error.message, "error");
  } finally {
    inFlight = false;
  }
}

async function sendHeartbeats() {
  if (document.hidden) return;
  try {
    const devices = await apiRequest("/api/v1/monitoramento");
    await Promise.all(devices.dispositivos.map(device =>
      apiRequest(`/api/v1/nos-rede/${device.noRedeId}/heartbeat`, { method:"POST" })
    ));
  } catch (error) {
    toast(error.message, "error");
  }
}

function schedule() {
  if (timer) window.clearTimeout(timer);
  if (document.hidden) return;
  timer = window.setTimeout(async () => {
    await loadPanel(true);
    schedule();
  }, intervalMs);
}

document.getElementById("normalSample")?.addEventListener("click", () =>
  apiRequest("/api/v1/monitoramento/leituras/simular?forcarAlerta=false", {method:"POST"})
    .then(() => loadPanel(false)).catch(error => toast(error.message,"error")));
document.getElementById("alertSample")?.addEventListener("click", () =>
  apiRequest("/api/v1/monitoramento/leituras/simular?forcarAlerta=true", {method:"POST"})
    .then(() => loadPanel(false)).catch(error => toast(error.message,"error")));

document.addEventListener("visibilitychange", () => {
  if (document.hidden) {
    if (timer) window.clearTimeout(timer);
    if (heartbeatTimer) window.clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  } else {
    loadPanel(false);
    schedule();
    sendHeartbeats();
    heartbeatTimer = window.setInterval(sendHeartbeats, 30000);
  }
});

loadPanel(true).then(() => schedule());
sendHeartbeats();
heartbeatTimer = window.setInterval(sendHeartbeats, 30000);
