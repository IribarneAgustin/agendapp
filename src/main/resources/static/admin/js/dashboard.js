class DashboardManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        
        if (!this.token || !this.user) {
            window.location.href = BASE_URL;
            return;
        }
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();

        // Critical Status Checks for UI Warnings
        this.loadSubscriptionStatus();
        this.loadMpConnectionStatus();

        this.setupShareableUrl();
        this.setupBookingDashboardLink();

        // Setup Mercado Pago Action Buttons
        this.setupMpConnectButton();
        this.setupMpUnlinkButton();
    }

    getStoredUser() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }

    setupEventListeners() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', this.logout.bind(this));
        }

        const toastClose = document.getElementById('toastClose');
        if (toastClose) {
            toastClose.addEventListener('click', this.hideToast.bind(this));
        }

        const copyUrlBtn = document.getElementById('copyUrlBtn');
        if (copyUrlBtn) {
            copyUrlBtn.addEventListener('click', this.copyShareableUrl.bind(this));
        }
    }

    async loadDashboardData() {
        try {
            await this.loadOfferingsStats();
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showToast('Error al cargar los datos del panel', 'error');
        }
    }

    // --- UI HELPER: GLOBAL ALERTS ---

    injectGlobalAlert(id, type, title, message, actionHtml = '') {
        const alertsContainer = document.getElementById('globalAlerts');
        if (!alertsContainer) return;

        // Prevent duplicate alerts
        if (document.getElementById(id)) return;

        const alertDiv = document.createElement('div');
        alertDiv.id = id;

        let colors = '';
        let icon = '';

        if (type === 'danger') {
            colors = 'bg-red-50 border-red-200 text-red-800';
            icon = `<svg class="h-6 w-6 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
        } else if (type === 'warning') {
            colors = 'bg-yellow-50 border-yellow-200 text-yellow-800';
            icon = `<svg class="h-6 w-6 text-yellow-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.664-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg>`;
        } else {
             colors = 'bg-blue-50 border-blue-200 text-blue-800';
             icon = `<svg class="h-6 w-6 text-blue-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 18a6 6 0 110-12 6 6 0 010 12z"></path></svg>`;
        }

        // Updated layout: Added more right padding (pr-10) to the container to clear the absolute close button.
        // Also adjusted the action area to ensure it doesn't push into the close button territory.
        alertDiv.className = `relative border-l-4 p-4 pr-10 rounded-r shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4 ${colors}`;

        alertDiv.innerHTML = `
            <div class="flex items-start md:items-center">
                <div class="flex-shrink-0">
                    ${icon}
                </div>
                <div>
                    <h3 class="font-bold text-sm sm:text-base">${title}</h3>
                    <p class="text-sm mt-1">${message}</p>
                </div>
            </div>
            <div class="w-full md:w-auto flex-shrink-0 flex items-center">
                ${actionHtml}
            </div>
            <button onclick="document.getElementById('${id}').remove()" class="absolute top-2 right-2 text-gray-400 hover:text-gray-600 p-1 focus:outline-none" title="Cerrar">
                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        `;

        alertsContainer.appendChild(alertDiv);
    }

    // --- SUBSCRIPTION LOGIC ---

    async loadSubscriptionStatus() {
        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/subscription`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const subscription = await response.json();
                this.handleSubscriptionAlerts(subscription);
            } else if (response.status === 401) {
                this.handleUnauthorized();
            }
        } catch (error) {
            console.error('Error loading subscription status:', error);
        }
    }

    handleSubscriptionAlerts(subscription) {
        let dateStr = "N/A";
        if (subscription.expiration) {
            const date = new Date(subscription.expiration);
            dateStr = date.toLocaleDateString();
        }

        const checkoutLink = subscription.checkoutLink;
        let actionBtn = '';
        if (checkoutLink) {
            actionBtn = `
                <a href="${checkoutLink}" target="_blank"
                   class="w-full md:w-auto justify-center inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
                   Renovar
                </a>
            `;
        }

        const title = "Estado de Suscripción";
        const message = `Tu suscripción vence el <strong>${dateStr}</strong>.`;

        if (subscription.expired) {
            this.injectGlobalAlert('sub-status', 'danger', '¡Suscripción Vencida!', `Tu servicio ha expirado el ${dateStr}. Renueva ahora.`, actionBtn);
        } else if (checkoutLink) {
             this.injectGlobalAlert('sub-status', 'warning', 'Próximo Vencimiento', `${message}`, actionBtn);
        } else {
             this.injectGlobalAlert('sub-status', 'info', 'Suscripción Activa', message);
        }
    }

    // --- MERCADO PAGO LOGIC ---

    async loadMpConnectionStatus() {
        const statusElement = document.getElementById('mpConnectionStatus');
        const connectBtn = document.getElementById('connectMpBtn');
        const unlinkBtn = document.getElementById('unlinkMpBtn');
        const mpCard = document.getElementById('mpCard');

        try {
            const response = await fetch(`${this.baseUrl}/mercadopago/oauth/status/${this.user.id}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();

                if (data.connected) {
                    if (statusElement) {
                        statusElement.innerHTML = `<svg class="h-4 w-4 mr-1 text-green-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg> Cuenta vinculada`;
                        statusElement.className = "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700 border border-green-300 mb-3";
                    }
                    if (connectBtn) connectBtn.classList.add('hidden');
                    if (unlinkBtn) unlinkBtn.classList.remove('hidden');
                    if (mpCard) mpCard.classList.remove("border-red-300");

                } else {
                    if (statusElement) {
                        statusElement.innerHTML = `<svg class="h-4 w-4 mr-1 text-red-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg> No vinculada`;
                        statusElement.className = "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-700 border border-red-300 mb-3";
                    }
                    if (connectBtn) connectBtn.classList.remove('hidden');
                    if (unlinkBtn) unlinkBtn.classList.add('hidden');
                    if (mpCard) mpCard.classList.add("border-red-300");

                    const scrollAction = `<button onclick="document.getElementById('mpCard').scrollIntoView({behavior: 'smooth'})" class="text-sm text-yellow-800 underline font-medium">Ir a vincular</button>`;
                    this.injectGlobalAlert('mp-missing', 'warning', 'Mercado Pago no conectado', 'Debes vincular tu cuenta para recibir pagos.', scrollAction);
                }
            }
        } catch (error) {
            console.error('Error checking MP status:', error);
        }
    }

    setupMpConnectButton() {
        const connectBtn = document.getElementById('connectMpBtn');
        if (!connectBtn) return;

        connectBtn.addEventListener('click', async () => {
            try {
                const response = await fetch(`${this.baseUrl}/mercadopago/oauth/link?userId=${this.user.id}`, {
                    headers: {
                        'Authorization': `Bearer ${this.token}`
                    }
                });
                if (response.ok) {
                    const { authUrl } = await response.json();
                    window.location.href = authUrl; // redirige al OAuth de MP
                } else {
                    this.showToast('No se pudo iniciar la vinculación con Mercado Pago', 'error');
                }
            } catch (err) {
                console.error(err);
                this.showToast('Error al conectar con Mercado Pago', 'error');
            }
        });
    }

    setupMpUnlinkButton() {
        const btn = document.getElementById('unlinkMpBtn');
        if (!btn) return;

        btn.addEventListener('click', async () => {
            const result = await Swal.fire({
                title: '¿Desvincular cuenta?',
                text: "Ya no podrás procesar pagos.",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                confirmButtonText: 'Sí, desvincular',
                cancelButtonText: 'Cancelar'
            });

            if (result.isConfirmed) {
                try {
                     const response = await fetch(`${this.baseUrl}/mercadopago/oauth/unlink/${this.user.id}`, {
                         method: 'DELETE',
                         headers: {
                             'Authorization': `Bearer ${this.token}`,
                             'Content-Type': 'application/json'
                         }
                     });
                     if (response.ok) {
                         Swal.fire('Desvinculado', 'La cuenta ha sido desvinculada.', 'success');
                         this.loadMpConnectionStatus();
                     } else {
                         this.showToast('Error al desvincular', 'error');
                     }
                } catch (e) {
                    this.showToast('Error de conexión', 'error');
                }
            }
        });
    }

    // --- OTHER DATA ---

    async loadOfferingsStats() {
        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                const offerings = await response.json();
                this.updateStats(offerings);
            } else if (response.status === 401) {
                this.handleUnauthorized();
            }
        } catch (error) {
            console.error('Error loading offerings:', error);
        }
    }

    updateStats(offerings) {}

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = BASE_URL;
    }

    async logout() {
        try {
            await fetch(BASE_URL + "/auth/logout", { method: "POST", credentials: "include" });
        } catch (e) {}
        localStorage.removeItem("authToken");
        localStorage.removeItem("userData");
        window.location.href = BASE_URL;
    }

    showToast(message, type = 'info') {
        const toast = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');
        if (!toast) return;

        toastMessage.textContent = message;
        toast.classList.remove('hidden');
        setTimeout(() => this.hideToast(), 5000);
    }

    hideToast() {
        const toast = document.getElementById('toast');
        if (toast) toast.classList.add('hidden');
    }

    async setupShareableUrl() {
        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/public-url`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${this.token}` }
            });
            if (response.ok) {
                const publicURL = await response.text();
                const shareableUrlElement = document.getElementById('shareableUrl');
                if (shareableUrlElement) shareableUrlElement.textContent = publicURL;
            }
        } catch (error) {}
    }

    setupBookingDashboardLink() {
        const link = document.getElementById('bookingDashboardLink');
        if (link) link.href = `booking-dashboard.html`;
    }

    async copyShareableUrl() {
        const shareableUrlElement = document.getElementById('shareableUrl');
        const copyBtn = document.getElementById('copyUrlBtn');
        if (shareableUrlElement) {
            const url = shareableUrlElement.textContent;
            const textarea = document.createElement('textarea');
            textarea.value = url;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            this.showToast('URL copiada', 'success');
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new DashboardManager();
});