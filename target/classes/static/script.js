// ===== ENHANCED MONEY TRACKER - MAIN SCRIPT =====

// ===== CONFIGURATION & CONSTANTS =====
const CONFIG = {
    API: {
        BASE_URL: ["localhost", "127.0.0.1"].includes(window.location.hostname)
            ? "http://localhost:8080"
            : "https://track-transaction.onrender.com",
        TIMEOUT: 30000,
        RETRY_ATTEMPTS: 3,
        RETRY_DELAY: 1000
    },
    VALIDATION: {
        MIN_AMOUNT: 0.01,
        MAX_AMOUNT: 1000000,
        MAX_DESCRIPTION_LENGTH: 200,
        MIN_USERNAME_LENGTH: 3,
        MAX_USERNAME_LENGTH: 30
    },
    UI: {
        DEBOUNCE_DELAY: 300,
        NOTIFICATION_DURATION: 5000,
        LOADING_DELAY: 200
    }
};

// API Endpoints
const API_ENDPOINTS = {
    PEOPLE: `${CONFIG.API.BASE_URL}/api/people`,
    TRANSACTIONS: `${CONFIG.API.BASE_URL}/api/transactions`,
    AUTH: `${CONFIG.API.BASE_URL}/api/auth`
};

// ===== STATE MANAGEMENT =====
const AppState = {
    user: null,
    people: [],
    transactions: [],
    connectionOnline: navigator.onLine,
    lastActivePerson: null,
    cache: new Map(),

    // Update state and notify observers
    update(key, value) {
        this[key] = value;
        this.notifyObservers(key, value);
    },

    observers: new Map(),

    subscribe(key, callback) {
        if (!this.observers.has(key)) {
            this.observers.set(key, []);
        }
        this.observers.get(key).push(callback);
    },

    notifyObservers(key, value) {
        const callbacks = this.observers.get(key);
        if (callbacks) {
            callbacks.forEach(callback => callback(value));
        }
    }
};

// ===== UTILITY FUNCTIONS =====

// Secure HTML sanitizer
function sanitizeHTML(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Input validation
function validateInput(value, type, options = {}) {
    const errors = [];

    switch (type) {
        case 'username':
            if (!value || value.length < CONFIG.VALIDATION.MIN_USERNAME_LENGTH) {
                errors.push(`Username must be at least ${CONFIG.VALIDATION.MIN_USERNAME_LENGTH} characters`);
            }
            if (value.length > CONFIG.VALIDATION.MAX_USERNAME_LENGTH) {
                errors.push(`Username must be less than ${CONFIG.VALIDATION.MAX_USERNAME_LENGTH} characters`);
            }
            if (!/^[a-zA-Z0-9_]+$/.test(value)) {
                errors.push('Username can only contain letters, numbers, and underscores');
            }
            break;

        case 'amount':
            const amount = parseFloat(value);
            if (isNaN(amount) || amount < CONFIG.VALIDATION.MIN_AMOUNT) {
                errors.push(`Amount must be at least ₹${CONFIG.VALIDATION.MIN_AMOUNT}`);
            }
            if (amount > CONFIG.VALIDATION.MAX_AMOUNT) {
                errors.push(`Amount cannot exceed ₹${CONFIG.VALIDATION.MAX_AMOUNT.toLocaleString()}`);
            }
            break;

        case 'description':
            if (value && value.length > CONFIG.VALIDATION.MAX_DESCRIPTION_LENGTH) {
                errors.push(`Description must be less than ${CONFIG.VALIDATION.MAX_DESCRIPTION_LENGTH} characters`);
            }
            break;

        case 'email':
            if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
                errors.push('Please enter a valid email address');
            }
            break;
    }

    return errors;
}

// Debounce function for performance
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func.apply(this, args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return {
        date: date.toLocaleDateString('en-IN'),
        time: date.toLocaleTimeString('en-IN', {
            hour: '2-digit',
            minute: '2-digit'
        }),
        relative: getRelativeTime(date)
    };
}

// Get relative time
function getRelativeTime(date) {
    const now = new Date();
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
    return `${Math.floor(diffDays / 30)} months ago`;
}

// ===== ENHANCED AUTHENTICATION =====

// Get auth token with validation
function getAuthToken() {
    const token = sessionStorage.getItem("authToken");
    if (!token) return null;

    try {
        // Basic JWT validation (check if it's expired)
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp * 1000 < Date.now()) {
            sessionStorage.clear();
            return null;
        }
        return token;
    } catch (e) {
        sessionStorage.clear();
        return null;
    }
}

// Enhanced fetch with retry logic and better error handling
async function authFetch(url, options = {}) {
    const token = getAuthToken();

    const defaultOptions = {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            ...(token && { "Authorization": `Bearer ${token}` })
        },
        credentials: "include",
        signal: AbortSignal.timeout(CONFIG.API.TIMEOUT)
    };

    const fetchOptions = { ...defaultOptions, ...options };

    for (let attempt = 1; attempt <= CONFIG.API.RETRY_ATTEMPTS; attempt++) {
        try {
            const response = await fetch(url, fetchOptions);

            // Handle authentication errors
            if (response.status === 401 || response.status === 403) {
                handleAuthError();
                throw new Error("Authentication failed");
            }

            // Handle server errors with retry
            if (response.status >= 500 && attempt < CONFIG.API.RETRY_ATTEMPTS) {
                await new Promise(resolve => setTimeout(resolve, CONFIG.API.RETRY_DELAY * attempt));
                continue;
            }

            return response;

        } catch (error) {
            if (error.name === 'AbortError') {
                throw new Error("Request timeout. Please check your connection.");
            }

            if (attempt === CONFIG.API.RETRY_ATTEMPTS) {
                if (!navigator.onLine) {
                    throw new Error("No internet connection. Please check your network.");
                }
                throw error;
            }

            await new Promise(resolve => setTimeout(resolve, CONFIG.API.RETRY_DELAY * attempt));
        }
    }
}

