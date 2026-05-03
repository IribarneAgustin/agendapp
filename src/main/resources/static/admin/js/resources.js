const resourceSelect = document.getElementById('resourceSelect');
const nameInput = document.getElementById('name');
const lastNameInput = document.getElementById('lastName');
const emailInput = document.getElementById('email');

const saveBtn = document.getElementById('saveBtn');
const deleteBtn = document.getElementById('deleteBtn');

const userDataStorage = JSON.parse(localStorage.getItem('userData'));
const userId = userDataStorage.id;

let resources = [];
let selectedResourceId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadResources();
    resourceSelect.addEventListener('change', onResourceSelected);
    saveBtn.addEventListener('click', saveResource);
    deleteBtn.addEventListener('click', deleteResource);
});


async function loadResources() {
    try {
        const response = await fetch(`${BASE_URL}/resource/user/${userId}`);

        if (!response.ok) {
            throw new Error();
        }

        resources = await response.json();
        renderCombo();
        resetForm();

    } catch (e) {
        console.error(e);
        Swal.fire('Error', 'No se pudieron cargar los profesionales', 'error');
    }
}

async function saveResource() {
    const payload = getPayload();
    if (!payload) return;

    const isUpdate = !!selectedResourceId;

    try {
        const response = await fetch(
            isUpdate
                ? `${BASE_URL}/resource/${selectedResourceId}`
                : `${BASE_URL}/resource/user/${userId}`,
            {
                method: isUpdate ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            const errorCode = await handleError(response);
            if (errorCode === 'FEATURE_LIMIT_EXCEEDED') {
                Swal.fire({
                    icon: 'warning',
                    title: 'Llegaste al límite de tu plan',
                    html: `
                        <p>Tu plan actual no permite agregar más profesionales.</p>
                        <p><b>Actualizá tu plan para seguir creciendo</b></p>
                    `,
                    showCancelButton: true,
                    confirmButtonText: 'Actualizar plan',
                    cancelButtonText: 'Cancelar',
                    confirmButtonColor: '#6366f1'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/admin/subscription-status.html';
                    }
                });
                return;
            } else {
                throw new Error("Error inesperado.");
            }
        }

        Swal.fire({
            icon: 'success',
            title: isUpdate ? 'Profesional actualizado' : 'Profesional creado',
            timer: 1500,
            showConfirmButton: true
        });

        await loadResources();

    } catch (e) {
        console.error(e);
        Swal.fire('Error', 'No se pudo guardar el profesional', 'error');
    }
}

async function deleteResource() {
    if (!selectedResourceId) return;

    if (resources.length <= 1) {
        Swal.fire({
            icon: 'info',
            title: 'No se puede eliminar',
            text: 'Debes tener al menos un profesional'
        });
        return;
    }

    const result = await Swal.fire({
        icon: 'warning',
        title: '¿Eliminar profesional?',
        text: 'Esta acción no se puede deshacer',
        showCancelButton: true,
        confirmButtonText: 'Eliminar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#dc2626'
    });

    if (!result.isConfirmed) return;

    try {
        const response = await fetch(`${BASE_URL}/resource/${selectedResourceId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            let errorCode;

            try {
                const body = await response.json();
                errorCode = body.errorCode;
            } catch (_) {}

            if (response.status === 409 && errorCode === 'RESOURCE_HAS_ACTIVE_BOOKINGS') {
                throw { type: 'BUSINESS', errorCode };
            }

            throw new Error();
        }

        Swal.fire({
            icon: 'success',
            title: 'Profesional eliminado',
            timer: 1500,
            showConfirmButton: true
        });

        await loadResources();

    } catch (e) {
        console.error(e);

        let message = 'No se pudo eliminar el profesional';

        if (e.type === 'BUSINESS' && e.errorCode === 'RESOURCE_HAS_ACTIVE_BOOKINGS') {
            message = 'No se puede eliminar el profesional porque tiene reservas activas';
        }

        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: message
        });
    }
}

function renderCombo() {
    resourceSelect.innerHTML = `<option value="">Nuevo profesional</option>`;

    resources.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r.id;
        opt.textContent = `${r.name} ${r.lastName || ''}`;
        resourceSelect.appendChild(opt);
    });

    deleteBtn.disabled = resources.length <= 1;
}

function onResourceSelected(e) {
    selectedResourceId = e.target.value || null;

    if (!selectedResourceId) {
        resetForm();
        return;
    }

    const resource = resources.find(r => r.id === selectedResourceId);

    nameInput.value = resource?.name || '';
    lastNameInput.value = resource?.lastName || '';
    emailInput.value = resource?.email || '';

    deleteBtn.disabled = resources.length <= 1;
}

function getPayload() {
    const name = nameInput.value.trim();
    const lastName = lastNameInput.value.trim();
    const email = emailInput.value.trim();

    if (!name) {
        Swal.fire({
            icon: 'error',
            title: 'Nombre obligatorio'
        });
        return null;
    }

    if (email && !isValidEmail(email)) {
        Swal.fire({
            icon: 'error',
            title: 'Email inválido'
        });
        return null;
    }

    return {
        name,
        lastName,
        email: email || null
    };
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function resetForm() {
    resourceSelect.value = '';
    selectedResourceId = null;

    nameInput.value = '';
    lastNameInput.value = '';
    emailInput.value = '';

    deleteBtn.disabled = true;
}

async function handleError(response) {
    try {
        const body = await response.json();
        return body.errorCode;
    } catch {
        return null;
    }
}