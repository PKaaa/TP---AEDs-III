const API = 'http://localhost:7777';
let resetCodeSentTo = '';
const SIDEBAR_STORAGE_KEY = 'nc_sidebar_collapsed';

/* SEARCH STATE — armazena termos de busca atuais */
let _searchClientesQuery = '';
let _searchAlimentosQuery = '';
let _searchReceitasQuery = '';
let _searchClientesMode = 'all';
let _searchAlimentosMode = 'all';
let _searchReceitasMode = 'all';
 
/* API HELPER */
async function api(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  try {
    const r = await fetch(API + path, opts);
    const data = await r.json().catch(() => ({}));
    return { ok: r.ok, status: r.status, data };
  } catch {
    return {
      ok: false, status: 0,
      data: { erro: 'Servidor offline. Verifique se o backend está rodando na porta 7777.' }
    };
  }
}
 
/* NAVEGAÇÃO — definido ANTES da IIFE iniciar()*/
const PAGES = {
  dashboard: { title: 'Dashboard',  sub: 'Visão geral do sistema', render: renderDashboard },
  clientes:  { title: 'Clientes',   sub: 'Gerenciar clientes',     render: renderClientes  },
  alimentos: { title: 'Alimentos',  sub: 'Gerenciar alimentos',    render: renderAlimentos },
  receitas:  { title: 'Receitas',   sub: 'Gerenciar receitas',     render: renderReceitas  },
};
 
function goPage(page) {
  if (!PAGES[page]) return;
 
  document.querySelectorAll('.nav-item').forEach(el =>
    el.classList.toggle('active', el.dataset.page === page));
 
  document.getElementById('page-title').textContent = PAGES[page].title;
  document.getElementById('page-sub').textContent   = PAGES[page].sub;
  document.getElementById('topbar-action').innerHTML = '';
 
  PAGES[page].render();
}
 
/* SIDEBAR TOGGLE */
function isSidebarCollapsed() {
  return localStorage.getItem(SIDEBAR_STORAGE_KEY) === 'true';
}
 
function applySidebarState(collapsed) {
  document.body.classList.toggle('sidebar-collapsed', collapsed);
  localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed));
 
  const toggle = document.querySelector('.sidebar-toggle');
  if (toggle) {
    toggle.textContent = collapsed ? '⟩' : '⟨';
    toggle.setAttribute('aria-label', collapsed ? 'Expandir sidebar' : 'Recolher sidebar');
    toggle.title = collapsed ? 'Expandir sidebar' : 'Recolher sidebar';
  }
}
 
function toggleSidebar() {
  applySidebarState(!isSidebarCollapsed());
}
 
/* AUTH — LOGIN / LOGOUT */
function showLogin() {
  document.getElementById('login-screen').classList.remove('hidden');
  document.getElementById('app').classList.remove('visible');
}
 
function showApp(usuario) {
  document.getElementById('login-screen').classList.add('hidden');
  document.getElementById('app').classList.add('visible');
  const nome = usuario?.nome || usuario?.email || 'Chef Admin';
  document.getElementById('chef-nome').textContent = nome;
  applySidebarState(isSidebarCollapsed());
}
 
function showLoginMode() {
  document.getElementById('login-form').classList.remove('hidden-auth');
  document.getElementById('register-form').classList.add('hidden-auth');
  document.getElementById('forgot-form').classList.add('hidden-auth');
  document.getElementById('sw-login').classList.add('active');
  document.getElementById('sw-register').classList.remove('active');
  document.getElementById('register-err').style.display = 'none';
  document.getElementById('forgot-err').style.display = 'none';
}
 
function showRegisterMode() {
  document.getElementById('register-form').classList.remove('hidden-auth');
  document.getElementById('login-form').classList.add('hidden-auth');
  document.getElementById('forgot-form').classList.add('hidden-auth');
  document.getElementById('sw-register').classList.add('active');
  document.getElementById('sw-login').classList.remove('active');
  document.getElementById('login-err').style.display = 'none';
  document.getElementById('forgot-err').style.display = 'none';
}
 
function showForgotMode() {
  document.getElementById('forgot-form').classList.remove('hidden-auth');
  document.getElementById('login-form').classList.add('hidden-auth');
  document.getElementById('register-form').classList.add('hidden-auth');
  document.getElementById('sw-login').classList.remove('active');
  document.getElementById('sw-register').classList.remove('active');
  document.getElementById('login-err').style.display = 'none';
  document.getElementById('register-err').style.display = 'none';
}
 
