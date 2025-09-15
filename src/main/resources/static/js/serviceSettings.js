document.addEventListener('DOMContentLoaded', () => {

    // --- UI Elements and State ---
    const navDropdownButton = document.getElementById('navDropdownButton');
    const navDropdownMenu = document.getElementById('navDropdownMenu');
    const logoutBtn = document.getElementById('logoutBtn');
    const serviceNameHeader = document.getElementById('serviceNameHeader');
    const calendarElement = document.getElementById('calendar');
    const currentMonthYearElement = document.getElementById('currentMonthYear');
    const prevMonthBtn = document.getElementById('prevMonthBtn');
    const nextMonthBtn = document.getElementById('nextMonthBtn');
    const availabilityForm = document.getElementById('availabilityForm');
    const availabilityTimeInput = document.getElementById('availabilityTime');
    const availabilityList = document.getElementById('availabilityList');
    const availabilityMessage = document.getElementById('availabilityMessage');
    const selectedDateText = document.getElementById('selectedDateText');

    let services = JSON.parse(localStorage.getItem('agendapp_services')) || [];
    let currentService = null;
    let currentDate = new Date();
    let selectedDates = [];

    // --- Initialization ---
    const urlParams = new URLSearchParams(window.location.search);
    const serviceId = urlParams.get('id');

    if (!serviceId) {
        window.location.href = 'dashboard.html';
    } else {
        currentService = services.find(s => s.id === serviceId);
        if (!currentService) {
            window.location.href = 'dashboard.html';
        }
        serviceNameHeader.textContent = `Configurar Disponibilidad para "${currentService.name}"`;
        renderCalendar();
    }

    // --- Event Listeners ---
    navDropdownButton.addEventListener('click', () => {
        navDropdownMenu.classList.toggle('hidden');
    });

    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('agendapp_token');
        window.location.href = 'index.html';
    });

    // Close dropdown if clicked outside of it
    window.addEventListener('click', (event) => {
        if (!navDropdownButton.contains(event.target) && !navDropdownMenu.contains(event.target)) {
            navDropdownMenu.classList.add('hidden');
        }
    });

    prevMonthBtn.addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar();
    });

    nextMonthBtn.addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar();
    });

    availabilityForm.addEventListener('submit', handleAvailabilitySubmit);
    availabilityList.addEventListener('click', handleSlotDelete);

    // --- Calendar and Availability Logic ---

    function renderCalendar() {
        calendarElement.innerHTML = '';
        currentMonthYearElement.textContent = currentDate.toLocaleString('es-ES', { month: 'long', year: 'numeric' });

        const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
        // Adjust to Monday = 0
        const startingDayOfWeek = firstDayOfMonth.getDay() === 0 ? 6 : firstDayOfMonth.getDay() - 1;
        const lastDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);

        // Render empty days for alignment
        for (let i = 0; i < startingDayOfWeek; i++) {
            const emptyDay = document.createElement('div');
            calendarElement.appendChild(emptyDay);
        }

        // Render days of the month
        for (let i = 1; i <= lastDayOfMonth.getDate(); i++) {
            const dayElement = document.createElement('div');
            const day = new Date(currentDate.getFullYear(), currentDate.getMonth(), i);
            const dateISO = day.toISOString().split('T')[0];

            dayElement.textContent = i;
            dayElement.className = 'calendar-day';
            dayElement.dataset.date = dateISO;

            // Highlight if selected
            if (selectedDates.includes(dateISO)) {
                dayElement.classList.add('selected');
            }

            dayElement.addEventListener('click', () => {
                const index = selectedDates.indexOf(dateISO);
                if (index > -1) {
                    selectedDates.splice(index, 1); // Deselect
                } else {
                    selectedDates.push(dateISO); // Select
                }
                updateSelectedDateText();
                renderCalendar();
            });
            calendarElement.appendChild(dayElement);
        }
    }

    function updateSelectedDateText() {
        if (selectedDates.length > 0) {
            selectedDates.sort();
            const dateStrings = selectedDates.map(date => new Date(date).toLocaleDateString('es-ES', { month: 'short', day: 'numeric' }));
            selectedDateText.textContent = `Fechas seleccionadas: ${dateStrings.join(', ')}`;
        } else {
            selectedDateText.textContent = 'Selecciona una o más fechas.';
        }
        renderAvailabilitySlots();
    }

    function renderAvailabilitySlots() {
        if (!currentService) return;

        availabilityList.innerHTML = '';
        availabilityMessage.classList.add('hidden');

        if (selectedDates.length === 0) {
            availabilityList.innerHTML = '<p class="text-gray-400 text-center py-4">Selecciona fechas en el calendario para añadir horarios.</p>';
            return;
        }

        const groupedSlots = currentService.availability
            .filter(slot => selectedDates.includes(slot.date))
            .reduce((acc, slot) => {
                if (!acc[slot.date]) {
                    acc[slot.date] = [];
                }
                acc[slot.date].push(slot);
                return acc;
            }, {});

        selectedDates.forEach(date => {
            const dateHeader = document.createElement('h4');
            dateHeader.className = 'font-semibold text-gray-700 mt-4 mb-2';
            dateHeader.textContent = new Date(date).toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
            availabilityList.appendChild(dateHeader);

            const timeGrid = document.createElement('div');
            timeGrid.className = 'grid grid-cols-3 gap-2';

            const slotsForDate = groupedSlots[date] || [];
            slotsForDate.sort((a, b) => a.time.localeCompare(b.time)).forEach(slot => {
                const buttonItem = document.createElement('button');
                buttonItem.className = 'time-slot-button px-3 py-1 bg-gray-200 rounded-md text-sm font-medium hover:bg-red-500 hover:text-white transition-colors duration-200';
                buttonItem.dataset.id = slot.id;
                buttonItem.dataset.date = slot.date;
                buttonItem.textContent = slot.time;
                timeGrid.appendChild(buttonItem);
            });

            if (slotsForDate.length === 0) {
                 const emptySlot = document.createElement('p');
                 emptySlot.className = 'col-span-3 text-sm text-gray-400 italic';
                 emptySlot.textContent = 'Aún no hay horarios añadidos para este día.';
                 timeGrid.appendChild(emptySlot);
            }

            availabilityList.appendChild(timeGrid);
        });
    }

    function handleAvailabilitySubmit(e) {
        e.preventDefault();

        const time = availabilityTimeInput.value;
        if (selectedDates.length === 0) {
            showMessage('Por favor, selecciona al menos una fecha en el calendario.', 'error');
            return;
        }

        if (!time) {
            showMessage('Por favor, introduce una hora.', 'error');
            return;
        }

        // Add slots for all selected dates
        selectedDates.forEach(date => {
            currentService.availability.push({
                id: crypto.randomUUID(),
                date,
                time
            });
        });

        // Save to localStorage
        localStorage.setItem('agendapp_services', JSON.stringify(services));

        // Update UI
        availabilityTimeInput.value = '';
        renderAvailabilitySlots();
        showMessage('Horario(s) añadido(s) con éxito.', 'success');
    }

    function handleSlotDelete(e) {
        const target = e.target;
        if (target.classList.contains('time-slot-button')) {
            const slotId = target.dataset.id;

            // Filter out the deleted slot
            currentService.availability = currentService.availability.filter(slot => slot.id !== slotId);

            // Save to localStorage
            localStorage.setItem('agendapp_services', JSON.stringify(services));

            renderAvailabilitySlots();
        }
    }

    function showMessage(message, type) {
        availabilityMessage.textContent = message;
        availabilityMessage.classList.remove('hidden', 'text-red-500', 'text-green-500');
        if (type === 'error') {
            availabilityMessage.classList.add('text-red-500');
        } else if (type === 'success') {
            availabilityMessage.classList.add('text-green-500');
        }
    }
});
