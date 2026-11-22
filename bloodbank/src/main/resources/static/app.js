const GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
const COMPONENTS = ['Whole Blood', 'Red Blood Cells', 'Plasma', 'Platelets'];
const $ = (selector, root = document) => root.querySelector(selector);
const page = document.body.dataset.page;
let session = JSON.parse(localStorage.getItem('bb') || 'null');
let selectedGroup = null;
let socketRetry = null;

const escapeHtml = value => String(value ?? '').replace(/[&<>"]/g, char => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'
}[char]));

async function api(method, url, body) {
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(session ? { Authorization: `Bearer ${session.token}` } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await response.text();
  let data = {};
  if (text) {
    try { data = JSON.parse(text); }
    catch { data = { error: text }; }
  }
  if (!response.ok) throw new Error(data.error || `Request failed (${response.status}).`);
  return data;
}

function formValues(form) {
  return Object.fromEntries([...new FormData(form)].filter(([, value]) => value !== ''));
}

function showMessage(selector, text, success = false) {
  const element = $(selector);
  if (!element) return;
  element.textContent = text;
  element.className = `msg ${success ? 'good' : 'err'}`;
}

function toast(text) {
  let element = $('#toast');
  if (!element) {
    element = document.createElement('div');
    element.id = 'toast';
    element.setAttribute('role', 'status');
    document.body.append(element);
  }
  element.textContent = text;
  element.style.display = 'block';
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { element.style.display = 'none'; }, 6000);
}

function initializeHeader() {
  const host = $('#site-header');
  if (!host) return;
  host.innerHTML = `
    <nav class="site-nav" aria-label="Main navigation">
      <a class="brand" href="/">Blood Bank</a>
      <button class="menu-toggle" id="menuToggle" type="button" aria-expanded="false" aria-controls="navLinks">Menu</button>
      <div class="nav-links" id="navLinks">
        <a href="/" data-page-link="home">Home</a>
        <a href="/inventory.html" data-page-link="inventory">Live inventory</a>
        <a href="/drives.html" data-page-link="drives">Blood drives</a>
        <a href="/about.html" data-page-link="about">How it works</a>
        <a href="/donor.html" data-role-link="donor">Donor</a>
        <a href="/hospital.html" data-role-link="hospital">Hospital</a>
        <a href="/admin.html" data-role-link="admin">Admin</a>
        <a href="/account.html" data-page-link="account">Account</a>
      </div>
      <div class="nav-user">
        <span id="who"></span>
        <button id="signOut" class="small hide" type="button">Sign out</button>
      </div>
    </nav>`;

  const currentPage = document.body.dataset.page;
  document.querySelectorAll('[data-page-link]').forEach(link => {
    if (link.dataset.pageLink === currentPage) link.classList.add('active');
  });
  document.querySelectorAll('[data-role-link]').forEach(link => {
    const role = link.dataset.roleLink;
    link.hidden = !session || (session.role !== role && session.role !== 'admin');
  });
  $('#who').textContent = session ? `Signed in: ${session.name}` : 'Not signed in';
  $('#signOut').classList.toggle('hide', !session);
  $('#signOut').addEventListener('click', () => {
    session = null;
    localStorage.removeItem('bb');
    window.location.href = '/';
  });
  $('#menuToggle').addEventListener('click', event => {
    const links = $('#navLinks');
    const open = links.classList.toggle('open');
    event.currentTarget.setAttribute('aria-expanded', String(open));
  });
}

function populateSelects() {
  document.querySelectorAll('.gsel').forEach(select => {
    select.replaceChildren(...GROUPS.map(group => new Option(group, group)));
  });
  document.querySelectorAll('.comp').forEach(select => {
    select.replaceChildren(...COMPONENTS.map(component => new Option(component, component)));
  });
}

function requireRole(role) {
  const allowed = session && (session.role === role || session.role === 'admin');
  const protectedContent = $('#protectedContent');
  const denied = $('#accessDenied');
  if (allowed) return true;
  if (protectedContent) protectedContent.classList.add('hide');
  if (denied) denied.classList.remove('hide');
  return false;
}

function stockMarkup(summary) {
  return GROUPS.map(group => {
    const count = Number(summary[group] || 0);
    return `<div class="stock-chip"><span>${escapeHtml(group)}</span><strong>${count}</strong><span>${count === 1 ? 'unit' : 'units'}</span></div>`;
  }).join('');
}

