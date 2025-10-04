// User Offerings Page functionality for AgendApp
class UserOfferingsManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.userId = this.getUserIdFromUrl();

        if (!this.userId) {
            this.showError('ID de usuario no válido');
            return;
        }

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadOfferings();
    }

    getUserIdFromUrl() {
        // Get userId from URL parameter: user-offerings.html?userId=xxx
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('userId');
    }

    setupEventListeners() {
        const closeModal = document.getElementById('closeModal');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const serviceModal = document.getElementById('serviceModal');

        if (closeModal) {
            closeModal.addEventListener('click', () => this.closeServiceModal());
        }

        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => this.closeServiceModal());
        }

        if (serviceModal) {
            serviceModal.addEventListener('click', (e) => {
                if (e.target === serviceModal) {
                    this.closeServiceModal();
                }
            });
        }
    }

    async loadOfferings() {
        try {
            this.showLoading();

            const response = await fetch(`${this.baseUrl}/users/${this.userId}/offerings`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const offerings = await response.json();
                this.displayOfferings(offerings);
            } else {
                throw new Error('Error al cargar servicios');
            }
        } catch (error) {
            console.error('Error loading offerings:', error);
            this.showError('No se pudieron cargar los servicios');
        }
    }

    displayOfferings(offerings) {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const servicesContainer = document.getElementById('servicesContainer');
        const servicesGrid = document.getElementById('servicesGrid');

        // Hide all states
        loadingState.classList.add('hidden');
        errorState.classList.add('hidden');
        noServicesState.classList.add('hidden');
        servicesContainer.classList.add('hidden');

        // Filter only active offerings
        const activeOfferings = offerings.filter(offering => offering.active);

        if (activeOfferings.length === 0) {
            noServicesState.classList.remove('hidden');
            return;
        }

        // Clear previous content
        servicesGrid.innerHTML = '';

        // Display offerings
        activeOfferings.forEach(offering => {
            const serviceCard = this.createServiceCard(offering);
            servicesGrid.appendChild(serviceCard);
        });

        servicesContainer.classList.remove('hidden');
    }

    createServiceCard(offering) {
        const card = document.createElement('div');
        card.className = 'bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 cursor-pointer';
        card.addEventListener('click', () => this.selectService(offering));

        const priceDisplay = offering.showPrice
            ? `<div class="flex items-center justify-between mt-4">
                 <span class="text-2xl font-bold text-indigo-600">${offering.price?.toFixed(2) ?? ""}</span>
                 ${offering.advancePaymentPercentage > 0
                   ? `<span class="text-sm text-gray-500">Anticipo: ${offering.advancePaymentPercentage}%</span>`
                   : ''}
               </div>`
            : '<div class="mt-4"><span class="text-lg font-medium text-gray-600">Consultar precio</span></div>';

        card.innerHTML = `
            <div class="flex items-start justify-between mb-3">
                <h3 class="text-xl font-semibold text-gray-900">${offering.name}</h3>
                <div class="flex items-center">
                    <svg class="h-5 w-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <span class="text-sm text-green-600 ml-1">Disponible</span>
                </div>
            </div>

            <p class="text-gray-600 mb-4">${offering.description}</p>

            <div class="flex items-center text-sm text-gray-500 mb-3">
                <svg class="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
                Capacidad: ${offering.capacity} ${offering.capacity === 1 ? 'persona' : 'personas'}
            </div>

            ${priceDisplay}

            <div class="mt-6">
                <button class="w-full bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition-colors duration-200 font-medium">
                    Reservar Cita
                </button>
            </div>
        `;

        return card;
    }

    selectService(offering) {
        const selectedServiceInfo = document.getElementById('selectedServiceInfo');
        const serviceModal = document.getElementById('serviceModal');

        const priceInfo = offering.showPrice
            ? `<p class="text-gray-600 mb-2"><span class="font-medium">Precio:</span> ${offering.price?.toFixed(2) ?? ""}</p>
               ${offering.advancePaymentPercentage > 0
                 ? `<p class="text-gray-600 mb-2"><span class="font-medium">Anticipo requerido:</span> ${offering.advancePaymentPercentage}%</p>`
                 : ''}`
            : '<p class="text-gray-600 mb-2"><span class="font-medium">Precio:</span> Consultar</p>';

        selectedServiceInfo.innerHTML = `
            <div class="mb-4">
                <h4 class="text-lg font-semibold text-gray-900 mb-2">${offering.name}</h4>
                <p class="text-gray-600 mb-3">${offering.description}</p>
                ${priceInfo}
                <p class="text-gray-600"><span class="font-medium">Capacidad:</span> ${offering.capacity} ${offering.capacity === 1 ? 'persona' : 'personas'}</p>
            </div>
        `;

        serviceModal.classList.remove('hidden');
    }

    closeServiceModal() {
        const serviceModal = document.getElementById('serviceModal');
        serviceModal.classList.add('hidden');
    }

    showLoading() {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const servicesContainer = document.getElementById('servicesContainer');

        loadingState.classList.remove('hidden');
        errorState.classList.add('hidden');
        noServicesState.classList.add('hidden');
        servicesContainer.classList.add('hidden');
    }

    showError(message) {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const servicesContainer = document.getElementById('servicesContainer');

        loadingState.classList.add('hidden');
        errorState.classList.remove('hidden');
        noServicesState.classList.add('hidden');
        servicesContainer.classList.add('hidden');

        // Update error message if needed
        const errorTitle = errorState.querySelector('h3');
        if (errorTitle && message) {
            errorTitle.textContent = message;
        }
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new UserOfferingsManager();
});