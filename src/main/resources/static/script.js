// ===== API Base URL =====
const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const BASE_URL = isLocal
    ? "http://localhost:8080"
    : "https://track-transaction.onrender.com";

const PEOPLE_API = `${BASE_URL}/api/people`;
const TRANSACTION_API = `${BASE_URL}/api/transactions`;
const AUTH_API = `${BASE_URL}/api/auth`;

let lastActiveSafeId = null; // Track by safeId instead of person name

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

// ===== Utility: Clear and reset a form =====
function resetForm(formId) {
    const form = document.getElementById(formId);
    if (form) form.reset();
}

// ===== Authenticated Fetch =====
async function authFetch(url, options = {}) {
    const token = getAuthToken();
    options.method = options.method || "GET";
    options.headers = {
        ...(options.headers || {}),
        "Content-Type": "application/json"
    };
    if (token) options.headers["Authorization"] = `Bearer ${token}`;
    options.credentials = "include";

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
            body: JSON.stringify({ username, password }),
            credentials: "include"
        });

        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Invalid login");
        }

        const data = await res.json();
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("loggedIn", "true");
        sessionStorage.setItem("username", username);

        showNotification("✅ Login successful! Redirecting...", "success");
        resetForm("loginForm");

        setTimeout(() => (window.location.href = "index.html"), 1000);
    } catch (err) {
        showNotification("Login failed: " + err.message, "error");
    }
}

