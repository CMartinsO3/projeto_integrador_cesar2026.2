(() => {
  'use strict';

  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const clamp = (v, a, b) => Math.max(a, Math.min(b, v));

  const stage = $('#stage');
  const wrap = $('#stage-wrap');
  const slides = $$('.slide', stage);
  const N = slides.length;
  const isPresenter = new URLSearchParams(location.search).has('presenter');
  if (isPresenter) document.body.classList.add('presenter');

  // Quantos passos (cliques) cada slide tem
  const maxStep = slides.map((s) => {
    let m = +(s.dataset.steps || 0);
    $$('[data-f],[data-only]', s).forEach((e) => {
      m = Math.max(m, +(e.dataset.f ?? e.dataset.only));
    });
    return m;
  });

  let cur = 0;
  let step = 0;
  let black = false;

  // ---------- Encaixe 1600x900 ----------
  function fit() {
    const s = Math.min(wrap.clientWidth / 1600, wrap.clientHeight / 900);
    stage.style.transform = `translate(-50%, -50%) scale(${s})`;
  }

  // ---------- Contadores animados ----------
  const fmt = (v, dec) => v.toLocaleString('pt-BR', { minimumFractionDigits: dec, maximumFractionDigits: dec });
  function runCount(el) {
    const target = parseFloat(el.dataset.count);
    const dec = +(el.dataset.dec || 0);
    const t0 = performance.now();
    const dur = 1300;
    el.dataset.done = '1';
    const tick = (t) => {
      if (!el.dataset.done) return;
      const p = Math.min(1, (t - t0) / dur);
      const e = 1 - Math.pow(1 - p, 3);
      el.textContent = fmt(target * e, dec);
      if (p < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }

  // ---------- Render ----------
  function render(broadcast = true, prevCur = cur, prevStep = step) {
    slides.forEach((s, k) => {
      const active = k === cur;
      s.classList.toggle('active', active);
      s.classList.toggle('before', k < cur);
      const st = active ? step : k < cur ? maxStep[k] : 0;

      for (let n = 1; n <= maxStep[k]; n++) s.classList.toggle('st' + n, n <= st);

      $$('[data-f]', s).forEach((e) => {
        const f = +e.dataset.f;
        e.classList.toggle('on', st >= f);
        e.classList.toggle('past', st > f);
      });
      $$('[data-only]', s).forEach((e) => e.classList.toggle('on', active && st === +e.dataset.only));

      $$('[data-count]', s).forEach((el) => {
        const host = el.closest('[data-f]');
        const visible = active && (!host || host.classList.contains('on'));
        if (visible && !el.dataset.done) runCount(el);
        if (!visible) { el.dataset.done = ''; el.textContent = fmt(0, +(el.dataset.dec || 0)); }
      });

      if (active && k === prevCur && step > prevStep) {
        $$('[data-shake]', s).forEach((el) => {
          el.classList.remove('shake');
          void el.offsetWidth;
          el.classList.add('shake');
        });
      }
    });

    $('#counter').textContent = `${cur + 1} / ${N}`;
    $('#bar').style.width = `${((cur + (maxStep[cur] ? step / (maxStep[cur] + 1) : 0)) / (N - 1)) * 100}%`;

    const notes = $('.notes', slides[cur]);
    const html = notes ? notes.innerHTML : '';
    $('#notes-overlay').innerHTML = html;
    $('#pp-notes').innerHTML = html;
    $('#pp-pos').textContent = `slide ${cur + 1}/${N} · passo ${step}/${maxStep[cur]}`;
    $('#pp-next').textContent = step < maxStep[cur]
      ? `mais ${maxStep[cur] - step} clique(s) neste slide`
      : cur < N - 1 ? slides[cur + 1].dataset.title : 'fim';

    $('#blackout').classList.toggle('show', black);

    const hash = `#${cur + 1}${step ? '.' + step : ''}`;
    history.replaceState(null, '', location.pathname + location.search + hash);

    if (broadcast) send();
  }

  function go(k, s = 0) {
    const pc = cur, ps = step;
    cur = clamp(k, 0, N - 1);
    step = clamp(s, 0, maxStep[cur]);
    render(true, pc, ps);
  }
  const next = () => (step < maxStep[cur] ? go(cur, step + 1) : cur < N - 1 && go(cur + 1, 0));
  const prev = () => (step > 0 ? go(cur, step - 1) : cur > 0 && go(cur - 1, maxStep[cur - 1]));

  // ---------- Sincronização com a janela do apresentador ----------
  const chan = 'BroadcastChannel' in window ? new BroadcastChannel('hemoflow-remodelagem') : null;
  let peer = isPresenter ? window.opener : null;

  function send() {
    const msg = { hf: 1, cur, step, black };
    if (chan) chan.postMessage(msg);
    try { if (peer && !peer.closed) peer.postMessage(msg, '*'); } catch (_) { /* janela fechada */ }
  }
  function receive(d) {
    if (!d || d.hf !== 1) return;
    if (d.cur === cur && d.step === step && d.black === black) return;
    const pc = cur, ps = step;
    cur = clamp(d.cur, 0, N - 1);
    step = clamp(d.step, 0, maxStep[cur]);
    black = !!d.black;
    render(false, pc, ps);
  }
  if (chan) chan.onmessage = (e) => receive(e.data);
  window.addEventListener('message', (e) => { if (peer && e.source === peer) receive(e.data); });

  function openPresenter() {
    if (isPresenter) return;
    peer = window.open(location.pathname + '?presenter' + location.hash, 'hemoflow-presenter', 'width=1280,height=760');
  }

  // ---------- Entrada ----------
  document.addEventListener('keydown', (e) => {
    if (e.ctrlKey || e.metaKey || e.altKey) return;
    const k = e.key;
    if (['ArrowRight', 'ArrowDown', ' ', 'PageDown', 'Enter'].includes(k)) {
      e.preventDefault(); e.shiftKey ? go(cur + 1) : next();
    } else if (['ArrowLeft', 'ArrowUp', 'PageUp', 'Backspace'].includes(k)) {
      e.preventDefault(); e.shiftKey ? go(cur - 1) : prev();
    } else if (k === 'Home') go(0);
    else if (k === 'End') go(N - 1);
    else if (k === 'n' || k === 'N') $('#notes-overlay').classList.toggle('show');
    else if (k === 'p' || k === 'P') openPresenter();
    else if (k === 'b' || k === 'B' || k === '.') { black = !black; render(); }
    else if (k === 'f' || k === 'F') {
      if (document.fullscreenElement) document.exitFullscreen();
      else document.documentElement.requestFullscreen?.();
    } else if (k === 'r' || k === 'R') resetTimer();
    else return;
    hideHint();
  });

  wrap.addEventListener('click', (e) => {
    if (e.target.closest('a,button')) return;
    const r = wrap.getBoundingClientRect();
    (e.clientX - r.left < r.width * 0.25 ? prev : next)();
    hideHint();
  });
  $('#blackout').addEventListener('click', () => { black = false; render(); });

  let tx = null;
  wrap.addEventListener('touchstart', (e) => { tx = e.touches[0].clientX; }, { passive: true });
  wrap.addEventListener('touchend', (e) => {
    if (tx === null) return;
    const dx = e.changedTouches[0].clientX - tx;
    if (Math.abs(dx) > 50) { (dx < 0 ? next : prev)(); }
    tx = null;
  });

  // ---------- Dica inicial ----------
  const hint = $('#hint');
  let hintTimer = setTimeout(hideHint, 5000);
  function hideHint() { clearTimeout(hintTimer); hint.classList.add('hide'); }

  // ---------- Cronômetro (apresentador) ----------
  let t0 = Date.now();
  function resetTimer() { t0 = Date.now(); }
  $('#pp-reset').addEventListener('click', resetTimer);
  setInterval(() => {
    const s = Math.floor((Date.now() - t0) / 1000);
    $('#pp-timer').textContent = `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
  }, 500);

  // ---------- Fundo: células fluindo ----------
  const cv = $('#bg');
  const ctx = cv.getContext('2d');
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  let W = 0, H = 0;
  const cells = [];
  function resizeBg() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    W = innerWidth; H = innerHeight;
    cv.width = W * dpr; cv.height = H * dpr;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }
  function makeCells() {
    cells.length = 0;
    const count = Math.round((W * H) / 42000);
    for (let i = 0; i < count; i++) {
      const r = 4 + Math.random() * 13;
      cells.push({
        x: Math.random() * W, y: Math.random() * H, r,
        vx: 0.12 + Math.random() * 0.35 * (r / 10), vy: (Math.random() - 0.5) * 0.08,
        rot: Math.random() * Math.PI, vr: (Math.random() - 0.5) * 0.004,
        a: 0.04 + Math.random() * 0.12, ph: Math.random() * 6.28,
      });
    }
  }
  function drawBg(t) {
    ctx.clearRect(0, 0, W, H);
    for (const c of cells) {
      if (!reduce) {
        c.x += c.vx; c.y += c.vy + Math.sin(t / 2000 + c.ph) * 0.06; c.rot += c.vr;
        if (c.x - c.r > W) { c.x = -c.r; c.y = Math.random() * H; }
      }
      ctx.save();
      ctx.translate(c.x, c.y); ctx.rotate(c.rot);
      ctx.fillStyle = `rgba(224,67,75,${c.a})`;
      ctx.beginPath(); ctx.ellipse(0, 0, c.r, c.r * 0.82, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = `rgba(7,19,29,${c.a * 1.6})`;
      ctx.beginPath(); ctx.ellipse(0, 0, c.r * 0.45, c.r * 0.36, 0, 0, Math.PI * 2); ctx.fill();
      ctx.restore();
    }
    looping = !reduce && !document.hidden;
    if (looping) requestAnimationFrame(drawBg);
  }
  let looping = false;
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden && !reduce && !looping) { looping = true; requestAnimationFrame(drawBg); }
  });

  // ---------- Início ----------
  function fromHash() {
    const m = location.hash.match(/^#(\d+)(?:\.(\d+))?/);
    if (!m) return false;
    cur = clamp(+m[1] - 1, 0, N - 1);
    step = clamp(+(m[2] || 0), 0, maxStep[cur]);
    return true;
  }
  fromHash();
  window.addEventListener('hashchange', () => { const pc = cur, ps = step; if (fromHash()) render(true, pc, ps); });

  window.addEventListener('resize', () => { fit(); resizeBg(); makeCells(); });
  fit(); resizeBg(); makeCells(); requestAnimationFrame(drawBg);
  render(false);
})();