async function doLogin() {
  const email = document.getElementById('l-email').value.trim();
  const senha = document.getElementById('l-senha').value;
  const errEl = document.getElementById('login-err');
  errEl.style.display = 'none';
 
  if (!email || !senha) {
    errEl.textContent = 'Preencha e-mail e senha.';
    errEl.style.display = 'block';
    return;
  }
 
  const r = await api('POST', '/clientes/login', { email, senha });
 
  if (r.ok) {
    sessionStorage.setItem('nc_usuario', JSON.stringify(r.data));
    showApp(r.data);
    goPage('dashboard');
  } else {
    errEl.textContent = r.data.erro || 'Credenciais inválidas.';
    errEl.style.display = 'block';
  }
}
 
async function doRegister() {
  const nome = document.getElementById('r-nome').value.trim();
  const dataNascimento = document.getElementById('r-nasc').value;
  const email = document.getElementById('r-email').value.trim();
  const senha = document.getElementById('r-senha').value;
  const senha2 = document.getElementById('r-senha2').value;
  const errEl = document.getElementById('register-err');
  errEl.style.display = 'none';
 
  if (!nome || !dataNascimento || !email || !senha || !senha2) {
    errEl.textContent = 'Preencha todos os campos para criar a conta.';
    errEl.style.display = 'block';
    return;
  }
 
  if (senha !== senha2) {
    errEl.textContent = 'As senhas não conferem.';
    errEl.style.display = 'block';
    return;
  }
 
  const body = { nome, dataNascimento, dataAdicao: today(), email: [email], senha };
  const r = await api('POST', '/clientes', body);
 
  if (!r.ok) {
    errEl.textContent = r.data.erro || 'Não foi possível criar a conta.';
    errEl.style.display = 'block';
    return;
  }
 
  document.getElementById('l-email').value = email;
  document.getElementById('l-senha').value = senha;
  ['r-nome','r-nasc','r-email','r-senha','r-senha2'].forEach(id =>
    document.getElementById(id).value = '');
 
  showLoginMode();
  toast('Conta criada com sucesso. Faça login para continuar.', 'ok');
}
 
async function doForgotPassword() {
  const email  = document.getElementById('f-email').value.trim();
  const codigo = document.getElementById('f-codigo').value.trim();
  const senha  = document.getElementById('f-senha').value;
  const senha2 = document.getElementById('f-senha2').value;
  const errEl  = document.getElementById('forgot-err');
  errEl.style.display = 'none';
 
  if (!email || !codigo || !senha || !senha2) {
    errEl.textContent = 'Preencha e-mail, código e nova senha.';
    errEl.style.display = 'block';
    return;
  }
  if (!resetCodeSentTo || resetCodeSentTo !== email.toLowerCase()) {
    errEl.textContent = 'Solicite o código para este e-mail antes de continuar.';
    errEl.style.display = 'block';
    return;
  }
  if (senha !== senha2) {
    errEl.textContent = 'As senhas não conferem.';
    errEl.style.display = 'block';
    return;
  }
 
  const r = await api('POST', '/clientes/esqueci-senha/confirmar', { email, codigo, novaSenha: senha });
  if (!r.ok) {
    errEl.textContent = r.data.erro || 'Não foi possível atualizar a senha.';
    errEl.style.display = 'block';
    return;
  }
 
  document.getElementById('l-email').value = email;
  ['f-email','f-codigo','f-senha','f-senha2'].forEach(id =>
    document.getElementById(id).value = '');
  resetCodeSentTo = '';
  showLoginMode();
  toast('Senha atualizada. Faça login com a nova senha.', 'ok');
}
 
async function sendResetCode() {
  const email = document.getElementById('f-email').value.trim();
  const errEl = document.getElementById('forgot-err');
  errEl.style.display = 'none';
 
  if (!email) {
    errEl.textContent = 'Informe o e-mail para receber o código.';
    errEl.style.display = 'block';
    return;
  }
 
  const r = await api('POST', '/clientes/esqueci-senha/solicitar', { email });
  if (!r.ok) {
    errEl.textContent = r.data.erro || 'Não foi possível enviar o código.';
    errEl.style.display = 'block';
    return;
  }
 
  resetCodeSentTo = email.toLowerCase();
  toast('Código enviado. Confira o e-mail e informe no campo abaixo.', 'ok');
}
 
