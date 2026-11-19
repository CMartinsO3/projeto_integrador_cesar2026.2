import { apiRequest } from "./api-client.js";
import { toast, withBusy } from "./ui.js";

const profile = document.body.dataset.authProfile
  || (window.location.pathname === "/login-motorista" ? "MOTORISTA" : "HOSPITAL");

if (window.location.pathname === "/login-motorista" || window.location.pathname === "/login-hospital") {
  const isMotorista = window.location.pathname === "/login-motorista";
  document.querySelector("[data-operational-profile]").textContent = isMotorista ? "Motorista" : "Hospital";
  document.querySelector("[data-operational-title]").textContent =
    isMotorista ? "Acompanhamento de entrega" : "Requisições hospitalares";
  const alternativo = document.getElementById("perfilAlternativo");
  if (alternativo) {
    alternativo.textContent = isMotorista ? "Acesso hospital" : "Acesso motorista";
    alternativo.href = isMotorista ? "/login-hospital" : "/login-motorista";
  }
}

function safeReturnTo(fallback) {
  const returnTo = new URLSearchParams(window.location.search).get("returnTo");
  if (!returnTo || !returnTo.startsWith("/")) return fallback;
  const destination = new URL(returnTo, window.location.origin);
  return destination.origin === window.location.origin
    ? destination.pathname + destination.search + destination.hash
    : fallback;
}

async function submitLogin(button, form) {
  await withBusy(button, async () => {
    try {
      let endpoint;
      let body;
      if (profile === "DOADOR") {
        endpoint = "/api/v1/auth/login-doador";
        body = {
          email: document.getElementById("email").value,
          senha: document.getElementById("senha").value
        };
      } else if (profile === "ADMIN") {
        endpoint = "/api/v1/auth/login-admin";
        body = {
          usuario: document.getElementById("usuario").value,
          senha: document.getElementById("senha").value
        };
      } else {
        endpoint = "/api/v1/auth/login-operacional";
        body = {
          usuario: document.getElementById("usuario").value,
          senha: document.getElementById("senha").value,
          perfil: profile
        };
      }
      const result = await apiRequest(endpoint, {
        method: "POST",
        body: JSON.stringify(body)
      });
      if (profile === "DOADOR") window.location.href = safeReturnTo(result.redirect);
      else window.location.href = result.redirect;
    } catch (error) {
      toast(error.message, "error");
    }
  });
}

const loginForm = document.querySelector("[data-auth-login]");
if (loginForm) {
  loginForm.addEventListener("submit", event => {
    event.preventDefault();
    submitLogin(loginForm.querySelector("[type=submit]"), loginForm);
  });
}

const cadastroForm = document.querySelector("[data-auth-cadastro]");
if (cadastroForm) {
  const profileSelect = document.getElementById("perfil");
  const hospitalGroup = document.getElementById("hospitalGroup");
  const hospitalSelect = document.getElementById("hospitalId");
  const loginLink = document.getElementById("loginLink");

  function updateProfile() {
    const isHospital = profileSelect.value === "HOSPITAL";
    hospitalGroup.hidden = !isHospital;
    hospitalSelect.required = isHospital;
    if (loginLink) loginLink.href = isHospital ? "/login-hospital" : "/login-motorista";
  }

  profileSelect.addEventListener("change", updateProfile);
  updateProfile();

  apiRequest("/api/v1/hospitais")
    .then(hospitals => {
      hospitalSelect.replaceChildren(new Option("Selecione seu hospital", ""));
      hospitals.filter(hospital => hospital.ativo).forEach(hospital => {
        hospitalSelect.add(new Option(hospital.nome, hospital.id));
      });
    })
    .catch(error => toast(error.message, "error"));

  cadastroForm.addEventListener("submit", event => {
    event.preventDefault();
    const button = cadastroForm.querySelector("[type=submit]");
    withBusy(button, async () => {
      try {
        const result = await apiRequest("/api/v1/auth/cadastro-operacional", {
          method: "POST",
          body: JSON.stringify({
            nome: document.getElementById("nome").value,
            usuario: document.getElementById("usuario").value,
            senha: document.getElementById("senha").value,
            perfil: profileSelect.value,
            hospitalId: profileSelect.value === "HOSPITAL" ? Number(hospitalSelect.value) : null
          })
        });
        if (result.redirect) {
          window.location.href = result.redirect;
          return;
        }
        toast(result.mensagem || "Cadastro enviado para aprovação da equipe clínica.", "success");
        cadastroForm.reset();
        updateProfile();
      } catch (error) {
        toast(error.message, "error");
      }
    });
  });
}
