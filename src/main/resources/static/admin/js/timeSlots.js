document.addEventListener("DOMContentLoaded", () => {
            // --- Utility Functions ---
            const baseUrl = BASE_URL;

            /**
             * Converts YYYY-MM-DD date string to DD/MM/YYYY format (Argentina).
             * @param {string} dateString - The date string in YYYY-MM-DD format.
             * @returns {string} The formatted date.
             */
            const toArgentinaDateFormat = (dateString) => {
                if (!dateString) return 'N/A';
                const parts = dateString.split('-');
                if (parts.length === 3) {
                    return `${parts[2]}/${parts[1]}/${parts[0]}`;
                }
                return dateString;
            };

            /**
             * Extracts HH:mm time from a YYYY-MM-DDTHH:mm:ss string.
             * @param {string} dateTimeString - The ISO 8601 date-time string.
             * @returns {string} The time string in HH:mm format.
             */
            const toTimeFormat = (dateTimeString) => {
                if (!dateTimeString || dateTimeString.length < 16) return 'N/A';
                return dateTimeString.substring(11, 16);
            };

            /**
             * Formats a Date object into a YYYY-MM-DD string.
             * @param {Date} date - The Date object.
             * @returns {string} The formatted date string.
             */
            const getFormattedDate = (date) => {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            };

            /**
             * Formats the price for display.
             * @param {number|null|undefined} price - The price value.
             * @returns {string} The formatted price string.
             */
            const formatPrice = (price) => price !== null && price !== undefined && !isNaN(parseFloat(price)) ? `$${parseFloat(price).toFixed(2)}` : '';

            /**
             * Displays a user-friendly message in the dedicated message box.
             * @param {string} text - The message content.
             * @param {('info'|'success'|'error')} type - The type of message.
             */
            function displayMessage(text, type = 'info') {
                let icon;
                switch (type) {
                    case 'error':
                        icon = 'error';
                        break;
                    case 'success':
                        icon = 'success';
                        break;
                    case 'info':
                    default:
                        icon = 'info';
                        break;
                }

                Swal.fire({
                    text: text,
                    icon: icon,
                    toast: true,
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 4000,
                    timerProgressBar: false
                });
            }


            // --- DOM Elements ---
            const calendarGrid = document.getElementById("calendarGrid");
            const currentMonthYear = document.getElementById("currentMonthYear");
            const prevMonthBtn = document.getElementById("prevMonthBtn");
            const nextMonthBtn = document.getElementById("nextMonthBtn");
            const selectedDatesSummary = document.getElementById("selectedDatesSummary");
            const clearSelectionBtn = document.getElementById("clearSelectionBtn");

            const startTimeInput = document.getElementById("startTime");
            const endTimeInput = document.getElementById("endTime");
            const priceInput = document.getElementById("price");

            const applySlotsBtn = document.getElementById("applySlotsBtn");
            const datesAppliedCount = document.getElementById("datesAppliedCount");

            const pendingSlotsList = document.getElementById("pendingSlotsList");
            const saveSlotsBtn = document.getElementById("saveSlotsBtn");
            const pendingSlotCountSummary = document.getElementById("pendingSlotCountSummary");

            // Recurrence elements
            const manualSelectionToggle = document.getElementById("manualSelectionToggle");
            const recurrenceSelectionToggle = document.getElementById("recurrenceSelectionToggle");
            const manualSelectionContainer = document.getElementById("manualSelectionContainer");
            const recurrenceSelectionContainer = document.getElementById("recurrenceSelectionContainer");
            const recurrenceDayCheckboxes = document.getElementById("recurrenceDayCheckboxes");
            const recurrenceDurationInput = document.getElementById("recurrenceDuration");
            const previewRecurrenceBtn = document.getElementById("previewRecurrenceBtn");

            // Existing Slots Management Elements
            const existingSlotsGrid = document.getElementById("existingSlotsGrid");
            const existingSlotsLoading = document.getElementById("existingSlotsLoading");
            const existingSlotsLoadingIcon = document.getElementById("existingSlotsLoadingIcon");
            const refreshIcon = document.getElementById("refreshIcon");
            const existingSlotsCount = document.getElementById("existingSlotsCount");
            const refreshExistingSlotsBtn = document.getElementById("refreshExistingSlotsBtn");

            // Modification Indicator
            const modificationIndicator = document.getElementById("modificationIndicator");
            const modifyingSlotIdDisplay = document.getElementById("modifyingSlotId");
            const cancelModificationBtn = document.getElementById("cancelModificationBtn");

            // Pagination Elements
            const prevPageBtn = document.getElementById("prevPageBtn");
            const nextPageBtn = document.getElementById("nextPageBtn");
            const currentPageDisplay = document.getElementById("currentPageDisplay");
            const totalPagesDisplay = document.getElementById("totalPagesDisplay");


            // --- State Management ---
            const params = new URLSearchParams(window.location.search);
            const offeringId = params.get("offeringId");
            const currentOfferingId = offeringId;

            const FETCH_SLOTS_ENDPOINT = `${baseUrl}/slot-time/offering/${currentOfferingId}`;
            const DELETE_SLOT_ENDPOINT = `${baseUrl}/slot-time/`; // Will append slotId
            const CREATE_SLOT_ENDPOINT = `${baseUrl}/slot-time/list`;
            const UPDATE_SLOT_ENDPOINT = `${baseUrl}/slot-time/`; // Will append slotId (PUT)

            let currentDate = new Date();
            let selectedDates = new Set(); // Stores YYYY-MM-DD strings
            let pendingSlots = []; // New slots waiting to be saved

            // Modification State
            let slotIdToModify = null; // ID of the slot being modified

            // Pagination State
            let currentPage = 0; // Backend pages usually start at 0
            const pageSize = 5;
            let totalPages = 0;
            let existingSlots = []; // Slots fetched from the current page

            const today = new Date();
            today.setHours(0, 0, 0, 0); // Normalize today for comparison

            // --- Helper to clear modification state ---
            function clearModificationState() {
                slotIdToModify = null;
                // Revert button text and behavior
                applySlotsBtn.textContent = `Aplicar a ${selectedDates.size} Fechas`;
                // Clear selection and form only if it wasn't just cleared by applying
                selectedDates.clear();
                updateSummary();
                if (manualSelectionContainer.style.display !== 'none') {
                    renderCalendar();
                }
                startTimeInput.value = '';
                endTimeInput.value = '';
                priceInput.value = '';
            }

          function renderExistingSlots(existingSlots) {
              existingSlotsGrid.innerHTML = '';

              if (existingSlots.length === 0) {
                  existingSlotsGrid.innerHTML = '<div class="text-gray-400 text-center py-4">No hay slots en esta página.</div>';
              } else {
                  existingSlots.forEach(slot => {
                      const datePart = slot.startDateTime.substring(0, 10);
                      const startTimePart = toTimeFormat(slot.startDateTime);
                      const endTimePart = toTimeFormat(slot.endDateTime);
                      const price = slot.price;
                      const capacityAvailable = slot.capacityAvailable;
                      const maxCapacity = slot.maxCapacity;

                      let capacityLabel = '';
                      if (capacityAvailable != null && maxCapacity != null) {
                          const isFull = capacityAvailable === 0;
                          const labelColor = isFull ? 'text-red-600' : 'text-gray-900';
                          capacityLabel = `
                              <div class="${labelColor} font-semibold text-sm">
                                  Disponibilidad (${capacityAvailable}/${maxCapacity})
                              </div>`;
                      }

                      const slotItem = document.createElement('div');
                      slotItem.className = 'slot-item border border-gray-200 rounded-xl p-3 shadow-sm hover:shadow transition-shadow bg-white';
                      slotItem.innerHTML = `
                          <div class="slot-details flex-1 min-w-0">
                              <div class="text-gray-900 font-semibold text-base">${toArgentinaDateFormat(datePart)}</div>
                              <div class="text-sm text-gray-700">${startTimePart} a ${endTimePart}</div>
                              <div class="text-indigo-600 font-medium">${formatPrice(price)}</div>
                              ${capacityLabel}
                          </div>
                          <div class="slot-actions flex space-x-2 mt-2 sm:mt-0">
                              <button data-id="${slot.id}"
                                      data-start-time="${startTimePart}"
                                      data-end-time="${endTimePart}"
                                      data-date="${datePart}"
                                      data-price="${price || ''}"
                                      data-full-slot='${JSON.stringify(slot)}'
                                      class="modify-slot-btn text-blue-500 hover:text-blue-700 p-1 rounded-full hover:bg-blue-50 transition-colors"
                                      title="Modificar Slot">
                                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                                  </svg>
                              </button>
                              <button data-id="${slot.id}"
                                      class="delete-slot-btn text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-50 transition-colors"
                                      title="Eliminar Slot">
                                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                  </svg>
                              </button>
                          </div>
                      `;
                      existingSlotsGrid.appendChild(slotItem);
                  });
              }

              currentPageDisplay.textContent = currentPage + 1;
              totalPagesDisplay.textContent = totalPages;
              prevPageBtn.disabled = currentPage === 0;
              nextPageBtn.disabled = currentPage >= totalPages - 1 || totalPages === 0;

              existingSlotsCount.textContent = `${existingSlots.length} (Pág ${currentPage + 1}/${totalPages})`;
          }

            /**
             * Handles setting the form for modification of an existing slot.
             * @param {object} slotData - The full slot object.
             */
            function handleModifyExistingSlot(slotData) {
                // 1. Set modification state
                slotIdToModify = slotData.id;

                // 2. Populate form fields
                const datePart = slotData.startDateTime.substring(0, 10);
                const startTimePart = toTimeFormat(slotData.startDateTime);
                const endTimePart = toTimeFormat(slotData.endDateTime);

                // Clear existing selections and form fields first
                selectedDates.clear();

                startTimeInput.value = startTimePart;
                endTimeInput.value = endTimePart;
                priceInput.value = slotData.price || '';

                // 3. Set the date for modification (using manual selection mode)
                setSelectionMode('manual');
                selectedDates.add(datePart);

                // 4. Update UI to reflect modification mode
                updateSummary();
                renderCalendar();

                // Change apply button text
                applySlotsBtn.textContent = `Actualizar Slot`;

                displayMessage(`Slot agregado para modificación. Cambia la fecha/hora/precio y haz clic en 'Actualizar'.`, 'info');
            }


            /**
             * Handles fetching existing slots with pagination.
             * @param {number} page - The page number to fetch (0-indexed).
             */
            async function fetchExistingSlots(page = currentPage) {
                const token = localStorage.getItem("authToken");
                if (!token) {
                    displayMessage("Error: No se encontró token de autenticación para cargar slots.", "error");
                    return;
                }

                existingSlotsGrid.innerHTML = '';
                existingSlotsLoading.classList.remove('hidden');
                refreshExistingSlotsBtn.disabled = true;
                refreshIcon.classList.add('hidden');
                existingSlotsLoadingIcon.classList.remove('hidden');

                const endpointWithPagination = `${FETCH_SLOTS_ENDPOINT}?page=${page}&pageSize=${pageSize}`;

                try {
                    const response = await fetch(endpointWithPagination, {
                        method: "GET",
                        headers: {
                            "Authorization": `Bearer ${token}`
                        }
                    });

                    if (!response.ok) {
                        const errorText = await response.text();
                        throw new Error(`(${response.status}) Error al cargar slots: ${errorText.substring(0, 100)}...`);
                    }

                    const pageData = await response.json();

                    existingSlots = pageData.content || [];
                    totalPages = pageData.totalPages || 1;
                    currentPage = pageData.pageable.pageNumber || 0;
                    renderExistingSlots(existingSlots);

                } catch (error) {
                    console.error("Error fetching existing slots:", error);
                    existingSlotsGrid.innerHTML = `<div class="text-red-500 text-center py-4">Error al cargar: ${error.message}</div>`;
                    displayMessage("Error al cargar slots existentes.", "error");
                } finally {
                    refreshExistingSlotsBtn.disabled = false;
                    existingSlotsLoading.classList.add('hidden');
                    existingSlotsLoadingIcon.classList.add('hidden');
                    refreshIcon.classList.remove('hidden');
                }
            }

            /**
             * Handles page navigation.
             * @param {number} newPage - The target page number (0-indexed).
             */
            function goToPage(newPage) {
                if (newPage >= 0 && newPage < totalPages) {
                    currentPage = newPage;
                    fetchExistingSlots(currentPage);
                }
            }


            // --- Action Handlers for Existing Slots ---

            /**
             * Handles the deletion of an existing slot.
             * @param {string} slotId - The ID of the slot to delete.
             */
            async function handleDeleteExistingSlot(slotId) {
                Swal.fire({
                    title: '¿Estás seguro?',
                    text: `¿Deseas eliminar el slot con ID ${slotId}? Esta acción es irreversible.`,
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#d33',
                    cancelButtonColor: '#3085d6',
                    confirmButtonText: 'Sí, eliminar',
                    cancelButtonText: 'Cancelar'
                }).then(async (result) => {
                    if (!result.isConfirmed) {
                        return;
                    }

                    const token = localStorage.getItem("authToken");
                    if (!token) {
                        displayMessage("Error: No se encontró token de autenticación.", "error");
                        return;
                    }

                    try {
                        const response = await fetch(DELETE_SLOT_ENDPOINT + slotId, {
                            method: "DELETE",
                            headers: { "Authorization": `Bearer ${token}` }
                        });

                        if (!response.ok) {
                            const errorText = await response.text();
                            let errorBody = {};
                            try {
                                errorBody = JSON.parse(errorText);
                            } catch {
                                // Not JSON — ignore, keep as empty object
                            }

                            const { errorCode, details, message } = errorBody ?? {};

                            if (errorCode === 'OFFERING_HAS_ACTIVE_BOOKINGS') {
                                const count = details?.count ?? '?';
                                const message =
                                    count === 1
                                        ? `El slot tiene 1 reserva activa. Debe cancelarla antes de eliminarlo.`
                                        : `El slot tiene ${count} reservas activas. Debe cancelarlas antes de eliminarlo.`;

                                displayMessage(message, 'error');

                                return;
                            }

                            displayMessage(message || 'Error al eliminar el servicio', 'error');

                            throw new Error(`(${response.status}) Error al eliminar: ${errorText.substring(0, 100)}...`);
                        }

                        displayMessage(`Slot ID ${slotId} eliminado correctamente.`, "success");
                        fetchExistingSlots(currentPage);

                    } catch (error) {
                        console.error("Error deleting slot:", error);
                        displayMessage("Error al eliminar slot: " + error.message, "error");
                    }
                });
            }


            // --- Event Listeners for Pagination and Existing Slots ---
            prevPageBtn.addEventListener('click', () => goToPage(currentPage - 1));
            nextPageBtn.addEventListener('click', () => goToPage(currentPage + 1));
            refreshExistingSlotsBtn.addEventListener('click', () => fetchExistingSlots(currentPage));

            existingSlotsGrid.addEventListener('click', (event) => {
                const deleteButton = event.target.closest('.delete-slot-btn');
                const modifyButton = event.target.closest('.modify-slot-btn');

                if (deleteButton) {
                    const slotId = deleteButton.dataset.id;
                    handleDeleteExistingSlot(slotId);
                }

                if (modifyButton) {
                    const slotData = JSON.parse(modifyButton.dataset.fullSlot);
                    handleModifyExistingSlot(slotData);
                }
            });

            function updateSummary() {
                const sortedDates = Array.from(selectedDates).sort();

                selectedDatesSummary.innerHTML = sortedDates.length > 0
                    ? sortedDates.map(date => `<span class="bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full text-xs font-medium">${toArgentinaDateFormat(date)}</span>`).join('')
                    : '<span class="text-gray-500">Ninguna</span>';

                const count = sortedDates.length;
                datesAppliedCount.textContent = count;
                applySlotsBtn.disabled = count === 0 && slotIdToModify === null;

                if (slotIdToModify !== null) {
                    applySlotsBtn.textContent = `Actualizar Slot`;
                } else {
                    applySlotsBtn.textContent = `Aplicar a ${count} Fechas`;
                }

                clearSelectionBtn.disabled = count === 0;
            }

            function updatePendingSlotsList() {
                pendingSlotsList.innerHTML = '';

                if (pendingSlots.length === 0) {
                    pendingSlotsList.innerHTML = '<li class="text-gray-400 text-center py-4">Aplica un horario para ver los nuevos slots aquí.</li>';
                } else {
                    pendingSlots.forEach((slot, index) => {
                        const datePart = slot.startDateTime.substring(0, 10);
                        const startTimePart = slot.startDateTime.substring(11, 16);
                        const endTimePart = slot.endDateTime.substring(11, 16);

                        const li = document.createElement('li');
                        li.className = 'flex justify-between items-center p-2 bg-white rounded-lg border border-gray-200 shadow-sm';
                        li.innerHTML = `
                            <div class="truncate">
                                <span class="font-semibold text-indigo-600">${toArgentinaDateFormat(datePart)}</span>:
                                ${startTimePart} a ${endTimePart}
                                <span class="text-gray-500 ml-2">(${formatPrice(slot.price)})</span>
                            </div>
                            <button data-index="${index}" class="remove-slot-btn text-red-500 hover:text-red-700 ml-2 p-1 rounded-full hover:bg-red-50 transition-colors" title="Eliminar este slot">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                            </button>
                        `;
                        pendingSlotsList.appendChild(li);
                    });
                }

                const count = pendingSlots.length;
                pendingSlotCountSummary.textContent = `${count} pendientes`;
                saveSlotsBtn.disabled = count === 0;
            }

            function handleDateClick(event) {
                const dateElement = event.currentTarget;
                const date = dateElement.dataset.date;

                if (slotIdToModify) {
                    // In modification mode, only one date can be selected (the new date)
                    selectedDates.clear();
                    // Re-render calendar to deselect all first
                    renderCalendar();

                    selectedDates.add(date);
                    dateElement.classList.add('selected-date');

                } else {
                    // In creation mode, multiple dates can be selected
                    if (selectedDates.has(date)) {
                        selectedDates.delete(date);
                        dateElement.classList.remove('selected-date');
                    } else {
                        selectedDates.add(date);
                        dateElement.classList.add('selected-date');
                    }
                }
                updateSummary();
            }

            function renderCalendar() {
                calendarGrid.innerHTML = '';
                const monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];

                const year = currentDate.getFullYear();
                const month = currentDate.getMonth();

                currentMonthYear.textContent = `${monthNames[month]} ${year}`;

                const firstDayOfMonth = new Date(year, month, 1);
                const startDayIndex = (firstDayOfMonth.getDay() + 6) % 7;
                const lastDayOfMonth = new Date(year, month + 1, 0).getDate();

                for (let i = 0; i < startDayIndex; i++) {
                    const emptyCell = document.createElement('div');
                    emptyCell.className = 'date-cell text-gray-400 opacity-0';
                    calendarGrid.appendChild(emptyCell);
                }

                for (let day = 1; day <= lastDayOfMonth; day++) {
                    const date = new Date(year, month, day);
                    const dateString = getFormattedDate(date);

                    const cell = document.createElement('button');
                    cell.type = 'button';
                    cell.textContent = day;
                    cell.dataset.date = dateString;
                    cell.className = 'date-cell border border-gray-100 hover:bg-indigo-50 text-gray-800';

                    if (dateString === getFormattedDate(new Date())) {
                        cell.classList.add('today', 'font-bold');
                    }

                    if (date < today) {
                        cell.setAttribute('disabled', 'true');
                        cell.classList.add('text-gray-400');
                    } else {
                        cell.addEventListener('click', handleDateClick);
                    }

                    if (selectedDates.has(dateString)) {
                        cell.classList.add('selected-date');
                    }

                    calendarGrid.appendChild(cell);
                }
            }

            function setSelectionMode(mode) {
                if (mode === 'manual') {
                    manualSelectionToggle.classList.add('bg-indigo-600', 'text-white');
                    manualSelectionToggle.classList.remove('text-gray-700', 'hover:bg-gray-200');
                    manualSelectionToggle.setAttribute('aria-selected', 'true');

                    recurrenceSelectionToggle.classList.remove('bg-indigo-600', 'text-white');
                    recurrenceSelectionToggle.classList.add('text-gray-700', 'hover:bg-gray-200');
                    recurrenceSelectionToggle.setAttribute('aria-selected', 'false');

                    manualSelectionContainer.style.display = 'block';
                    recurrenceSelectionContainer.style.display = 'none';
                    renderCalendar();
                } else {
                    recurrenceSelectionToggle.classList.add('bg-indigo-600', 'text-white');
                    recurrenceSelectionToggle.classList.remove('text-gray-700', 'hover:bg-gray-200');
                    recurrenceSelectionToggle.setAttribute('aria-selected', 'true');

                    manualSelectionToggle.classList.remove('bg-indigo-600', 'text-white');
                    manualSelectionToggle.classList.add('text-gray-700', 'hover:bg-gray-200');
                    manualSelectionToggle.setAttribute('aria-selected', 'false');

                    manualSelectionContainer.style.display = 'none';
                    recurrenceSelectionContainer.style.display = 'block';
                }
                // Clear selection unless we are in modification mode which already set the date
                if (slotIdToModify === null) {
                    selectedDates.clear();
                }
                updateSummary();
            }

            function handlePreviewRecurrence() {
                const checkedDays = Array.from(recurrenceDayCheckboxes.querySelectorAll('input:checked'))
                    .map(input => parseInt(input.value));

                const duration = parseInt(recurrenceDurationInput.value);

                if (checkedDays.length === 0) {
                    displayMessage("Por favor, selecciona al menos un día de la semana para la recurrencia.", 'error');
                    return;
                }

                if (duration < 7 || isNaN(duration)) {
                    displayMessage("La duración mínima debe ser de 7 días y debe ser un número válido.", 'error');
                    return;
                }

                if (slotIdToModify !== null) {
                    displayMessage("No puedes usar recurrencia en modo de modificación.", 'error');
                    return;
                }

                selectedDates.clear();

                let datesFound = 0;

                for (let i = 1; i <= duration; i++) {
                    const date = new Date(today);
                    date.setDate(today.getDate() + i);

                    if (checkedDays.includes(date.getDay())) {
                        selectedDates.add(getFormattedDate(date));
                        datesFound++;
                    }
                }

                displayMessage(`Se han seleccionado ${datesFound} fechas coincidentes en los próximos ${duration} días.`, 'success');
                updateSummary();
            }

            /**
             * Handles both creating new pending slots (POST) or updating an existing slot (PUT).
             */
            async function handleCreateOrUpdateSlot() {
                const startTime = startTimeInput.value;
                const endTime = endTimeInput.value;
                const priceValue = priceInput.value === "" ? null : parseFloat(priceInput.value);

                if (!startTime || !endTime) {
                    displayMessage("Por favor, define la hora de inicio y fin.", 'error');
                    return;
                }

                if (startTime >= endTime) {
                    displayMessage("La hora de inicio debe ser anterior a la hora de fin.", 'error');
                    return;
                }

                const token = localStorage.getItem("authToken");
                if (!token) {
                    displayMessage("Error: No se encontró token de autenticación.", "error");
                    return;
                }

                if (slotIdToModify) {
                    // --- UPDATE (PUT) MODE ---
                    if (selectedDates.size !== 1) {
                        displayMessage("Debes seleccionar exactamente una nueva fecha para la modificación.", 'error');
                        return;
                    }
                    const date = Array.from(selectedDates)[0];
                    const startDateTime = `${date}T${startTime}:00`;
                    const endDateTime = `${date}T${endTime}:00`;

                    const payload = {
                        id: slotIdToModify,
                        offeringId: currentOfferingId,
                        startDateTime: startDateTime,
                        endDateTime: endDateTime,
                        price: priceValue,
                    };

                    applySlotsBtn.disabled = true;
                    applySlotsBtn.textContent = "Actualizando...";

                   try {
                       const response = await fetch(UPDATE_SLOT_ENDPOINT + slotIdToModify, {
                           method: "PUT",
                           headers: {
                               "Content-Type": "application/json",
                               "Authorization": `Bearer ${token}`
                           },
                           body: JSON.stringify(payload)
                       });

                       if (!response.ok) {
                           const errorText = await response.text();
                           let errorBody = {};
                           try {
                               errorBody = JSON.parse(errorText);
                           } catch {
                               // Not JSON — ignore
                           }

                           const { errorCode, details, message } = errorBody ?? {};

                           switch (errorCode) {
                               case 'OFFERING_HAS_ACTIVE_BOOKINGS': {
                                   const count = details?.count ?? '?';
                                   const msg =
                                       count === 1
                                           ? `El slot tiene 1 reserva activa. Debe cancelarla para poder actualizarlo.`
                                           : `El slot tiene ${count} reservas activas. Debe cancelarlas para poder actualizarlo.`;
                                   displayMessage(msg, 'error');
                                   clearModificationState();
                                   return;
                               }

                               case 'SLOT_TIME_OVERLAPPED': {
                                   const start = details?.startDateTime ?? '';
                                   const end = details?.endDateTime ?? '';
                                   const msg = `El horario ingresado se superpone con otro existente (${start} - ${end}).`;
                                   displayMessage(msg, 'error');
                                   clearModificationState();
                                   return;
                               }

                               default: {
                                   clearModificationState();
                                   throw new Error(`(${response.status}) Error al actualizar: ${errorText.substring(0, 100)}...`);
                               }
                           }
                       }

                       displayMessage(`Slot ID ${slotIdToModify} actualizado correctamente.`, "success");
                       clearModificationState();
                       fetchExistingSlots(currentPage); // Refresh current page

                   } catch (error) {
                       console.error("Error updating slot:", error);
                       displayMessage("Error al actualizar slot: " + error.message, "error");
                       applySlotsBtn.disabled = false;
                       applySlotsBtn.textContent = `Actualizar Slot`;
                   }

                } else {
                    // --- CREATE (POST to PENDING) MODE ---
                    if (selectedDates.size === 0) {
                        displayMessage("Por favor, selecciona al menos una fecha antes de aplicar el horario.", 'error');
                        return;
                    }

                    const newSlots = Array.from(selectedDates).map(date => {
                        const startDateTime = `${date}T${startTime}:00`;
                        const endDateTime = `${date}T${endTime}:00`;

                        return {
                            startDateTime: startDateTime,
                            endDateTime: endDateTime,
                            price: priceValue,
                            date: date,
                            startTime: startTime,
                            endTime: endTime,
                            offeringId: currentOfferingId,
                        };
                    });

                    pendingSlots.push(...newSlots);
                    selectedDates.clear();

                    updateSummary();
                    if (manualSelectionContainer.style.display !== 'none') {
                        renderCalendar();
                    }
                    updatePendingSlotsList();

                    displayMessage(`Se han añadido ${newSlots.length} slots a la lista pendiente.`, 'success');
                }
            }

            // Renaming the old handleApplySlots function
            applySlotsBtn.addEventListener('click', handleCreateOrUpdateSlot);


            function handleRemoveSlot(index) {
                try {
                    if (index >= 0 && index < pendingSlots.length) {
                        pendingSlots.splice(index, 1);
                        updatePendingSlotsList();
                        displayMessage("Slot eliminado de la lista pendiente.", 'info');
                    }
                } catch (error) {
                    console.error("Error removing slot:", error);
                    displayMessage("Ocurrió un error al intentar eliminar el slot.", 'error');
                }
            }


            async function handleSaveSlots() {
                if (pendingSlots.length === 0) {
                    displayMessage("No hay slots pendientes para guardar.", 'error');
                    return;
                }

                const token = localStorage.getItem("authToken");
                if (!token) {
                    displayMessage("Error: No se encontró token de autenticación.", "error");
                    return;
                }

                saveSlotsBtn.disabled = true;
                saveSlotsBtn.textContent = "Guardando...";

                try {
                    const response = await fetch(CREATE_SLOT_ENDPOINT, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`
                        },
                        body: JSON.stringify(
                            pendingSlots.map(slot => ({
                                offeringId: currentOfferingId,
                                startDateTime: slot.startDateTime,
                                endDateTime: slot.endDateTime,
                                price: slot.price || null
                            }))
                        )
                    });

                    if (!response.ok) {
                        const contentType = response.headers.get("content-type");
                        let errorMessage = "Error desconocido";

                        if (contentType && contentType.includes("application/json")) {
                            const errorJson = await response.json();
                            const { errorCode, details } = errorJson;

                            switch (errorCode) {
                                case "SLOT_TIME_OVERLAPPED":
                                    const start = new Date(details?.startDateTime).toLocaleString();
                                    const end = new Date(details?.endDateTime).toLocaleString();
                                    errorMessage = `El rango de horario se superpone con otro: ${start} - ${end}.`;
                                    break;
                                default:
                                    errorMessage = `Error: ${errorCode || 'Desconocido'}`;
                                    break;
                            }
                        } else {
                            errorMessage = await response.text();
                        }

                        throw new Error(errorMessage);
                    }

                    const savedSlots = await response.json();
                    displayMessage(`¡Éxito! ${savedSlots.length} slots guardados correctamente.`, "success");

                    pendingSlots = [];
                    updatePendingSlotsList();
                    fetchExistingSlots(0);

                } catch (error) {
                    console.error("Error guardando slots:", error);
                    displayMessage(error.message || "Error al guardar slots.", "error");
                } finally {
                    saveSlotsBtn.disabled = false;
                    saveSlotsBtn.textContent = "Guardar Nuevos Slots";
                }
            }


            // --- Other Event Listeners ---
            prevMonthBtn.addEventListener('click', () => {
                currentDate.setMonth(currentDate.getMonth() - 1);
                renderCalendar();
            });

            nextMonthBtn.addEventListener('click', () => {
                currentDate.setMonth(currentDate.getMonth() + 1);
                renderCalendar();
            });

            manualSelectionToggle.addEventListener('click', () => setSelectionMode('manual'));
            recurrenceSelectionToggle.addEventListener('click', () => setSelectionMode('recurrence'));
            previewRecurrenceBtn.addEventListener('click', handlePreviewRecurrence);

            clearSelectionBtn.addEventListener('click', () => {
                selectedDates.clear();
                slotIdToModify = null;
                if (manualSelectionContainer.style.display !== 'none') {
                    renderCalendar();
                }
                updateSummary();
                displayMessage("Selección de fechas limpiada.", 'info');
            });

            saveSlotsBtn.addEventListener('click', handleSaveSlots);

            pendingSlotsList.addEventListener('click', (event) => {
                const removeButton = event.target.closest('.remove-slot-btn');
                if (removeButton) {
                    const index = parseInt(removeButton.dataset.index);
                    handleRemoveSlot(index);
                }
            });

            // --- Initialization ---
            renderCalendar();
            updateSummary();
            updatePendingSlotsList();
            fetchExistingSlots(currentPage);
        });