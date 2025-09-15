// This file contains the JavaScript logic for the AgendApp login page.

const form = document.getElementById('loginForm');
const messageBox = document.getElementById('messageBox');

// The base URL for the API endpoints. You might need to change this if your Spring Boot app is not on the same origin.
const API_BASE_URL = window.location.origin;

// Add an event listener to the form to handle submission
form.addEventListener('submit', async (e) => {
    e.preventDefault(); // Prevent the default form submission

    const email = form.email.value;
    const password = form.password.value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            console.log('Inicio de sesión exitoso:', data);
            showMessage('¡Inicio de sesión exitoso!', 'success');

            // --- New logic to save token and redirect ---
            if (data.token) {
                // Assuming the backend returns a token in the 'token' field
                localStorage.setItem('agendapp_token', data.token);
                // Redirect to the dashboard page
                window.location.href = 'dashboard.html';
            } else {
                // Handle cases where no token is returned
                showMessage('Inicio de sesión exitoso, pero no se recibió el token de autenticación.', 'error');
            }
            // --- End of new logic ---

        } else {
            console.error('Error de inicio de sesión:', data);
            showMessage(`Error: ${data.message || 'Credenciales inválidas.'}`, 'error');
        }
    } catch (error) {
        console.error('Error de red o del servidor:', error);
        showMessage('No se pudo conectar al servidor. Inténtalo de nuevo más tarde.', 'error');
    }
});

/**
 * Displays a message in the message box.
 * @param {string} message - The message to display.
 * @param {'success'|'error'} type - The type of message.
 */
function showMessage(message, type) {
    messageBox.textContent = message;
    messageBox.classList.remove('hidden', 'bg-red-100', 'text-red-700', 'bg-green-100', 'text-green-700');
    if (type === 'success') {
        messageBox.classList.add('bg-green-100', 'text-green-700');
    } else if (type === 'error') {
        messageBox.classList.add('bg-red-100', 'text-red-700');
    }
}
