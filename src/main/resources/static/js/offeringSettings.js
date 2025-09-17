class OfferingManager {
    constructor() {
        this.baseUrl = 'http://localhost:8080';
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        this.offerings = [];
        this.currentOffering = null;
        this.isEditMode = false;
        
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

        // Delete modal controls
        const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
        const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');

        if (confirmDeleteBtn) {
            confirmDeleteBtn.addEventListener('click', this.confirmDelete.bind(this));
        }
        if (cancelDeleteBtn) {
            cancelDeleteBtn.addEventListener('click', this.closeDeleteModal.bind(this));
        }

        // Toast close
        const toastClose = document.getElementById('toastClose');
        if (toastClose) {
            toastClose.addEventListener('click', this.hideToast.bind(this));
        }

        // Close modals when clicking backdrop
        const offeringModal = document.getElementById('offeringModal');
        const deleteModal = document.getElementById('deleteModal');

        if (offeringModal) {
            offeringModal.addEventListener('click', (e) => {
                if (e.target === offeringModal) {
                    this.closeModal();
                }
            });
        }

        if (deleteModal) {
            deleteModal.addEventListener('click', (e) => {
                if (e.target === deleteModal) {
                    this.closeDeleteModal();
                }
            });
        }
    }


    async loadOfferings() {
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
            this.showToast('Error al cargar los servicios', 'error');
            this.offerings = [];
            this.renderOfferings();
        }
    }

    renderOfferings() {
        const container = document.getElementById('offeringsContainer');
        const loadingState = document.getElementById('loadingState');
        const emptyState = document.getElementById('emptyState');

        if (!container) return;

        // Hide loading state
        if (loadingState) {
            loadingState.style.display = 'none';
        }

        // Clear existing offerings (but keep loading and empty states)
        const existingCards = container.querySelectorAll('.offering-card');
        existingCards.forEach(card => card.remove());

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
        card.className = 'offering-card bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200';
        card.innerHTML = `
            <div class="flex justify-between items-start mb-4">
                <div class="flex items-center">
                    <div class="p-2 ${offering.active ? 'bg-green-100' : 'bg-gray-100'} rounded-lg">
                        <svg class="h-6 w-6 ${offering.active ? 'text-green-600' : 'text-gray-400'}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <h3 class="text-lg font-semibold text-gray-900">${this.escapeHtml(offering.name)}</h3>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${offering.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}">
                            ${offering.active ? 'Activo' : 'Inactivo'}
                        </span>
                    </div>
                </div>
                <div class="flex space-x-2">
                    <button onclick="offeringManager.editOffering('${offering.id}')" class="p-2 text-gray-400 hover:text-indigo-600 transition-colors duration-200">
                        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                    </button>
                    <button onclick="offeringManager.deleteOffering('${offering.id}')" class="p-2 text-gray-400 hover:text-red-600 transition-colors duration-200">
                        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </div>
            
            ${offering.description ? `<p class="text-gray-600 mb-4">${this.escapeHtml(offering.description)}</p>` : ''}
            
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div>
                    <p class="text-sm text-gray-500">Precio</p>
                    <p class="font-semibold text-gray-900">${offering.price?.toFixed(2) ?? ""}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Capacidad</p>
                    <p class="font-semibold text-gray-900">${offering.capacity} persona${offering.capacity !== 1 ? 's' : ''}</p>
                </div>
            </div>
            
            <div class="flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <div class="flex items-center">
                        <svg class="h-4 w-4 ${offering.showPrice ? 'text-green-500' : 'text-gray-400'} mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        <span class="text-xs text-gray-600">${offering.showPrice ? 'Precio visible' : 'Precio oculto'}</span>
                    </div>
                </div>
                ${offering.advancePaymentPercentage > 0 ? `
                    <div class="text-xs text-gray-600">
                        Adelanto: ${offering.advancePaymentPercentage}%
                    </div>
                ` : ''}
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

        if (!modal || !modalTitle || !form) return;

        // Update modal title
        modalTitle.textContent = this.isEditMode ? 'Editar Servicio' : 'Nuevo Servicio';

        // Reset form
        form.reset();

        // Fill form if editing
        if (this.isEditMode && offering) {
            document.getElementById('name').value = offering.name || '';
            document.getElementById('description').value = offering.description || '';
            document.getElementById('price').value = offering.price || '';
            document.getElementById('capacity').value = offering.capacity || 1;
            document.getElementById('advancePaymentPercentage').value = offering.advancePaymentPercentage || 0;
            document.getElementById('showPrice').checked = offering.showPrice !== false;
            document.getElementById('active').checked = offering.active !== false;
        } else {
            // Set default values for new offering
            document.getElementById('capacity').value = 1;
            document.getElementById('advancePaymentPercentage').value = 0;
            document.getElementById('showPrice').checked = true;
            document.getElementById('active').checked = true;
        }

        // Show modal
        modal.classList.remove('hidden');
    }

    closeModal() {
        const modal = document.getElementById('offeringModal');
        if (modal) {
            modal.classList.add('hidden');
        }
        this.currentOffering = null;
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
            price: parseFloat(formData.get('price')),
            capacity: parseInt(formData.get('capacity')),
            advancePaymentPercentage: parseInt(formData.get('advancePaymentPercentage')) || 0,
            showPrice: formData.get('showPrice') === 'on',
            active: formData.get('active') === 'on'
        };

        // Validation
        if (!offeringData.name) {
            this.showToast('El nombre del servicio es requerido', 'error');
            return;
        }
        if (offeringData.price < 0) {
            this.showToast('El precio debe ser mayor a 0', 'error');
            return;
        }
        if (offeringData.capacity < 1) {
            this.showToast('La capacidad debe ser al menos 1', 'error');
            return;
        }

        try {
            let response;
            
            if (this.isEditMode && this.currentOffering) {
                // Update existing offering
                response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings/${this.currentOffering.id}`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': `Bearer ${this.token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(offeringData)
                });
            } else {
                // Create new offering
                response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(offeringData)
                });
            }

            if (response.ok) {
                this.showToast(
                    this.isEditMode ? 'Servicio actualizado correctamente' : 'Servicio creado correctamente',
                    'success'
                );
                this.closeModal();
                await this.loadOfferings();
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                const errorData = await response.text();
                throw new Error(errorData || 'Error al guardar el servicio');
            }
        } catch (error) {
            console.error('Error saving offering:', error);
            this.showToast('Error al guardar el servicio', 'error');
        }
    }

    editOffering(offeringId) {
        const offering = this.offerings.find(o => o.id === offeringId);
        if (offering) {
            this.openModal(offering);
        }
    }

    deleteOffering(offeringId) {
        this.currentOffering = this.offerings.find(o => o.id === offeringId);
        if (this.currentOffering) {
            const deleteModal = document.getElementById('deleteModal');
            if (deleteModal) {
                deleteModal.classList.remove('hidden');
            }
        }
    }

    async confirmDelete() {
        if (!this.currentOffering) return;

        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings/${this.currentOffering.id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                this.showToast('Servicio eliminado correctamente', 'success');
                this.closeDeleteModal();
                await this.loadOfferings();
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al eliminar el servicio');
            }
        } catch (error) {
            console.error('Error deleting offering:', error);
            this.showToast('Error al eliminar el servicio', 'error');
        }
    }

    closeDeleteModal() {
        const deleteModal = document.getElementById('deleteModal');
        if (deleteModal) {
            deleteModal.classList.add('hidden');
        }
        this.currentOffering = null;
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

    showToast(message, type = 'info') {
        const toast = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');

        if (!toast || !toastMessage || !toastIcon) return;

        // Set message
        toastMessage.textContent = message;

        // Set icon based on type
        let iconHTML = '';
        switch (type) {
            case 'success':
                iconHTML = '<svg class="h-5 w-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
                break;
            case 'error':
                iconHTML = '<svg class="h-5 w-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
                break;
            case 'warning':
                iconHTML = '<svg class="h-5 w-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
                break;
            default:
                iconHTML = '<svg class="h-5 w-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
        }
        toastIcon.innerHTML = iconHTML;

        // Show toast
        toast.classList.remove('hidden');

        // Auto hide after 5 seconds
        setTimeout(() => {
            this.hideToast();
        }, 5000);
    }

    hideToast() {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.classList.add('hidden');
        }
    }

    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }
}

// Global reference for onclick handlers
let offeringManager;

// Initialize offering manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    offeringManager = new OfferingManager();
});