function doLogout() {
  sessionStorage.removeItem('nc_usuario');
  document.getElementById('l-email').value = '';
  document.getElementById('l-senha').value = '';
  showLogin();
}
 
// Atalhos de teclado no login
document.getElementById('l-senha').addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });
document.getElementById('r-senha2').addEventListener('keydown', e => { if (e.key === 'Enter') doRegister(); });
document.getElementById('f-senha2').addEventListener('keydown', e => { if (e.key === 'Enter') doForgotPassword(); });
 
/* INICIALIZAÇÃO — IIFE roda por último */
(function iniciar() {
  applySidebarState(isSidebarCollapsed());
  const salvo = sessionStorage.getItem('nc_usuario');
  if (salvo) {
    showApp(JSON.parse(salvo));
    goPage('dashboard');
  } else {
    showLogin();
  }
})();
 
/* DASHBOARD */
async function renderDashboard() {
  const c = document.getElementById('content');
  c.innerHTML = `<div class="loading-wrap"><div class="spinner"></div> Carregando...</div>`;
 
  const [cl, al, re] = await Promise.all([
    api('GET', '/clientes'),
    api('GET', '/alimentos'),
    api('GET', '/receitas'),
  ]);
 
  const nc = cl.ok ? cl.data.length : '—';
  const na = al.ok ? al.data.length : '—';
  const nr = re.ok ? re.data.length : '—';
 
  const recRows = re.ok && re.data.length
    ? re.data.slice(-4).reverse().map(r =>
        `<div class="dash-row">
           <span class="dash-row-name">${esc(r.titulo)}</span>
           <span class="badge badge-gold">⏱ ${r.tempoPreparo}min</span>
         </div>`).join('')
    : `<div class="empty"><div class="empty-ico">📋</div><p>Nenhuma receita ainda</p></div>`;
 
  const alRows = al.ok && al.data.length
    ? al.data.slice(-4).reverse().map(a =>
        `<div class="dash-row">
           <span class="dash-row-name">${esc(a.nome)}</span>
           <span class="badge badge-sage">${(a.categoria || []).join(', ') || '—'}</span>
         </div>`).join('')
    : `<div class="empty"><div class="empty-ico">🥦</div><p>Nenhum alimento ainda</p></div>`;
 
  c.innerHTML = `
    <div class="stats-grid">
      <div class="stat-card">
        <div><div class="stat-label">Total de Clientes</div><div class="stat-value">${nc}</div></div>
        <div class="stat-ico ico-rust">👥</div>
      </div>
      <div class="stat-card">
        <div><div class="stat-label">Alimentos</div><div class="stat-value">${na}</div></div>
        <div class="stat-ico ico-sage">🥦</div>
      </div>
      <div class="stat-card">
        <div><div class="stat-label">Receitas</div><div class="stat-value">${nr}</div></div>
        <div class="stat-ico ico-gold">📋</div>
      </div>
    </div>
    <div class="dash-grid">
      <div class="dash-panel">
        <div class="dash-panel-title">Receitas Recentes</div>${recRows}
      </div>
      <div class="dash-panel">
        <div class="dash-panel-title">Alimentos Recentes</div>${alRows}
      </div>
    </div>`;
}
 
/* CLIENTES */
let _clientes = [];
 
async function renderClientes() {
  document.getElementById('topbar-action').innerHTML =
    `<button class="btn btn-primary" onclick="openModalCliente(null)">+ Novo Cliente</button>`;
  const c = document.getElementById('content');
  c.innerHTML = `<div class="table-card"><div class="loading-wrap"><div class="spinner"></div></div></div>`;
  const r = await api('GET', '/clientes');
  if (!r.ok) { c.innerHTML = erroCard(r.data.erro); return; }
  _clientes = r.data;
  drawClientes(_clientes);
}
 