// Handle authentication errors
function handleAuthError() {
    sessionStorage.clear();
    showNotification("⚠️ Session expired. Please login again.", "error");
    setTimeout(() => {
        window.location.href = "login.html";
    }, 2000);
}

// ===== ENHANCED NOTIFICATION SYSTEM =====

let notificationQueue = [];
let notificationId = 0;

function showNotification(message, type = "success", duration = CONFIG.UI.NOTIFICATION_DURATION) {
    const container = document.getElementById("notificationContainer");
    if (!container) {
        // Fallback for pages without notification container
        alert(message);
        return;
    }

    const id = ++notificationId;
    const notification = document.createElement("div");
    notification.className = `notification ${type}`;
    notification.setAttribute("data-id", id);

    const icon = getNotificationIcon(type);
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">${icon}</span>
            <span class="notification-message">${sanitizeHTML(message)}</span>
            <button class="notification-close" onclick="closeNotification(${id})" aria-label="Close">×</button>
        </div>
        <div class="notification-progress">
            <div class="progress-bar" style="animation: progress ${duration}ms linear"></div>
        </div>
    `;

    container.appendChild(notification);

    // Auto-remove after duration
    setTimeout(() => closeNotification(id), duration);

    // Remove old notifications if too many
    const notifications = container.querySelectorAll('.notification');
    if (notifications.length > 5) {
        notifications[0].remove();
    }
}

function getNotificationIcon(type) {
    const icons = {
        success: "✅",
        error: "❌",
        warning: "⚠️",
        info: "ℹ️",
        loading: "⏳"
    };
    return icons[type] || "ℹ️";
}

function closeNotification(id) {
    const notification = document.querySelector(`[data-id="${id}"]`);
    if (notification) {
        notification.style.animation = "slideOut 0.3s ease-in-out";
        setTimeout(() => notification.remove(), 300);
    }
}

// ===== ENHANCED UI HELPERS =====

// Show/hide loading states
function setLoadingState(elementId, isLoading) {
    const element = document.getElementById(elementId);
    if (!element) return;

    if (isLoading) {
        element.classList.add('loading');
        element.disabled = true;
        const btnText = element.querySelector('.btn-text');
        const btnLoading = element.querySelector('.btn-loading');
        if (btnText) btnText.style.display = 'none';
        if (btnLoading) btnLoading.style.display = 'flex';
    } else {
        element.classList.remove('loading');
        element.disabled = false;
        const btnText = element.querySelector('.btn-text');
        const btnLoading = element.querySelector('.btn-loading');
        if (btnText) btnText.style.display = 'flex';
        if (btnLoading) btnLoading.style.display = 'none';
    }
}

// Show/hide global loading overlay
function setGlobalLoading(isLoading) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = isLoading ? 'flex' : 'none';
    }
}

// Form validation helper
function validateForm(formId, rules) {
    const form = document.getElementById(formId);
    if (!form) return false;

    let isValid = true;
    const errors = {};

    Object.keys(rules).forEach(fieldName => {
        const field = form.querySelector(`#${fieldName}`);
        const rule = rules[fieldName];

        if (field) {
            const fieldErrors = validateInput(field.value, rule.type, rule.options);
            if (fieldErrors.length > 0) {
                errors[fieldName] = fieldErrors[0];
                isValid = false;
                showFieldError(fieldName, fieldErrors[0]);
            } else {
                clearFieldError(fieldName);
            }
        }
    });

    return isValid;
}

function showFieldError(fieldName, message) {
    const errorElement = document.getElementById(`${fieldName}Error`);
    const field = document.getElementById(fieldName);

    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    if (field) {
        field.classList.add('error');
    }
}

function clearFieldError(fieldName) {
    const errorElement = document.getElementById(`${fieldName}Error`);
    const field = document.getElementById(fieldName);

    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }

    if (field) {
        field.classList.remove('error');
    }
}

// ===== AUTHENTICATION FUNCTIONS =====

