import { apiRequest } from "./api-client.js";

apiRequest("/api/v1/auth/sessao").then(session => {
  if (!session.logado || session.role !== "DOADOR") return;
  document.querySelectorAll("[data-auth-nav]").forEach(slot => {
    const link = document.createElement("a");
    link.className = "btn donor-access";
    link.href = "/dashboard-usuario";
    const icon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    icon.setAttribute("class", "nav-icon");
    icon.setAttribute("viewBox", "0 0 24 24");
    icon.setAttribute("fill", "none");
    icon.setAttribute("stroke", "currentColor");
    icon.setAttribute("aria-hidden", "true");
    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("d", "M12 3c-1.8 3.2-6 6.4-6 10.2A6 6 0 0 0 12 21a6 6 0 0 0 6-7.8C18 9.4 13.8 6.2 12 3Z");
    icon.append(path);
    const label = document.createElement("span");
    label.textContent = `Olá, ${(session.nome || "Doador").split(" ")[0]}`;
    link.append(icon, label);
    slot.replaceChildren(link);
  });
}).catch(() => {});
