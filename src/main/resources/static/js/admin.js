import { apiRequest } from "./api-client.js";
import { toast, formatDate, withBusy, confirmAction } from "./ui.js";

async function validarSessao() {
  const session = await apiRequest("/api/v1/auth/sessao");
  if (!session.logado || session.role !== "ADMIN") {
    window.location.href = "/login-admin";
    return false;
  }
  document.querySelectorAll("[data-session-name]").forEach(node => node.textContent = session.nome || "Administrador");
  return true;
}

function addTextRow(body, values) {
  const row = document.createElement("tr");
  values.forEach(value => {
    const cell = document.createElement("td");
    cell.textContent = value ?? "—";
    row.append(cell);
  });
  body.append(row);
  return row;
}

async function dashboardAdmin() {
  if (document.body.dataset.adminPage !== "dashboard") return;
  await validarSessao();
  const alertBody = document.getElementById("tbAlertas");
  const transitBody = document.getElementById("tbTransito");
  async function loadAlerts() {
    const dias = document.getElementById("selDias").value;
    try {
      const list = await apiRequest(`/api/v1/bolsas/alertas-vencimento?dias=${dias}`);
      document.getElementById("cntAlertas").textContent = list.length;
      alertBody.replaceChildren();
      if (!list.length) addTextRow(alertBody, ["Nenhuma bolsa próxima do vencimento."]);
      list.forEach(bag => addTextRow(alertBody, [
        bag.id, bag.lote,
        String(bag.tipoSanguineo).replace("_POS","+").replace("_NEG","−"),
        bag.hemocomponente, bag.dataValidade, bag.diasRestantes, bag.urgencia
      ]));
    } catch (error) { toast(error.message,"error"); }
  }
  async function loadTransit() {
    try {
      const list = await apiRequest("/api/v1/bolsas/em-transito");
      document.getElementById("cntTransito").textContent = list.length;
      transitBody.replaceChildren();
      if (!list.length) addTextRow(transitBody, ["Nenhuma bolsa em trânsito."]);
      list.forEach(bag => addTextRow(transitBody, [
        bag.id, bag.lote,
        String(bag.tipoSanguineo).replace("_POS","+").replace("_NEG","−"),
        bag.hemocomponente, bag.dataValidade, bag.local
      ]));
    } catch (error) { toast(error.message,"error"); }
  }
  async function loadBags() {
    const status = document.getElementById("filtroStatusBolsas").value;
    const body = document.getElementById("tbBolsas");
    try {
      const bags = await apiRequest("/api/v1/bolsas" + (status ? `?status=${encodeURIComponent(status)}` : ""));
      body.replaceChildren();
      if (!status) {
        document.getElementById("cntDisp").textContent = bags.filter(bag => bag.status === "DISPONIVEL").length;
        document.getElementById("cntEntregues").textContent = bags.filter(bag => bag.status === "ENTREGUE").length;
        document.getElementById("cntDescartadas").textContent = bags.filter(bag => bag.status === "DESCARTADA").length;
      }
      if (!bags.length) addTextRow(body, ["Nenhuma bolsa encontrada."]);
      bags.forEach(bag => {
        const row = addTextRow(body, [
          bag.id,
          bag.lote,
          String(bag.tipoSanguineo).replace("_POS","+").replace("_NEG","−"),
          bag.hemocomponente,
          bag.dataValidade,
          bag.status
        ]);
        const action = document.createElement("td");
        if (bag.status === "DISPONIVEL") {
          for (const [label, motivo] of [["Transfusão","TRANSFUSAO"],["Descartar","DESCARTE"]]) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "btn btn-sm " + (motivo === "DESCARTE" ? "btn-danger" : "btn-teal");
            button.textContent = label;
            button.dataset.testid = `admin-bolsa-${bag.id}-${motivo.toLowerCase()}`;
            button.addEventListener("click", () => withBusy(button, async () => {
              try {
                const confirmed = await confirmAction({
                  title: "Confirmar baixa",
                  message: `${motivo === "TRANSFUSAO" ? "Confirmar transfusão" : "Confirmar descarte"} da bolsa #${bag.id}?`,
                  confirmText: motivo === "TRANSFUSAO" ? "Confirmar transfusão" : "Confirmar descarte"
                });
                if (!confirmed) return;
                await apiRequest(`/api/v1/bolsas/${bag.id}/saida`, {method:"POST",body:JSON.stringify({motivo})});
                toast("Baixa registrada.", "success");
                await Promise.all([loadAlerts(),loadTransit(),loadBags()]);
              } catch (error) { toast(error.message,"error"); }
            }));
            action.append(button);
          }
        } else action.textContent = "—";
        row.append(action);
      });
    } catch (error) { toast(error.message,"error"); }
  }
  document.getElementById("selDias").addEventListener("change", loadAlerts);
  document.getElementById("filtroStatusBolsas").addEventListener("change", loadBags);
  document.getElementById("btnAtualizarBolsas").addEventListener("click", loadBags);
  document.getElementById("formEntrada")?.addEventListener("submit", event => {
    event.preventDefault();
    const button = event.submitter;
    withBusy(button, async () => {
      try {
        const result = await apiRequest("/api/v1/bolsas/entrada", {
          method:"POST",
          body:JSON.stringify({
            tipoSanguineo:document.getElementById("entTipo").value,
            hemocomponente:document.getElementById("entHemo").value,
            lote:document.getElementById("entLote").value,
            dataColeta:document.getElementById("entColeta").value,
            dataValidade:document.getElementById("entValidade").value,
            noRedeId:document.getElementById("entNoRede").value
          })
        });
        toast(`Bolsa #${result.id} registrada.`, "success");
        await Promise.all([loadAlerts(),loadTransit(),loadBags()]);
      } catch (error) { toast(error.message,"error"); }
    });
  });
  apiRequest("/api/v1/nos-rede").then(nodes => {
    const select = document.getElementById("entNoRede");
    select.replaceChildren(...nodes.map(node => new Option(`${node.codigo} — ${node.nome}`,node.id)));
  }).catch(error => toast(error.message,"error"));
  await Promise.all([loadAlerts(),loadTransit(),loadBags()]);
}