function drawClientes(lista) {
  document.getElementById('content').innerHTML = `
    <div class="table-card">
      <div class="table-search">
        <div class="search-wrap">
          <div class="search-input-wrap">
            <span class="search-ico">🔍</span>
            <input id="search-clientes" type="text" placeholder="Buscar clientes..." value="${esc(_searchClientesQuery)}" oninput="filterClientes(this.value)">
          </div>
          <select class="search-select" onchange="changeClientesMode(this.value)">
            <option value="all" ${_searchClientesMode === 'all' ? 'selected' : ''}>Todos</option>
            <option value="id" ${_searchClientesMode === 'id' ? 'selected' : ''}>ID</option>
            <option value="nome" ${_searchClientesMode === 'nome' ? 'selected' : ''}>Nome</option>
            <option value="email" ${_searchClientesMode === 'email' ? 'selected' : ''}>E-mail</option>
          </select>
          <button class="btn btn-ghost btn-sm search-btn" onclick="filterClientes(document.getElementById('search-clientes').value)">Pesquisar</button>
        </div>
      </div>
      ${lista.length === 0
        ? emptyState('👥', 'Nenhum cliente cadastrado', 'Clique em "+ Novo Cliente" para começar')
        : `<table>
            <thead><tr>
              <th>ID</th><th>Nome</th><th>E-mails</th>
              <th>Nascimento</th><th>Cadastro</th><th>Ações</th>
            </tr></thead>
            <tbody>
              ${lista.map(cli => `
                <tr>
                  <td><span class="badge badge-gold">#${cli.id}</span></td>
                  <td>${esc(cli.nome)}</td>
                  <td><div class="chips">
                    ${(cli.email || []).map(e => `<span class="chip">${esc(e)}</span>`).join('') || '—'}
                  </div></td>
                  <td>${fmtDate(cli.dataNascimento)}</td>
                  <td>${fmtDate(cli.dataAdicao)}</td>
                  <td><div class="td-actions">
                    <button class="btn-icon btn-icon-rust" onclick="openModalCliente(${cli.id})" title="Editar">✏️</button>
                    <button class="btn-icon btn-icon-red"  onclick="delCliente(${cli.id},'${esc(cli.nome)}')" title="Excluir">🗑️</button>
                  </div></td>
                </tr>`).join('')}
            </tbody>
          </table>`}
    </div>`;
}
 
function filterClientes(q) {
  const oldInput = document.getElementById('search-clientes');
  const cursorPos = oldInput?.selectionStart ?? q.length;
  _searchClientesQuery = q;
  const qLower = q.toLowerCase();
  const f = _clientes.filter(c => {
    if (_searchClientesMode === 'id') return String(c.id).includes(q);
    if (_searchClientesMode === 'nome') return c.nome.toLowerCase().includes(qLower);
    if (_searchClientesMode === 'email') return (c.email || []).some(e => e.toLowerCase().includes(qLower));
    return String(c.id).includes(q)
      || c.nome.toLowerCase().includes(qLower)
      || (c.email || []).some(e => e.toLowerCase().includes(qLower));
  });
  drawClientes(f);
  const newInput = document.getElementById('search-clientes');
  if (newInput) {
    newInput.focus();
    newInput.setSelectionRange(cursorPos, cursorPos);
  }
}

function changeClientesMode(mode) {
  _searchClientesMode = mode;
  filterClientes(_searchClientesQuery);
}
 
function openModalCliente(id) {
  const cli = id ? _clientes.find(c => c.id === id) : null;
  openModal(cli ? 'Editar Cliente' : 'Novo Cliente', `
    <div class="fg">
      <label>Nome *</label>
      <input id="fc-nome" class="fi" value="${esc(cli?.nome || '')}" placeholder="Nome completo">
    </div>
    <div class="fg-row">
      <div class="fg">
        <label>Data de Nascimento</label>
        <input id="fc-nasc" type="date" class="fi" value="${cli?.dataNascimento || ''}">
      </div>
      <div class="fg">
        <label>Senha${cli ? ' (vazio = manter)' : ' *'}</label>
        <input id="fc-senha" type="password" class="fi" placeholder="••••••••">
      </div>
    </div>
    <div class="fg">
      <label>E-mails</label>
      <div class="chip-input-wrap">
        <input id="fc-email-i" class="fi" placeholder="email@exemplo.com">
        <button class="btn btn-ghost btn-sm" onclick="addChip('fc-email-i','fc-chips')">+</button>
      </div>
      <div class="chips" id="fc-chips" style="margin-top:8px">
        ${(cli?.email || []).map(e => chipEl(e)).join('')}
      </div>
    </div>
    <div class="modal-ft">
      <button class="btn btn-ghost" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary" onclick="saveCliente(${cli?.id || 'null'})">Salvar</button>
    </div>`);
  setTimeout(() => document.getElementById('fc-nome')?.focus(), 80);
}
 
