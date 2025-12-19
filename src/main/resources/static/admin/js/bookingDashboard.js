class BookingDashboardManager {
    userId = null;
    authToken = null;
    currentPage = 0;
    totalPages = 0;
    totalItems = 0;
    clientNameFilter = '';
    startDateFilter = ''; // YYYY-MM-DD
    monthFilter = 'ALL';
    offeringIdFilter = 'ALL';
    bookingTypeFilter = 'INCOMING';
    isLoading = false;
    debounceTimer = null;
    selectedBookingId = null;
    bookings = [];
    offerings = [];
    isFilterOpen = false;

    // --- Constants ---
    PAGE_SIZE = 10;
    API_BOOKING_URL;
    API_OFFERING_URL;
    STATUS_COLORS_ES = {
        CONFIRMED: 'bg-green-100 text-green-700',
        CANCELLED: 'bg-red-100 text-red-700',
    };

    // --- DOM Elements (References assigned in setupDomReferences) ---
    tableBody;
    clientNameInput;
    startDateInput;
    monthSelect;
    offeringSelect;
    bookingTypeSelect;
    prevPageBtn;
    nextPageBtn;
    currentPageDisplay;
    totalPagesDisplay;
    totalItemsDisplay;
    cancelBookingBtn;
    filterToggleBtn;
    filterContent;
    toggleIcon;
    statusMessage;

    constructor() {
        // Initialize constants dependent on window context
        this.API_BOOKING_URL = `${BASE_URL}/booking/user/`;
        this.API_OFFERING_URL = `${BASE_URL}/users/`;
    }

    /**
     * Helper to get a parameter from the URL.
     * @param {string} name - Name of the parameter.
     * @returns {string} Value of the parameter or empty string.
     */
    getUrlParameter(name) {
        name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
        const regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
        const results = regex.exec(location.search);
        return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
    }

    /**
     * Loads auth token and checks for userId. Redirects on failure.
     * @returns {boolean} true if successful, false if redirecting.
     */
    loadAuthTokenAndCheck() {
        this.authToken = localStorage.getItem('authToken');
        const userDataStorage = JSON.parse(localStorage.getItem('userData'));
        this.userId = userDataStorage.id;

        if (!this.authToken || !this.userId) {
            const message = !this.userId ? "ERROR: Falta el ID de usuario en la URL." : "ERROR: Falta el token de autorización en localStorage.";
            console.error(message, "Redirigiendo a /index.html...");
            this.showStatusMessage(`${message} Redirigiendo...`, true);

            setTimeout(() => {
                window.location.href = BASE_URL;
            }, 1500);

            return false;
        }
        return true;
    }

    /** Muestra un mensaje de estado temporal. */
    showStatusMessage(message, isError = false) {
        this.statusMessage.textContent = message;
        this.statusMessage.className = `p-4 mb-4 text-sm rounded-lg ${isError ? 'text-red-700 bg-red-100' : 'text-blue-700 bg-blue-100'}`;
        this.statusMessage.classList.remove('hidden');
        setTimeout(() => {
            this.statusMessage.classList.add('hidden');
        }, 5000);
    }

    /**
     * Calls the API to get the list of offerings.
     * @returns {Promise<Array<Object>>} List of OfferingResponse or empty array on error.
     */
    async fetchOfferings() {
        if (!this.authToken) return [];

        try {
            const response = await fetch(`${this.API_OFFERING_URL}${this.userId}/offerings`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.authToken}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 401) {
                this.showStatusMessage("Sesión expirada (401). Intente cerrar e iniciar sesión nuevamente.", true);
                return [];
            }
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error("Fallo al obtener ofertas:", error);
            this.showStatusMessage(`Error de red o servidor al obtener servicios: ${error.message}`, true);
            return [];
        }
    }

    /**
     * Calls the API to get paginated and filtered bookings.
     * @returns {Promise<{data: Array<Object>, totalPages: number, totalItems: number}>}
     */
    async fetchBookings(pageNumber, pageSize) {
        if (!this.authToken) return { data: [], totalPages: 0, totalItems: 0 };

        this.isLoading = true;
        const params = new URLSearchParams();

        // 1. Mandatory pagination parameters
        params.append('pageNumber', pageNumber.toString());
        params.append('pageSize', pageSize.toString());

        // 2. Filters
        if (this.clientNameFilter) params.append('clientName', this.clientNameFilter);
        if (this.startDateFilter) params.append('startDate', this.startDateFilter);
        if (this.monthFilter && this.monthFilter !== 'ALL') params.append('month', this.monthFilter);
        if (this.offeringIdFilter && this.offeringIdFilter !== 'ALL') params.append('offeringId', this.offeringIdFilter);

        const url = `${this.API_BOOKING_URL}${this.userId}?${params.toString()}`;

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.authToken}`
                }
            });

            if (response.status === 401) {
                this.showStatusMessage("Sesión expirada (401). Intente cerrar e iniciar sesión nuevamente.", true);
                this.isLoading = false;
                return { data: [], totalPages: 0, totalItems: 0 };
            }

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} ${response.statusText}`);
            }

            const result = await response.json();

            let fetchedBookings = result.content || [];

            // INCOMING Filter: Apply local filter if needed
            if (this.bookingTypeFilter === 'INCOMING') {
                const now = new Date();
                fetchedBookings = fetchedBookings.filter(booking => new Date(booking.startDateTime) >= now);
            }

            this.isLoading = false;
            return {
                data: fetchedBookings,
                totalPages: result.totalPages || 0,
                totalItems: result.totalElements || 0,
            };
        } catch (error) {
            console.error("Fallo al obtener reservas:", error);
            this.showStatusMessage(`Error de red o servidor al obtener reservas: ${error.message}`, true);
            this.isLoading = false;
            return { data: [], totalPages: 0, totalItems: 0 };
        }
    }

    /**
     * Calls the API to cancel a booking.
     */
    async cancelBooking(bookingId) {
        if (!this.authToken) return false;

        try {
            const response = await fetch(`${BASE_URL}/booking/${bookingId}/cancel`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
            });

            if (response.status === 401) {
                this.showStatusMessage("No autorizado (401). Verifique su token de sesión.", true);
                return false;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: response.statusText }));
                throw new Error(`Error al cancelar: ${errorData.message || response.status}`);
            }

            this.showStatusMessage(`Reserva ID: ${bookingId.substring(0, 8)}... cancelada exitosamente.`);
            return true;

        } catch (error) {
            console.error("Fallo al cancelar la reserva:", error);
            this.showStatusMessage(`Error al cancelar: ${error.message}`, true);
            return false;
        }
    }

    // --- Rendering and Utility Methods ---

    /** Toggles the visibility of the filter section. */
    toggleFilters = () => {
        this.isFilterOpen = !this.isFilterOpen;

        if (this.isFilterOpen) {
            this.filterContent.classList.remove('collapsed');
            this.toggleIcon.classList.add('rotate-180');
        } else {
            this.filterContent.classList.add('collapsed');
            this.toggleIcon.classList.remove('rotate-180');
        }
    }

    /** Generates the Month options for the filter. */
    generateMonthOptions() {
        const monthOptions = [{ value: 'ALL', text: 'Todos los Meses' }];
        const monthNames = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ];
        for (let i = 0; i < 12; i++) {
            const monthNumber = String(i + 1).padStart(2, '0');
            monthOptions.push({ value: monthNumber, text: monthNames[i] });
        }
        this.monthSelect.innerHTML = monthOptions.map(opt =>
            `<option value="${opt.value}" ${opt.value === this.monthFilter ? 'selected' : ''}>${opt.text}</option>`
        ).join('');
    }

    /** Generates the Offering options for the filter. */
    generateOfferingOptions(offeringsList) {
        const options = [{ value: 'ALL', text: 'Todos los Servicios' }];

        offeringsList.forEach(offering => {
            options.push({ value: offering.id, text: offering.name });
        });

        this.offeringSelect.innerHTML = options.map(opt =>
            `<option value="${opt.value}" ${opt.value === this.offeringIdFilter ? 'selected' : ''}>${opt.text}</option>`
        ).join('');
    }

    /** Determines the time status (Past, Active Now, Upcoming). */
    getBookingTimeStatus(startIsoString, endIsoString) {
        const now = new Date();
        const start = new Date(startIsoString);
        const end = new Date(endIsoString);

        if (end < now) {
            return { status: 'PAST', color: 'bg-gray-200 text-gray-700', text: 'Terminada' };
        }
        if (start <= now && end > now) {
            return { status: 'ACTIVE', color: 'bg-blue-100 text-blue-700', text: 'Activa Ahora' };
        }
        if (start > now) {
            return { status: 'FUTURE', color: 'bg-indigo-100 text-indigo-700', text: 'Próxima' };
        }
        return { status: 'UNKNOWN', color: 'bg-yellow-100 text-yellow-700', text: 'Desconocido' };
    }

    /** Renders the booking status badge (CONFIRMED/CANCELLED). */
    renderStatusBadge(status) {
        const colorClass = this.STATUS_COLORS_ES[status] || 'bg-gray-100 text-gray-700';
        const statusMap = {
          CONFIRMED: 'Confirmada',
          CANCELLED: 'Cancelada'
        };
        const text = statusMap[status] ?? '';

        return `<span class="px-3 py-1 text-xs font-semibold rounded-full ${colorClass}">${text.toUpperCase()}</span>`;
    }

    /** Renders the bookings table. */
    renderBookingsTable() {
        // Clear the existing table body content before rendering new data
        this.tableBody.innerHTML = '';

        this.bookings.forEach(booking => {
            // --- 1. Safely Extract Simple Fields with Fallbacks ---
            const bookingId = booking.id;
            const serviceName = booking.serviceName ?? 'N/A';
            const clientName = booking.clientName ?? 'N/A';
            const clientEmail = booking.clientEmail ?? 'N/A';
            const paid = (typeof booking.paid === 'number' && booking.paid !== null)
                               ? "$" + booking.paid.toFixed(2) : 'N/A';
            const bookingStatus = booking.status;
            const quantity = booking.quantity;

            // --- 2. Date/Time Safety Check and Time Range Calculation ---
            let formattedDate = 'N/A';
            let formattedTimeRange = ''; // Now holds start and end time
            let timeStatus = { text: 'Unknown', color: 'bg-gray-200 text-gray-700' };
            let isDateValid = false;

            if (booking.startDateTime && booking.endDateTime) {
                try {
                    const startDate = new Date(booking.startDateTime);
                    const endDate = new Date(booking.endDateTime);
                    formattedDate = startDate.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });

                    const formattedStartTime = startDate.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
                    const formattedEndTime = endDate.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });

                    // Combine start and end time for display (e.g., "10:00 - 11:00")
                    formattedTimeRange = `${formattedStartTime} - ${formattedEndTime}`;

                    timeStatus = this.getBookingTimeStatus(booking.startDateTime, booking.endDateTime);
                    isDateValid = true;
                } catch (e) {
                    console.error("Error formatting date for booking ID:", bookingId, e);
                }
            }

            const isSelected = bookingId === this.selectedBookingId;

            // --- 4. Create and Populate Row Element ---
            const row = document.createElement('tr');
            // Only make the row clickable if a valid ID exists
            row.className = `row-item cursor-pointer transition duration-150 ${isSelected ? 'bg-indigo-50 border-l-4 border-indigo-600' : 'hover:bg-gray-50'}`;

            if (bookingId) {
                row.dataset.bookingId = bookingId;
                // Use an arrow function to preserve 'this' and pass the booking ID
                row.onclick = () => this.handleRowClick(bookingId);
            } else {
                 // Disable cursor pointer if no ID is available
                 row.className = row.className.replace('cursor-pointer', 'cursor-default');
            }

            row.innerHTML = `
                <td class="px-6 py-3 whitespace-nowrap text-sm font-medium text-gray-900 border-b border-gray-200">
                    <div class="flex items-center space-x-2">
                        <span>${serviceName}</span>
                    </div>
                </td>
                <td class="px-6 py-3 whitespace-nowrap text-sm text-gray-700 border-b border-gray-200">
                    <div class="font-medium">${clientName}</div>
                    <div class="text-xs text-gray-500">${clientEmail}</div>
                </td>
                <td class="px-6 py-3 whitespace-nowrap text-sm text-gray-700 border-b border-gray-200">
                    ${formattedDate}
                    ${isDateValid ? `<br><span class="text-xs font-semibold text-indigo-600">${formattedTimeRange}</span>` : ''}
                </td>
                <td class="px-6 py-3 whitespace-nowrap text-sm text-gray-700 border-b border-gray-200">
                    <div class="flex items-center space-x-2">
                        <span>${quantity}</span>
                    </div>
                </td>
                <td class="px-6 py-3 whitespace-nowrap text-sm text-gray-700 align-middle border-b border-gray-200">
                    ${paid}
                </td>
                <td class="px-6 py-3 whitespace-nowrap text-sm text-gray-700 align-middle border-b border-gray-200">
                    ${this.renderStatusBadge(bookingStatus)}
                </td>
            `;
            this.tableBody.appendChild(row);
        });
    }


    /** Renders the pagination controls. */
    renderPagination() {
        this.currentPageDisplay.textContent = (this.currentPage + 1);
        this.totalPagesDisplay.textContent = this.totalPages;
        this.totalItemsDisplay.textContent = this.totalItems;

        this.prevPageBtn.disabled = this.currentPage === 0 || this.isLoading;
        this.nextPageBtn.disabled = this.currentPage >= this.totalPages - 1 || this.isLoading;

        // Cancellation is only possible if a CONFIRMED booking is selected
        const selectedBooking = this.bookings.find(b => b.id === this.selectedBookingId);
        this.cancelBookingBtn.disabled = !selectedBooking || selectedBooking.status === 'CANCELLED' || this.isLoading;
    }

    /** Fetches, updates state, and renders bookings. */
    async loadBookingsAndRender(page = this.currentPage, shouldResetSelected = false) {
        if (this.isLoading || !this.authToken) return;
        this.isLoading = true;
        this.tableBody.innerHTML = `<tr><td colspan="6" class="px-6 py-10 text-center text-indigo-500 font-semibold">Cargando reservas...</td></tr>`;
        this.renderPagination();

        try {
            const result = await this.fetchBookings(page, this.PAGE_SIZE);
            this.bookings = result.data;
            this.totalPages = result.totalPages;
            this.totalItems = result.totalItems;
            this.currentPage = page;

            if (shouldResetSelected || !this.bookings.some(b => b.id === this.selectedBookingId)) {
                this.selectedBookingId = null;
            }

            this.renderBookingsTable();
        } finally {
            this.isLoading = false;
            this.renderPagination();
        }
    }

    // --- Handlers ---

    /** Handles the click on a table row. */
    handleRowClick(bookingId) {
        this.selectedBookingId = this.selectedBookingId === bookingId ? null : bookingId;

        document.querySelectorAll('.row-item').forEach(row => {
            row.classList.remove('selected');
            row.style.borderLeft = 'none';
            if (row.dataset.bookingId === this.selectedBookingId) {
                row.classList.add('selected');
            }
        });

        this.renderPagination();
    }

    /** Handles page change. */
    handlePageChange(direction) {
        const newPage = this.currentPage + direction;
        if (newPage >= 0 && newPage < this.totalPages) {
            this.loadBookingsAndRender(newPage, true);
        }
    }

    /** Applies filters and reloads the first page. */
    applyFilters = () => {
        this.clientNameFilter = this.clientNameInput.value.trim();
        this.startDateFilter = this.startDateInput.value;
        this.monthFilter = this.monthSelect.value;
        this.offeringIdFilter = this.offeringSelect.value;
        this.bookingTypeFilter = this.bookingTypeSelect.value;

        // Reload to page 0 with new filters
        this.loadBookingsAndRender(0, true);
    }

    handleCancelBooking = async () => {
        const bookingToCancel = this.bookings.find(b => b.id === this.selectedBookingId);
        if (!bookingToCancel || bookingToCancel.status === 'CANCELLED' || this.isLoading) return;

        this.cancelBookingBtn.disabled = true;
        const originalContent = this.cancelBookingBtn.innerHTML;
        this.cancelBookingBtn.innerHTML = '<svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> Cancelando...';

        try {
            const success = await this.cancelBooking(this.selectedBookingId);

            // Recargar los datos para reflejar el cambio de estado
            if (success) {
                await this.loadBookingsAndRender(this.currentPage, true);
            }
        } finally {
            // Restablecer el botón
            this.cancelBookingBtn.innerHTML = originalContent;
        }
    }

    setupDomReferences() {
        this.tableBody = document.getElementById('bookingsTableBody');
        this.clientNameInput = document.getElementById('clientNameInput');
        this.startDateInput = document.getElementById('startDateInput');
        this.monthSelect = document.getElementById('monthFilter');
        this.offeringSelect = document.getElementById('offeringFilter');
        this.bookingTypeSelect = document.getElementById('bookingTypeFilter');
        this.prevPageBtn = document.getElementById('prevPageBtn');
        this.nextPageBtn = document.getElementById('nextPageBtn');
        this.currentPageDisplay = document.getElementById('currentPageDisplay');
        this.totalPagesDisplay = document.getElementById('totalPagesDisplay');
        this.totalItemsDisplay = document.getElementById('totalItemsDisplay');
        this.cancelBookingBtn = document.getElementById('cancelBookingBtn');
        this.filterToggleBtn = document.getElementById('filterToggleBtn');
        this.filterContent = document.getElementById('filterContent');
        this.toggleIcon = document.getElementById('toggleIcon');
        this.statusMessage = document.getElementById('statusMessage');
    }

    setupEventListeners() {
        this.filterToggleBtn.addEventListener('click', this.toggleFilters);
        this.prevPageBtn.addEventListener('click', () => this.handlePageChange(-1));
        this.nextPageBtn.addEventListener('click', () => this.handlePageChange(1));
        this.cancelBookingBtn.addEventListener('click', this.handleCancelBooking);
        this.monthSelect.addEventListener('change', this.applyFilters);
        this.offeringSelect.addEventListener('change', this.applyFilters);
        this.bookingTypeSelect.addEventListener('change', this.applyFilters);
        this.startDateInput.addEventListener('change', this.applyFilters);

        this.clientNameInput.addEventListener('input', () => {
            clearTimeout(this.debounceTimer);
            this.debounceTimer = setTimeout(this.applyFilters, 500);
        });
    }

    async init() {

        this.setupDomReferences();

        if (!this.loadAuthTokenAndCheck()) {
            return;
        }

        this.filterContent.classList.add('collapsed');

        this.offerings = await this.fetchOfferings();
        this.generateOfferingOptions(this.offerings);

        this.generateMonthOptions();

        await this.loadBookingsAndRender(0, true);

        this.setupEventListeners();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const dashboardManager = new BookingDashboardManager();
    dashboardManager.init();
});