async function contasOperacionais() {
  if (document.body.dataset.adminPage !== "contas") return;
  if (!(await validarSessao())) return;
  async function loadAll() {
    try {
      const [hospitals, users, deliveries] = await Promise.all([
        apiRequest("/api/v1/hospitais"),
        apiRequest("/api/v1/auth/usuarios"),
        apiRequest("/api/v1/entregas")
      ]);
      const pending = users.filter(user => user.perfil === "HOSPITAL" && !user.aprovado);
      const pendingBody = document.getElementById("solicitacoesHospital");
      pendingBody.replaceChildren();
      if (!pending.length) addTextRow(pendingBody, ["Nenhuma solicitação pendente."]);
      pending.forEach(user => {
        const row = addTextRow(pendingBody,[user.nome,user.usuario,user.hospitalNome || "Hospital não informado"]);
        const action = document.createElement("td");
        const approve = document.createElement("button");
        approve.type = "button";
        approve.className = "btn btn-teal btn-sm";
        approve.textContent = "Aprovar";
        approve.dataset.testid = `contas-linha-${user.id}-aprovar`;
        approve.addEventListener("click", () => withBusy(approve, async () => {
          try {
            await apiRequest(`/api/v1/auth/usuarios/${user.id}/aprovar`, {method:"POST"});
            toast(`Acesso hospitalar de ${user.nome} aprovado.`, "success");
            await loadAll();
          } catch (error) { toast(error.message,"error"); }
        }));
        action.append(approve);
        row.append(action);
      });
      const hospitalSelect = document.getElementById("contaHospital");
      hospitalSelect.replaceChildren(new Option("Selecione um hospital",""), ...hospitals.filter(hospital => hospital.ativo).map(hospital => new Option(hospital.nome,hospital.id)));
      const drivers = document.getElementById("motoristaAtribuicao");
      drivers.replaceChildren(...users.filter(user => user.perfil === "MOTORISTA").map(user => new Option(`${user.nome} (${user.usuario})`,user.id)));
      const deliverySelect = document.getElementById("entregaAtribuicao");
      deliverySelect.replaceChildren(...deliveries.map(delivery => new Option(`#${delivery.entregaId} · ${delivery.nomeHospital} · ${delivery.lote}`,delivery.entregaId)));
    } catch (error) { toast(error.message,"error"); }
  }
  document.getElementById("criarContaForm")?.addEventListener("submit", event => {
    event.preventDefault();
    const button = event.submitter;
    withBusy(button, async () => {
      try {
        const perfil = document.getElementById("contaPerfil").value;
        const result = await apiRequest("/api/v1/auth/usuarios", {
          method:"POST",
          body:JSON.stringify({
            usuario:document.getElementById("contaUsuario").value,
            senha:document.getElementById("contaSenha").value,
            nome:document.getElementById("contaNome").value,
            perfil,
            hospitalId:perfil === "HOSPITAL" ? Number(document.getElementById("contaHospital").value) : null
          })
        });
        toast(`Conta ${result.usuario} criada.`, "success");
        event.target.reset();
        await loadAll();
      } catch (error) { toast(error.message,"error"); }
    });
  });
  document.getElementById("atribuirEntregaForm")?.addEventListener("submit", event => {
    event.preventDefault();
    withBusy(event.submitter, async () => {
      try {
        const entregaId = document.getElementById("entregaAtribuicao").value;
        const motoristaId = document.getElementById("motoristaAtribuicao").value;
        await apiRequest(`/api/v1/entregas/${entregaId}/motorista`, {
          method:"PUT",
          body:JSON.stringify({motoristaId:Number(motoristaId)})
        });
        toast("Entrega atribuída ao motorista.", "success");
      } catch (error) { toast(error.message,"error"); }
    });
  });
  document.getElementById("contaPerfil").addEventListener("change", event => {
    document.getElementById("contaHospital").disabled = event.target.value !== "HOSPITAL";
  });
  await loadAll();
}

dashboardAdmin();
contasOperacionais();