async function saveCliente(id) {
  const nome   = document.getElementById('fc-nome')?.value?.trim();
  const nasc   = document.getElementById('fc-nasc')?.value;
  const senha  = document.getElementById('fc-senha')?.value;
  const emails = chipsValues('fc-chips');
  if (!nome) { toast('Nome é obrigatório', 'err'); return; }
  if (!id && !senha) { toast('Senha é obrigatória', 'err'); return; }
  const body = { nome, dataNascimento: nasc || null, dataAdicao: today(), email: emails };
  if (senha) body.senha = senha;
  const r = id ? await api('PUT', `/clientes/${id}`, body) : await api('POST', `/clientes`, body);
  if (r.ok) { toast(id ? 'Cliente atualizado!' : 'Cliente criado!', 'ok'); closeModal(); renderClientes(); }
  else toast(r.data.erro || 'Erro ao salvar', 'err');
}
 
async function delCliente(id, nome) {
  if (!confirm(`Excluir cliente "${nome}"?`)) return;
  const r = await api('DELETE', `/clientes/${id}`);
  if (r.ok) { toast('Cliente excluído', 'ok'); renderClientes(); }
  else toast(r.data.erro || 'Erro', 'err');
}
 
/* ALIMENTOS */
let _alimentos = [];
 
async function renderAlimentos() {
  document.getElementById('topbar-action').innerHTML =
    `<button class="btn btn-primary" onclick="openModalAlimento(null)">+ Novo Alimento</button>`;
  const c = document.getElementById('content');
  c.innerHTML = `<div class="table-card"><div class="loading-wrap"><div class="spinner"></div></div></div>`;
  const r = await api('GET', '/alimentos');
  if (!r.ok) { c.innerHTML = erroCard(r.data.erro); return; }
  _alimentos = r.data;
  drawAlimentos(_alimentos);
}
 
function drawAlimentos(lista) {
  document.getElementById('content').innerHTML = `
    <div class="table-card">
      <div class="table-search">
        <div class="search-wrap">
          <div class="search-input-wrap">
            <span class="search-ico">🔍</span>
            <input id="search-alimentos" type="text" placeholder="Buscar alimentos..." value="${esc(_searchAlimentosQuery)}" oninput="filterAlimentos(this.value)">
          </div>
          <select class="search-select" onchange="changeAlimentosMode(this.value)">
            <option value="all" ${_searchAlimentosMode === 'all' ? 'selected' : ''}>Todos</option>
            <option value="id" ${_searchAlimentosMode === 'id' ? 'selected' : ''}>ID</option>
            <option value="nome" ${_searchAlimentosMode === 'nome' ? 'selected' : ''}>Nome</option>
            <option value="categoria" ${_searchAlimentosMode === 'categoria' ? 'selected' : ''}>Categoria</option>
          </select>
          <button class="btn btn-ghost btn-sm search-btn" onclick="filterAlimentos(document.getElementById('search-alimentos').value)">Pesquisar</button>
        </div>
      </div>
      ${lista.length === 0
        ? emptyState('🥦', 'Nenhum alimento cadastrado', 'Clique em "+ Novo Alimento" para começar')
        : `<table>
            <thead><tr><th>ID</th><th>Nome</th><th>Categorias</th><th>Ações</th></tr></thead>
            <tbody>
              ${lista.map(a => `
                <tr>
                  <td><span class="badge badge-gold">#${a.id}</span></td>
                  <td>${esc(a.nome)}</td>
                  <td><div class="chips">
                    ${(a.categoria || []).map(c => `<span class="chip">${esc(c)}</span>`).join('') || '—'}
                  </div></td>
                  <td><div class="td-actions">
                    <button class="btn-icon btn-icon-rust" onclick="openModalAlimento(${a.id})">✏️</button>
                    <button class="btn-icon btn-icon-red"  onclick="delAlimento(${a.id},'${esc(a.nome)}')">🗑️</button>
                  </div></td>
                </tr>`).join('')}
            </tbody>
          </table>`}
    </div>`;
}
 
