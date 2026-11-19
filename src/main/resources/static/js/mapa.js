import { apiRequest } from "./api-client.js";
import { toast, formatDate, withBusy } from "./ui.js";

const mapElement = document.getElementById("mapa");
let map = null;
if (mapElement && window.L) {
  map = window.L.map("mapa").setView([-8.0476, -34.877], 13);
  window.L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
  }).addTo(map);
}

if (mapElement) {
  const dialog = document.getElementById("dialogAgendamento");
  const dateInput = document.getElementById("dataAgendamento");
  const timeSelect = document.getElementById("horarioAgendamento");
  const confirmButton = document.getElementById("confirmarAgendamento");
  let session = null;
  let selectedHospital = null;

  loadSessionAndHospitals();

  async function loadSessionAndHospitals() {
    try {
      session = await apiRequest("/api/v1/auth/sessao");
      let hospitals;
      if (session.logado && session.role === "DOADOR") {
        hospitals = await apiRequest(`/api/v1/doadores/${session.id}/hospitais-recomendados`);
      } else {
        hospitals = await apiRequest("/api/v1/hospitais");
      }
      const hospitalsValidos = hospitals.filter(hospital =>
        Number.isFinite(hospital.latitude) &&
        Number.isFinite(hospital.longitude) &&
        hospital.latitude !== 0 &&
        hospital.longitude !== 0
      );
      hospitalsValidos.forEach(hospital => {
        if (!window.L) return;
        const marker = window.L.marker([hospital.latitude, hospital.longitude]).addTo(map);
        const popup = document.createElement("div");
        const name = document.createElement("strong");
        name.textContent = hospital.nome;
        popup.append(name);
        const actions = document.createElement("div");
        actions.className = "map-popup-actions";
        const schedule = document.createElement("button");
        schedule.type = "button";
        schedule.className = "btn btn-primary btn-sm";
        schedule.textContent = "Agendar doação";
        schedule.dataset.testid = "mapa-hospital-agendar";
        schedule.addEventListener("click", () => openSchedule(hospital));
        actions.append(schedule);
        const stock = document.createElement("button");
        stock.type = "button";
        stock.className = "btn btn-ghost btn-sm";
        stock.textContent = "Ver estoque";
        stock.dataset.testid = "mapa-hospital-estoque";
        stock.addEventListener("click", () => verEstoque(hospital.id, popup));
        actions.append(stock);
        popup.append(actions);
        marker.bindPopup(popup);
        if (Number(new URLSearchParams(window.location.search).get("agendarHospital")) === hospital.id) {
          window.setTimeout(() => openSchedule(hospital), 0);
        }
      });
      if (!window.L) renderFallbackHospitals(hospitalsValidos);
      renderCampaigns();
    } catch (error) {
      toast(error.message, "error");
    }
  }

  async function renderCampaigns() {
    const list = document.getElementById("campanhas");
    list.replaceChildren();
    try {
      const campaigns = await apiRequest("/api/v1/campanhas");
      if (!campaigns.length) {
        const item = document.createElement("li");
        item.textContent = "Nenhuma campanha ativa neste momento.";
        list.append(item);
        return;
      }

      campaigns.forEach(campaign => {
        const item = document.createElement("li");
        const title = document.createElement("b");
        title.textContent = campaign.titulo;
        item.append(title, document.createElement("br"));
        item.append(document.createTextNode(`${campaign.hospital} · até ${campaign.dataFim}`));
        list.append(item);
      });
    } catch (error) {
      const item = document.createElement("li");
      item.textContent = error.message;
      list.append(item);
    }
  }

  async function verEstoque(hospitalId, popup) {
    try {
      const result = await apiRequest(`/api/v1/bolsas/hospital/${hospitalId}/estoque-mock`);
      popup.querySelectorAll("[data-mapa-estoque]").forEach(node => node.remove());
      const stock = document.createElement("div");
      stock.dataset.mapaEstoque = "";
      const title = document.createElement("b");
      title.textContent = "Estoque demonstrativo";
      stock.append(title);
      const list = document.createElement("ul");
      Object.entries(result.estoque || {}).forEach(([tipo, quantidade]) => {
        const item = document.createElement("li");
        item.textContent = `${tipo.replace("_POS","+").replace("_NEG","−")}: ${quantidade}`;
        list.append(item);
      });
      stock.append(list);
      popup.append(stock);
      map?.invalidateSize();
    } catch (error) {
      toast(error.message, "error");
    }
  }

  function renderFallbackHospitals(hospitals) {
    mapElement.replaceChildren();
    mapElement.classList.add("map-fallback");
    hospitals.forEach(hospital => {
      const card = document.createElement("article");
      card.className = "map-fallback-card";
      const name = document.createElement("h2");
      name.textContent = hospital.nome;
      const type = document.createElement("p");
      type.textContent = hospital.tipoSanguineoDoador
        ? `Tipo sanguíneo compatível: ${hospital.tipoSanguineoDoador.replace("_POS","+").replace("_NEG","−")}`
        : "Ponto de coleta";
      const actions = document.createElement("div");
      actions.className = "map-fallback-actions";
      const schedule = document.createElement("button");
      schedule.type = "button";
      schedule.className = "btn btn-primary";
      schedule.textContent = "Agendar doação";
      schedule.addEventListener("click", () => openSchedule(hospital));
      const stock = document.createElement("button");
      stock.type = "button";
      stock.className = "btn btn-ghost";
      stock.textContent = "Ver estoque";
      stock.addEventListener("click", () => verEstoque(hospital.id, card));
      actions.append(schedule, stock);
      card.append(name, type, actions);
      mapElement.append(card);
    });
  }

  function isoDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  function nextAvailableDate(from) {
    const candidate = new Date(from);
    candidate.setHours(12, 0, 0, 0);
    if (candidate.getDay() === 0) candidate.setDate(candidate.getDate() + 1);
    for (let index = 0; index < 31; index++) {
      if (candidate.getDay() >= 1 && candidate.getDay() <= 6) return isoDate(candidate);
      candidate.setDate(candidate.getDate() + 1);
    }
    return isoDate(from);
  }

  function openSchedule(hospital) {
    if (!session || !session.logado || session.role !== "DOADOR") {
      const returnTo = `/mapa?agendarHospital=${encodeURIComponent(hospital.id)}`;
      window.location.href = `/login-usuario?returnTo=${encodeURIComponent(returnTo)}`;
      return;
    }
    selectedHospital = hospital;
    document.getElementById("hospitalAgendamento").textContent = hospital.nome;
    const today = new Date();
    const maximum = new Date(today);
    maximum.setDate(maximum.getDate() + 30);
    dateInput.min = isoDate(today);
    dateInput.max = isoDate(maximum);
    dateInput.value = nextAvailableDate(today);
    if (typeof dialog.showModal === "function") dialog.showModal();
    loadTimes();
  }

  async function loadTimes() {
    timeSelect.replaceChildren(new Option("Carregando horários...", ""));
    timeSelect.disabled = true;
    confirmButton.disabled = true;
    try {
      const times = await apiRequest(
        `/api/v1/agendamentos/hospital/${selectedHospital.id}/disponibilidade?data=${dateInput.value}`
      );
      if (!times.length) {
        const current = new Date(`${dateInput.value}T12:00:00`);
        current.setDate(current.getDate() + 1);
        const candidate = nextAvailableDate(current);
        if (candidate <= dateInput.max) {
          dateInput.value = candidate;
          await loadTimes();
          return;
        }
      }
      timeSelect.replaceChildren(new Option("Selecione um horário", ""));
      times.forEach(slot => timeSelect.add(new Option(
        `${slot.horario.substring(0,5)} · ${slot.vagasDisponiveis} vaga(s)`,
        slot.horario
      )));
      timeSelect.disabled = !times.length;
    } catch (error) {
      timeSelect.replaceChildren(new Option("Horários indisponíveis", ""));
      toast(error.message, "error");
    }
  }

  dateInput.addEventListener("change", loadTimes);
  timeSelect.addEventListener("change", () => {
    confirmButton.disabled = !timeSelect.value;
  });
  document.getElementById("fecharAgendamento").addEventListener("click", () => dialog.close());
  dialog.addEventListener("click", event => {
    if (event.target === dialog) dialog.close();
  });

  document.getElementById("formAgendamento").addEventListener("submit", event => {
    event.preventDefault();
    withBusy(confirmButton, async () => {
      try {
        await apiRequest("/api/v1/agendamentos", {
          method: "POST",
          body: JSON.stringify({
            hospitalId: selectedHospital.id,
            dataHora: `${dateInput.value}T${timeSelect.value}`
          })
        });
        toast("Doação agendada com sucesso.", "success");
        dialog.close();
      } catch (error) {
        toast(error.message, "error");
        await loadTimes();
      }
    });
  });
}
