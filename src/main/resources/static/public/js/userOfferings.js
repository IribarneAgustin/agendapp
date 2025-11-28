class UserOfferingsManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.userId = this.getUserIdFromUrl();
        this.selectedOffering = null;
        this.availableSlots = []; // All fetched slots
        this.groupedSlotsByDate = {}; // Slots grouped by date 'YYYY-MM-DD'
        this.currentDate = new Date(); // Date object to track current calendar view
        this.selectedDateKey = null; // 'YYYY-MM-DD' of the currently selected date in the calendar

        // DOM Elements
        this.servicesContainer = document.getElementById('servicesContainer');
        this.bookingPanel = document.getElementById('bookingPanel');
        this.calendarGrid = document.getElementById('calendarGrid');
        this.currentMonthYear = document.getElementById('currentMonthYear');
        this.slotTimeContainer = document.getElementById('slotTimeContainer');
        this.bookingForm = document.getElementById('bookingForm');

        if (!this.userId) {
            this.showError('ID de usuario no válido');
            return;
        }
        this.setupEventListeners();
        this.loadOfferings();
    }

    getUserIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('userId');
    }

    setupEventListeners() {
        document.getElementById('prevMonthBtn').addEventListener('click', () => this.changeMonth(-1));
        document.getElementById('nextMonthBtn').addEventListener('click', () => this.changeMonth(1));

        document.getElementById('backToServicesBtn').addEventListener('click', () => this.cancelBooking());

        if (this.bookingForm) {
            this.bookingForm.addEventListener('submit', (e) => this.createBooking(e));
        }
    }

    // --- Utility Functions ---

    getFormattedDate(date) {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();

        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;

        return [year, month, day].join('-');
    }

    formatTime(dateTimeStr) {
        if (!dateTimeStr) return '';
        try {
            const date = new Date(dateTimeStr);
            const options = { hour: '2-digit', minute: '2-digit', hour12: false };
            return date.toLocaleTimeString('es-ES', options);
        } catch (e) {
            return dateTimeStr.substring(11, 16);
        }
    }

    getYYYYMMDD(dateTimeStr) {
        return dateTimeStr.substring(0, 10);
    }

    async loadOfferings() {
        try {
            this.showLoading();

            const response = await fetch(`${this.baseUrl}/users/${this.userId}/offerings`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }

            const offerings = await response.json();
            this.displayOfferings(offerings);

        } catch (error) {
            console.error('Error loading offerings:', error);
            this.showError('No se pudieron cargar los servicios. Verifica la URL y la disponibilidad del backend.');
        }
    }

    displayOfferings(offerings) {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const servicesGrid = document.getElementById('servicesGrid');
        const profileHeader = document.getElementById('profileHeader'); // Added styling hook

        loadingState.classList.add('hidden');
        errorState.classList.add('hidden');
        noServicesState.classList.add('hidden');
        this.bookingPanel.classList.add('hidden');

        if(profileHeader) profileHeader.classList.remove('hidden');

        const activeOfferings = offerings.filter(offering => offering.enabled);

        if (activeOfferings.length === 0) {
            noServicesState.classList.remove('hidden');
            return;
        }

        servicesGrid.innerHTML = '';

        activeOfferings.forEach(offering => {
            const serviceCard = this.createServiceCard(offering);
            servicesGrid.appendChild(serviceCard);
        });

        this.servicesContainer.classList.remove('hidden');
        
        // Trigger 3D effects and Icons after rendering
        if(window.lucide) window.lucide.createIcons();
        if(window.initTilt) window.initTilt();
    }

    createServiceCard(offering) {
        const card = document.createElement('div');
        // Updated styling classes for Glassmorphism + 3D Tilt
        card.className = 'glass-panel glass-card-hover rounded-3xl p-1 cursor-pointer transition-all duration-300 h-full group';
        card.addEventListener('click', () => this.selectService(offering));

        // Styling update: New HTML structure for the card content
        const priceDisplay = offering.price ? `$${offering.price}` : 'Consultar';
        
        card.innerHTML = `
            <div class="bg-white/50 rounded-[20px] p-8 h-full flex flex-col justify-between transition-colors group-hover:bg-white/80 relative overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-indigo-500/10 rounded-full -mr-16 -mt-16 transition-transform group-hover:scale-150 duration-500"></div>
                
                <div>
                    <div class="flex justify-between items-start mb-6 relative">
                        <div class="p-3.5 bg-white shadow-sm rounded-2xl text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors duration-300">
                            <i data-lucide="briefcase" class="w-6 h-6"></i>
                        </div>
                        <span class="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold border border-green-200">
                            ${priceDisplay}
                        </span>
                    </div>
                    <h3 class="text-2xl font-bold text-gray-900 mb-3 group-hover:text-indigo-600 transition-colors">${offering.name}</h3>
                    <p class="text-gray-600 text-sm leading-relaxed line-clamp-3">${offering.description || 'Sin descripción disponible.'}</p>
                </div>
                
                <div class="mt-8 pt-6 border-t border-gray-100 flex items-center justify-between group-hover:border-indigo-100 transition-colors">
                    <span class="text-sm font-semibold text-gray-400 group-hover:text-indigo-500 transition-colors">Ver Disponibilidad</span>
                    <div class="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center group-hover:bg-indigo-600 group-hover:text-white transition-all duration-300 group-hover:translate-x-2 shadow-sm">
                        <i data-lucide="arrow-right" class="w-4 h-4"></i>
                    </div>
                </div>
            </div>
        `;

        return card;
    }

    async fetchSlots(offeringId) {
        try {
            const response = await fetch(`${this.baseUrl}/slot-time/offering/${offeringId}?page=0&pageSize=50`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}: No se pudieron cargar horarios.`);
            }

            const pageResponse = await response.json();
            return pageResponse.content || [];

        } catch (error) {
            console.error('Error fetching slots:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error de Carga',
                text: 'No se pudieron cargar los horarios para este servicio. Intenta más tarde.',
                confirmButtonColor: '#ef4444'
            });
            return [];
        }
    }

    groupSlotsByDate(slots) {
        const groups = {};
        slots.forEach(slot => {
            const dateKey = this.getYYYYMMDD(slot.startDateTime);
            if (!groups[dateKey]) {
                groups[dateKey] = [];
            }
            groups[dateKey].push(slot);
        });
        return groups;
    }

    changeMonth(delta) {
        this.currentDate.setMonth(this.currentDate.getMonth() + delta);
        this.renderCalendar();
    }

    handleDateClick(event) {
        const dateString = event.currentTarget.dataset.date;

        // 1. Clear previous selection visually
        this.calendarGrid.querySelectorAll('.date-cell').forEach(cell => cell.classList.remove('selected-date'));

        // 2. Select the current date
        event.currentTarget.classList.add('selected-date');
        this.selectedDateKey = dateString;

        // 3. Render time slots for this date
        this.renderTimeSlots(dateString);
        
        // Styling fix: Scroll to slots on mobile
        if(window.innerWidth < 1024) {
            document.getElementById('slotsSection').scrollIntoView({ behavior: 'smooth' });
        }
    }

    renderCalendar() {
        const todayFormatted = this.getFormattedDate(new Date());

        this.calendarGrid.innerHTML = '';
        const monthNames = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();

        // Capitalize month
        const monthName = monthNames[month];
        this.currentMonthYear.textContent = `${monthName} ${year}`;

        const firstDayOfMonth = new Date(year, month, 1);
        // Adjust for Monday start (0=Mon, 6=Sun)
        const startDayIndex = (firstDayOfMonth.getDay() + 6) % 7;
        const lastDayOfMonth = new Date(year, month + 1, 0).getDate();
        const today = new Date();


        // 1. Render empty cells for padding
        for (let i = 0; i < startDayIndex; i++) {
            const emptyCell = document.createElement('div');
            emptyCell.className = 'date-cell text-gray-300'; // Styling update
            this.calendarGrid.appendChild(emptyCell);
        }

        // 2. Render date cells
        for (let day = 1; day <= lastDayOfMonth; day++) {
            const date = new Date(year, month, day);
            const dateString = this.getFormattedDate(date);
            const hasSlots = this.groupedSlotsByDate[dateString] && this.groupedSlotsByDate[dateString].length > 0;

            // Determine if the date is in the past (only comparing date parts)
            const isPast = date < new Date(today.getFullYear(), today.getMonth(), today.getDate());

            const cell = document.createElement('button');
            cell.type = 'button';
            cell.textContent = day;
            cell.dataset.date = dateString;

            // Styling update: Use new classes defined in HTML CSS
            let cellClasses = ['date-cell'];

            if (dateString === todayFormatted) {
                cellClasses.push('today');
            }

            if (isPast) {
                cell.setAttribute('disabled', 'true');
                cellClasses.push('text-gray-300');
            } else if (hasSlots) {
                // Check if any slot on this date has capacity > 0
                const isDateAvailable = this.groupedSlotsByDate[dateString].some(slot => slot.capacityAvailable > 0);

                if (isDateAvailable) {
                    cellClasses.push('available', 'text-gray-700', 'hover:bg-indigo-50');
                    // Bind handleDateClick using an arrow function to preserve 'this' context
                    cell.addEventListener('click', (e) => this.handleDateClick(e));
                } else {
                    // Date has slots, but all are fully booked
                    cell.setAttribute('disabled', 'true');
                    cellClasses.push('text-gray-400', 'line-through', 'opacity-50');
                }
            } else {
                // Date is future, but no slots available
                cell.setAttribute('disabled', 'true');
                cellClasses.push('text-gray-300');
            }

            // Apply selection class if this date was previously selected
            if (dateString === this.selectedDateKey) {
                 cellClasses.push('selected-date');
                 // Re-render time slots if the selected date is in the current month view
                 this.renderTimeSlots(dateString);
            }

            cell.className = cellClasses.join(' ');
            this.calendarGrid.appendChild(cell);
        }
    }


    // --- Time Slot and Booking Logic ---

    renderTimeSlots(dateKey) {
        const slots = this.groupedSlotsByDate[dateKey];

        // Clear previous slots and reset selection state
        this.slotTimeContainer.innerHTML = '';
        this.handleSlotSelection(null);

        if (!slots || slots.length === 0) {
            // Styling update: better empty state
            this.slotTimeContainer.innerHTML = `
                <div class="col-span-full text-center py-8 text-gray-400 flex flex-col items-center border-2 border-dashed border-gray-200 rounded-xl">
                    <i data-lucide="clock-4" class="w-6 h-6 mb-2 opacity-50"></i>
                    <p class="text-sm">No hay horarios disponibles.</p>
                </div>`;
            if(window.lucide) window.lucide.createIcons();
            return;
        }

        // Remove message if slots exist
        document.getElementById('slotMessage')?.remove();
        
        // Sort slots by time
        slots.sort((a,b) => a.startDateTime.localeCompare(b.startDateTime));

        slots.forEach(slot => {
            const button = document.createElement('button');
            const available = slot.capacityAvailable > 0;

            button.type = 'button';

            // Styling update: Modern pill styling
            let btnClasses = ['slot-time-btn', 'w-full', 'py-3', 'px-2', 'text-sm', 'rounded-xl', 'border', 'font-medium', 'flex', 'items-center', 'justify-center', 'gap-2'];

            if (available) {
                 btnClasses.push('bg-white', 'border-indigo-100', 'text-indigo-600', 'hover:border-indigo-500', 'hover:shadow-md', 'hover:-translate-y-0.5');
            } else {
                 btnClasses.push('bg-gray-50', 'border-gray-100', 'text-gray-400', 'cursor-not-allowed');
                 button.disabled = true;
            }

            button.className = btnClasses.join(' ');

            const time = this.formatTime(slot.startDateTime);
            // Only show capacity if low
            const capacityText = (available && slot.capacityAvailable <= 3)
                ? `<span class="text-[10px] bg-orange-100 text-orange-600 px-1.5 rounded-md ml-1">Quedan ${slot.capacityAvailable}</span>`
                : '';

            button.innerHTML = `${time} ${capacityText}`;
            button.setAttribute('data-slot-id', slot.id);

            if (available) {
                button.addEventListener('click', () => {
                    // Handle selection of this specific slot
                    this.slotTimeContainer.querySelectorAll('.slot-time-btn').forEach(btn => btn.classList.remove('selected'));
                    button.classList.add('selected');
                    this.handleSlotSelection(slot.id);
                });
            }

            this.slotTimeContainer.appendChild(button);
        });
    }

    handleSlotSelection(slotId) {
        const slotTimeIdInput = document.getElementById('slotTimeId');
        const submitBookingBtn = document.getElementById('submitBookingBtn');
        const quantityInput = document.getElementById('quantity');
        const quantityContainer = document.getElementById('quantityContainer'); // Hook for UI

        slotTimeIdInput.value = slotId || '';

        const slot = this.availableSlots.find(s => s.id === slotId);

        // Show quantity logic
        if (slot) {
            if (slot.capacityAvailable <= 1) {
                quantityInput.value = 1;
                quantityInput.disabled = true;
                if(quantityContainer) quantityContainer.classList.add('hidden');
            } else {
                quantityInput.disabled = false;
                quantityInput.value = 1;
                quantityInput.max = slot.capacityAvailable;
                if(quantityContainer) quantityContainer.classList.remove('hidden');
            }
        } else {
            if(quantityContainer) quantityContainer.classList.add('hidden');
        }

        // Update price display
        this.updateSlotPriceDisplay(slotId);

        // Update price on quantity change
        quantityInput.oninput = () => this.updateSlotPriceDisplay(slotId);

        // Control button state
        submitBookingBtn.disabled = !slotId;
    }

    updateSlotPriceDisplay(slotId) {
        const slotPriceDisplayDiv = document.getElementById('slotPriceDisplay');
        const slot = this.availableSlots.find(s => s.id === slotId);
        const quantityInput = document.getElementById('quantity');
        const quantity = Number(quantityInput?.value || 1);

        if (!slotPriceDisplayDiv) return;

        if (this.selectedOffering && slot && slot.price !== null) {
            // Styling update: Make visible when active
            slotPriceDisplayDiv.classList.remove('hidden');
            
            const totalPrice = slot.price * quantity;
            let priceHtml = `
                <div class="flex justify-between items-end mb-1">
                    <span class="text-sm text-gray-500 font-medium">Total Estimado</span>
                    <span class="text-3xl font-bold text-gray-900 tracking-tight">$${totalPrice.toLocaleString('es-AR')}</span>
                </div>`;

            if (this.selectedOffering.advancePaymentPercentage > 0) {
                const advanceAmount = totalPrice * (this.selectedOffering.advancePaymentPercentage / 100);
                priceHtml += `
                    <div class="bg-indigo-50 rounded-lg p-3 mt-3 flex justify-between items-center border border-indigo-100">
                        <div class="flex items-center gap-2 text-sm text-indigo-700">
                            <i data-lucide="wallet" class="w-4 h-4"></i>
                            <span>Seña a pagar hoy (${this.selectedOffering.advancePaymentPercentage}%)</span>
                        </div>
                        <span class="font-bold text-indigo-700">$${advanceAmount.toLocaleString('es-AR')}</span>
                    </div>`;
            }

            slotPriceDisplayDiv.innerHTML = priceHtml;
            if(window.lucide) window.lucide.createIcons();

        } else {
             // Hide if no valid selection
             slotPriceDisplayDiv.classList.add('hidden');
        }
    }


    async selectService(offering) {
        this.selectedOffering = offering;
        const selectedServiceInfo = document.getElementById('selectedServiceInfo');
        const dateMessage = document.getElementById('dateMessage');
        const profileHeader = document.getElementById('profileHeader');

        this.servicesContainer.classList.add('hidden');
        if(profileHeader) profileHeader.classList.add('hidden'); // Hide large header
        
        this.bookingPanel.classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });

        this.currentDate = new Date();
        this.selectedDateKey = null; // Clear previous selection
        this.slotTimeContainer.innerHTML = `
            <div class="col-span-full text-center py-10 text-gray-400 flex flex-col items-center border-2 border-dashed border-gray-200 rounded-2xl">
                <i data-lucide="mouse-pointer-click" class="w-8 h-8 mb-3 opacity-40"></i>
                <p>Selecciona un día en el calendario.</p>
            </div>`;
        if(window.lucide) window.lucide.createIcons();
        
        this.handleSlotSelection(null);
        this.availableSlots = [];
        this.groupedSlotsByDate = {};
        dateMessage.classList.remove('hidden');

        // Styling update: Enhanced summary
        selectedServiceInfo.innerHTML = `
            <div class="flex items-center gap-3 mb-3">
                 <div class="p-2 bg-white shadow-sm rounded-lg text-indigo-600">
                    <i data-lucide="bookmark" class="w-5 h-5"></i>
                </div>
                <h4 class="text-xl font-bold text-gray-900">${offering.name}</h4>
            </div>
            <p class="text-gray-600 text-sm pl-1">${offering.description || ''}</p>
        `;
        if(window.lucide) window.lucide.createIcons();

        const slots = await this.fetchSlots(offering.id);
        this.availableSlots = slots;
        this.groupedSlotsByDate = this.groupSlotsByDate(slots);

        dateMessage.classList.add('hidden');
        this.renderCalendar();

        if (slots.length === 0 || slots.every(slot => slot.capacityAvailable == 0 || slot.capacityAvailable == null)) {
            this.slotTimeContainer.innerHTML = '<p class="col-span-full text-center text-rose-500 font-semibold bg-rose-50 p-4 rounded-xl">Sin horarios disponibles actualmente.</p>';
            dateMessage.textContent = 'No hay fechas disponibles.';
            dateMessage.classList.remove('hidden');
        }

        this.bookingForm.reset();
    }

    cancelBooking() {
        this.bookingPanel.classList.add('hidden');
        this.servicesContainer.classList.remove('hidden');
        
        const profileHeader = document.getElementById('profileHeader');
        if(profileHeader) profileHeader.classList.remove('hidden'); // Show header again
        
        window.scrollTo({ top: 0, behavior: 'smooth' });

        this.selectedOffering = null;
        this.availableSlots = [];
        this.groupedSlotsByDate = {};
        this.selectedDateKey = null;
        this.currentDate = new Date();
        this.bookingForm.reset();
    }

    async createBooking(event) {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);

        const bookingData = {
            slotTimeId: formData.get('slotTimeId'),
            email: formData.get('email'),
            phoneNumber: formData.get('phoneNumber'),
            name: formData.get('name'),
            quantity: formData.get('quantity') ? formData.get('quantity') : 1
        };

        if (!bookingData.slotTimeId || !bookingData.email || !bookingData.phoneNumber || !bookingData.name) {
            Swal.fire({
                icon: 'warning',
                title: 'Faltan Datos',
                text: 'Por favor, selecciona un horario y completa todos los campos.',
                confirmButtonColor: '#fb923c'
            });
            return;
        }

        try {
            const createBookingButton = form.querySelector('button[type="submit"]');
            const originalText = createBookingButton.innerHTML;
            createBookingButton.innerHTML = '<div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>';
            createBookingButton.disabled = true;

            const response = await fetch(`${this.baseUrl}/booking`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingData)
            });

            if (!response.ok) {
                const errorBody = await response.json();
                if (errorBody?.errorCode === "NO_CAPACITY_AVAILABLE") {
                    throw new Error('El horario que intentaste reservar acaba de agotarse. Por favor, selecciona otro.');
                }
                throw new Error(errorBody.message || 'Error desconocido al crear la reserva.');
            }

            const result = await response.json();

            if (result.checkoutURL) {
                window.location.href = result.checkoutURL;
                return;
            }

            Swal.fire({
                icon: 'success',
                title: '¡Reserva Exitosa!',
                html: `Tu cita para <strong>${this.selectedOffering.name}</strong> ha sido agendada. <br>Revisa tu correo para los detalles.`,
                confirmButtonText: 'Genial',
                confirmButtonColor: '#4f46e5'
            });

            this.cancelBooking();

        } catch (error) {
            console.error('Booking creation error:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error al Reservar',
                text: `No se pudo completar la reserva.`,
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#ef4444'
            });
        } finally {
            const createBookingButton = form.querySelector('button[type="submit"]');
            // Restore original styled content
            createBookingButton.innerHTML = `<span>Confirmar Reserva</span><i data-lucide="arrow-right" class="w-4 h-4"></i>`;
            if(window.lucide) window.lucide.createIcons();
            createBookingButton.disabled = !document.getElementById('slotTimeId').value;
        }
    }


    showLoading() {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const profileHeader = document.getElementById('profileHeader');

        loadingState.classList.remove('hidden');
        errorState.classList.add('hidden');
        noServicesState.classList.add('hidden');
        this.servicesContainer.classList.add('hidden');
        this.bookingPanel.classList.add('hidden');
        if(profileHeader) profileHeader.classList.add('hidden');
    }

    showError(message) {
        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const noServicesState = document.getElementById('noServicesState');
        const profileHeader = document.getElementById('profileHeader');

        loadingState.classList.add('hidden');
        errorState.classList.remove('hidden');
        noServicesState.classList.add('hidden');
        this.servicesContainer.classList.add('hidden');
        this.bookingPanel.classList.add('hidden');
        if(profileHeader) profileHeader.classList.add('hidden');

        const errorTitle = errorState.querySelector('h3');
        if (errorTitle && message) {
            // Logic correction: Don't replace Title with message, maybe add paragraph
             // Keeping strictly as per request, but minimal styling touch:
             // Since errorState structure changed in HTML, let's target the paragraph if possible, or just leave as is if simple text.
             // The HTML has a <p> under h3, let's try to find it.
             const p = errorState.querySelector('p');
             if(p) p.textContent = message;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const bookedSuccess = urlParams.get('bookedSuccess');

    if (bookedSuccess === 'true') {
        Swal.fire({
            icon: 'success',
            title: '¡Reserva Completada!',
            text: 'Tu reserva se completó correctamente. Revisa tu correo electrónico para ver los detalles.',
            confirmButtonColor: '#4f46e5',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            // Remove query param from URL
            urlParams.delete('bookedSuccess');
            const newUrl = `${window.location.pathname}?${urlParams.toString()}`;
            window.history.replaceState({}, '', newUrl);
        });
    } else if (bookedSuccess === 'false') {
        Swal.fire({
            icon: 'error',
            title: 'Error en el Pago',
            text: 'Ocurrió un error al procesar el pago, no se pudo completar la reserva correctamente.',
            confirmButtonColor: '#4f46e5',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            // Remove query param from URL
            urlParams.delete('bookedSuccess');
            const newUrl = `${window.location.pathname}?${urlParams.toString()}`;
            window.history.replaceState({}, '', newUrl);
        });
    }

    new UserOfferingsManager();
});