// ===== REGISTER =====
async function register(event) {
    event.preventDefault();
    const username = document.getElementById("regUsername").value.trim();
    const password = document.getElementById("regPassword").value.trim();

    if (!username || !password) {
        return showNotification("Enter both username and password", "error");
    }

    try {
        const res = await fetch(`${AUTH_API}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
            credentials: "include"
        });

        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Registration failed");
        }

        showNotification("✅ Registration successful! You can now log in.", "success");
        resetForm("registerForm");

        setTimeout(() => {
            window.location.href = "login.html";
        }, 1500);
    } catch (err) {
        showNotification("Registration failed: " + err.message, "error");
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
        sendSelect.innerHTML = "<option value=''>Select Person</option>";
        receiveSelect.innerHTML = "<option value=''>Select Person</option>";

        if (!people.length) {
            table.innerHTML = "<tr><td colspan='3'>No people yet</td></tr>";
            return;
        }

        people.forEach(p => {
            const balance = p.balance ?? 0;
            const balanceClass =
                balance > 0 ? "balance-positive" :
                balance < 0 ? "balance-negative" : "balance-zero";

            const row = document.createElement("tr");
            row.setAttribute("data-person", p.name);
            row.innerHTML = `
                <td>${p.name}</td>
                <td class="balance-cell"><span class="${balanceClass}">₹${balance}</span></td>
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
        resetForm("personForm");
        document.getElementById("personName").value = "";
        loadPeople();
    } catch (err) {
        showNotification("Failed to add person: " + err.message, "error");
    }
}

// ===== SEND MONEY =====
async function sendMoney() {
    const name = document.getElementById("sendName").value;
    const amount = parseFloat(document.getElementById("sendAmount").value);
    const desc = document.getElementById("sendDesc").value.trim();

    if (!name || isNaN(amount) || amount <= 0) {
        return showNotification("Select person and enter a valid amount", "error");
    }

    // Set last active person with safe ID
    lastActiveSafeId = name.replace(/[^a-zA-Z0-9]/g, "_");

    try {
        const res = await authFetch(
            `${PEOPLE_API}/send?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(desc)}`,
            { method: "POST" }
        );
        if (!res.ok) throw new Error(await res.text());

        showNotification(`📤 Sent ₹${amount} to ${name}`, "success");
        document.getElementById("sendName").value = "";
        document.getElementById("sendAmount").value = "";
        document.getElementById("sendDesc").value = "";

        await loadPeople();
        await loadTransactions();
    } catch (err) {
        showNotification("Failed to send money: " + err.message, "error");
    }
}

// ===== RECEIVE MONEY =====
async function receiveMoney() {
    const name = document.getElementById("receiveName").value;
    const amount = parseFloat(document.getElementById("receiveAmount").value);
    const desc = document.getElementById("receiveDesc").value.trim();

    if (!name || isNaN(amount) || amount <= 0) {
        return showNotification("Select person and enter a valid amount", "error");
    }

    // Set last active person with safe ID
    lastActiveSafeId = name.replace(/[^a-zA-Z0-9]/g, "_");

    try {
        const res = await authFetch(
            `${PEOPLE_API}/receive?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(desc)}`,
            { method: "POST" }
        );
        if (!res.ok) throw new Error(await res.text());

        showNotification(`📥 Received ₹${amount} from ${name}`, "success");
        document.getElementById("receiveName").value = "";
        document.getElementById("receiveAmount").value = "";
        document.getElementById("receiveDesc").value = "";

        await loadPeople();
        await loadTransactions();
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
        loadPeople();
        loadTransactions();
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

        // Reload both people and transactions
        await loadPeople();
        await loadTransactions();

    } catch (err) {
        showNotification("Failed to reverse transaction: " + err.message, "error");
    }
}

// ===== LOAD TRANSACTIONS (Grouped & Collapsible) - COMPLETELY FIXED =====
async function loadTransactions() {
    const container = document.getElementById("transactionsContainer");
    container.innerHTML = "<p>Loading transactions...</p>";

    try {
        const res = await authFetch(`${TRANSACTION_API}/all`);
        if (!res.ok) throw new Error(await res.text());

        const transactions = await res.json();
        let totalSent = 0, totalReceived = 0;

        console.log("Loaded transactions:", transactions); // Debug log

        if (!transactions.length) {
            container.innerHTML = "<p>No transactions yet</p>";
            document.getElementById("totalSent").textContent = "₹0";
            document.getElementById("totalReceived").textContent = "₹0";
            document.getElementById("netBalance").textContent = "₹0";
            return;
        }

        // Group by person
        const grouped = {};
        transactions.forEach(t => {
            if (t.reversed !== true) {
                if (t.type?.toUpperCase() === "SEND") totalSent += t.amount;
                else if (t.type?.toUpperCase() === "RECEIVE") totalReceived += t.amount;
            }

            const personName = t.person?.name || t.personName || "Unknown";
            if (!grouped[personName]) grouped[personName] = [];
            grouped[personName].push(t);
        });

        console.log("Grouped transactions:", grouped); // Debug log

        container.innerHTML = "";

        Object.keys(grouped).forEach(personName => {
            const safeId = personName.replace(/[^a-zA-Z0-9]/g, "_");
            console.log(`Creating group for ${personName} with safeId: ${safeId}`); // Debug log

            const personDiv = document.createElement("div");
            personDiv.className = "person-group";

            // Create the person header
            const personHeader = document.createElement("div");
            personHeader.className = "person-header";
            personHeader.setAttribute("data-person", safeId);
            personHeader.style.cursor = "pointer";
            personHeader.innerHTML = `
                <h3>👤 ${personName} (${grouped[personName].length} transactions)</h3>
                <span class="toggle-indicator">+</span>
            `;

            // Create the transactions container
            const txContainer = document.createElement("div");
            txContainer.className = "person-transactions";
            txContainer.id = `tx-${safeId}`;
            txContainer.style.display = "none";

            // Add transactions to container
            grouped[personName]
                .sort((a, b) => {
                    // Sort by date descending, but put reversed transactions at bottom
                    if (a.reversed !== b.reversed) {
                        return a.reversed ? 1 : -1;
                    }
                    return new Date(b.date) - new Date(a.date);
                })
                .forEach(t => {
                    const isReversed = t.reversed === true;
                    const txDiv = document.createElement("div");
                    txDiv.className = `transaction-item ${t.type?.toLowerCase()} ${isReversed ? "reversed" : ""}`;

                    const typeIcon = t.type?.toUpperCase() === "SEND" ? "📤" : "📥";
                    const typeText = t.type?.toUpperCase() === "SEND" ? "Sent" : "Received";

                    txDiv.innerHTML = `
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <b>${typeIcon} ${typeText} ₹${t.amount}</b>
                                ${isReversed ? "<span class='reversed-label'>(Reversed)</span>" : ""}
                            </div>
                            <div>
                                ${!isReversed ? `<button class="reverse-btn" onclick="reverseTransaction(${t.id})">↩ Reverse</button>` : ""}
                            </div>
                        </div>
                        <div>
                            <small>${t.description || "No description"} • ${new Date(t.date).toLocaleDateString()} ${new Date(t.date).toLocaleTimeString()}</small>
                        </div>
                    `;
                    txContainer.appendChild(txDiv);
                });

            // Add click event listener to person header
            personHeader.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log(`Clicked on person header for: ${personName} (${safeId})`); // Debug log
                toggleTransactions(safeId);
            });

            // Assemble the person group
            personDiv.appendChild(personHeader);
            personDiv.appendChild(txContainer);
            container.appendChild(personDiv);

            // Auto-expand if this was the last active person
            if (lastActiveSafeId && lastActiveSafeId === safeId) {
                setTimeout(() => toggleTransactions(safeId, true), 100);
            }
        });

        // Update totals
        document.getElementById("totalSent").textContent = `₹${totalSent}`;
        document.getElementById("totalReceived").textContent = `₹${totalReceived}`;
        document.getElementById("netBalance").textContent = `₹${totalReceived - totalSent}`;

        console.log("Transaction loading completed"); // Debug log
    } catch (err) {
        console.error("Transaction load error:", err);
        showNotification("Failed to load transactions: " + err.message, "error");
        container.innerHTML = "<p style='color:red;'>Error loading transactions.</p>";
    }
}

