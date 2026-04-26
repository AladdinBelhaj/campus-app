const dashboardIds = {
  departments: document.getElementById("departments-count"),
  offers: document.getElementById("offers-count"),
  applications: document.getElementById("applications-count"),
  pending: document.getElementById("pending-count")
};

const offersList = document.getElementById("offers-list");
const departmentsList = document.getElementById("departments-list");
const applicationsList = document.getElementById("applications-list");
const offerSelect = document.getElementById("offer-select");
const form = document.getElementById("application-form");
const formMessage = document.getElementById("form-message");
const backendHealth = document.getElementById("backend-health");

async function fetchJson(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

function renderOffers(items) {
  offersList.innerHTML = "";
  offerSelect.innerHTML = "";

  items.forEach((offer) => {
    const element = document.createElement("article");
    element.className = "stack-item";
    element.innerHTML = `
      <h3>${offer.title}</h3>
      <p>${offer.summary}</p>
      <div class="meta-line">
        <span>${offer.department}</span>
        <span>${offer.domain}</span>
        <span>${offer.location}</span>
        <span>${offer.status}</span>
      </div>
    `;
    offersList.appendChild(element);

    if (offer.status === "PUBLISHED") {
      const option = document.createElement("option");
      option.value = offer.id;
      option.textContent = `${offer.title} - ${offer.department}`;
      offerSelect.appendChild(option);
    }
  });
}

function renderDepartments(items) {
  departmentsList.innerHTML = "";

  items.forEach((department) => {
    const element = document.createElement("article");
    element.className = "stack-item";
    element.innerHTML = `
      <h3>${department.name}</h3>
      <p>${department.campus} · ${department.contactEmail}</p>
      <div class="meta-line">
        <span>${department.code}</span>
      </div>
    `;
    departmentsList.appendChild(element);
  });
}

function renderApplications(items) {
  applicationsList.innerHTML = "";

  items.slice(0, 5).forEach((application) => {
    const element = document.createElement("article");
    element.className = "stack-item";
    element.innerHTML = `
      <h3>${application.studentName}</h3>
      <p>${application.track} · ${application.offerTitle}</p>
      <div class="meta-line">
        <span>${application.status}</span>
        <span>${application.studentEmail}</span>
      </div>
    `;
    applicationsList.appendChild(element);
  });
}

async function refreshDashboard() {
  const [dashboard, offers, departments, applications] = await Promise.all([
    fetchJson("/api/dashboard"),
    fetchJson("/api/internships"),
    fetchJson("/api/departments"),
    fetchJson("/api/applications")
  ]);

  dashboardIds.departments.textContent = dashboard.departments;
  dashboardIds.offers.textContent = dashboard.publishedOffers;
  dashboardIds.applications.textContent = dashboard.applications;
  dashboardIds.pending.textContent = dashboard.pendingApplications;

  renderOffers(offers);
  renderDepartments(departments);
  renderApplications(applications);
}

async function refreshHealth() {
  try {
    const health = await fetchJson("/actuator/health");
    backendHealth.textContent = `API ${health.status}`;
  } catch (error) {
    backendHealth.textContent = "API indisponible";
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  formMessage.textContent = "";

  const formData = new FormData(form);
  const payload = {
    studentName: formData.get("studentName"),
    studentEmail: formData.get("studentEmail"),
    track: formData.get("track"),
    notes: formData.get("notes"),
    offerId: Number(formData.get("offerId"))
  };

  try {
    await fetchJson("/api/applications", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    form.reset();
    await refreshDashboard();
    formMessage.style.color = "#0a7d53";
    formMessage.textContent = "Candidature enregistree avec succes.";
  } catch (error) {
    formMessage.style.color = "#c44900";
    formMessage.textContent = "Impossible d'envoyer la candidature.";
  }
});

Promise.all([refreshDashboard(), refreshHealth()])
  .catch(() => {
    formMessage.textContent = "Le backend n'est pas encore disponible.";
  });
