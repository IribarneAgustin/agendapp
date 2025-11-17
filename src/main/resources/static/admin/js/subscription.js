class SubscriptionManager {
    constructor() {
        // BASE_URL is assumed to be defined globally via js/config.js
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        this.subscriptionId = null;
        this.currentSubscription = null;

        if (!this.token || !this.user) {
            // Redirect to login if not authenticated
            window.location.href = BASE_URL;
            return;
        }

        this.init();
    }

    getStoredUser() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }

    init() {
        this.setupEventListeners();
        this.loadSubscriptionStatus();
    }

    setupEventListeners() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.handleUnauthorized());
        }

        const cancelBtn = document.getElementById('cancelSubscriptionBtn');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', this.confirmCancellation.bind(this));
        }

        // The Subscribe button (mpButtonBackend) is now a simple anchor tag.
        // Its 'href' is set dynamically in renderSubscriptionStatus.
    }

    // --- LÓGICA DE CARGA Y VISUALIZACIÓN ---
    async loadSubscriptionStatus() {
        document.getElementById('loading').classList.remove('hidden');
        document.getElementById('subscriptionContent').classList.add('hidden');

        try {
            // Llama a tu backend para obtener el estado de la suscripción
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/subscription`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const subscription = await response.json();
                this.currentSubscription = subscription;
                // Use the MP ID for cancellation purposes
                this.subscriptionId = subscription.mercadopagoPreapprovalId || null;
                this.renderSubscriptionStatus(subscription);
            } else if (response.status === 404) {
                // Usuario sin suscripción activa o expirada (backend returned 404)
                this.renderSubscriptionStatus(null);
            } else {
                throw new Error('Error al consultar el estado: ' + response.statusText);
            }

        } catch (error) {
            console.error('Error loading subscription status:', error);
            Swal.fire('Error', 'No se pudo cargar el estado de la suscripción. Intenta más tarde.', 'error');
            this.renderSubscriptionStatus(null);
        } finally {
            document.getElementById('loading').classList.add('hidden');
            document.getElementById('subscriptionContent').classList.remove('hidden');
        }
    }

    /**
     * Helper function to control the visual state of a button (enabled/disabled).
     * @param {HTMLElement} buttonElement
     * @param {boolean} isEnabled
     */
    setButtonState(buttonElement, isEnabled) {
        if (!buttonElement) return;
        buttonElement.disabled = !isEnabled;
        if (isEnabled) {
            buttonElement.classList.remove('opacity-50', 'cursor-not-allowed');
            buttonElement.classList.add('hover:shadow-lg');
        } else {
            buttonElement.classList.add('opacity-50', 'cursor-not-allowed');
            buttonElement.classList.remove('hover:shadow-lg');
        }
    }

    renderSubscriptionStatus(subscription) {
        const statusCard = document.getElementById('statusCard');
        const currentStatus = document.getElementById('currentStatus');
        const creationDate = document.getElementById('creationDate');
        const expirationDate = document.getElementById('expirationDate');
        const statusIcon = document.getElementById('statusIcon');

        // Buttons
        const subscribeLink = document.getElementById('mpButtonBackend');
        const cancelBtn = document.getElementById('cancelSubscriptionBtn');
        const cancelAccessDate = document.getElementById('cancelAccessDate');

        const isSubscriptionValid = subscription && !subscription.expired;

        if (isSubscriptionValid) {
            // Estado: ACTIVA
            statusCard.className = 'p-6 rounded-xl mb-8 bg-green-100 border-2 border-green-400';
            currentStatus.textContent = 'ACTIVA';
            currentStatus.className = 'ml-2 font-bold text-green-700';
            statusIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>';

            // Formatear fechas
            creationDate.textContent = new Date(subscription.creationDateTime).toLocaleDateString();
            const expDateString = new Date(subscription.expiration).toLocaleDateString();
            expirationDate.textContent = expDateString;
            cancelAccessDate.textContent = expDateString;

            // --- Button Logic (Active) ---
            // Subscribe button: Disabled (already active)
            this.setButtonState(subscribeLink, false);
            subscribeLink.removeAttribute('href');

            // Cancel button: Enabled (can be cancelled)
            this.setButtonState(cancelBtn, true);

        } else {
            // Estado: INACTIVA / EXPIRADA / NO ENCONTRADA (404)
            statusCard.className = 'p-6 rounded-xl mb-8 bg-red-100 border-2 border-red-400';
            currentStatus.textContent = 'INACTIVA';
            currentStatus.className = 'ml-2 font-bold text-red-700';
            statusIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path>';

            creationDate.textContent = 'N/A';
            expirationDate.textContent = subscription ? 'Tu acceso ha expirado' : 'No tienes una suscripción activa';
            cancelAccessDate.textContent = 'N/A';

            // --- Button Logic (Inactive/Expired) ---
            // Subscribe button: Enabled (needs activation/renewal)
            this.setButtonState(subscribeLink, true);
            // Set the pre-created checkout link from the response
            if (subscription && subscription.checkoutLink) {
                 subscribeLink.href = subscription.checkoutLink;
            } else {
                 // Fallback if checkoutLink is missing (should not happen in new approach)
                 subscribeLink.removeAttribute('href');
                 this.setButtonState(subscribeLink, false);
            }

            // Cancel button: Disabled (nothing to cancel)
            this.setButtonState(cancelBtn, false);
        }

        // Handle possible PENDING status, though it doesn't change button enablement here
        // const pendingPanel = document.getElementById('pendingPanel');
        // if (subscription && subscription.status === 'PENDING') { pendingPanel.classList.remove('hidden'); }
    }


    // --- LÓGICA DE CANCELACIÓN (Mercado Pago y Backend) ---
    async confirmCancellation() {
        if (!this.subscriptionId) {
            Swal.fire('Error', 'No se encontró un ID de suscripción para cancelar.', 'error');
            return;
        }

        const result = await Swal.fire({
            title: '¿Estás seguro de cancelar?',
            text: "El servicio seguirá activo hasta la fecha de expiración indicada. No se realizarán más cobros recurrentes.",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, Cancelar Suscripción',
            cancelButtonText: 'Mantener Activa'
        });

        if (result.isConfirmed) {
            this.executeCancellation();
        }
    }

    async executeCancellation() {
        const cancelBtn = document.getElementById('cancelSubscriptionBtn');
        cancelBtn.textContent = 'Cancelando...';
        cancelBtn.disabled = true;

        try {
            // Llama a tu backend para ejecutar el PUT a Mercado Pago
            const response = await fetch(`${this.baseUrl}/subscription/${this.subscriptionId}/cancel`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ userId: this.user.id }) // Por seguridad, enviamos el userId
            });

            if (response.ok) {
                Swal.fire('¡Suscripción Cancelada!', 'El débito automático ha sido detenido. Tu acceso continuará hasta el final del ciclo actual.', 'success').then(() => {
                    this.loadSubscriptionStatus(); // Recargar el estado para reflejar la cancelación
                });
            } else {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error desconocido al cancelar la suscripción.');
            }
        } catch (error) {
            console.error('Cancellation Error:', error);
            Swal.fire('Error', error.message || 'Hubo un error al procesar la cancelación. Intenta más tarde.', 'error');
        } finally {
            cancelBtn.textContent = 'Cancelar Suscripción';
            cancelBtn.disabled = false;
        }
    }

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = BASE_URL;
    }
}

document.addEventListener('DOMContentLoaded', () => {
        new SubscriptionManager();
    }
});