// ===== TOGGLE TRANSACTIONS - COMPLETELY FIXED =====
function toggleTransactions(safeId, autoScroll = false) {
    console.log(`toggleTransactions called with safeId: ${safeId}`); // Debug log

    const txDiv = document.getElementById(`tx-${safeId}`);
    const personHeader = document.querySelector(`[data-person="${safeId}"]`);
    const indicator = personHeader?.querySelector(".toggle-indicator");

    console.log("Found elements:", { txDiv: !!txDiv, personHeader: !!personHeader, indicator: !!indicator }); // Debug log

    if (!txDiv) {
        console.error(`Could not find transaction container with ID: tx-${safeId}`);
        return;
    }

    if (!indicator) {
        console.error(`Could not find indicator for person: ${safeId}`);
        return;
    }

    const isCurrentlyHidden = txDiv.style.display === "none" || txDiv.style.display === "";

    if (isCurrentlyHidden) {
        // Close all other open transactions first
        document.querySelectorAll(".person-transactions").forEach(el => {
            if (el !== txDiv) {
                el.style.display = "none";
            }
        });
        document.querySelectorAll(".toggle-indicator").forEach(el => {
            if (el !== indicator) {
                el.textContent = "+";
            }
        });

        // Open this one
        txDiv.style.display = "block";
        indicator.textContent = "−";
        lastActiveSafeId = safeId;

        console.log(`Opened transactions for: ${safeId}`); // Debug log

        if (autoScroll) {
            setTimeout(() => {
                personHeader.scrollIntoView({ behavior: "smooth", block: "start" });
            }, 200);
        }
    } else {
        // Close this one
        txDiv.style.display = "none";
        indicator.textContent = "+";
        lastActiveSafeId = null;

        console.log(`Closed transactions for: ${safeId}`); // Debug log
    }
}

// ===== INITIALIZATION =====
document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    if (loginForm) loginForm.addEventListener("submit", login);

    const regForm = document.getElementById("registerForm");
    if (regForm) regForm.addEventListener("submit", register);

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