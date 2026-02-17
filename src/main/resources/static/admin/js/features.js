class FeaturesManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();

        this.FEATURE_NAME = 'WHATSAPP_NOTIFICATIONS';
        this.currentFeatureData = null;

        if (!this.user) {
            console.error('No user found');
            window.location.href = '/';
            return;
        }

        this.init();
    }

    async init() {
        this.handlePaymentResult();
        await this.checkCurrentStatus();
        this.setupEventListeners();
    }

    getStoredUser() {
        try {
            return JSON.parse(localStorage.getItem('userData'));
        } catch {
            return null;
        }
    }

    setupEventListeners() {
        document.querySelectorAll('.whatsapp-pack-btn').forEach(btn => {
            btn.addEventListener('click', e => {
                const limit = parseInt(e.target.dataset.limit, 10);

                if (this.currentFeatureData?.id) {
                    this.confirmUpdate(limit);
                } else {
                    this.confirmCreate(limit);
                }
            });
        });

        const deactivateBtn = document.getElementById('btn-deactivate');
        if (deactivateBtn) {
            deactivateBtn.addEventListener('click', () => this.confirmDeactivation());
        }
        const changeBtn = document.getElementById('btn-change-pack');
        if (changeBtn) {
            changeBtn.addEventListener('click', () => {
                document.getElementById('active-state').classList.add('hidden');
                document.getElementById('selection-state').classList.remove('hidden');
                // Scroll to top to see options
                document.getElementById('selection-state').scrollIntoView({ behavior: 'smooth' });
            });
        }
    }

    // ---------- API ----------

    async checkCurrentStatus() {
        this.toggleLoading(true);

        try {
            const response = await fetch(
                `${this.baseUrl}/user/${this.user.id}/subscription-feature`,
                { headers: this.getHeaders() }
            );

            if (!response.ok) return;

            const featureUsages = await response.json();

            // Filter ACTIVE + enabled
            const activeFeatures = featureUsages
                .filter(f =>
                    f.enabled === true &&
                    f.featureStatus === 'ACTIVE'
                )
                // ensure latest if multiple
                .sort((a, b) => (b.id || '').localeCompare(a.id || ''));

            this.currentFeatureData = activeFeatures.length > 0
                ? activeFeatures[0]
                : null;

            this.renderUI();

        } catch (e) {
            console.error(e);
        } finally {
            this.toggleLoading(false);
        }
    }

    async createFeature(limit) {
        try {
            const response = await fetch(
                `${this.baseUrl}/user/${this.user.id}/subscription-feature`,
                {
                    method: 'POST',
                    headers: this.getHeaders(),
                    body: JSON.stringify({
                        name: this.FEATURE_NAME,
                        usageLimit: limit
                    })
                }
            );

            const raw = await response.text();
            let data = raw ? JSON.parse(raw) : null;

            if (!response.ok) {
                if (data?.errorCode === "FEATURE_ALREADY_EXISTS") {
                    throw new Error("La funcionalidad ya está activa en tu suscripción.");
                }
                throw new Error(data?.message || "Error inesperado");
            }

            return data;

        } catch (e) {
            return { error: e.message };
        }
    }

    async updateFeature(limit) {
        if (!this.currentFeatureData?.id) return;

        try {
            const response = await fetch(
                `${this.baseUrl}/user/${this.user.id}/subscription-feature/${this.currentFeatureData.id}`,
                {
                    method: 'PUT',
                    headers: this.getHeaders(),
                    body: JSON.stringify({
                        name: this.FEATURE_NAME,
                        usageLimit: limit
                    })
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data?.message || 'Error actualizando la funcionalidad');
            }

            return data;

        } catch (e) {
            return { error: e.message };
        }
    }

    async deleteFeature() {
        if (!this.currentFeatureData?.id) return;

        try {
            const response = await fetch(
                `${this.baseUrl}/user/${this.user.id}/subscription-feature/${this.currentFeatureData.id}`,
                {
                    method: 'DELETE',
                    headers: this.getHeaders()
                }
            );

            if (!response.ok) {
                throw new Error('Error eliminando la funcionalidad');
            }

            return { success: true };

        } catch (e) {
            return { error: e.message };
        }
    }

    getHeaders() {
        return {
            'Authorization': `Bearer ${this.token}`,
            'Content-Type': 'application/json'
        };
    }

    // ---------- UI ----------

    renderUI() {
        const selectionState = document.getElementById('selection-state');
        const activeState = document.getElementById('active-state');
        const pendingState = document.getElementById('pending-state');

        // New Elements for Dashboard
        const planName = document.getElementById('plan-name-display');
        const usageCount = document.getElementById('usage-count-display');
        const limitCount = document.getElementById('limit-count-display');
        const percentageDisplay = document.getElementById('percentage-display');
        const progressBar = document.getElementById('usage-progress-bar');

        if (!selectionState || !activeState) return;

        // Reset visibility
        selectionState.classList.add('hidden');
        activeState.classList.add('hidden');
        if (pendingState) pendingState.classList.add('hidden');

        // No active feature -> Show Pricing Grid
        if (!this.currentFeatureData) {
            selectionState.classList.remove('hidden');
            // Ensure buttons look actionable
            document.querySelectorAll('.whatsapp-pack-btn').forEach(btn => {
                btn.innerText = "Elegir Pack";
                btn.disabled = false;
                btn.classList.remove('opacity-50', 'cursor-not-allowed');
            });
            return;
        }

        // PENDING Status
        if (this.currentFeatureData.featureStatus === 'PENDING') {
            if (pendingState) pendingState.classList.remove('hidden');
            return;
        }

        // ACTIVE Status -> Show Dashboard
        if (this.currentFeatureData.enabled) {
            activeState.classList.remove('hidden');

            const limit = this.currentFeatureData.usageLimit || 0;
            const usage = this.currentFeatureData.usage || 0;

            // Calculate Percentage (Cap at 100%)
            let percent = limit > 0 ? (usage / limit) * 100 : 0;
            if (percent > 100) percent = 100;

            // Update Text
            if(planName) planName.innerText = `Pack ${limit}`;
            if(usageCount) usageCount.innerText = usage;
            if(limitCount) limitCount.innerText = limit;
            if(percentageDisplay) percentageDisplay.innerText = `${Math.round(percent)}%`;

            // Update Bar Color based on usage
            if (progressBar) {
                progressBar.style.width = `${percent}%`;

                // Color logic: <70% Indigo, >70% Yellow, >90% Red
                progressBar.classList.remove('bg-yellow-500', 'bg-red-500', 'from-indigo-500', 'to-indigo-600');

                if (percent >= 90) {
                     progressBar.classList.add('bg-red-500');
                } else if (percent >= 75) {
                     progressBar.classList.add('bg-yellow-500');
                } else {
                     progressBar.classList.add('bg-gradient-to-r', 'from-indigo-500', 'to-indigo-600');
                }
            }

            // Also highlight the active pack in the grid (background logic for later)
            this.highlightActivePackInGrid();
            return;
        }

        // Fallback
        selectionState.classList.remove('hidden');
    }

    toggleLoading(isLoading) {
        const loader = document.getElementById('loading-state');
        if (!loader) return;
        loader.classList.toggle('hidden', !isLoading);
    }

    // ---------- FLOWS ----------

    async confirmCreate(limit) {
        const result = await Swal.fire({
            title: 'Activar WhatsApp',
            text: `Vas a contratar el pack de ${limit} mensajes mensuales.`,
            icon: 'info',
            showCancelButton: true,
            confirmButtonColor: '#4f46e5',
            cancelButtonColor: '#d1d5db',
            confirmButtonText: 'Sí, ir al pago',
            cancelButtonText: 'Cancelar'
        });

        if (!result.isConfirmed) return;

        const response = await this.createFeature(limit);

        if (response?.premiumFeatureCheckoutURL) {
            window.location.href = response.premiumFeatureCheckoutURL;
            return;
        }

        Swal.fire('Error', response?.error || 'Ocurrió un error', 'error');
    }

    async confirmUpdate(limit) {
        // Prevent selecting the same plan
        if (this.currentFeatureData && this.currentFeatureData.usageLimit === limit) {
             Swal.fire('Plan Actual', 'Ya tenés activo este plan.', 'info');
             return;
        }

        const result = await Swal.fire({
            title: 'Cambiar de Plan',
            text: `¿Querés cambiar tu plan actual al pack de ${limit} mensajes?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#4f46e5',
            cancelButtonColor: '#d1d5db',
            confirmButtonText: 'Sí, cambiar plan',
            cancelButtonText: 'Cancelar'
        });

        if (!result.isConfirmed) return;

        const response = await this.updateFeature(limit);

        if (response?.premiumFeatureCheckoutURL) {
            window.location.href = response.premiumFeatureCheckoutURL;
            return;
        }

        Swal.fire('Error', response?.error || 'Ocurrió un error', 'error');
    }

    async confirmDeactivation() {
        const result = await Swal.fire({
            title: '¿Cancelar suscripción?',
            text: 'Se dejarán de enviar recordatorios automáticos por WhatsApp y perderás los mensajes restantes del mes.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc2626',
            cancelButtonColor: '#d1d5db',
            confirmButtonText: 'Sí, cancelar',
            cancelButtonText: 'Volver'
        });

        if (!result.isConfirmed) return;

        const response = await this.deleteFeature();

        if (response.success) {
            await Swal.fire('Cancelado', 'El servicio de WhatsApp se ha desactivado.', 'success');
            // Reload state
            this.currentFeatureData = null;
            await this.checkCurrentStatus();
            return;
        }

        Swal.fire('Error', response.error || 'Ocurrió un error', 'error');
    }

    handlePaymentResult() {
        const params = new URLSearchParams(window.location.search);
        const successParam = params.get('success');

        if (successParam === null) return;

        // Clean URL
        window.history.replaceState({}, document.title, window.location.pathname);

        if (successParam === 'true') {
            Swal.fire({
                title: '¡Todo listo!',
                text: 'El pack de mensajes ya está activo.',
                icon: 'success',
                confirmButtonColor: '#4f46e5'
            });
        } else {
            Swal.fire({
                title: 'Pago no completado',
                text: 'Hubo un problema con el pago. Por favor intentá nuevamente.',
                icon: 'error'
            });
        }
    }

    // Helper to visually update the grid even if hidden, just in case
    highlightActivePackInGrid() {
        if (!this.currentFeatureData) return;
        const activeLimit = this.currentFeatureData.usageLimit;

        document.querySelectorAll('.premium-card').forEach(card => {
            const btn = card.querySelector('.whatsapp-pack-btn');
            const limit = parseInt(card.dataset.pack, 10);

            if (limit === activeLimit) {
                card.classList.add('ring-2', 'ring-indigo-500', 'bg-indigo-50');
                if(btn) {
                    btn.innerText = "Plan Actual";
                    btn.disabled = true;
                    btn.classList.add('opacity-50', 'cursor-not-allowed');
                }
            } else {
                card.classList.remove('ring-2', 'ring-indigo-500', 'bg-indigo-50');
                if(btn) {
                    btn.disabled = false;
                    btn.classList.remove('opacity-50', 'cursor-not-allowed');
                }
            }
        });
    }
}

// ---------- BOOT ----------

document.addEventListener('DOMContentLoaded', () => {
    new FeaturesManager();
});