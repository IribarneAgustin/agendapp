class SubscriptionManager {
    constructor() {
        this.baseUrl = typeof BASE_URL !== 'undefined' ? BASE_URL : '';
        this.init();
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.setupPageTexts();
            this.initDynamicPricing();
            this.bindEvents();
        });
    }

    setupPageTexts() {
        const titleEl = document.getElementById("page-title");
        const subtitleEl = document.getElementById("page-subtitle");
        if (titleEl) {
            titleEl.innerText = "Elegí tu Plan";
        }

        if (subtitleEl) {
            subtitleEl.innerText = "Seleccioná la opción que mejor se adapte a las necesidades de tu negocio.";
        }

    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return isNaN(date.getTime())
            ? "recientemente"
            : date.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    initDynamicPricing() {
        const BASE_PRICE = 29000;
        const PRICE_PER_PRO = 6000;
        const INCLUDED_PROS = 2;

        const slider = document.getElementById('pro-slider');
        const priceEl = document.getElementById('pro-price');
        const countEl = document.getElementById('pro-count');

        if (!slider) return;

        const update = () => {
            const pros = parseInt(slider.value);
            const extra = Math.max(0, pros - INCLUDED_PROS);
            const total = BASE_PRICE + (extra * PRICE_PER_PRO);

            if (priceEl) priceEl.textContent = total.toLocaleString('es-AR');
            if (countEl) countEl.textContent = pros;
        };

        slider.addEventListener('input', update);
        update();
    }

    bindEvents() {
        document.querySelectorAll('[data-plan]').forEach(button => {
            button.addEventListener('click', () => {
                const planCode = button.getAttribute('data-plan');
                this.handleCheckout(planCode);
            });
        });
    }

    async handleCheckout(planCode) {
        const authToken = localStorage.getItem('authToken');
        const userDataStorage = localStorage.getItem('userData');

        if (!authToken || !userDataStorage) {
            this.showError('No autorizado', 'Tu sesión ha expirado. Por favor, reingresá.');
            setTimeout(() => window.location.href = BASE_URL, 1500);
            return;
        }

        const { id: userId } = JSON.parse(userDataStorage);
        let selectedResources = 0;

        if (planCode === 'PROFESSIONAL') {
            const slider = document.getElementById('pro-slider');
            selectedResources = slider ? parseInt(slider.value) : 2;
        }

        const confirm = await Swal.fire({
            title: 'Confirmar selección',
            text: `Serás redirigido para completar el pago.`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#4f46e5',
            confirmButtonText: 'Ir a pagar',
            cancelButtonText: 'Cancelar'
        });

        if (!confirm.isConfirmed) return;

        Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

        try {
            const response = await fetch(`${this.baseUrl}/subscription/${planCode}/user/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify({ selectedResources })
            });

            if (!response.ok) throw new Error('Error al generar la orden de pago.');

            const checkoutUrl = await response.text();
            window.location.href = checkoutUrl;

        } catch (e) {
            this.showError('Error', e.message);
        }
    }

    showError(title, message) {
        Swal.fire({ title, text: message, icon: 'error', confirmButtonColor: '#4f46e5' });
    }
}

const subscriptionManager = new SubscriptionManager();