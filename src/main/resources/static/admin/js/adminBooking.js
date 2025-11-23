
class AdminBookingManager {
    constructor() {
        this.baseUrl = BASE_URL

        const userDataStorage = JSON.parse(localStorage.getItem('userData'));
        this.userId = userDataStorage.id;
        this.selectedOffering = null;
        this.availableSlots = [];
        this.groupedSlotsByDate = {};
        this.currentDate = new Date();
        this.selectedDateKey = null;

        // UI References
        this.servicesContainer = document.getElementById('servicesContainer');
        this.bookingPanel = document.getElementById('bookingPanel');
        this.calendarGrid = document.getElementById('calendarGrid');
        this.currentMonthYear = document.getElementById('currentMonthYear');
        this.slotTimeContainer = document.getElementById('slotTimeContainer');
        this.bookingForm = document.getElementById('bookingForm');

        if (!this.userId) {
            this.showError('ID de usuario no encontrado. Asegúrese de que la URL contiene ?userId=...');
            return;
        }

        this.init();
    }

    init() {
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
        document.getElementById('backToServicesBtn').addEventListener('click', () => this.resetView());

        if (this.bookingForm) {
            this.bookingForm.addEventListener('submit', (e) => this.createBooking(e));
        }
    }

    async loadOfferings() {
        try {
            document.getElementById('loadingState').classList.remove('hidden');

            const response = await fetch(`${this.baseUrl}/users/${this.userId}/offerings`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error('Error loading services');

            const offerings = await response.json();
            this.renderOfferings(offerings);
        } catch (error) {
            console.error(error);
            document.getElementById('loadingState').classList.add('hidden');
            document.getElementById('errorState').classList.remove('hidden');
        }
    }

    async fetchSlots(offeringId) {
        try {
            const response = await fetch(`${this.baseUrl}/slot-time/offering/${offeringId}?page=0&pageSize=200`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) throw new Error('Error loading slots');

            const data = await response.json();
            return data.content || [];
        } catch (error) {
            Swal.fire('Error', 'No se pudieron cargar los horarios', 'error');
            return [];
        }
    }

    renderOfferings(offerings) {
        document.getElementById('loadingState').classList.add('hidden');
        const grid = document.getElementById('servicesGrid');
        grid.innerHTML = '';

        const activeOfferings = offerings.filter(o => o.enabled);

        activeOfferings.forEach(offering => {
            const card = document.createElement('div');
            card.className = 'bg-white rounded-xl shadow-lg border border-gray-100 p-6 hover:shadow-xl transition-all duration-300 cursor-pointer flex flex-col justify-between transform hover:-translate-y-1';
            card.onclick = () => this.selectService(offering);

            card.innerHTML = `
                <div>
                    <div class="flex items-start justify-between mb-3">
                        <h3 class="text-xl font-semibold text-gray-900">${offering.name}</h3>
                    </div>
                    <p class="text-gray-600 mb-4 line-clamp-3">${offering.description}</p>
                </div>
                <div class="mt-4">
                    <button class="w-full bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition-colors duration-200 font-medium shadow-md shadow-indigo-300">
                        Ver Disponibilidad
                    </button>
                </div>
            `;
            grid.appendChild(card);
        });

        this.servicesContainer.classList.remove('hidden');
    }

    async selectService(offering) {
        this.selectedOffering = offering;
        this.servicesContainer.classList.add('hidden');
        this.bookingPanel.classList.remove('hidden');

        const infoBox = document.getElementById('selectedServiceInfo');
        infoBox.innerHTML = `
            <h4 class="font-bold text-indigo-900 text-lg">${offering.name}</h4>
        `;

        const slots = await this.fetchSlots(offering.id);
        this.availableSlots = slots;
        this.groupSlotsByDate(slots);
        this.renderCalendar();
    }

    groupSlotsByDate(slots) {
        this.groupedSlotsByDate = {};
        slots.forEach(slot => {
            const dateKey = slot.startDateTime.substring(0, 10); // YYYY-MM-DD
            if (!this.groupedSlotsByDate[dateKey]) this.groupedSlotsByDate[dateKey] = [];
            this.groupedSlotsByDate[dateKey].push(slot);
        });
    }

    renderCalendar() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];

        this.currentMonthYear.textContent = `${monthNames[month]} ${year}`;
        this.calendarGrid.innerHTML = '';

        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const startOffset = (firstDay === 0 ? 6 : firstDay - 1);

        for (let i = 0; i < startOffset; i++) {
            const empty = document.createElement('div');
            empty.className = 'date-cell text-gray-400';
            this.calendarGrid.appendChild(empty);
        }

        const todayStr = new Date().toISOString().split('T')[0];