// Enhanced login with validation
async function login(event) {
    event.preventDefault();

    const username = document.getElementById("username")?.value?.trim();
    const password = document.getElementById("password")?.value?.trim();
    const rememberMe = document.getElementById("rememberMe")?.checked;

    // Client-side validation
    const validationRules = {
        username: { type: 'username' },
        password: { type: 'password' }
    };

    if (!validateForm('loginForm', validationRules)) {
        return;
    }

    setLoadingState('loginBtn', true);

    try {
        const response = await fetch(`${API_ENDPOINTS.AUTH}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password, rememberMe }),
            credentials: "include"
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: "Login failed" }));
            throw new Error(errorData.message || "Invalid credentials");
        }

        const data = await response.json();

        // Store auth data
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("loggedIn", "true");
        sessionStorage.setItem("username", username);

        if (rememberMe) {
            localStorage.setItem("rememberedUser", username);
        }

        showNotification("✅ Login successful! Redirecting...", "success");

        // Clear form
        document.getElementById("loginForm").reset();

        setTimeout(() => {
            window.location.href = "index.html";
        }, 1000);

    } catch (error) {
        showNotification(`❌ ${error.message}`, "error");
    } finally {
        setLoadingState('loginBtn', false);
    }
}

// Enhanced registration with validation
async function register(event) {
    event.preventDefault();

    const username = document.getElementById("regUsername")?.value?.trim();
    const email = document.getElementById("regEmail")?.value?.trim();
    const password = document.getElementById("regPassword")?.value;
    const passwordConfirm = document.getElementById("regPasswordConfirm")?.value;
    const agreeTerms = document.getElementById("agreeTerms")?.checked;

    // Client-side validation
    const validationRules = {
        regUsername: { type: 'username' },
        regEmail: { type: 'email' },
        regPassword: { type: 'password' }
    };

    if (!validateForm('registerForm', validationRules)) {
        return;
    }

    if (password !== passwordConfirm) {
        showFieldError('regPasswordConfirm', 'Passwords do not match');
        return;
    }

    if (!agreeTerms) {
        showFieldError('agreeTerms', 'You must agree to the terms of service');
        return;
    }

    setLoadingState('registerBtn', true);

    try {
        const response = await fetch(`${API_ENDPOINTS.AUTH}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, password }),
            credentials: "include"
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: "Registration failed" }));
            throw new Error(errorData.message || "Registration failed");
        }

        showNotification("✅ Registration successful! You can now log in.", "success");
        document.getElementById("registerForm").reset();

        setTimeout(() => {
            window.location.href = "login.html";
        }, 2000);

    } catch (error) {
        showNotification(`❌ ${error.message}`, "error");
    } finally {
        setLoadingState('registerBtn', false);
    }
}

// Logout with confirmation
function logout() {
    showConfirmModal(
        "Confirm Logout",
        "Are you sure you want to logout?",
        () => {
            sessionStorage.clear();
            localStorage.removeItem('rememberedUser');
            showNotification("🚪 Logged out successfully!", "success");
            setTimeout(() => {
                window.location.href = "login.html";
            }, 1000);
        }
    );
}

// ===== DATA MANAGEMENT FUNCTIONS =====

// Enhanced load people with caching and error handling
async function loadPeople() {
    const table = document.getElementById("peopleList");
    const sendSelect = document.getElementById("sendName");
    const receiveSelect = document.getElementById("receiveName");

    if (!table) return;

    // Show loading state
    table.innerHTML = `
        <tr class="loading-row">
            <td colspan="4">
                <div class="table-loading">
                    <div class="loading-dots"></div>
                    Loading people...
                </div>
            </td>
        </tr>
    `;

    try {
        const response = await authFetch(`${API_ENDPOINTS.PEOPLE}/all`);
        if (!response.ok) {
            throw new Error(await response.text());
        }

        const people = await response.json();
        AppState.update('people', people);

        // Update UI
        updatePeopleTable(people);
        updatePeopleSelects(people);
        updateStats();

    } catch (error) {
        console.error("Failed to load people:", error);
        table.innerHTML = `
            <tr class="error-row">
                <td colspan="4">
                    <div class="table-error">
                        <span class="error-icon">⚠️</span>
                        Failed to load people: ${error.message}
                        <button onclick="loadPeople()" class="retry-btn">Retry</button>
                    </div>
                </td>
            </tr>
        `;
        showNotification(`❌ Failed to load people: ${error.message}`, "error");
    }
}

