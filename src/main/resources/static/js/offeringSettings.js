class OfferingManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        this.offerings = [];
        this.currentOffering = null;
        this.currentOfferingId = null;
        this.isEditMode = false;

        // Check authentication
        if (!this.token || !this.user) {
            window.location.href = 'index.html';
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

        // Modal controls
        const addOfferingBtn = document.getElementById('addOfferingBtn');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const cancelBtn = document.getElementById('cancelBtn');
        const offeringForm = document.getElementById('offeringForm');
        const configureBtn = document.getElementById("configureSlotsBtn");
        const offeringModal = document.getElementById('offeringModal');

        if (addOfferingBtn) {
            addOfferingBtn.addEventListener('click', () => this.openModal());
        }
        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => this.closeModal());
        }
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => this.closeModal());
        }
        if (offeringForm) {
            offeringForm.addEventListener('submit', this.handleFormSubmit.bind(this));
        }

        // Close modal when clicking backdrop
        if (offeringModal) {
            offeringModal.addEventListener('click', (e) => {
                if (e.target === offeringModal) {
                    this.closeModal();
                }
            });
        }

        // Configure button logic
        if (configureBtn) {
            configureBtn.addEventListener("click", () => {
                const serviceId = this.currentOfferingId;

                if (!serviceId) {
                    this.showAlert("Debes guardar el servicio antes de configurar horarios.", 'warning');
                    return;
                }

                window.location.href = `/time-slots.html?offeringId=${serviceId}`;
            });
        }

        // Price removal: no need to track capacity/advance inputs here, but they exist
    }


    async loadOfferings() {
        // Show loading state
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

    // REFACTORED: Layout changed to stacked and yellow color removed
    createOfferingCard(offering) {
        const card = document.createElement('div');
        card.className = 'offering-card bg-white rounded-xl shadow-md border border-gray-200 p-6 hover:shadow-lg hover:border-indigo-600 transition-all duration-200 cursor-pointer';

        card.addEventListener('click', (e) => {
            // Prevent opening the edit modal if the delete button was clicked
            if (e.target.closest('.delete-btn')) {
                return;
            }
            offeringManager.editOffering(offering.id);
        });


        card.innerHTML = `
            <div class="flex justify-between items-start mb-4">
                <div class="flex items-center flex-1">
                    <div class="p-2 bg-indigo-100 rounded-lg">
                        <!-- Icon representing a service/offering -->
                        <svg class="h-6 w-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c1.657 0 3 .895 3 2s-1.343 2-3 2-3 .895-3 2 1.343 2 3 2m0-8h.01M12 21a9 9 0 100-18 9 9 0 000 18z"></path>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <h3 class="text-xl font-bold text-gray-900">${this.escapeHtml(offering.name)}</h3>
                    </div>
                </div>
                <div class="flex space-x-2">
                    <!-- DELETE BUTTON (uses SweetAlert for confirmation) -->
                    <button onclick="offeringManager.deleteOffering('${offering.id}')" class="delete-btn p-2 text-gray-400 hover:text-red-600 transition-colors duration-200" title="Eliminar Servicio">
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </div>

            <p class="text-gray-600 mb-4 line-clamp-2">${this.escapeHtml(offering.description || 'Sin descripción.')}</p>

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
        `;
        return card;
    }

    openModal(offering = null) {
        this.isEditMode = !!offering;
        this.currentOffering = offering;

        const modal = document.getElementById('offeringModal');
        const modalTitle = document.getElementById('modalTitle');
        const form = document.getElementById('offeringForm');
        const advanceInput = document.getElementById('advancePaymentPercentage');
        const advanceValue = document.getElementById('advanceValue');

        if (!modal || !modalTitle || !form || !advanceInput || !advanceValue) return;

        modalTitle.textContent = this.isEditMode ? 'Editar Servicio' : 'Nuevo Servicio';
        form.reset();

        if (this.isEditMode && offering) {
            document.getElementById('name').value = offering.name || '';
            document.getElementById('description').value = offering.description || '';
            document.getElementById('capacity').value = offering.capacity || 1;
            advanceInput.value = offering.advancePaymentPercentage || 0;
            this.currentOfferingId = offering.id;
        } else {
            this.currentOfferingId = null;
            document.getElementById('capacity').value = 1;
            advanceInput.value = 0;
        }

        // Update the slider value display
        advanceValue.textContent = advanceInput.value;
        modal.classList.remove('hidden');
    }

    closeModal() {
        const modal = document.getElementById('offeringModal');
        if (modal) {
            modal.classList.add('hidden');
        }
        this.currentOffering = null;
        this.currentOfferingId = null;
        this.isEditMode = false;
    }

    async handleFormSubmit(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        const offeringData = {
            userId: this.user.id,
            name: formData.get('name').trim(),
            description: formData.get('description').trim(),
            capacity: parseInt(formData.get('capacity')),
            advancePaymentPercentage: parseInt(formData.get('advancePaymentPercentage')) || 0,
        };

        if (!offeringData.name) {
            this.showAlert('El nombre del servicio es requerido', 'error');
            return;
        }
        if (offeringData.capacity < 1) {
            this.showAlert('La capacidad debe ser al menos 1', 'error');
            return;
        }

        try {
            let response;
            let method = 'POST';
            let url = `${this.baseUrl}/users/${this.user.id}/offerings`;

            if (this.isEditMode && this.currentOffering) {
                method = 'PUT';
                url = `${this.baseUrl}/users/${this.user.id}/offerings/${this.currentOffering.id}`;
            }

            response = await fetch(url, {
                method: method,
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(offeringData)
            });

            if (response.ok) {
                this.showAlert(
                    this.isEditMode ? 'Servicio actualizado correctamente' : 'Servicio creado correctamente',
                    'success'
                );
                await this.loadOfferings();

                if (!this.isEditMode) {
                    const newOffering = await response.json();
                    this.currentOfferingId = newOffering.id;
                    this.isEditMode = true;

                } else {
                    this.closeModal();
                }
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                const errorData = await response.text();
                throw new Error(errorData || 'Error al guardar el servicio');
            }
        } catch (error) {
            console.error('Error saving offering:', error);
            this.showAlert('Error al guardar el servicio', 'error');
        }
    }

    editOffering(offeringId) {
        const offering = this.offerings.find(o => o.id === offeringId);
        if (offering) {
            this.openModal(offering);
        }
    }


    async deleteOffering(offeringId) {
        const offering = this.offerings.find(o => o.id === offeringId);
        if (!offering) return;

        const result = await Swal.fire({
            title: '¿Estás seguro?',
            text: `Se eliminará el servicio: ${this.escapeHtml(offering.name)}. Esta acción no se puede deshacer.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc2626', // Red-600
            cancelButtonColor: '#6b7280', // Gray-500
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
         window.location.href = 'index.html';
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = 'index.html';
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