        for (let d = 1; d <= daysInMonth; d++) {
            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
            const hasSlots = this.groupedSlotsByDate[dateStr] && this.groupedSlotsByDate[dateStr].some(s => s.capacityAvailable > 0);

            const btn = document.createElement('button');
            btn.textContent = d;
            btn.type = 'button';

            let classes = ['date-cell', 'border', 'border-gray-200', 'text-gray-800'];

            if (dateStr === todayStr) {
                classes.push('today', 'font-bold');
            }

            if (dateStr === this.selectedDateKey) {
                classes.push('selected-date');
            }

            if (hasSlots) {
                classes.push('available', 'border-indigo-300', 'cursor-pointer');
                btn.onclick = () => this.selectDate(dateStr, btn);
            } else {
                btn.disabled = true;
                classes.push('text-gray-400', 'border-gray-100', 'opacity-50');
            }

            btn.className = classes.join(' ');
            this.calendarGrid.appendChild(btn);
        }
    }

    changeMonth(delta) {
        this.currentDate.setMonth(this.currentDate.getMonth() + delta);
        this.renderCalendar();
    }

    selectDate(dateStr, btnElement) {
        document.querySelectorAll('.date-cell').forEach(b => b.classList.remove('selected-date'));
        btnElement.classList.add('selected-date');
        this.selectedDateKey = dateStr;
        this.renderSlots(dateStr);
    }

    renderSlots(dateKey) {
        const container = document.getElementById('slotTimeContainer');
        container.innerHTML = '';
        const slots = this.groupedSlotsByDate[dateKey] || [];

        // Sort slots
        slots.sort((a, b) => a.startDateTime.localeCompare(b.startDateTime));

        if(slots.length === 0) {
             container.innerHTML = '<p class="col-span-full text-center text-gray-500">No hay horarios disponibles.</p>';
             return;
        }

        document.getElementById('slotMessage')?.remove();

        slots.forEach(slot => {
            const time = new Date(slot.startDateTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
            const btn = document.createElement('button');
            btn.type = 'button';

            const available = slot.capacityAvailable > 0;
            let btnClasses = ['slot-time-btn', 'px-3', 'py-2', 'text-sm', 'bg-white', 'border', 'rounded-lg', 'transition-all', 'duration-150', 'font-medium'];

            if (available) {
                btnClasses.push('border-indigo-300', 'text-indigo-600', 'hover:bg-indigo-50', 'hover:border-indigo-500');
                btn.onclick = () => this.selectSlot(slot, btn);
            } else {
                btnClasses.push('border-gray-300', 'text-gray-500', 'cursor-not-allowed', 'opacity-70');
                btn.disabled = true;
            }

            btn.className = btnClasses.join(' ');
            btn.textContent = time;
            container.appendChild(btn);
        });
    }

    selectSlot(slot, btnElement) {
        document.querySelectorAll('.slot-time-btn').forEach(b => b.classList.remove('selected'));
        btnElement.classList.add('selected');

        document.getElementById('slotTimeId').value = slot.id;
        document.getElementById('submitBookingBtn').disabled = false;

        this.updatePrice(slot);
    }

    updatePrice(slot) {
        const qtyInput = document.getElementById('quantity');
        const priceDisplay = document.getElementById('slotPriceDisplay');

        const updateCalc = () => {
            const qty = parseInt(qtyInput.value) || 1;
            const total = (slot.price || 0) * qty;

            priceDisplay.classList.remove('text-gray-500', 'italic', 'text-center');
            priceDisplay.classList.add('bg-indigo-100', 'p-3', 'rounded-lg');

            priceDisplay.innerHTML = `
                <p class="text-lg font-semibold text-indigo-700">
                    Precio Total: <span class="text-2xl ml-2">$${total.toFixed(2)}</span>
                </p>
            `;
        };

        qtyInput.max = slot.capacityAvailable;
        qtyInput.onchange = updateCalc;
        qtyInput.onkeyup = updateCalc;
        updateCalc();
    }

    async createBooking(e) {
        e.preventDefault();
        const btn = document.getElementById('submitBookingBtn');
        const originalText = btn.textContent;

        try {
            btn.disabled = true;
            btn.textContent = 'Procesando...';

            const formData = new FormData(e.target);
            const payload = {
                slotTimeId: formData.get('slotTimeId'),
                name: formData.get('name'),
                email: formData.get('email'),
                phoneNumber: formData.get('phoneNumber'),
                quantity: parseInt(formData.get('quantity'))
            };

            const response = await fetch(`${this.baseUrl}/booking/admin`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error al crear reserva');
            }

            await Swal.fire({
                icon: 'success',
                title: 'Reserva Creada',
                text: 'La reserva se ha registrado exitosamente.',
                confirmButtonColor: '#4f46e5' // Indigo 600
            });

            this.resetView();

        } catch (error) {
            console.error(error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: error.message,
                confirmButtonColor: '#d33'
            });
        } finally {
            btn.disabled = false;
            btn.textContent = originalText;
        }
    }

    resetView() {
        this.bookingForm.reset();
        this.selectedOffering = null;
        this.selectedDateKey = null;

        const priceDisplay = document.getElementById('slotPriceDisplay');
        priceDisplay.innerHTML = '<p class="text-gray-500 italic text-center">Selecciona un horario para ver el precio.</p>';
        priceDisplay.classList.remove('bg-indigo-100', 'p-3', 'rounded-lg');

        document.getElementById('submitBookingBtn').disabled = true;
        this.bookingPanel.classList.add('hidden');
        this.servicesContainer.classList.remove('hidden');
        this.loadOfferings();
    }

    showError(msg) {
        Swal.fire('Error', msg, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new AdminBookingManager();
});