class OfferingManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        this.offerings = [];

        // Check authentication
        if (!this.token || !this.user) {
            window.location.href = BASE_URL;
            return;
        }

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadOfferings();
    }

    getStoredUser() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }

    setupEventListeners() {
        // Navigation
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', this.logout.bind(this));
        }

        // Redirect to new page instead of modal
        const addOfferingBtn = document.getElementById('addOfferingBtn');
        if (addOfferingBtn) {
            addOfferingBtn.addEventListener('click', () => {
                window.location.href = './offering-crud.html';
            });
        }
    }

    async loadOfferings() {
        const loadingState = document.getElementById('loadingState');
        if (loadingState) {
            loadingState.classList.remove('hidden');
        }

        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                this.offerings = await response.json();
                this.renderOfferings();
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al cargar servicios');
            }
        } catch (error) {
            console.error('Error loading offerings:', error);
            this.showAlert('Error al cargar los servicios', 'error');
            this.offerings = [];
            this.renderOfferings();
        } finally {
            if (loadingState) {
                loadingState.classList.add('hidden');
            }
        }
    }

    renderOfferings() {
        const container = document.getElementById('offeringsContainer');
        const emptyState = document.getElementById('emptyState');

        if (!container) return;

        container.querySelectorAll('.offering-card').forEach(card => card.remove());

        if (this.offerings.length === 0) {
            if (emptyState) {
                emptyState.classList.remove('hidden');
            }
            return;
        }

        // Hide empty state
        if (emptyState) {
            emptyState.classList.add('hidden');
        }

        // Render offerings
        this.offerings.forEach(offering => {
            const card = this.createOfferingCard(offering);
            container.appendChild(card);
        });
    }

    createOfferingCard(offering) {
        const card = document.createElement('div');
        card.className = 'offering-card bg-white rounded-xl shadow-md border border-gray-200 p-6 hover:shadow-lg hover:border-indigo-600 transition-all duration-200 cursor-pointer';

        card.addEventListener('click', (e) => {
            if (e.target.closest('.delete-btn') || e.target.closest('.configure-btn')) {
                return;
            }
            offeringManager.editOffering(offering.id);
        });

        card.innerHTML = `
            <div class="flex justify-between items-start mb-4">
                <div class="flex items-center flex-1">
                    <div class="p-2 bg-indigo-100 rounded-lg">
                        <svg class="h-6 w-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c1.657 0 3 .895 3 2s-1.343 2-3 2-3 .895-3 2 1.343 2 3 2m0-8h.01M12 21a9 9 0 100-18 9 9 0 000 18z"></path>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <h3 class="text-xl font-bold text-gray-900">${this.escapeHtml(offering.name)}</h3>
                    </div>
                </div>
                <div class="flex space-x-2">
                    <button onclick="offeringManager.deleteOffering('${offering.id}')" class="delete-btn p-2 text-gray-400 hover:text-red-600 transition-colors duration-200" title="Eliminar Servicio">
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </div>

            <p class="text-gray-600 mb-4 whitespace-pre-line">
                ${this.escapeHtml(offering.description || '')}
            </p>

            <div class="space-y-4 border-t pt-4 border-gray-100">
                <div>
                    <p class="text-sm text-gray-500 font-medium">Capacidad Máx.</p>
                    <p class="text-lg font-bold text-gray-900">${offering.capacity} persona${offering.capacity !== 1 ? 's' : ''}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500 font-medium">Pago Anticipado (Seña)</p>
                    <p class="text-lg font-bold text-gray-700">
                        ${offering.advancePaymentPercentage > 0 ? `${offering.advancePaymentPercentage}% Requerido` : 'No Requerido (0%)'}
                    </p>
                </div>
            </div>
            <div class="flex pt-2">
                <button type="button" class="configure-btn flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors shadow-md">
                    Configurar Horarios
                </button>
            </div>
        `;

        const configureBtn = card.querySelector('.configure-btn');
        if (configureBtn) {
            configureBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                window.location.href = `./time-slots.html?offeringId=${offering.id}`;
            });
        }

        return card;
    }

    editOffering(offeringId) {
        // Redirect to new page in edit mode
        window.location.href = `./offering-crud.html?id=${offeringId}`;
    }

    async deleteOffering(offeringId) {
        const offering = this.offerings.find(o => o.id === offeringId);
        if (!offering) return;

        const result = await Swal.fire({
            title: '¿Estás seguro?',
            text: `Se eliminará el servicio: ${this.escapeHtml(offering.name)}. Esta acción no se puede deshacer.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc2626',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        });

        if (result.isConfirmed) {
            try {
                const response = await fetch(
                    `${this.baseUrl}/users/${this.user.id}/offerings/${offering.id}`,
                    {
                        method: 'DELETE',
                        headers: {
                            'Authorization': `Bearer ${this.token}`,
                            'Content-Type': 'application/json'
                        }
                    }
                );

                if (response.ok) {
                    this.showAlert('Servicio eliminado correctamente', 'success');
                    await this.loadOfferings();
                    return;
                }

                if (response.status === 401) {
                    return this.handleUnauthorized();
                }

                let errorBody = {};
                try {
                    errorBody = await response.json();
                } catch (e) {
                    console.warn("Could not parse error body as JSON");
                }

                const { errorCode, details, message } = errorBody ?? {};

                if (errorCode === 'OFFERING_HAS_ACTIVE_BOOKINGS') {
                    const count = details?.count ?? '?';
                    this.showAlert(`El servicio tiene ${count} reservas activas. Debe cancelarlas antes de eliminarlo.`, 'warning');
                    return;
                }

                this.showAlert(message || 'Error al eliminar el servicio', 'error');

            } catch (error) {
                console.error('Error deleting offering:', error);
                this.showAlert('Error al eliminar el servicio', 'error');
            }
        }
    }

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = this.baseUrl;
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = this.baseUrl;
    }

    showAlert(message, icon = 'info') {
        const swalIcon = {
            'success': 'success',
            'error': 'error',
            'warning': 'warning',
            'info': 'info'
        }[icon] || 'info';

        Swal.fire({
            icon: swalIcon,
            title: message,
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 4000,
            timerProgressBar: false,
            customClass: {
                container: 'sweetalert-container'
            }
        });
    }

    escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Global reference for onclick handlers
let offeringManager;

document.addEventListener('DOMContentLoaded', () => {
    offeringManager = new OfferingManager();
});
