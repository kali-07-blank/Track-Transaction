// ===== API Base URL (Render Backend) =====
const BASE_URL = "https://track-transaction.onrender.com"; // ⬅️ Replace with your Render backend URL
const PEOPLE_API = `${BASE_URL}/api/people`;
const TRANSACTION_API = `${BASE_URL}/api/transactions`;
const AUTH_API = `${BASE_URL}/api/auth`;

let lastActivePerson = null; // to auto-expand after send/receive

// ===== Utility: Always get fresh token =====
function getAuthToken() {
    return sessionStorage.getItem("authToken");
}

// ===== Notification =====
function showNotification(message, type = "success") {
    const notification = document.getElementById("notification");
    if (!notification) {
        alert(message);
        return;
    }
    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.style.display = "block";
    setTimeout(() => (notification.style.display = "none"), 3000);
}

// ===== Authenticated Fetch =====
async function authFetch(url, options = {}) {
    const token = getAuthToken();
    options.headers = options.headers || {};
    options.headers["Content-Type"] = "application/json";
    if (token) options.headers["Authorization"] = `Bearer ${token}`;

    try {
        const res = await fetch(url, options);
        if (res.status === 401 || res.status === 403) {
            showNotification("⚠️ Session expired. Please login again.", "error");
            sessionStorage.clear();
            setTimeout(() => (window.location.href = "login.html"), 1500);
            throw new Error("Unauthorized");
        }
        return res;
    } catch (err) {
        console.error("Fetch error:", err);
        showNotification("Network error: " + err.message, "error");
        throw err;
    }
}

// ===== LOGIN =====
async function login(event) {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
        return showNotification("Enter both username and password", "error");
    }

    try {
        const res = await fetch(`${AUTH_API}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) throw new Error(await res.text());
        const data = await res.json();
        const token = data.token;

        sessionStorage.setItem("authToken", token);
        sessionStorage.setItem("loggedIn", "true");
        sessionStorage.setItem("username", username);

        showNotification("✅ Login successful! Redirecting...", "success");

        setTimeout(() => {
            window.location.href = "index.html";
        }, 1000);
    } catch (err) {
        showNotification("Login failed: " + err.message, "error");
    }
}

// ===== LOGOUT =====
function logout() {
    sessionStorage.clear();
    showNotification("🚪 Logged out successfully!", "success");
    setTimeout(() => (window.location.href = "login.html"), 800);
}

// ===== LOAD PEOPLE =====
async function loadPeople() {
    const table = document.getElementById("peopleList");
    const sendSelect = document.getElementById("sendName");
    const receiveSelect = document.getElementById("receiveName");

    try {
        const res = await authFetch(`${PEOPLE_API}/all`);
        if (!res.ok) throw new Error(await res.text());

        const people = await res.json();
        table.innerHTML = "";
        sendSelect.innerHTML = "<option>Select Person</option>";
        receiveSelect.innerHTML = "<option>Select Person</option>";

        if (!people.length) {
            table.innerHTML = "<tr><td colspan='3'>No people yet</td></tr>";
            return;
        }

        people.forEach(p => {
            const balance = p.balance ?? 0;
            const balanceClass = balance > 0 ? "balance-positive" :
                                 balance < 0 ? "balance-negative" : "balance-zero";

            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${p.name}</td>
                <td><span class="${balanceClass}">₹${balance}</span></td>
                <td><button class="delete-btn" onclick="deletePerson('${p.name}')">🗑️ Delete</button></td>
            `;
            table.appendChild(row);

            [sendSelect, receiveSelect].forEach(sel => {
                const opt = document.createElement("option");
                opt.value = p.name;
                opt.textContent = p.name;
                sel.appendChild(opt);
            });
        });
    } catch (err) {
        showNotification("Failed to load people: " + err.message, "error");
    }
}

// ===== ADD PERSON =====
async function addPerson() {
    const name = document.getElementById("personName").value.trim();
    if (!name) return showNotification("Enter a name", "error");

    try {
        const res = await authFetch(`${PEOPLE_API}/add?name=${encodeURIComponent(name)}`, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());

        showNotification(`✅ Person "${name}" added successfully!`, "success");
        document.getElementById("personName").value = "";
        loadPeople();
    } catch (err) {
        showNotification("Failed to add person: " + err.message, "error");
    }
}

// ===== SEND MONEY =====
async function sendMoney() {
    const name = document.getElementById("sendName").value;
    const amount = document.getElementById("sendAmount").value;
    const desc = document.getElementById("sendDesc").value;

    if (!name || !amount || amount <= 0) {
        return showNotification("Select person and enter a valid amount", "error");
    }

    lastActivePerson = name;

    try {
        const res = await authFetch(
            `${PEOPLE_API}/send?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(desc)}`,
            { method: "POST" }
        );
        if (!res.ok) throw new Error(await res.text());

        showNotification(`📤 Sent ₹${amount} to ${name}`, "success");
        document.getElementById("sendAmount").value = "";
        document.getElementById("sendDesc").value = "";
        loadPeople(); loadTransactions();
    } catch (err) {
        showNotification("Failed to send money: " + err.message, "error");
    }
}