async function loadHomeSummary() {
  const target = $('#homeStockSummary');
  if (!target) return;
  try {
    const summary = await api('GET', '/api/inventory/summary');
    target.innerHTML = stockMarkup(summary);
    const total = Object.values(summary).reduce((sum, count) => sum + Number(count || 0), 0);
    const totalElement = $('#totalUnits');
    if (totalElement) totalElement.textContent = String(total);
  } catch (error) {
    target.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

function bloodTank(group, count) {
  const percent = count ? Math.max(6, Math.min(100, count / 20 * 100)) : 0;
  const warning = count <= 3
    ? `<span class="badge ${count ? 'pulse' : ''}">${count ? 'Low stock' : 'Out of stock'}</span>`
    : '';
  return `<button class="tcard ${selectedGroup === group ? 'sel' : ''}" data-group="${escapeHtml(group)}" aria-pressed="${selectedGroup === group}">
    <div class="thead"><b>${escapeHtml(group)}</b>${warning}</div>
    <div class="blood-tank"><div class="blood-liquid" style="--h:${percent}%"></div></div>
    <div class="units"><b>${count}</b> ${count === 1 ? 'unit' : 'units'} available</div>
  </button>`;
}

async function loadInventory() {
  const tanks = $('#tanks');
  if (!tanks) return;
  try {
    const summary = await api('GET', '/api/inventory/summary');
    tanks.innerHTML = GROUPS.map(group => bloodTank(group, Number(summary[group] || 0))).join('');
    tanks.querySelectorAll('[data-group]').forEach(button => {
      button.addEventListener('click', () => {
        selectedGroup = button.dataset.group;
        loadInventory();
        searchInventory(selectedGroup);
      });
    });
  } catch (error) {
    tanks.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

async function searchInventory(group) {
  const result = $('#result');
  if (!result) return;
  try {
    const data = await api('GET', `/api/inventory/search?blood_group=${encodeURIComponent(group)}`);
    const available = Object.entries(data.available || {});
    result.innerHTML = `<strong>Compatible with ${escapeHtml(group)}:</strong> ${escapeHtml((data.compatibleGroups || []).join(', '))}`
      + (available.length
        ? available.map(([component, count]) => `<div class="stock"><span>${escapeHtml(component)}</span><b>${Number(count)} ${Number(count) === 1 ? 'unit' : 'units'}</b></div>`).join('')
        : '<div class="notice" style="margin-top:12px">No compatible units are currently listed. Hospitals can submit a request after signing in.</div>');
  } catch (error) {
    result.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

function driveMarkup(drive) {
  const time = value => escapeHtml(String(value || '').slice(0, 5));
  return `<article class="drive">
    <div class="banner"><b>${escapeHtml(drive.title)}</b>
      <div>${escapeHtml(drive.driveDate)} · ${time(drive.startTime)}–${time(drive.endTime)}</div>
    </div>
    <div class="details">${escapeHtml(drive.venueAddress)}${drive.city ? `, ${escapeHtml(drive.city)}` : ''}
      ${drive.contactPhone ? `<br>Contact: ${escapeHtml(drive.contactPhone)}` : ''}
    </div>
  </article>`;
}

async function loadDrives(selector) {
  const container = $(selector);
  if (!container) return;
  try {
    const drives = await api('GET', '/api/drives');
    container.innerHTML = drives.length
      ? drives.map(driveMarkup).join('')
      : '<p class="sub">No upcoming drives are listed yet. Please check back later.</p>';
  } catch (error) {
    container.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

function renderTable(rows, columns, actions) {
  if (!rows || !rows.length) return '<p class="sub">Nothing here yet.</p>';
  return `<div class="table-wrap"><table><thead><tr>${columns.map(column => `<th>${column.label}</th>`).join('')}${actions ? '<th>Actions</th>' : ''}</tr></thead>
    <tbody>${rows.map(row => `<tr>${columns.map(column => `<td>${column.render ? column.render(row[column.key], row) : escapeHtml(row[column.key])}</td>`).join('')}
      ${actions ? `<td><div class="table-actions">${actions(row)}</div></td>` : ''}</tr>`).join('')}</tbody></table></div>`;
}

function dateTime(value) {
  return escapeHtml(String(value || '').replace('T', ' ').slice(0, 16));
}

function routeForRole(role) {
  if (role === 'admin') return '/admin.html';
  if (role === 'hospital') return '/hospital.html';
  return '/donor.html';
}

function initializeAccount() {
  const login = $('#login');
  const register = $('#register');
  const accountInfo = $('#accountInfo');
  if (session && accountInfo) {
    accountInfo.innerHTML = `<p>You are signed in as <strong>${escapeHtml(session.name)}</strong> (${escapeHtml(session.role)}).</p>
      <a class="button primary" href="${routeForRole(session.role)}">Open your dashboard</a>`;
  }
  if (login) login.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      session = await api('POST', '/api/auth/login', formValues(login));
      localStorage.setItem('bb', JSON.stringify(session));
      window.location.href = routeForRole(session.role);
    } catch (error) {
      showMessage('#loginMsg', error.message);
    }
  });
  if (register) register.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      session = await api('POST', '/api/auth/register', formValues(register));
      localStorage.setItem('bb', JSON.stringify(session));
      window.location.href = routeForRole(session.role);
    } catch (error) {
      showMessage('#regMsg', error.message);
    }
  });
}

async function loadAppointments() {
  const list = $('#apptList');
  if (!list) return;
  try {
    const rows = await api('GET', '/api/appointments/mine');
    list.innerHTML = renderTable(rows, [
      { label: 'Centre', key: 'centerName' },
      { label: 'When', key: 'appointmentDate', render: dateTime },
      { label: 'Status', key: 'status' }
    ]);
  } catch (error) {
    list.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

function initializeDonor() {
  if (!requireRole('donor')) return;
  loadAppointments();
  const appointment = $('#appointmentForm');
  if (!appointment) return;
  appointment.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      await api('POST', '/api/appointments', formValues(appointment));
      showMessage('#apptMsg', 'Appointment booked.', true);
      appointment.reset();
      loadAppointments();
    } catch (error) {
      showMessage('#apptMsg', error.message);
    }
  });
}

async function loadRequests() {
  const list = $('#requestList');
  if (!list) return;
  try {
    const rows = await api('GET', '/api/requests');
    list.innerHTML = renderTable(rows, [
      { label: 'Patient', key: 'patientName' },
      { label: 'Blood group', key: 'bloodGroup' },
      { label: 'Units', key: 'unitsNeeded' },
      { label: 'Urgency', key: 'urgency' },
      { label: 'Status', key: 'status' }
    ]);
  } catch (error) {
    list.innerHTML = `<p class="notice">${escapeHtml(error.message)}</p>`;
  }
}

function initializeHospital() {
  if (!requireRole('hospital')) return;
  loadRequests();
  const request = $('#requestForm');
  if (!request) return;
  request.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      await api('POST', '/api/requests', formValues(request));
      showMessage('#requestMsg', 'Blood request submitted.', true);
      request.reset();
      loadRequests();
    } catch (error) {
      showMessage('#requestMsg', error.message);
    }
  });
}

