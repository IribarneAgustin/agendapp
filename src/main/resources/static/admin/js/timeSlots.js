document.addEventListener("DOMContentLoaded", () => {
    const baseUrl = BASE_URL;

    const toArgentinaDateFormat = (dateString) => {
        if (!dateString) return 'N/A';
        const parts = dateString.split('-');
        if (parts.length === 3) {
            return `${parts[2]}/${parts[1]}/${parts[0]}`;
        }
        return dateString;
    };

    const toTimeFormat = (dateTimeString) => {
        if (!dateTimeString || dateTimeString.length < 16) return 'N/A';
        return dateTimeString.substring(11, 16);
    };

    const getFormattedDate = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const formatPrice = (price) => price !== null && price !== undefined && !isNaN(parseFloat(price)) ? `$${parseFloat(price).toFixed(2)}` : '';

    function displayMessage(text, type = 'info') {
        let icon;
        switch (type) {
            case 'error': icon = 'error'; break;
            case 'success': icon = 'success'; break;
            case 'info':
            default: icon = 'info'; break;
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

    const calendarGrid = document.getElementById("calendarGrid");
    const currentMonthYear = document.getElementById("currentMonthYear");
    const prevMonthBtn = document.getElementById("prevMonthBtn");
    const nextMonthBtn = document.getElementById("nextMonthBtn");

    const startTimeInput = document.getElementById("startTime");
    const endTimeInput = document.getElementById("endTime");
    const priceInput = document.getElementById("price");

    const applySlotsBtn = document.getElementById("applySlotsBtn");
    const datesAppliedCount = document.getElementById("datesAppliedCount");

    const pendingSlotsList = document.getElementById("pendingSlotsList");
    const saveSlotsBtn = document.getElementById("saveSlotsBtn");

    if (pendingSlotsList) pendingSlotsList.parentElement.style.display = 'none';
    if (saveSlotsBtn) saveSlotsBtn.style.display = 'none';

    const manualSelectionToggle = document.getElementById("manualSelectionToggle");
    const recurrenceSelectionToggle = document.getElementById("recurrenceSelectionToggle");
    const manualSelectionContainer = document.getElementById("manualSelectionContainer");
    const recurrenceSelectionContainer = document.getElementById("recurrenceSelectionContainer");
    const recurrenceDayCheckboxes = document.getElementById("recurrenceDayCheckboxes");
    const recurrenceDurationInput = document.getElementById("recurrenceDuration");

    const existingSlotsGrid = document.getElementById("existingSlotsGrid");
    const existingSlotsLoading = document.getElementById("existingSlotsLoading");
    const refreshExistingSlotsBtn = document.getElementById("refreshExistingSlotsBtn");

    const prevPageBtn = document.getElementById("prevPageBtn");
    const nextPageBtn = document.getElementById("nextPageBtn");
    const currentPageDisplay = document.getElementById("currentPageDisplay");
    const totalPagesDisplay = document.getElementById("totalPagesDisplay");

    const params = new URLSearchParams(window.location.search);
    const offeringId = params.get("offeringId");
    const currentOfferingId = offeringId;

    let userId = null;
    try {
        const userDataStorage = JSON.parse(localStorage.getItem('userData'));
        userId = userDataStorage ? userDataStorage.id : null;
    } catch (e) {
        console.error("Error parsing userData from localStorage", e);
    }

    const FETCH_SLOTS_ENDPOINT = `${baseUrl}/slot-time/offering/${currentOfferingId}`;
    const DELETE_SLOT_ENDPOINT = `${baseUrl}/slot-time/`;
    const CREATE_SLOT_ENDPOINT = `${baseUrl}/slot-time/list`;
    const UPDATE_SLOT_ENDPOINT = `${baseUrl}/slot-time/`;

    let currentDate = new Date();
    let selectedDates = new Set();
    let slotIdToModify = null;
    let currentPage = 0;
    const pageSize = 5;
    let totalPages = 0;
    let existingSlots = [];

    let resources = [];
    let selectedResource = null;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    async function loadResources() {
        if (!userId) {
            console.error("No User ID found in localStorage");
            displayMessage("Error de sesión: No se identificó al usuario.", "error");
            existingSlotsLoading?.classList.add('hidden');
            return;
        }

        try {
            const token = localStorage.getItem("authToken");

            const response = await fetch(`${baseUrl}/resource/user/${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to load resources');
            }

            resources = await response.json();

            if (resources.length === 0) {
                selectedResource = null;
                displayMessage("No tienes profesionales creados.", "info");
                return;
            }

            selectedResource = resources.find(r => r.isDefault) || resources[0];

            renderResourceSelector();

            fetchExistingSlots(0);

        } catch (error) {
            console.error('Error loading resources:', error);
            displayMessage("Error cargando profesionales.", "error");
            resources = [];
            selectedResource = null;
            existingSlotsLoading?.classList.add('hidden');
        }
    }


    function renderResourceSelector() {
        const container = document.getElementById("resourceSelectorContainer");
        const select = document.getElementById("resourceSelect");

        if (!container || !select) {
            console.warn("Resource selector HTML not found");
            return;
        }

        select.innerHTML = "";

        if (!resources || resources.length === 0) {
            container.classList.add("hidden");
            selectedResource = null;
            return;
        }

        container.classList.remove("hidden");

        const isSingleResource = resources.length === 1;

        resources.forEach(res => {
            const option = document.createElement("option");
            option.value = res.id;
            option.textContent = `${res.name} ${res.lastName}`;
            select.appendChild(option);
        });

        // Select default
        selectedResource =
            resources.find(r => r.isDefault) || resources[0];

        select.value = selectedResource.id;
        select.disabled = isSingleResource;
        select.className =
            "w-full px-3 py-2 border rounded-lg shadow-sm text-sm transition " +
            (isSingleResource
                ? "bg-gray-100 text-gray-500 cursor-not-allowed border-gray-200"
                : "bg-white border-gray-300 focus:ring-indigo-500 focus:border-indigo-500");

        select.onchange = null;
    }

    function clearModificationState() {
        slotIdToModify = null;
        applySlotsBtn.textContent = `Aplicar a ${selectedDates.size} Fechas`;
        applySlotsBtn.disabled = selectedDates.size === 0;
        selectedDates.clear();
        updateSummary();
        if (manualSelectionContainer.style.display !== 'none') {
            renderCalendar();
        }
        startTimeInput.value = '';
        endTimeInput.value = '';
        priceInput.value = '';
    }

    async function fetchExistingSlots(page = currentPage) {
        const token = localStorage.getItem("authToken");

        existingSlotsLoading.classList.remove('hidden');
        existingSlotsGrid.innerHTML = '';

        try {
            if (!token) {
                throw new Error("No auth token");
            }

            if (!selectedResource) {
                existingSlotsGrid.innerHTML =
                    '<div class="text-gray-400 text-center py-4">Selecciona un profesional para ver sus horarios.</div>';
                return;
            }

            if (page < 0) page = 0;
            if (page >= totalPages && totalPages > 0) page = totalPages - 1;

            const endpoint = `${FETCH_SLOTS_ENDPOINT}/resource/${selectedResource.id}?page=${page}&pageSize=${pageSize}`;

            const response = await fetch(endpoint, {
                method: "GET",
                headers: { "Authorization": `Bearer ${token}` }
            });

            if (!response.ok) {
                throw new Error("Error fetching slots");
            }

            const pageData = await response.json();

            existingSlots = pageData.content || [];
            totalPages = pageData.totalPages || 1;
            currentPage = pageData.pageable?.pageNumber ?? 0;

            renderExistingSlots(existingSlots);

        } catch (error) {
            console.error(error);
            existingSlotsGrid.innerHTML =
                '<div class="text-red-400 text-center py-4">Error al cargar los horarios.</div>';
        } finally {
            existingSlotsLoading.classList.add('hidden');
        }
    }


    function renderExistingSlots(existingSlots) {
      existingSlotsGrid.innerHTML = '';

      if (existingSlots.length === 0) {
          existingSlotsGrid.innerHTML = '<div class="text-gray-400 text-center py-4">No hay turnos en esta página.</div>';
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
                          Disponibles (${capacityAvailable}/${maxCapacity})
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
    }

    async function handleCreateOrUpdateSlot() {
        const startTime = startTimeInput.value;
        const endTime = endTimeInput.value;
        const priceValue = priceInput.value === "" ? null : parseFloat(priceInput.value);
        const token = localStorage.getItem("authToken");

        if (!selectedResource) {
            displayMessage("Selecciona un profesional.", "error");
            return;
        }

        if (!startTime || !endTime || startTime >= endTime) {
            displayMessage("Horario inválido.", "error");
            return;
        }

        if (!token) {
            displayMessage("Sesión inválida.", "error");
            return;
        }

        applySlotsBtn.disabled = true;
        applySlotsBtn.textContent = slotIdToModify ? "Actualizando..." : "Guardando...";

        try {
            if (slotIdToModify) {
                if (selectedDates.size !== 1) {
                    throw new Error("Selecciona una fecha válida");
                }

                const date = [...selectedDates][0];

                const payload = {
                    id: slotIdToModify,
                    offeringId: currentOfferingId,
                    resourceId: selectedResource.id,
                    startDateTime: `${date}T${startTime}:00`,
                    endDateTime: `${date}T${endTime}:00`,
                    price: priceValue
                };

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
                                   ? `El turno tiene 1 reserva activa. Debe cancelarla para poder actualizarlo.`
                                   : `El turno tiene reservas activas. Debe cancelarlas para poder actualizarlo.`;
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

                displayMessage("Slot actualizado correctamente.", "success");
                clearModificationState();
                fetchExistingSlots(currentPage);

            } else {
                if (selectedDates.size === 0) {
                    throw new Error("Selecciona al menos una fecha");
                }

                const payload = [...selectedDates].map(date => ({
                    offeringId: currentOfferingId,
                    resourceId: selectedResource.id,
                    startDateTime: `${date}T${startTime}:00`,
                    endDateTime: `${date}T${endTime}:00`,
                    price: priceValue
                }));

                const response = await fetch(CREATE_SLOT_ENDPOINT, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`
                    },
                    body: JSON.stringify(payload)
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

                const saved = await response.json();
                displayMessage(`Se agregaron ${saved.length} turnos.`, "success");

                selectedDates.clear();
                updateSummary();
                renderCalendar();
                fetchExistingSlots(0);
            }

        } catch (error) {
            console.error(error);
            displayMessage(error.message || "Error inesperado.", "error");
        } finally {
            applySlotsBtn.disabled = false;
            applySlotsBtn.textContent = slotIdToModify
                ? "Actualizar turno"
                : `Aplicar a ${selectedDates.size} Fechas`;
        }
    }

    async function handleDeleteExistingSlot(slotId) {
        Swal.fire({
            title: '¿Estás seguro?',
            text: `¿Deseas eliminar el horario?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        }).then(async (result) => {
            if (!result.isConfirmed) return;
            const token = localStorage.getItem("authToken");
            try {
                const response = await fetch(DELETE_SLOT_ENDPOINT + slotId, {
                    method: "DELETE",
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (!response.ok) {
                    const errorJson = await response.json();
                    if (errorJson.errorCode === 'OFFERING_HAS_ACTIVE_BOOKINGS') {
                        displayMessage(`Error: El turno ya tiene reservas activas.`, 'error');
                        return;
                    }
                    throw new Error('Error al eliminar');
                }
                displayMessage(`Turno eliminado correctamente.`, "success");
                fetchExistingSlots(currentPage);
            } catch (error) {
                displayMessage("Error al eliminar slot.", "error");
            }
        });
    }

    function handleModifyExistingSlot(slotData) {
        slotIdToModify = slotData.id;
        const datePart = slotData.startDateTime.substring(0, 10);
        startTimeInput.value = toTimeFormat(slotData.startDateTime);
        endTimeInput.value = toTimeFormat(slotData.endDateTime);
        priceInput.value = slotData.price || '';

        manualSelectionToggle.click();
        selectedDates.clear();
        selectedDates.add(datePart);
        renderCalendar();
        updateSummary();
        applySlotsBtn.textContent = `Actualizar Slot`;
        displayMessage(`Turno seleccionado para edición.`, 'info');
    }

    function handleDateClick(event) {
        const date = event.currentTarget.dataset.date;
        if (slotIdToModify) {
            selectedDates.clear();
            selectedDates.add(date);
            renderCalendar();
        } else {
            if (selectedDates.has(date)) {
                selectedDates.delete(date);
            } else {
                selectedDates.add(date);
            }
            event.currentTarget.classList.toggle('selected-date');
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
            emptyCell.className = 'date-cell opacity-0';
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
            if (dateString === getFormattedDate(new Date())) cell.classList.add('today', 'font-bold');
            if (date < today) {
                cell.setAttribute('disabled', 'true');
                cell.classList.add('text-gray-400');
            } else {
                cell.addEventListener('click', handleDateClick);
            }
            if (selectedDates.has(dateString)) cell.classList.add('selected-date');
            calendarGrid.appendChild(cell);
        }
    }

    function updateSummary() {
        datesAppliedCount.textContent = selectedDates.size;
        applySlotsBtn.disabled = selectedDates.size === 0 && !slotIdToModify;
    }


    function updateRecurrenceDates() {
        if (manualSelectionContainer.style.display !== 'none') return;

        const checkedDays = Array.from(
            recurrenceDayCheckboxes.querySelectorAll('input:checked')
        ).map(input => parseInt(input.value));

        const min = Number(recurrenceDurationInput.min) || 1;
        const max = Number(recurrenceDurationInput.max) || 365;

        let duration = parseInt(recurrenceDurationInput.value, 10);

        if (isNaN(duration)) duration = min;
        if (duration < min) duration = min;
        if (duration > max) duration = max;

        // normalize the input value so UI matches logic
        recurrenceDurationInput.value = duration;

        selectedDates.clear();

        if (checkedDays.length > 0 && duration > 0) {
            for (let i = 1; i <= duration; i++) {
                const d = new Date(today);
                d.setDate(today.getDate() + i);

                if (checkedDays.includes(d.getDay())) {
                    selectedDates.add(getFormattedDate(d));
                }
            }
        }

        updateSummary();
    }


    // --- Init & Listeners ---

    manualSelectionToggle.addEventListener('click', () => {
        manualSelectionToggle.classList.add('bg-indigo-600', 'text-white');
        recurrenceSelectionToggle.classList.remove('bg-indigo-600', 'text-white');
        manualSelectionContainer.style.display = 'block';
        recurrenceSelectionContainer.style.display = 'none';
        selectedDates.clear(); // Clear selection when switching modes
        renderCalendar();
        updateSummary();
    });

    recurrenceSelectionToggle.addEventListener('click', () => {
        recurrenceSelectionToggle.classList.add('bg-indigo-600', 'text-white');
        manualSelectionToggle.classList.remove('bg-indigo-600', 'text-white');
        manualSelectionContainer.style.display = 'none';
        recurrenceSelectionContainer.style.display = 'block';
        selectedDates.clear(); // Clear manual selection when switching modes
        updateRecurrenceDates(); // Calculate if fields already have values
    });

    prevMonthBtn.addEventListener('click', () => { currentDate.setMonth(currentDate.getMonth() - 1); renderCalendar(); });
    nextMonthBtn.addEventListener('click', () => { currentDate.setMonth(currentDate.getMonth() + 1); renderCalendar(); });

    applySlotsBtn.addEventListener('click', handleCreateOrUpdateSlot);
    prevPageBtn.addEventListener('click', () => fetchExistingSlots(currentPage - 1));
    nextPageBtn.addEventListener('click', () => fetchExistingSlots(currentPage + 1));
    refreshExistingSlotsBtn.addEventListener('click', () => fetchExistingSlots(currentPage));

    existingSlotsGrid.addEventListener('click', (event) => {
        const deleteButton = event.target.closest('.delete-slot-btn');
        const modifyButton = event.target.closest('.modify-slot-btn');
        if (deleteButton) handleDeleteExistingSlot(deleteButton.dataset.id);
        if (modifyButton) handleModifyExistingSlot(JSON.parse(modifyButton.dataset.fullSlot));
    });

    recurrenceDayCheckboxes.addEventListener('change', updateRecurrenceDates);
    recurrenceDurationInput.addEventListener('input', updateRecurrenceDates);

    const resourceSelect = document.getElementById("resourceSelect");
    resourceSelect.addEventListener("change", (e) => {
        const resourceId = e.target.value;
        selectedResource = resources.find(r => r.id === resourceId);
        selectedDates.clear();

        updateSummary();
        document.getElementById("applySlotsBtn").disabled = true;
        document.getElementById("existingSlotsGrid").innerHTML = "";

        fetchExistingSlots(0);
    });

    loadResources();
    renderCalendar();
});