function filterAlimentos(q) {
  const oldInput = document.getElementById('search-alimentos');
  const cursorPos = oldInput?.selectionStart ?? q.length;
  _searchAlimentosQuery = q;
  const qLower = q.toLowerCase();
  const f = _alimentos.filter(a => {
    if (_searchAlimentosMode === 'id') return String(a.id).includes(q);
    if (_searchAlimentosMode === 'nome') return a.nome.toLowerCase().includes(qLower);
    if (_searchAlimentosMode === 'categoria') return (a.categoria || []).some(c => c.toLowerCase().includes(qLower));
    return String(a.id).includes(q)
      || a.nome.toLowerCase().includes(qLower)
      || (a.categoria || []).some(c => c.toLowerCase().includes(qLower));
  });
  drawAlimentos(f);
  const newInput = document.getElementById('search-alimentos');
  if (newInput) {
    newInput.focus();
    newInput.setSelectionRange(cursorPos, cursorPos);
  }
}

function changeAlimentosMode(mode) {
  _searchAlimentosMode = mode;
  filterAlimentos(_searchAlimentosQuery);
}
 
function openModalAlimento(id) {
  const al = id ? _alimentos.find(a => a.id === id) : null;
  openModal(al ? 'Editar Alimento' : 'Novo Alimento', `
    <div class="fg">
      <label>Nome *</label>
      <input id="fa-nome" class="fi" value="${esc(al?.nome || '')}" placeholder="Ex: Frango, Arroz...">
    </div>
    <div class="fg">
      <label>Categorias</label>
      <div class="chip-input-wrap">
        <input id="fa-cat-i" class="fi" placeholder="Ex: Proteína, Carboidrato...">
        <button class="btn btn-ghost btn-sm" onclick="addChip('fa-cat-i','fa-chips')">+</button>
      </div>
      <div class="chips" id="fa-chips" style="margin-top:8px">
        ${(al?.categoria || []).map(c => chipEl(c)).join('')}
      </div>
    </div>
    <div class="modal-ft">
      <button class="btn btn-ghost" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary" onclick="saveAlimento(${al?.id || 'null'})">Salvar</button>
    </div>`);
}
 
async function saveAlimento(id) {
  const nome = document.getElementById('fa-nome')?.value?.trim();
  const cats = chipsValues('fa-chips');
  if (!nome) { toast('Nome é obrigatório', 'err'); return; }
  const body = { nome, categoria: cats };
  const r = id ? await api('PUT', `/alimentos/${id}`, body) : await api('POST', `/alimentos`, body);
  if (r.ok) { toast(id ? 'Alimento atualizado!' : 'Alimento criado!', 'ok'); closeModal(); renderAlimentos(); }
  else toast(r.data.erro || 'Erro ao salvar', 'err');
}
 
async function delAlimento(id, nome) {
  if (!confirm(`Excluir alimento "${nome}"?`)) return;
  const r = await api('DELETE', `/alimentos/${id}`);
  if (r.ok) { toast('Alimento excluído', 'ok'); renderAlimentos(); }
  else toast(r.data.erro || 'Erro', 'err');
}
 
/* RECEITAS */
let _receitas = [];
 
async function renderReceitas() {
  document.getElementById('topbar-action').innerHTML =
    `<button class="btn btn-primary" onclick="openModalReceita(null)">+ Nova Receita</button>`;
  const c = document.getElementById('content');
  c.innerHTML = `<div class="table-card"><div class="loading-wrap"><div class="spinner"></div></div></div>`;
  const r = await api('GET', '/receitas');
  if (!r.ok) { c.innerHTML = erroCard(r.data.erro); return; }
  _receitas = r.data;
  drawReceitas(_receitas);
}
 