// Update people table with enhanced features
function updatePeopleTable(people) {
    const table = document.getElementById("peopleList");
    if (!table) return;

    if (!people.length) {
        table.innerHTML = `
            <tr class="empty-row">
                <td colspan="4">
                    <div class="empty-state">
                        <span class="empty-icon">👥</span>
                        <p>No people added yet</p>
                        <small>Add your first person to start tracking transactions</small>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    table.innerHTML = "";

    people.forEach(person => {
        const balance = person.balance ?? 0;
        const balanceClass = balance > 0 ? "balance-positive" :
                           balance < 0 ? "balance-negative" : "balance-zero";

        const lastTransaction = getLastTransaction(person.name);
        const lastTxText = lastTransaction ?
            `${formatDate(lastTransaction.date).relative}` : 'No transactions';

        const row = document.createElement("tr");
        row.setAttribute("data-person", person.name);
        row.innerHTML = `
            <td>
                <div class="person-info">
                    <div class="person-avatar">${person.name.charAt(0).toUpperCase()}</div>
                    <div class="person-details">
                        <strong>${sanitizeHTML(person.name)}</strong>
                        <small>Added ${formatDate(person.createdAt || new Date()).relative}</small>
                    </div>
                </div>
            </td>
            <td class="balance-cell">
                <span class="${balanceClass}">${formatCurrency(balance)}</span>
            </td>
            <td class="last-transaction">
                <small>${lastTxText}</small>
            </td>
            <td class="actions-cell">
                <div class="action-buttons">
                    <button class="edit-btn" onclick="editPerson('${sanitizeHTML(person.name)}')"
                            title="Edit person" aria-label="Edit ${person.name}">
                        ✏️
                    </button>
                    <button class="delete-btn" onclick="deletePerson('${sanitizeHTML(person.name)}')"
                            title="Delete person" aria-label="Delete ${person.name}">
                        🗑️
                    </button>
                </div>
            </td>
        `;
        table.appendChild(row);
    });
}

// Update people selects
function updatePeopleSelects(people) {
    const selects = [
        document.getElementById("sendName"),
        document.getElementById("receiveName")
    ];

    selects.forEach(select => {
        if (!select) return;

        const currentValue = select.value;
        select.innerHTML = "<option value=''>Choose person...</option>";

        people.forEach(person => {
            const option = document.createElement("option");
            option.value = person.name;
            option.textContent = person.name;
            if (person.name === currentValue) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    });
}

// Get last transaction for a person
function getLastTransaction(personName) {
    return AppState.transactions
        .filter(t => (t.person?.name || t.personName) === personName && !t.reversed)
        .sort((a, b) => new Date(b.date) - new Date(a.date))[0];
}

// Enhanced add person with validation
async function addPerson() {
    const nameInput = document.getElementById("personName");
    const name = nameInput?.value?.trim();

    // Validate input
    const errors = validateInput(name, 'username');
    if (errors.length > 0) {
        showFieldError('personName', errors[0]);
        return;
    }

    // Check for duplicates
    if (AppState.people.some(p => p.name.toLowerCase() === name.toLowerCase())) {
        showFieldError('personName', 'A person with this name already exists');
        return;
    }

    setLoadingState('addPersonBtn', true);

    try {
        const response = await authFetch(
            `${API_ENDPOINTS.PEOPLE}/add?name=${encodeURIComponent(name)}`,
            { method: "POST" }
        );

        if (!response.ok) {
            throw new Error(await response.text());
        }

        showNotification(`✅ ${name} added successfully!`, "success");

        // Clear form
        nameInput.value = "";
        clearFieldError('personName');

        // Reload data
        await Promise.all([loadPeople(), loadTransactions()]);

    } catch (error) {
        showNotification(`❌ Failed to add person: ${error.message}`, "error");
    } finally {
        setLoadingState('addPersonBtn', false);
    }
}

// Enhanced send money with validation
async function sendMoney() {
    const nameSelect = document.getElementById("sendName");
    const amountInput = document.getElementById("sendAmount");
    const descInput = document.getElementById("sendDesc");

    const name = nameSelect?.value;
    const amount = parseFloat(amountInput?.value || "0");
    const description = descInput?.value?.trim() || "";

    // Validate inputs
    let hasErrors = false;

    if (!name) {
        showFieldError('sendName', 'Please select a person');
        hasErrors = true;
    }

    const amountErrors = validateInput(amount, 'amount');
    if (amountErrors.length > 0) {
        showFieldError('sendAmount', amountErrors[0]);
        hasErrors = true;
    }

    const descErrors = validateInput(description, 'description');
    if (descErrors.length > 0) {
        showFieldError('sendDesc', descErrors[0]);
        hasErrors = true;
    }

    if (hasErrors) return;

    AppState.lastActivePerson = name;
    setLoadingState('sendBtn', true);

    try {
        const response = await authFetch(
            `${API_ENDPOINTS.PEOPLE}/send?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(description)}`,
            { method: "POST" }
        );

        if (!response.ok) {
            throw new Error(await response.text());
        }

        showNotification(`📤 Sent ${formatCurrency(amount)} to ${name}`, "success");

        // Clear form
        document.getElementById("sendForm").reset();
        clearFieldError('sendName');
        clearFieldError('sendAmount');
        clearFieldError('sendDesc');

        // Reload data
        await Promise.all([loadPeople(), loadTransactions()]);

    } catch (error) {
        showNotification(`❌ Failed to send money: ${error.message}`, "error");
    } finally {
        setLoadingState('sendBtn', false);
    }
}

// Enhanced receive money with validation
async function receiveMoney() {
    const nameSelect = document.getElementById("receiveName");
    const amountInput = document.getElementById("receiveAmount");
    const descInput = document.getElementById("receiveDesc");

    const name = nameSelect?.value;
    const amount = parseFloat(amountInput?.value || "0");
    const description = descInput?.value?.trim() || "";

    // Validate inputs
    let hasErrors = false;

    if (!name) {
        showFieldError('receiveName', 'Please select a person');
        hasErrors = true;
    }

    const amountErrors = validateInput(amount, 'amount');
    if (amountErrors.length > 0) {
        showFieldError('receiveAmount', amountErrors[0]);
        hasErrors = true;
    }

    const descErrors = validateInput(description, 'description');
    if (descErrors.length > 0) {
        showFieldError('receiveDesc', descErrors[0]);
        hasErrors = true;
    }

    if (hasErrors) return;

    AppState.lastActivePerson = name;
    setLoadingState('receiveBtn', true);

    try {
        const response = await authFetch(
            `${API_ENDPOINTS.PEOPLE}/receive?name=${encodeURIComponent(name)}&amount=${amount}&description=${encodeURIComponent(description)}`,
            { method: "POST" }
        );

        if (!response.ok) {
            throw new Error(await response.text());
        }

        showNotification(`📥 Received ${formatCurrency(amount)} from ${name}`, "success");

        // Clear form
        document.getElementById("receiveForm").reset();
        clearFieldError('receiveName');
        clearFieldError('receiveAmount');
        clearFieldError('receiveDesc');

        // Reload data
        await Promise.all([loadPeople(), loadTransactions()]);

    } catch (error) {
        showNotification(`❌ Failed to receive money: ${error.message}`, "error");
    } finally {
        setLoadingState('receiveBtn', false);
    }
}

// Enhanced delete person with confirmation
async function deletePerson(name) {
    const person = AppState.people.find(p => p.name === name);
    const transactionCount = AppState.transactions.filter(
        t => (t.person?.name || t.personName) === name && !t.reversed
    ).length;

    const message = transactionCount > 0
        ? `Delete ${name}? This will also delete ${transactionCount} transaction${transactionCount > 1 ? 's' : ''}.`
        : `Delete ${name}?`;

    showConfirmModal(
        "Delete Person",
        message,
        async () => {
            try {
                const response = await authFetch(
                    `${API_ENDPOINTS.PEOPLE}/${encodeURIComponent(name)}`,
                    { method: "DELETE" }
                );

                if (!response.ok) {
                    throw new Error(await response.text());
                }

                showNotification(`🗑️ ${name} deleted successfully`, "success");

                // Reload data
                await Promise.all([loadPeople(), loadTransactions()]);

            } catch (error) {
                showNotification(`❌ Failed to delete person: ${error.message}`, "error");
            }
        }
    );
}

// ===== TRANSACTION MANAGEMENT =====

// Enhanced load transactions with filtering and search
async function loadTransactions() {
    const container = document.getElementById("transactionsContainer");
    if (!container) return;

    // Show loading state
    container.innerHTML = `
        <div class="loading-transactions">
            <div class="loading-dots"></div>
            <p>Loading transactions...</p>
        </div>
    `;

    try {
        const response = await authFetch(`${API_ENDPOINTS.TRANSACTIONS}/all`);
        if (!response.ok) {
            throw new Error(await response.text());
        }

        const transactions = await response.json();
        AppState.update('transactions', transactions);

        // Update UI components
        updateTransactionSummary(transactions);
        updateTransactionsList(transactions);
        updateStats();

    } catch (error) {
        console.error("Failed to load transactions:", error);
        container.innerHTML = `
            <div class="error-state">
                <span class="error-icon">⚠️</span>
                <p>Failed to load transactions</p>
                <small>${error.message}</small>
                <button onclick="loadTransactions()" class="retry-btn">Retry</button>
            </div>
        `;
        showNotification(`❌ Failed to load transactions: ${error.message}`, "error");
    }
}

// Update transaction summary cards
function updateTransactionSummary(transactions) {
    let totalSent = 0, totalReceived = 0;
    let sentCount = 0, receivedCount = 0;

    transactions.forEach(t => {
        if (t.reversed) return;

        if (t.type?.toUpperCase() === "SEND") {
            totalSent += t.amount;
            sentCount++;
        } else if (t.type?.toUpperCase() === "RECEIVE") {
            totalReceived += t.amount;
            receivedCount++;
        }
    });

    const netBalance = totalReceived - totalSent;

    // Update summary cards
    const elements = {
        totalSent: document.getElementById("totalSent"),
        totalReceived: document.getElementById("totalReceived"),
        netBalance: document.getElementById("netBalance"),
        sentCount: document.getElementById("sentCount"),
        receivedCount: document.getElementById("receivedCount"),
        balanceStatus: document.getElementById("balanceStatus")
    };

    if (elements.totalSent) elements.totalSent.textContent = formatCurrency(totalSent);
    if (elements.totalReceived) elements.totalReceived.textContent = formatCurrency(totalReceived);
    if (elements.netBalance) {
        elements.netBalance.textContent = formatCurrency(Math.abs(netBalance));
        elements.netBalance.className = netBalance >= 0 ? 'positive' : 'negative';
    }
    if (elements.sentCount) elements.sentCount.textContent = `${sentCount} transaction${sentCount !== 1 ? 's' : ''}`;
    if (elements.receivedCount) elements.receivedCount.textContent = `${receivedCount} transaction${receivedCount !== 1 ? 's' : ''}`;
    if (elements.balanceStatus) {
        elements.balanceStatus.textContent = netBalance > 0 ? 'Positive' : netBalance < 0 ? 'Negative' : 'Even';
    }
}

// Update transactions list with grouping and filtering
function updateTransactionsList(transactions) {
    const container = document.getElementById("transactionsContainer");
    if (!container) return;

    if (!transactions.length) {
        container.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📝</span>
                <p>No transactions yet</p>
                <small>Start by sending or receiving money</small>
            </div>
        `;
        return;
    }

    // Apply current filters
    const searchTerm = document.getElementById("transactionSearch")?.value?.toLowerCase() || "";
    const filterType = document.getElementById("transactionFilter")?.value || "all";

    let filteredTransactions = transactions;

    // Apply search filter
    if (searchTerm) {
        filteredTransactions = filteredTransactions.filter(t => {
            const personName = (t.person?.name || t.personName || "").toLowerCase();
            const description = (t.description || "").toLowerCase();
            return personName.includes(searchTerm) || description.includes(searchTerm);
        });
    }

    // Apply type filter
    if (filterType !== "all") {
        filteredTransactions = filteredTransactions.filter(t => {
            if (filterType === "reversed") return t.reversed;
            return t.type?.toLowerCase() === filterType && !t.reversed;
        });
    }

    // Group by person
    const grouped = {};
    filteredTransactions.forEach(t => {
        const personName = t.person?.name || t.personName || "Unknown";
        if (!grouped[personName]) {
            grouped[personName] = [];
        }
        grouped[personName].push(t);
    });

    // Render grouped transactions
    container.innerHTML = "";

    if (Object.keys(grouped).length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">🔍</span>
                <p>No transactions match your search</p>
                <small>Try adjusting your filters</small>
            </div>
        `;
        return;
    }

    Object.keys(grouped).forEach(personName => {
        const personTransactions = grouped[personName];
        const safeId = personName.replace(/[^a-zA-Z0-9]/g, "_");

        // Sort transactions by date (newest first)
        personTransactions.sort((a, b) => {
            if (a.reversed !== b.reversed) return a.reversed ? 1 : -1;
            return new Date(b.date) - new Date(a.date);
        });

        const personDiv = document.createElement("div");
        personDiv.className = "person-group";

        // Person header
        const personHeader = document.createElement("div");
        personHeader.className = "person-header";
        personHeader.setAttribute("data-person", safeId);

        const totalAmount = personTransactions
            .filter(t => !t.reversed)
            .reduce((sum, t) => sum + (t.type?.toUpperCase() === "RECEIVE" ? t.amount : -t.amount), 0);

        personHeader.innerHTML = `
            <div class="person-header-content">
                <div class="person-avatar">${personName.charAt(0).toUpperCase()}</div>
                <div class="person-info">
                    <h3>${sanitizeHTML(personName)}</h3>
                    <small>${personTransactions.length} transactions • Balance: ${formatCurrency(Math.abs(totalAmount))} ${totalAmount >= 0 ? 'to receive' : 'to pay'}</small>
                </div>
            </div>
            <div class="toggle-controls">
                <span class="balance-indicator ${totalAmount >= 0 ? 'positive' : 'negative'}">
                    ${totalAmount >= 0 ? '📥' : '📤'}
                </span>
                <span class="toggle-indicator">+</span>
            </div>
        `;

        // Transactions container
        const txContainer = document.createElement("div");
        txContainer.className = "person-transactions";
        txContainer.id = `tx-${safeId}`;
        txContainer.style.display = "none";

        // Add transactions
        personTransactions.forEach(t => {
            const txDiv = document.createElement("div");
            const isReversed = t.reversed === true;
            txDiv.className = `transaction-item ${t.type?.toLowerCase()} ${isReversed ? "reversed" : ""}`;

            const typeIcon = t.type?.toUpperCase() === "SEND" ? "📤" : "📥";
            const typeText = t.type?.toUpperCase() === "SEND" ? "Sent" : "Received";
            const dateInfo = formatDate(t.date);

            txDiv.innerHTML = `
                <div class="transaction-header">
                    <div class="transaction-info">
                        <div class="transaction-type">
                            <span class="type-icon">${typeIcon}</span>
                            <strong>${typeText} ${formatCurrency(t.amount)}</strong>
                            ${isReversed ? '<span class="reversed-label">Reversed</span>' : ''}
                        </div>
                        <div class="transaction-description">
                            ${t.description ? sanitizeHTML(t.description) : '<em>No description</em>'}
                        </div>
                    </div>
                    <div class="transaction-actions">
                        ${!isReversed ? `
                            <button class="reverse-btn" onclick="reverseTransaction(${t.id})"
                                    title="Reverse this transaction" aria-label="Reverse transaction">
                                ↩️ Reverse
                            </button>
                        ` : ''}
                    </div>
                </div>
                <div class="transaction-footer">
                    <small class="transaction-date" title="${dateInfo.date} at ${dateInfo.time}">
                        🕒 ${dateInfo.relative}
                    </small>
                    <small class="transaction-id">ID: ${t.id}</small>
                </div>
            `;

            txContainer.appendChild(txDiv);
        });

        // Add click handler for toggle
        personHeader.addEventListener('click', () => toggleTransactions(safeId));

        personDiv.appendChild(personHeader);
        personDiv.appendChild(txContainer);
        container.appendChild(personDiv);

        // Auto-expand if this was the last active person
        if (AppState.lastActivePerson === personName) {
            setTimeout(() => toggleTransactions(safeId, true), 100);
        }
    });
}

// Enhanced toggle transactions with smooth animation
function toggleTransactions(safeId, autoScroll = false) {
    const txDiv = document.getElementById(`tx-${safeId}`);
    const personHeader = document.querySelector(`[data-person="${safeId}"]`);
    const indicator = personHeader?.querySelector(".toggle-indicator");

    if (!txDiv || !indicator) return;

    const isCurrentlyHidden = txDiv.style.display === "none" || txDiv.style.display === "";

    if (isCurrentlyHidden) {
        // Close all other sections
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

        // Open this section
        txDiv.style.display = "block";
        indicator.textContent = "−";

        if (autoScroll) {
            setTimeout(() => {
                personHeader.scrollIntoView({ behavior: "smooth", block: "start" });
            }, 200);
        }
    } else {
        // Close this section
        txDiv.style.display = "none";
        indicator.textContent = "+";
    }
}

// Reverse transaction with confirmation
async function reverseTransaction(id) {
    const transaction = AppState.transactions.find(t => t.id === id);
    if (!transaction) return;

    const personName = transaction.person?.name || transaction.personName || "Unknown";
    const typeText = transaction.type?.toUpperCase() === "SEND" ? "sent to" : "received from";

    showConfirmModal(
        "Reverse Transaction",
        `Reverse ${formatCurrency(transaction.amount)} ${typeText} ${personName}?`,
        async () => {
            try {
                const response = await authFetch(
                    `${API_ENDPOINTS.TRANSACTIONS}/reverse/${id}`,
                    { method: "POST" }
                );

                if (!response.ok) {
                    throw new Error(await response.text());
                }

                showNotification("🔄 Transaction reversed successfully!", "success");

                // Reload data
                await Promise.all([loadPeople(), loadTransactions()]);

            } catch (error) {
                showNotification(`❌ Failed to reverse transaction: ${error.message}`, "error");
            }
        }
    );
}

// ===== SEARCH AND FILTER FUNCTIONALITY =====

// Setup search and filter handlers
function setupSearchAndFilters() {
    // People search
    const peopleSearch = document.getElementById("peopleSearch");
    if (peopleSearch) {
        peopleSearch.addEventListener('input', debounce(filterPeople, CONFIG.UI.DEBOUNCE_DELAY));
    }

    // Transaction search and filter
    const transactionSearch = document.getElementById("transactionSearch");
    const transactionFilter = document.getElementById("transactionFilter");

    if (transactionSearch) {
        transactionSearch.addEventListener('input', debounce(filterTransactions, CONFIG.UI.DEBOUNCE_DELAY));
    }

    if (transactionFilter) {
        transactionFilter.addEventListener('change', filterTransactions);
    }
}

// Filter people table
function filterPeople() {
    const searchTerm = document.getElementById("peopleSearch")?.value?.toLowerCase() || "";
    const rows = document.querySelectorAll("#peopleList tr[data-person]");

    let visibleCount = 0;
    rows.forEach(row => {
        const personName = row.getAttribute("data-person").toLowerCase();
        const isVisible = personName.includes(searchTerm);
        row.style.display = isVisible ? "" : "none";
        if (isVisible) visibleCount++;
    });

    // Show/hide empty state
    const emptyState = document.querySelector("#peopleList .empty-row");
    if (visibleCount === 0 && searchTerm && !emptyState) {
        const tbody = document.getElementById("peopleList");
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="4">
                    <div class="empty-state">
                        <span class="empty-icon">🔍</span>
                        <p>No people match "${searchTerm}"</p>
                    </div>
                </td>
            </tr>
        `;
    } else if (visibleCount > 0 && emptyState) {
        loadPeople(); // Reload to show actual data
    }
}