async function loadAdmin() {
  if (!requireRole('admin')) return;
  try {
    const [requests, appointments, inventory] = await Promise.all([
      api('GET', '/api/admin/requests'),
      api('GET', '/api/appointments/all'),
      api('GET', '/api/admin/inventory')
    ]);
    $('#adminRequests').innerHTML = renderTable(requests, [
      { label: 'ID', key: 'requestId' },
      { label: 'Patient', key: 'patientName' },
      { label: 'Hospital', key: 'hospitalName' },
      { label: 'Group', key: 'bloodGroup' },
      { label: 'Component', key: 'componentType' },
      { label: 'Units', key: 'unitsNeeded' },
      { label: 'Urgency', key: 'urgency' },
      { label: 'Status', key: 'status' }
    ], row => ['Approved', 'Fulfilled', 'Rejected']
      .filter(status => status !== row.status)
      .map(status => `<button class="small" data-action="request-status" data-id="${Number(row.requestId)}" data-status="${status}">${status}</button>`).join(''));

    $('#adminAppointments').innerHTML = renderTable(appointments, [
      { label: 'ID', key: 'appointmentId' },
      { label: 'Donor ID', key: 'donorId' },
      { label: 'Centre', key: 'centerName' },
      { label: 'When', key: 'appointmentDate', render: dateTime },
      { label: 'Status', key: 'status' }
    ], row => row.status === 'Scheduled'
      ? `<button class="small" data-action="complete-appointment" data-id="${Number(row.appointmentId)}">Mark completed</button>` : '');

    $('#adminInventory').innerHTML = renderTable(inventory, [
      { label: 'Bag', key: 'bagId' },
      { label: 'Group', key: 'bloodGroup' },
      { label: 'Component', key: 'componentType' },
      { label: 'Expires', key: 'expirationDate' },
      { label: 'Status', key: 'status' }
    ], row => row.status === 'Expired' ? '' : ['Reserved', 'Dispatched']
      .filter(status => status !== row.status)
      .map(status => `<button class="small" data-action="bag-status" data-id="${Number(row.bagId)}" data-status="${status}">${status}</button>`).join(''));
  } catch (error) {
    const target = $('#adminError');
    if (target) target.textContent = error.message;
  }
}