// ===== RECEIVE MONEY =====
async function receiveMoney() {
    const name = document.getElementById("receiveName").value;
    const amount = document.getElementById("receiveAmount").value;
    const desc = document.getElementById("receiveDesc").value;

    if (!name || !amount || amount <= 0) {
        return showNotification("Select person and enter a valid amount", "error");
    }

    lastActivePerson = name;

    try {
        const res = await authFetch(
            `${PEOPLE_API}/receive?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(desc)}`,
            { method: "POST" }
        );
        if (!res.ok) throw new Error(await res.text());

        showNotification(`📥 Received ₹${amount} from ${name}`, "success");
        document.getElementById("receiveAmount").value = "";
        document.getElementById("receiveDesc").value = "";
        loadPeople(); loadTransactions();
    } catch (err) {
        showNotification("Failed to receive money: " + err.message, "error");
    }
}

// ===== DELETE PERSON =====
async function deletePerson(name) {
    if (!confirm(`Delete ${name}?`)) return;
    try {
        const res = await authFetch(`${PEOPLE_API}/${encodeURIComponent(name)}`, { method: "DELETE" });
        if (!res.ok) throw new Error(await res.text());

        showNotification(`🗑️ Deleted ${name}`, "success");
        loadPeople(); loadTransactions();
    } catch (err) {
        showNotification("Failed to delete person: " + err.message, "error");
    }
}

// ===== REVERSE TRANSACTION =====
async function reverseTransaction(id) {
    if (!confirm("Are you sure you want to reverse this transaction?")) return;

    try {
        const res = await authFetch(`${TRANSACTION_API}/reverse/${id}`, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());

        showNotification("🔄 Transaction reversed successfully!", "success");
        loadPeople(); loadTransactions();
    } catch (err) {
        showNotification("Failed to reverse transaction: " + err.message, "error");
    }
}

// ===== LOAD TRANSACTIONS (Grouped) =====
async function loadTransactions() {
    const container = document.getElementById("transactionsContainer");
    container.innerHTML = "<p>Loading...</p>";

    try {
        const res = await authFetch(`${TRANSACTION_API}/all`);
        if (!res.ok) throw new Error(await res.text());

        const transactions = await res.json();
        let totalSent = 0, totalReceived = 0;

        if (!transactions.length) {
            container.innerHTML = "<p>No transactions yet</p>";
            return;
        }

        // Group by person
        const grouped = {};
        transactions.forEach(t => {
            if (t.type === "SEND") totalSent += t.amount;
            else totalReceived += t.amount;

            const personName = t.person.name;
            if (!grouped[personName]) grouped[personName] = [];
            grouped[personName].push(t);
        });

        container.innerHTML = "";

        Object.keys(grouped).forEach(personName => {
            const safeId = personName.replace(/\s+/g, "_");
            const personDiv = document.createElement("div");
            personDiv.className = "person-group";

            personDiv.innerHTML = `
                <div class="person-header" onclick="toggleTransactions('${safeId}')">
                    <h3>👤 ${personName}</h3>
                    <span class="toggle-indicator">+</span>
                </div>
                <div class="person-transactions" id="tx-${safeId}" style="display:none;"></div>
            `;
            container.appendChild(personDiv);

            const txContainer = personDiv.querySelector(`#tx-${safeId}`);
            grouped[personName].forEach(t => {
                const txDiv = document.createElement("div");
                txDiv.className = `transaction-item ${t.type.toLowerCase()}`;
                txDiv.innerHTML = `
                    <div>
                        <b>${t.type === "SEND" ? "📤 Sent" : "📥 Received"}</b>
                        ₹${t.amount}
                        <button class="reverse-btn" onclick="reverseTransaction(${t.id})">↩ Reverse</button>
                    </div>
                    <div>
                        <small>${t.description || "No description"} (${new Date(t.date).toLocaleDateString()})</small>
                    </div>
                `;
                txContainer.appendChild(txDiv);
            });

            if (lastActivePerson && lastActivePerson === personName) {
                toggleTransactions(safeId);
            }
        });

        document.getElementById("totalSent").textContent = `₹${totalSent}`;
        document.getElementById("totalReceived").textContent = `₹${totalReceived}`;
        document.getElementById("netBalance").textContent = `₹${totalReceived - totalSent}`;
    } catch (err) {
        showNotification("Failed to load transactions: " + err.message, "error");
    }
}

// ===== TOGGLE TRANSACTIONS =====
function toggleTransactions(safeId) {
    const txDiv = document.getElementById(`tx-${safeId}`);
    const indicator = txDiv.parentElement.querySelector(".toggle-indicator");
    if (txDiv.style.display === "none") {
        txDiv.style.display = "block";
        indicator.textContent = "−";
    } else {
        txDiv.style.display = "none";
        indicator.textContent = "+";
    }
}

// ===== INITIALIZATION =====
document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", login);
    }

    if (document.body.classList.contains("dashboard")) {
        const token = getAuthToken();
        if (!token) {
            window.location.href = "login.html";
        } else {
            loadPeople();
            loadTransactions();
        }
    }
});