// Filter transactions
function filterTransactions() {
    updateTransactionsList(AppState.transactions);
}

// ===== ENHANCED UI FEATURES =====

// Character counter for description fields
function setupCharacterCounters() {
    const fields = [
        { input: 'sendDesc', counter: 'sendDescCount' },
        { input: 'receiveDesc', counter: 'receiveDescCount' }
    ];

    fields.forEach(({ input, counter }) => {
        const inputField = document.getElementById(input);
        const counterField = document.getElementById(counter);

        if (inputField && counterField) {
            inputField.addEventListener('input', () => {
                const length = inputField.value.length;
                const maxLength = CONFIG.VALIDATION.MAX_DESCRIPTION_LENGTH;
                counterField.textContent = `${length}/${maxLength}`;
                counterField.className = length > maxLength * 0.9 ? 'char-count warning' : 'char-count';
            });
        }
    });
}

// Update statistics dashboard
function updateStats() {
    const stats = {
        totalPeople: AppState.people.length,
        totalTransactions: AppState.transactions.filter(t => !t.reversed).length,
        totalSent: AppState.transactions
            .filter(t => t.type?.toUpperCase() === "SEND" && !t.reversed)
            .reduce((sum, t) => sum + t.amount, 0),
        totalReceived: AppState.transactions
            .filter(t => t.type?.toUpperCase() === "RECEIVE" && !t.reversed)
            .reduce((sum, t) => sum + t.amount, 0)
    };

    const elements = {
        totalPeople: document.getElementById("totalPeople"),
        totalTransactions: document.getElementById("totalTransactions"),
        totalSentStat: document.getElementById("totalSentStat"),
        totalReceivedStat: document.getElementById("totalReceivedStat")
    };

    Object.keys(elements).forEach(key => {
        if (elements[key]) {
            const value = key.includes('Sent') || key.includes('Received')
                ? formatCurrency(stats[key])
                : stats[key].toLocaleString();
            elements[key].textContent = value;
        }
    });
}

