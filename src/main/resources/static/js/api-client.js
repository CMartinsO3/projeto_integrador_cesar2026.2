export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

const loginRoutes = {
  DOADOR: "/login-usuario",
  HOSPITAL: "/login-hospital",
  MOTORISTA: "/login-motorista",
  ADMIN: "/login-admin"
};

function errorMessage(body, status) {
  if (body && Array.isArray(body.detalhes) && body.detalhes.length) {
    return body.detalhes.join(" · ");
  }
  if (body && typeof body.mensagem === "string" && body.mensagem.trim()) {
    return body.mensagem;
  }
  if (body && typeof body.erro === "string" && body.erro.trim()) {
    return body.erro;
  }
  if (status === 401) return "Sua sessão expirou. Entre novamente.";
  if (status === 403) return "Você não tem autorização para esta operação.";
  if (status === 404) return "O recurso solicitado não foi encontrado.";
  if (status === 422) return "A operação não pôde ser concluída pelas regras do sistema.";
  if (status >= 500) return "O servidor encontrou um erro. Tente novamente.";
  return "Não foi possível concluir a operação.";
}

export async function apiRequest(path, options = {}) {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), options.timeout ?? 15000);
  const init = {
    ...options,
    credentials: "same-origin",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    signal: controller.signal
  };

  let response;
  try {
    response = await fetch(path, init);
  } catch (error) {
    if (error.name === "AbortError") {
      throw new ApiError("A operação demorou mais que o esperado. Tente novamente.", 408, null);
    }
    throw new ApiError("Não foi possível conectar ao sistema. Verifique sua conexão.", 0, null);
  } finally {
    window.clearTimeout(timeout);
  }

  const text = await response.text();
  let body = null;
  if (text) {
    try { body = JSON.parse(text); }
    catch { body = { erro: text }; }
  }

  if (!response.ok) {
    if (response.status === 401) {
      let role = null;
      try {
        const sessionResponse = await fetch("/api/v1/auth/sessao", { credentials: "same-origin" });
        if (sessionResponse.ok) {
          const session = await sessionResponse.json();
          role = session.role;
        }
      } catch (_) {}
      const redirect = loginRoutes[role] || "/login-usuario";
      window.setTimeout(() => { window.location.href = redirect; }, 600);
    }
    throw new ApiError(errorMessage(body, response.status), response.status, body);
  }

  return body;
}
