import { apiRequest } from "./api-client.js";
import { toast, formatDate, formatNumber, withBusy } from "./ui.js";

async function inicializarCadastro() {
  const form = document.querySelector("[data-doador-cadastro]");
  if (!form) return;
  form.addEventListener("submit", event => {
    event.preventDefault();
    const button = form.querySelector("[type=submit]");
    withBusy(button, async () => {
      try {
        const body = {
          nome: document.getElementById("nome").value,
          email: document.getElementById("email").value,
          senha: document.getElementById("senha").value,
          dataNascimento: document.getElementById("nascimento").value,
          tipoSanguineo: document.getElementById("tipo").value
        };
        const result = await apiRequest("/api/v1/doadores", {
          method: "POST",
          body: JSON.stringify(body)
        });
        const returnTo = new URLSearchParams(window.location.search).get("returnTo");
        if (returnTo && returnTo.startsWith("/")) {
          const destination = new URL(returnTo, window.location.origin);
          if (destination.origin === window.location.origin) {
            window.location.href = destination.pathname + destination.search + destination.hash;
            return;
          }
        }
        window.location.href = result.redirect || "/mapa";
      } catch (error) {
        toast(error.message, "error");
      }
    });
  });
}

function textCell(row, value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "—";
  row.append(cell);
  return cell;
}

async function inicializarDashboard() {
  const dashboard = document.querySelector("[data-doador-dashboard]");
  if (!dashboard) return;
  try {
    const session = await apiRequest("/api/v1/auth/sessao");
    if (!session.logado || session.role !== "DOADOR") {
      window.location.href = "/login-usuario";
      return;
    }
    document.getElementById("nomeDoador").textContent = session.nome || "Doador";
    document.getElementById("saudacao").textContent = `Olá, ${(session.nome || "Doador").split(" ")[0]}.`;

    const [doador, doacoes, agendamentos] = await Promise.all([
      apiRequest(`/api/v1/doadores/${session.id}`),
      apiRequest(`/api/v1/doadores/${session.id}/doacoes`),
      apiRequest(`/api/v1/agendamentos/doador/${session.id}`)
    ]);

    document.getElementById("tipoSanguineo").textContent = `Tipo sanguíneo: ${doador.tipoSanguineo.replace("_POS","+").replace("_NEG","−")}`;

    const historico = document.getElementById("tbHistorico");
    historico.replaceChildren();
    if (!doacoes.length) {
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.colSpan = 3;
      cell.className = "empty";
      cell.textContent = "Nenhuma doação registrada ainda.";
      row.append(cell);
      historico.append(row);
    } else {
      doacoes.forEach(item => {
        const row = document.createElement("tr");
        textCell(row, item.dataDoacao);
        textCell(row, item.hospital);
        textCell(row, formatNumber(item.volumeMl));
        historico.append(row);
      });
    }
    const volume = doacoes.reduce((total, item) => total + (item.volumeMl || 0), 0);
    document.getElementById("statDoacoes").textContent = formatNumber(doacoes.length);
    document.getElementById("statVidas").textContent = formatNumber(doacoes.length * 4);
    document.getElementById("statVol").textContent = formatNumber(volume);

    const agenda = document.getElementById("tbAgendamentos");
    agenda.replaceChildren();
    if (!agendamentos.length) {
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.colSpan = 3;
      cell.className = "empty";
      cell.textContent = "Nenhum agendamento no momento.";
      row.append(cell);
      agenda.append(row);
    } else {
      agendamentos.forEach(item => {
        const row = document.createElement("tr");
        textCell(row, formatDate(item.dataHora));
        textCell(row, item.hospital);
        const status = document.createElement("td");
        const badge = document.createElement("span");
        badge.className = "badge badge-success";
        badge.dataset.status = item.status;
        badge.textContent = item.status;
        status.append(badge);
        row.append(status);
        agenda.append(row);
      });
    }
  } catch (error) {
    toast(error.message, "error");
  }
}

document.querySelectorAll("[data-doador-logout]").forEach(button => {
  button.addEventListener("click", async () => {
    await apiRequest("/api/v1/auth/logout", { method: "POST" });
    window.location.href = "/login-usuario";
  });
});

inicializarCadastro();
inicializarDashboard();
