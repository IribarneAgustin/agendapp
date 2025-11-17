function showToast(message, type = 'success') {
    // Determine SweetAlert2 icon based on type
    const icon = type === 'success' ? 'success' : 'error';

    // Use Swal.fire for a toast style notification
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon,
        title: message,
        showConfirmButton: false,
        timer: 4000,
        timerProgressBar: false,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer);
            toast.addEventListener('mouseleave', Swal.resumeTimer);
        }
    });
}

// Function to toggle loading state
function toggleLoading(isLoading) {
    document.getElementById('saveBtn').classList.toggle('hidden', isLoading);
    document.getElementById('loadingBtn').classList.toggle('hidden', !isLoading);
    const inputs = document.querySelectorAll('#userSettingsForm input');
    inputs.forEach(input => input.disabled = isLoading);
}

// Function to handle logout
document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('authToken');
    // Using 'userData' as per the user's provided snippet.
    localStorage.removeItem('userData');
    // Redirect to login page, assuming 'index.html' is the login/home page as per user's snippet
    window.location.href = BASE_URL;
});


// --- Main Logic ---
document.addEventListener('DOMContentLoaded', () => {
    // Retrieving auth data using the pattern from the user's latest provided JS snippet
    const authToken = localStorage.getItem('authToken');
    const userDataStorage = localStorage.getItem('userData');
    const form = document.getElementById('userSettingsForm');

    if (!authToken || !userDataStorage) {
        showToast('No autorizado. Por favor, inicia sesión de nuevo.', 'error');
        setTimeout(() => { window.location.href = BASE_URL; }, 1000);
        return;
    }

    // Parse user data to get ID
    const userData = JSON.parse(userDataStorage);
    const userId = userData.id;

    // 1. Fetch current user data to pre-fill the form
    async function fetchUserData() {
        try {
            toggleLoading(true);

            const response = await fetch(`${BASE_URL}/users/${userId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });

            if (!response.ok) {
                // Check for 404 or other errors
                const error = await response.json().catch(() => ({ message: 'Error al cargar los datos del usuario.' }));
                showToast(`Error: ${error.message || response.statusText}`, 'error');
                return;
            }

            const userData = await response.json();

            // Pre-fill the form fields
            document.getElementById('name').value = userData.name || '';
            document.getElementById('lastName').value = userData.lastName || '';
            document.getElementById('email').value = userData.email || '';
            document.getElementById('phone').value = userData.phone || '';
            document.getElementById('brandName').value = userData.brandName || '';

        } catch (error) {
            console.error('Fetch User Data Error:', error);
            showToast('Hubo un error de conexión al cargar el perfil.', 'error');
        } finally {
            toggleLoading(false);
        }
    }

    // 2. Handle form submission (PUT request)
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        toggleLoading(true);

        // Collect form data.
        const updatedUserData = {
            email: document.getElementById('email').value,
            name: document.getElementById('name').value,
            lastName: document.getElementById('lastName').value,
            phone: document.getElementById('phone').value,
            brandName: document.getElementById('brandName').value,
        };

        try {
            const response = await fetch(`${BASE_URL}/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify(updatedUserData)
            });

            if (response.ok) {
                showToast('¡Configuración de usuario actualizada con éxito!', 'success');
            } else {
                let errorBody = null;
                let errorMessage = 'Error inesperado, por favor vuelva a intentar más tarde.';

                try {
                    errorBody = await response.json();
                } catch (e) {
                    console.warn("Could not parse error body as JSON. Status:", response.status);
                }

                const { errorCode, details } = errorBody || {};

                // --- Specific Error Handling from User's Snippet ---
                if (errorCode === 'UNIQUE_FIELD_ALREADY_EXISTS') {
                    const { field, value } = details || {};

                    const uniqueFieldMessages = {
                        brandName: `¡Ese nombre de marca (${value}) ya está en uso! Por favor, elige uno diferente.`
                    };

                    if (field && value) {
                        errorMessage = uniqueFieldMessages[field] || errorMessage;
                    } else {
                        errorMessage = 'Error inesperado de unicidad, por favor vuelva a intentar más tarde.';
                    }
                } else {
                    errorMessage = errorBody.message || response.statusText || errorMessage;
                }
                // ---------------------------------------------------

                showToast(`Error al guardar: ${errorMessage}`, 'error');
            }


        } catch (error) {
            console.error('Update User Data Error:', error);
            showToast('Hubo un error de conexión al actualizar el perfil.', 'error');
        } finally {
            toggleLoading(false);
        }
    });

    // Initial data load
    fetchUserData();
});
