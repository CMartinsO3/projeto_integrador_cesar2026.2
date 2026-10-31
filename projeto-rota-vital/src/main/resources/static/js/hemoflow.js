window.HF = {
  tipo(v) {
    const m = {O_NEG:'O−',O_POS:'O+',A_POS:'A+',A_NEG:'A−',B_POS:'B+',B_NEG:'B−',AB_POS:'AB+',AB_NEG:'AB−'};
    if (!v) return '—';
    if (String(v).toLowerCase() === 'todos') return 'Todos';
    return m[v] || String(v).replace('_POS','+').replace('_NEG','−');
  },
  tiposLista(s) {
    if (!s) return '—';
    if (String(s).toLowerCase() === 'todos') return 'Todos';
    return String(s).split(',').map(p => this.tipo(p.trim())).join(' · ');
  },
  hemo(v) {
    const m = {HEMACIAS:'Hemácias',PLAQUETAS:'Plaquetas',PLASMA:'Plasma',CRIOPRECIPITADO:'Crioprecipitado'};
    return m[v] || v || '—';
  },
  status(v) {
    const m = {DISPONIVEL:'Disponível',ALOCADA:'Alocada',EM_TRANSITO:'Em trânsito',ENTREGUE:'Entregue',DESCARTADA:'Descartada',AGENDADO:'Agendado',CONCLUIDO:'Concluído',CANCELADO:'Cancelado'};
    return m[v] || v || '—';
  },
  urgencia(v) {
    const m = {CRITICO:'Crítico',ALTO:'Alto',MEDIO:'Médio'};
    return m[v] || v || '—';
  },
  abo(v) { return '<span class="abo">' + this.tipo(v) + '</span>'; },
  aplicarSessaoNav() {
    const slots = document.querySelectorAll('[data-auth-nav]');
    if (!slots.length) return;
    fetch('/api/v1/auth/sessao').then(r => r.json()).then(s => {
      slots.forEach(el => {
        if (s.logado && s.role === 'DOADOR') {
          const nome = (s.nome || 'Doador').split(' ')[0];
          el.innerHTML = '<a class="btn btn-primary" href="/dashboard-usuario">Olá, ' + nome + '</a>';
          document.querySelectorAll('a[href="/doe-agora"]').forEach(a => { a.style.display = 'none'; });
        } else if (s.logado && s.role === 'ADMIN') {
          el.innerHTML = '<a class="btn btn-teal" href="/dashboard-admin">Sala de operações</a>';
        }
      });
    }).catch(() => {});
  }
};
document.addEventListener('DOMContentLoaded', () => HF.aplicarSessaoNav());
