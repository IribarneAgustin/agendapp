class OfferingCrudManager {
    constructor() {
        this.baseUrl = typeof BASE_URL !== 'undefined' ? BASE_URL : '';
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        const urlParams = new URLSearchParams(window.location.search);
        this.currentOfferingId = urlParams.get('id');
        this.isEditMode = !!this.currentOfferingId;
        this.currentOffering = null;
        this.categories = [];

        if (!this.token || !this.user) {
            window.location.href = this.baseUrl;
            return;
        }

        this.init();
    }

    async init() {
        this.setupEventListeners();
        this.updateUIForMode();

        await this.loadCategories();

        if (this.isEditMode) {
            await this.loadOfferingData();
        }
    }

    getStoredUser() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }

    setupEventListeners() {
        const form = document.getElementById('offeringForm');
        if (form) {
            form.addEventListener('submit', this.handleFormSubmit.bind(this));
        }

        const cancelBtn = document.getElementById('cancelBtn');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                window.history.back();
            });
        }
        const textarea = document.getElementById('termsAndConditions');
        const counter = document.getElementById('termsCounter');

        if (textarea && counter) {
            const updateCounter = () => {
                counter.textContent = `${textarea.value.length} / 2000`;
            };

            textarea.addEventListener('input', updateCounter);
            updateCounter();
        }
    }

    updateUIForMode() {
        const title = document.getElementById('pageTitle');
        const submitBtn = document.getElementById('submitBtn');

        if (this.isEditMode) {
            if (title) title.textContent = 'Editar Servicio';
            if (submitBtn) submitBtn.textContent = 'Guardar Cambios';
        } else {
            if (title) title.textContent = 'Nuevo Servicio';
            if (submitBtn) submitBtn.textContent = 'Crear Servicio';
        }
    }

    async loadOfferingData() {
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
                const offering = offerings.find(o => o.id === this.currentOfferingId);

                if (offering) {
                    this.currentOffering = offering;
                    this.populateForm(offering);
                    this.applyCategorySelection();
                } else {
                    this.showAlert('Servicio no encontrado', 'error');
                    setTimeout(() => window.history.back(), 2000);
                }
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al cargar servicio');
            }
        } catch (error) {
            console.error('Error loading offering:', error);
            this.showAlert('Error al cargar el servicio', 'error');
        }
    }

    populateForm(offering) {
        document.getElementById('name').value = offering.name || '';
        document.getElementById('description').value = offering.description || '';
        document.getElementById('capacity').value = offering.capacity || 1;

        const advanceInput = document.getElementById('advancePaymentPercentage');
        advanceInput.value = offering.advancePaymentPercentage || 0;
        document.getElementById('advanceValue').textContent = advanceInput.value;

        const textarea = document.getElementById('termsAndConditions');
        if (textarea) {
            textarea.value = offering.termsAndConditions || '';
            textarea.dispatchEvent(new Event('input'));
        }
    }

    applyCategorySelection() {
        const select = document.getElementById('category');
        if (!select) {
            return;
        }

        let targetValue = '';

        if (this.currentOffering?.categoryId) {
            const rawId = typeof this.currentOffering.categoryId === 'object' ? this.currentOffering.categoryId.id : this.currentOffering.categoryId;
            const categoryExists = this.categories.some(cat => String(cat.id) === String(rawId));

            if (categoryExists) {
                targetValue = rawId;
            } else {
                console.warn(`Category ID ${rawId} not found in available categories. Defaulting to placeholder.`);
            }
        }

        select.value = targetValue;
    }

    async handleFormSubmit(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        const categoryValue = formData.get('category');

        const offeringData = {
            userId: this.user.id,
            name: formData.get('name').trim(),
            description: formData.get('description').trim(),
            capacity: parseInt(formData.get('capacity')),
            advancePaymentPercentage: parseInt(formData.get('advancePaymentPercentage')) || 0,
            termsAndConditions: formData.get('termsAndConditions') || null,
            categoryId: categoryValue || null
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
            let method = this.isEditMode ? 'PUT' : 'POST';
            let url = `${this.baseUrl}/users/${this.user.id}/offerings`;

            if (this.isEditMode) {
                url += `/${this.currentOfferingId}`;
            }

            const response = await fetch(url, {
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

                window.history.back();

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

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = this.baseUrl;
    }

    async loadCategories() {
        try {
            const response = await fetch(`${this.baseUrl}/user/${this.user.id}/category`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (response.ok) {
                this.categories = await response.json();
                this.renderCategorySelect();
            }
        } catch (error) {
            console.error('Error loading categories:', error);
        }
    }

    renderCategorySelect() {
        const select = document.getElementById('category');
        const container = document.getElementById('categoryContainer');

        if (!select || !container) return;

        if (this.categories.length === 0) {
            container.classList.add('hidden');
            return;
        }

        container.classList.remove('hidden');

        select.innerHTML = '';

        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.textContent = 'Seleccionar categoría...';
        select.appendChild(placeholder);

        this.categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            select.appendChild(option);
        });

        this.applyCategorySelection();
    }

    showAlert(message, icon = 'info') {
        const swalIcon = ['success', 'error', 'warning', 'info'].includes(icon) ? icon : 'info';
        Swal.fire({
            icon: swalIcon,
            title: message,
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: false,
            customClass: {
                container: 'sweetalert-container'
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new OfferingCrudManager();
});