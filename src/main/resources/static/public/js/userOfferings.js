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

            loadingState.classList.add('hidden');
            errorState.classList.add('hidden');
            noServicesState.classList.add('hidden');
            this.bookingPanel.classList.add('hidden');

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
        }

        createServiceCard(offering) {
            const card = document.createElement('div');
            card.className = 'bg-white rounded-xl shadow-lg border border-gray-100 p-6 hover:shadow-xl transition-all duration-300 cursor-pointer flex flex-col justify-between transform hover:-translate-y-1';
            card.addEventListener('click', () => this.selectService(offering));

            card.innerHTML = `
                <div>
                    <div class="flex items-start justify-between mb-3">
                        <h3 class="text-xl font-semibold text-gray-900">${offering.name}</h3>
                    </div>
                    <p class="text-gray-600 mb-4">${offering.description}</p>
                </div>
                <div class="mt-4">
                    <button class="w-full bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition-colors duration-200 font-medium shadow-md shadow-indigo-300">
                        Ver Disponibilidad
                    </button>
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
        }

        renderCalendar() {
            const todayFormatted = this.getFormattedDate(new Date());

            this.calendarGrid.innerHTML = '';
            const monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];

            const year = this.currentDate.getFullYear();
            const month = this.currentDate.getMonth();

            this.currentMonthYear.textContent = `${monthNames[month]} ${year}`;

            const firstDayOfMonth = new Date(year, month, 1);
            // Adjust for Monday start (0=Mon, 6=Sun)
            const startDayIndex = (firstDayOfMonth.getDay() + 6) % 7;
            const lastDayOfMonth = new Date(year, month + 1, 0).getDate();
            const today = new Date();


            // 1. Render empty cells for padding
            for (let i = 0; i < startDayIndex; i++) {
                const emptyCell = document.createElement('div');
                emptyCell.className = 'date-cell text-gray-400';
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

                let cellClasses = ['date-cell', 'border', 'border-gray-200', 'text-gray-800'];

                if (dateString === todayFormatted) {
                    cellClasses.push('today', 'font-bold');
                }

                if (isPast) {
                    cell.setAttribute('disabled', 'true');
                    cellClasses = ['date-cell', 'text-gray-400', 'border', 'border-gray-100'];
                } else if (hasSlots) {
                    // Check if any slot on this date has capacity > 0
                    const isDateAvailable = this.groupedSlotsByDate[dateString].some(slot => slot.capacityAvailable > 0);

                    if (isDateAvailable) {
                        cellClasses.push('available', 'border-indigo-300');
                        // Bind handleDateClick using an arrow function to preserve 'this' context
                        cell.addEventListener('click', (e) => this.handleDateClick(e));
                    } else {
                        // Date has slots, but all are fully booked
                        cell.setAttribute('disabled', 'true');
                        cellClasses.push('text-gray-400', 'border-gray-100', 'opacity-50');
                    }
                } else {
                    // Date is future, but no slots available
                    cell.setAttribute('disabled', 'true');
                    cellClasses.push('text-gray-400', 'border-gray-100');
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
                this.slotTimeContainer.innerHTML = '<p id="slotMessage" class="col-span-full text-center text-gray-500">No hay horarios disponibles para esta fecha.</p>';
                return;
            }

            // Remove message if slots exist
            document.getElementById('slotMessage')?.remove();

            slots.forEach(slot => {
                const button = document.createElement('button');
                const available = slot.capacityAvailable > 0;

                button.type = 'button';

                // Base class
                let btnClasses = ['slot-time-btn', 'px-3', 'py-2', 'text-sm', 'bg-white', 'border', 'rounded-lg', 'transition-all', 'duration-150', 'font-medium'];

                if (available) {
                     btnClasses.push('border-indigo-300', 'text-indigo-600', 'hover:bg-indigo-50', 'hover:border-indigo-500');
                } else {
                     btnClasses.push('border-gray-300', 'text-gray-500', 'cursor-not-allowed', 'opacity-70');
                     button.disabled = true;
                }

                button.className = btnClasses.join(' ');

                const time = this.formatTime(slot.startDateTime);
                const capacityText = available
                    ? ` (${slot.capacityAvailable} lugar/es disponibles)`
                    : ' (Agotado)';

                button.textContent = time + capacityText;
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

            slotTimeIdInput.value = slotId || '';

            const slot = this.availableSlots.find(s => s.id === slotId);

            // Show quantity logic
            if (slot) {
                if (slot.capacityAvailable <= 1) {
                    quantityInput.value = 1;
                    quantityInput.disabled = true;
                } else {
                    quantityInput.disabled = false;
                    quantityInput.value = 1;
                    quantityInput.max = slot.capacityAvailable;
                }
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

            slotPriceDisplayDiv.classList.remove('bg-indigo-100', 'p-3', 'rounded-lg');
            slotPriceDisplayDiv.classList.add('text-gray-500', 'italic', 'text-center');

            if (this.selectedOffering && slot && slot.price !== null) {
                const totalPrice = slot.price * quantity;
                let priceHtml = `
                    <p class="text-lg font-semibold text-indigo-700">
                        Precio Total (${quantity} ${quantity > 1 ? 'personas' : 'persona'}):
                        <span class="text-2xl ml-2">$${totalPrice.toFixed(2)}</span>
                    </p>`;

                if (this.selectedOffering.advancePaymentPercentage > 0) {
                    const advanceAmount = totalPrice * (this.selectedOffering.advancePaymentPercentage / 100);
                    priceHtml += `
                        <p class="text-sm text-gray-600 mt-1">
                            Anticipo Requerido (${this.selectedOffering.advancePaymentPercentage}%):
                            <span class="font-medium text-indigo-500">$${advanceAmount.toFixed(2)}</span>
                        </p>`;
                }

                slotPriceDisplayDiv.innerHTML = priceHtml;
                slotPriceDisplayDiv.classList.add('bg-indigo-100', 'p-3', 'rounded-lg');
                slotPriceDisplayDiv.classList.remove('text-gray-500', 'italic', 'text-center');

            } else if (slot) {
                slotPriceDisplayDiv.innerHTML = `<p class="text-gray-600 font-medium text-center">
                    Horario seleccionado.
                </p>`;
            } else {
                slotPriceDisplayDiv.innerHTML = `<p class="text-gray-500 italic text-center">
                    Seleccione un horario.
                </p>`;
            }
        }


        async selectService(offering) {
            this.selectedOffering = offering;
            const selectedServiceInfo = document.getElementById('selectedServiceInfo');
            const dateMessage = document.getElementById('dateMessage');

            this.servicesContainer.classList.add('hidden');
            this.bookingPanel.classList.remove('hidden');
            window.scrollTo({ top: 0, behavior: 'smooth' });

            this.currentDate = new Date();
            this.selectedDateKey = null; // Clear previous selection
            this.slotTimeContainer.innerHTML = '<p id="slotMessage" class="col-span-full text-center text-gray-500">Selecciona una fecha en el calendario para ver los horarios.</p>';
            this.handleSlotSelection(null);
            this.availableSlots = [];
            this.groupedSlotsByDate = {};
            dateMessage.classList.remove('hidden');

            selectedServiceInfo.innerHTML = `
                <h4 class="text-xl font-bold text-gray-900 mb-2">${offering.name}</h4>
                <p class="text-gray-600 mb-3">${offering.description}</p>
            `;

            const slots = await this.fetchSlots(offering.id);
            this.availableSlots = slots;
            this.groupedSlotsByDate = this.groupSlotsByDate(slots);

            dateMessage.classList.add('hidden');
            this.renderCalendar();

            if (slots.length === 0 || slots.every(slot => slot.capacityAvailable == 0 || slot.capacityAvailable == null)) {
                this.slotTimeContainer.innerHTML = '<p class="col-span-full text-center text-red-500 font-semibold">Sin horarios disponibles actualmente.</p>';
                dateMessage.textContent = 'No hay fechas disponibles.';
                dateMessage.classList.remove('hidden');
            }

            this.bookingForm.reset();
        }

        cancelBooking() {
            this.bookingPanel.classList.add('hidden');
            this.servicesContainer.classList.remove('hidden');
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
                    title: 'Error de Validación',
                    text: 'Por favor, selecciona un horario y completa todos los campos del formulario.',
                    confirmButtonColor: '#fb923c'
                });
                return;
            }

            try {
                const createBookingButton = form.querySelector('button[type="submit"]');
                createBookingButton.textContent = 'Reservando...';
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
                    title: 'Reserva Exitosa',
                    html: `Tu cita para <strong>${this.selectedOffering.name}</strong> ha sido agendada con éxito. ¡Revisa tu correo para los detalles!`,
                    confirmButtonText: 'Aceptar',
                    confirmButtonColor: '#4f46e5'
                });

                this.cancelBooking();

            } catch (error) {
                console.error('Booking creation error:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Error al Reservar',
                    text: `No se pudo completar la reserva. ${error.message}`,
                    confirmButtonText: 'Entendido',
                    confirmButtonColor: '#ef4444'
                });
            } finally {
                const createBookingButton = form.querySelector('button[type="submit"]');
                createBookingButton.textContent = 'Confirmar Reserva';
                createBookingButton.disabled = !document.getElementById('slotTimeId').value;
            }
        }


        showLoading() {
            const loadingState = document.getElementById('loadingState');
            const errorState = document.getElementById('errorState');
            const noServicesState = document.getElementById('noServicesState');

            loadingState.classList.remove('hidden');
            errorState.classList.add('hidden');
            noServicesState.classList.add('hidden');
            this.servicesContainer.classList.add('hidden');
            this.bookingPanel.classList.add('hidden');
        }

        showError(message) {
            const loadingState = document.getElementById('loadingState');
            const errorState = document.getElementById('errorState');
            const noServicesState = document.getElementById('noServicesState');

            loadingState.classList.add('hidden');
            errorState.classList.remove('hidden');
            noServicesState.classList.add('hidden');
            this.servicesContainer.classList.add('hidden');
            this.bookingPanel.classList.add('hidden');

            const errorTitle = errorState.querySelector('h3');
            if (errorTitle && message) {
                errorTitle.textContent = message;
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