function initializeAdminForms() {
  const bag = $('#bagForm');
  if (bag) bag.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      await api('POST', '/api/admin/inventory', formValues(bag));
      showMessage('#bagMsg', 'Blood bag added to inventory.', true);
      bag.reset();
      loadAdmin();
    } catch (error) {
      showMessage('#bagMsg', error.message);
    }
  });
  const drive = $('#driveForm');
  if (drive) drive.addEventListener('submit', async event => {
    event.preventDefault();
    try {
      await api('POST', '/api/admin/drives', formValues(drive));
      showMessage('#driveMsg', 'Blood drive published.', true);
      drive.reset();
      loadDrives('#adminUpcomingDrives');
    } catch (error) {
      showMessage('#driveMsg', error.message);
    }
  });
}

document.addEventListener('click', async event => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const id = Number(button.dataset.id);
  try {
    if (button.dataset.action === 'request-status') {
      await api('PUT', `/api/admin/requests/${id}?status=${encodeURIComponent(button.dataset.status)}`);
      toast('Request status updated.');
      await loadAdmin();
    } else if (button.dataset.action === 'bag-status') {
      await api('PUT', `/api/admin/inventory/${id}?status=${encodeURIComponent(button.dataset.status)}`);
      toast('Inventory status updated.');
      await loadAdmin();
    } else if (button.dataset.action === 'complete-appointment') {
      await api('PUT', `/api/appointments/${id}/complete`);
      toast('Appointment marked completed.');
      await loadAdmin();
    }
  } catch (error) {
    toast(error.message);
  }
});

function setLive(connected) {
  document.querySelectorAll('[data-live]').forEach(element => {
    element.classList.toggle('on', connected);
    const text = element.querySelector('[data-live-text]');
    if (text) text.textContent = connected ? 'Live updates connected' : 'Reconnecting to live updates';
  });
}

function connectLiveUpdates() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const socket = new WebSocket(`${protocol}//${window.location.host}/ws`);
  socket.addEventListener('open', () => setLive(true));
  socket.addEventListener('close', () => {
    setLive(false);
    clearTimeout(socketRetry);
    socketRetry = setTimeout(connectLiveUpdates, 3000);
  });
  socket.addEventListener('error', () => socket.close());
  socket.addEventListener('message', event => {
    let message;
    try { message = JSON.parse(event.data); }
    catch { return; }
    if (message.type === 'inventory') {
      if (page === 'inventory') {
        loadInventory();
        if (selectedGroup) searchInventory(selectedGroup);
      }
      if (page === 'home') loadHomeSummary();
      if (page === 'admin') loadAdmin();
    }
    if (message.type === 'request' && session?.role === 'admin') {
      toast(`${message.urgency} request: ${message.units} unit(s) of ${message.bloodGroup}`);
      if (page === 'admin') loadAdmin();
    }
    if (message.type === 'requestUpdate') {
      if (page === 'admin' && session?.role === 'admin') loadAdmin();
      if (page === 'hospital' && session?.role === 'hospital') loadRequests();
    }
  });
}

initializeHeader();
populateSelects();
initializeAccount();
if (page === 'home') {
  loadHomeSummary();
  loadDrives('#featuredDrives');
}
if (page === 'inventory') loadInventory();
if (page === 'drives') loadDrives('#driveList');
if (page === 'donor') initializeDonor();
if (page === 'hospital') initializeHospital();
if (page === 'admin') {
  if (requireRole('admin')) {
    loadAdmin();
    initializeAdminForms();
  }
}
connectLiveUpdates();