// ===== MODAL SYSTEM =====

// Show confirmation modal
function showConfirmModal(title, message, onConfirm) {
    const modal = document.getElementById("confirmModal");
    const titleEl = document.getElementById("confirmTitle");
    const messageEl = document.getElementById("confirmMessage");
    const confirmBtn = document.getElementById("confirmBtn");

    if (!modal) return;

    titleEl.textContent = title;
    messageEl.textContent = message;

    // Remove existing listeners
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

    // Add new listener
    newConfirmBtn.addEventListener('click', () => {
        onConfirm();
        closeModal();
    });

    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");
    newConfirmBtn.focus();
}

// Close modal
function closeModal() {
    const modal = document.getElementById("confirmModal");
    if (modal) {
        modal.style.display = "none";
        modal.setAttribute("aria-hidden", "true");
    }
}

// ===== DATA EXPORT FUNCTIONALITY =====

async function exportData() {
    try {
        const data = {
            people: AppState.people,
            transactions: AppState.transactions,
            exportDate: new Date().toISOString(),
            version: "1.0"
        };

        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = `money-tracker-export-${new Date().toISOString().split('T')[0]}.json`;
        a.click();

        URL.revokeObjectURL(url);
        showNotification("📊 Data exported successfully!", "success");

    } catch (error) {
        showNotification(`❌ Export failed: ${error.message}`, "error");
    }
}