function drawReceitas(lista) {
  document.getElementById('content').innerHTML = `
    <div class="table-card">
      <div class="table-search">
        <div class="search-wrap">
          <div class="search-input-wrap">
            <span class="search-ico">🔍</span>
            <input id="search-receitas" type="text" placeholder="Buscar receitas..." value="${esc(_searchReceitasQuery)}" oninput="filterReceitas(this.value)">
          </div>
          <select class="search-select" onchange="changeReceitasMode(this.value)">
            <option value="all" ${_searchReceitasMode === 'all' ? 'selected' : ''}>Todos</option>
            <option value="id" ${_searchReceitasMode === 'id' ? 'selected' : ''}>ID</option>
            <option value="titulo" ${_searchReceitasMode === 'titulo' ? 'selected' : ''}>Título</option>
            <option value="tempo" ${_searchReceitasMode === 'tempo' ? 'selected' : ''}>Tempo</option>
          </select>
          <button class="btn btn-ghost btn-sm search-btn" onclick="filterReceitas(document.getElementById('search-receitas').value)">Pesquisar</button>
        </div>
      </div>
      ${lista.length === 0
        ? emptyState('📋', 'Nenhuma receita cadastrada', 'Clique em "+ Nova Receita" para começar')
        : `<table>
            <thead><tr><th>ID</th><th>Título</th><th>Tempo</th><th>Porção</th><th>Ações</th></tr></thead>
            <tbody>
              ${lista.map(r => `
                <tr>
                  <td><span class="badge badge-gold">#${r.id}</span></td>
                  <td>${esc(r.titulo)}</td>
                  <td><span class="badge badge-gold"> ${r.tempoPreparo}min</span></td>
                  <td>${esc(r.porcao || '—')}</td>
                  <td><div class="td-actions">
                    <button class="btn-icon btn-icon-sage" onclick="viewReceita(${r.id})" title="Ver">👁️</button>
                    <button class="btn-icon btn-icon-rust" onclick="openModalReceita(${r.id})" title="Editar">✏️</button>
                    <button class="btn-icon btn-icon-red"  onclick="delReceita(${r.id},'${esc(r.titulo)}')" title="Excluir">🗑️</button>
                  </div></td>
                </tr>`).join('')}
            </tbody>
          </table>`}
    </div>`;
}
 
function filterReceitas(q) {
  const oldInput = document.getElementById('search-receitas');
  const cursorPos = oldInput?.selectionStart ?? q.length;
  _searchReceitasQuery = q;
  const qLower = q.toLowerCase();
  const f = _receitas.filter(r => {
    if (_searchReceitasMode === 'id') return String(r.id).includes(q);
    if (_searchReceitasMode === 'titulo') return r.titulo.toLowerCase().includes(qLower);
    if (_searchReceitasMode === 'tempo') return String(r.tempoPreparo || '').includes(q);
    return String(r.id).includes(q)
      || r.titulo.toLowerCase().includes(qLower)
      || String(r.tempoPreparo || '').includes(q)
      || String(r.porcao || '').toLowerCase().includes(qLower);
  });
  drawReceitas(f);
  const newInput = document.getElementById('search-receitas');
  if (newInput) {
    newInput.focus();
    newInput.setSelectionRange(cursorPos, cursorPos);
  }
}

function changeReceitasMode(mode) {
  _searchReceitasMode = mode;
  filterReceitas(_searchReceitasQuery);
}
 
function viewReceita(id) {
  const r = _receitas.find(x => x.id === id);
  if (!r) return;
  openModal(r.titulo, `
    <div class="detail-meta">
      <div class="detail-meta-item">⏱ <strong>${r.tempoPreparo} minutos</strong></div>
      <div class="detail-meta-item">🍽 <strong>${esc(r.porcao || '—')}</strong></div>
    </div>
    <div style="font-size:0.75rem;font-weight:600;color:var(--ink40);letter-spacing:1.5px;text-transform:uppercase;margin-bottom:8px">
      Modo de Preparo
    </div>
    <div class="detail-body">${esc(r.informacoes || 'Sem informações.')}</div>
    <div class="modal-ft">
      <button class="btn btn-ghost" onclick="closeModal()">Fechar</button>
      <button class="btn btn-primary" onclick="closeModal();openModalReceita(${r.id})">✏️ Editar</button>
    </div>`);
}
 
