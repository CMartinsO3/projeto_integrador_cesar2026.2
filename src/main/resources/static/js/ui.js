export function formatDate(value, options = {}) {
  if (!value) return "—";
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
    ...options
  }).format(date);
}

export function formatNumber(value, options = {}) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return "—";
  return new Intl.NumberFormat("pt-BR", options).format(Number(value));
}

export function formatTemperature(value) {
  if (value === null || value === undefined) return "—";
  return `${formatNumber(value, { minimumFractionDigits: 1, maximumFractionDigits: 1 })} °C`;
}

export function toast(message, tone = "info") {
  let region = document.getElementById("feedbackRegion");
  if (!region) {
    region = document.createElement("div");
    region.id = "feedbackRegion";
    region.className = "toast-region";
    region.setAttribute("aria-live", tone === "error" ? "assertive" : "polite");
    document.body.append(region);
  }
  const item = document.createElement("div");
  item.className = "toast";
  item.dataset.tone = tone;
  item.textContent = message;
  if (tone === "error") item.setAttribute("role", "alert");
  region.append(item);
  window.setTimeout(() => item.remove(), tone === "error" ? 8000 : 5000);
  return item;
}

export async function confirmAction({ title = "Confirmar ação", message, confirmText = "Confirmar", cancelText = "Cancelar" }) {
  let dialog = document.getElementById("confirmDialog");
  if (!dialog) {
    dialog = document.createElement("dialog");
    dialog.id = "confirmDialog";
    dialog.className = "confirm-dialog";
    const heading = document.createElement("h2");
    heading.textContent = title;
    const description = document.createElement("p");
    description.dataset.confirmMessage = "";
    const actions = document.createElement("div");
    actions.className = "confirm-actions";
    const cancel = document.createElement("button");
    cancel.type = "button";
    cancel.className = "btn btn-ghost";
    cancel.dataset.confirmCancel = "";
    const accept = document.createElement("button");
    accept.type = "button";
    accept.className = "btn btn-danger";
    accept.dataset.confirmAccept = "";
    actions.append(cancel, accept);
    dialog.append(heading, description, actions);
    document.body.append(dialog);
  }
  const description = dialog.querySelector("[data-confirm-message], #confirmMessage");
  const cancel = dialog.querySelector("[data-confirm-cancel], #confirmCancel");
  const accept = dialog.querySelector("[data-confirm-accept], #confirmAccept");
  if (description) description.textContent = message;
  if (cancel) cancel.textContent = cancelText;
  if (accept) accept.textContent = confirmText;

  const trigger = document.activeElement;
  if (typeof dialog.showModal === "function") dialog.showModal();
  else return false;

  return new Promise(resolve => {
    const finish = (result) => {
      dialog.close();
      if (trigger instanceof HTMLElement) trigger.focus();
      resolve(result);
    };
    cancel.addEventListener("click", () => finish(false), { once: true });
    accept.addEventListener("click", () => finish(true), { once: true });
    dialog.addEventListener("cancel", () => finish(false), { once: true });
  });
}

export async function withBusy(button, operation) {
  if (!button) return operation();
  if (button.disabled) return;
  const text = button.textContent;
  button.disabled = true;
  button.setAttribute("aria-busy", "true");
  try {
    return await operation();
  } finally {
    button.disabled = false;
    button.removeAttribute("aria-busy");
    button.textContent = text;
  }
}

export function skeleton(container, rows = 3) {
  if (!container) return;
  container.replaceChildren();
  for (let index = 0; index < rows; index++) {
    const line = document.createElement("div");
    line.className = "skeleton";
    line.setAttribute("aria-hidden", "true");
    container.append(line);
  }
}