// ===== CONNECTION STATUS MONITORING =====

function setupConnectionMonitoring() {
    const statusEl = document.getElementById("connectionStatus");
    if (!statusEl) return;

    function updateConnectionStatus() {
        const isOnline = navigator.onLine;
        AppState.update('connectionOnline', isOnline);

        const indicator = statusEl.querySelector(".status-indicator");
        const text = statusEl.querySelector(".status-text");

        if (isOnline) {
            statusEl.className = "connection-status online";
            indicator.textContent = "●";
            text.textContent = "Connected";
        } else {
            statusEl.className = "connection-status offline";
            indicator.textContent = "●";
            text.textContent = "Offline";
            showNotification("⚠️ Connection lost. Some features may not work.", "warning");
        }
    }

    window.addEventListener('online', updateConnectionStatus);
    window.addEventListener('offline', updateConnectionStatus);
    updateConnectionStatus();
}

// ===== THEME MANAGEMENT =====

function setupThemeManager() {
    // Apply saved theme immediately
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
    }

    // Setup theme toggle
    const toggleBtn = document.getElementById('darkToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const isDark = document.body.classList.toggle('dark-mode');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');

            // Update button text
            toggleBtn.innerHTML = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';

            showNotification(`${isDark ? '🌙' : '☀️'} ${isDark ? 'Dark' : 'Light'} mode enabled`, "info", 2000);
        });

        // Set initial button text
        const isDark = document.body.classList.contains('dark-mode');
        toggleBtn.innerHTML = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';
    }
}