function openModalReceita(id) {
  const re = id ? _receitas.find(r => r.id === id) : null;
  openModal(re ? 'Editar Receita' : 'Nova Receita', `
    <div class="fg">
      <label>Título *</label>
      <input id="fr-titulo" class="fi" value="${esc(re?.titulo || '')}" placeholder="Ex: Frango Grelhado com Legumes">
    </div>
    <div class="fg-row">
      <div class="fg">
        <label>Tempo de Preparo (min) *</label>
        <input id="fr-tempo" type="number" min="1" class="fi" value="${re?.tempoPreparo || ''}">
      </div>
      <div class="fg">
        <label>Porção</label>
        <input id="fr-porcao" class="fi" value="${esc(re?.porcao || '')}" placeholder="Ex: 2 porções">
      </div>
    </div>
    <div class="fg">
      <label>Informações / Modo de Preparo</label>
      <textarea id="fr-info" class="fi" rows="5" placeholder="Descreva os ingredientes e passos...">${esc(re?.informacoes || '')}</textarea>
    </div>
    <div class="modal-ft">
      <button class="btn btn-ghost" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary" onclick="saveReceita(${re?.id || 'null'})">Salvar</button>
    </div>`);
}
 
async function saveReceita(id) {
  const titulo      = document.getElementById('fr-titulo')?.value?.trim();
  const tempo       = parseInt(document.getElementById('fr-tempo')?.value);
  const porcao      = document.getElementById('fr-porcao')?.value?.trim();
  const informacoes = document.getElementById('fr-info')?.value?.trim();
  if (!titulo) { toast('Título é obrigatório', 'err'); return; }
  if (!tempo || tempo < 1) { toast('Informe o tempo de preparo', 'err'); return; }
  const body = { titulo, tempoPreparo: tempo, porcao, informacoes };
  const r = id ? await api('PUT', `/receitas/${id}`, body) : await api('POST', `/receitas`, body);
  if (r.ok) { toast(id ? 'Receita atualizada!' : 'Receita criada!', 'ok'); closeModal(); renderReceitas(); }
  else toast(r.data.erro || 'Erro ao salvar', 'err');
}
 
async function delReceita(id, titulo) {
  if (!confirm(`Excluir receita "${titulo}"?`)) return;
  const r = await api('DELETE', `/receitas/${id}`);
  if (r.ok) { toast('Receita excluída', 'ok'); renderReceitas(); }
  else toast(r.data.erro || 'Erro', 'err');
}
 
/* MODAL */
function openModal(title, body) {
  document.getElementById('modal-title').textContent = title;
  document.getElementById('modal-body').innerHTML = body;
  document.getElementById('overlay').classList.add('open');
}
 
function closeModal() {
  document.getElementById('overlay').classList.remove('open');
}
 
function overlayClick(e) {
  if (e.target === document.getElementById('overlay')) closeModal();
}
 
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') closeModal();
});
 
/* CHIPS */
function chipEl(val) {
  return `<span class="chip chip-edit">${esc(val)}<button class="chip-rm" onclick="this.parentElement.remove()">×</button></span>`;
}
 
function addChip(inputId, containerId) {
  const inp = document.getElementById(inputId);
  const val = inp?.value?.trim();
  if (!val) return;
  document.getElementById(containerId).insertAdjacentHTML('beforeend', chipEl(val));
  inp.value = '';
  inp.focus();
}
 
function chipsValues(id) {
  return [...document.querySelectorAll(`#${id} .chip`)]
    .map(c => c.childNodes[0].textContent.trim())
    .filter(Boolean);
}
 
document.addEventListener('keydown', e => {
  if (e.key !== 'Enter') return;
  const id = document.activeElement?.id;
  if (id === 'fc-email-i') addChip('fc-email-i', 'fc-chips');
  if (id === 'fa-cat-i')   addChip('fa-cat-i',   'fa-chips');
});
 
/* TOAST */
function toast(msg, type = 'inf') {
  const el = document.createElement('div');
  el.className = `toast t-${type}`;
  el.textContent = msg;
  document.getElementById('toasts').appendChild(el);
  setTimeout(() => el.remove(), 3200);
}
 
/* UTILS */
function esc(s) {
  return String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
 
function fmtDate(d) {
  if (!d) return '—';
  const p = String(d).split('-');
  return p.length === 3 ? `${p[2]}/${p[1]}/${p[0]}` : d;
}
 
function today() {
  return new Date().toISOString().split('T')[0];
}
 
function emptyState(ico, h, p) {
  return `<div class="empty">
    <div class="empty-ico">${ico}</div>
    <h3>${h}</h3>
    <p>${p}</p>
  </div>`;
}
 
function erroCard(msg) {
  return `<div class="table-card" style="padding:40px;text-align:center;color:var(--red)">
    ⚠️ <strong>Erro:</strong> ${esc(msg)}
  </div>`;
}
 