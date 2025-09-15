// This file contains the JavaScript logic for the AgendApp registration page.

const form = document.getElementById('registrationForm');
const messageBox = document.getElementById('messageBox');

// The base URL for the API endpoints. You might need to change this if your Spring Boot app is not on the same origin.
const API_BASE_URL = window.location.origin;

// Add an event listener to the form to handle submission
form.addEventListener('submit', async (e) => {
    e.preventDefault(); // Prevent the default form submission

    const name = form.name.value;
    const lastName = form.lastName.value;
    const email = form.email.value;
    const password = form.password.value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, lastName, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            console.log('Registro exitoso:', data);
            showMessage('¡Registro exitoso! Ahora puedes iniciar sesión.', 'success');
            // In a real application, you might redirect the user to the login page
            // window.location.href = 'login.html';
        } else {
            console.error('Error de registro:', data);
            showMessage(`Error: ${data.message || 'El correo electrónico ya está registrado.'}`, 'error');
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