// ===== KEYBOARD SHORTCUTS =====

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Skip if user is typing in an input
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
            return;
        }

        // Global shortcuts
        if (e.altKey) {
            switch (e.key.toLowerCase()) {
                case 'p':
                    e.preventDefault();
                    document.getElementById('personName')?.focus();
                    break;
                case 's':
                    e.preventDefault();
                    document.getElementById('sendName')?.focus();
                    break;
                case 'r':
                    e.preventDefault();
                    document.getElementById('receiveName')?.focus();
                    break;
                case 'f':
                    e.preventDefault();
                    document.getElementById('peopleSearch')?.focus();
                    break;
            }
        }

        // ESC to close modals
        if (e.key === 'Escape') {
            closeModal();
        }
    });
}

// ===== INITIALIZATION =====

document.addEventListener("DOMContentLoaded", async () => {
    try {
        // Initialize theme immediately
        setupThemeManager();

        // Setup connection monitoring
        setupConnectionMonitoring();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup form event listeners
        const loginForm = document.getElementById("loginForm");
        if (loginForm) {
            loginForm.addEventListener("submit", login);

            // Auto-fill remembered username
            const rememberedUser = localStorage.getItem("rememberedUser");
            if (rememberedUser) {
                const usernameField = document.getElementById("username");
                if (usernameField) {
                    usernameField.value = rememberedUser;
                    document.getElementById("rememberMe").checked = true;
                }
            }
        }

        const registerForm = document.getElementById("registerForm");
        if (registerForm) {
            registerForm.addEventListener("submit", register);
        }

        // Dashboard-specific initialization
        if (document.body.classList.contains("dashboard")) {
            const token = getAuthToken();
            if (!token) {
                window.location.href = "login.html";
                return;
            }

            // Setup welcome message
            const username = sessionStorage.getItem("username");
            const welcomeEl = document.getElementById("welcomeUser");
            if (welcomeEl && username) {
                welcomeEl.textContent = `Welcome, ${username}!`;
            }

            // Setup search and filters
            setupSearchAndFilters();

            // Setup character counters
            setupCharacterCounters();

            // Load initial data
            setGlobalLoading(true);

            try {
                await Promise.all([
                    loadPeople(),
                    loadTransactions()
                ]);
            } catch (error) {
                console.error("Failed to load initial data:", error);
                showNotification("⚠️ Failed to load some data. Please refresh the page.", "warning");
            } finally {
                setGlobalLoading(false);
            }
        }

        // Setup modal click-outside-to-close
        const modal = document.getElementById("confirmModal");
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    closeModal();
                }
            });
        }

    } catch (error) {
        console.error("Initialization error:", error);
        showNotification("⚠️ Application failed to initialize properly", "error");
    }
});

// ===== ERROR HANDLING =====

// Global error handler
window.addEventListener('error', (e) => {
    console.error('Global error:', e.error);
    showNotification("⚠️ An unexpected error occurred", "error");
});

// Unhandled promise rejection handler
window.addEventListener('unhandledrejection', (e) => {
    console.error('Unhandled promise rejection:', e.reason);
    showNotification("⚠️ An unexpected error occurred", "error");
    e.preventDefault();
});

// ===== PERFORMANCE MONITORING =====

// Simple performance monitoring
if ('performance' in window) {
    window.addEventListener('load', () => {
        setTimeout(() => {
            const loadTime = performance.now();
            console.log(`App loaded in ${loadTime.toFixed(2)}ms`);

            if (loadTime > 3000) {
                console.warn('Slow loading detected');
            }
        }, 0);
    });
}