
class RegistrationManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        const form = document.getElementById('registrationForm');
        if (form) {
            form.addEventListener('submit', this.handleRegistration.bind(this));
        }
    }

    isValidSlug(str) {
        if (!str) return false;
        const slugRegex = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
        return slugRegex.test(str);
    }

    clientSideValidation(userData) {
        if (!userData.name || !userData.lastName || !userData.email || !userData.password || !userData.brandName || !userData.phone) {
            this.showMessage('Por favor, completa todos los campos requeridos.', 'error');
            return false;
        }

        if (userData.password.length < 3) {
            this.showMessage('La contraseña debe tener al menos 3 caracteres.', 'error');
            return false;
        }

        // Basic Email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userData.email)) {
            this.showMessage('Por favor, ingresa un email válido.', 'error');
            return false;
        }

        // New BrandName/Slug validation
        if (!this.isValidSlug(userData.brandName.toLowerCase())) {
            this.showMessage('El Nombre de Marca debe ser corto, en minúsculas, y solo puede contener letras, números y guiones (ej: mi-tienda-online).', 'error');
            return false;
        }

        return true;
    }

    handleApiError(response, errorBody) {
        const { errorCode, details } = errorBody || {};
        let errorMessage = 'Error desconocido al registrar usuario. Inténtalo de nuevo.';

        if (errorCode === 'UNIQUE_FIELD_ALREADY_EXISTS') {

            // 💡 Use the generic 'field' and 'value' keys from the 'details' map
            const field = details?.field;
            const value = details?.value;

            if (field && value) {

                // Customize the message based on the specific 'field' name
                if (field === 'brandName') {
                    errorMessage = `¡Ese nombre de marca (${value}) ya está en uso! Por favor, elige uno diferente.`;

                } else if (field === 'email') {
                    errorMessage = `El correo electrónico (${value}) ya está registrado.`;

                } else {
                    errorMessage = `Error inesperado, por favor vuelva a intentar más tarde.`;
                }

            } else {
                errorMessage = 'Error inesperado de unicidad, por favor vuelva a intentar más tarde.';
            }

        } else if (response.status === 409) {
             errorMessage = 'Conflicto de datos: Este email o alguna otra información ya está registrada.';
        } else if (response.status === 400) {
            errorMessage = 'Datos inválidos. Verifica la información ingresada.';
        } else if (errorBody?.message) {
            errorMessage = errorBody.message;
        }

        this.showMessage(errorMessage, 'error');
    }

    async handleRegistration(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        const userData = {
            name: formData.get('name').trim(),
            lastName: formData.get('lastName').trim(),
            email: formData.get('email').trim(),
            password: formData.get('password'),
            brandName: formData.get('brandname').trim().toLowerCase(),
            phone: formData.get('phone').trim()
        };

        if (!this.clientSideValidation(userData)) {
            return;
        }


        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Registrando...';
        }

        try {

            const response = await fetch(`${this.baseUrl}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            // 4. Handle Response
            if (response.ok) {
                this.showMessage('Registro exitoso. Iniciando sesión...', 'success');

                setTimeout(() => {
                    window.location.href = BASE_URL;
                }, 2000);

            } else {
                let errorBody = null;
                try {
                    errorBody = await response.json();
                } catch (e) {
                    console.warn("Could not parse error body as JSON. Status:", response.status);
                }

                this.handleApiError(response, errorBody);
            }
        } catch (error) {
            console.error('Registration fetch error:', error);
            this.showMessage('Error de conexión con el servidor. Inténtalo de nuevo.', 'error');
        } finally {

            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Registrarse';
            }
        }
    }

    showMessage(message, type = 'info') {
        const messageBox = document.getElementById('messageBox');
        if (!messageBox) return;

        messageBox.className = 'mt-6 p-4 rounded-lg text-sm text-center font-medium';

        switch (type) {
            case 'success':
                messageBox.classList.add('bg-green-100', 'text-green-800', 'border', 'border-green-200');
                break;
            case 'error':
                messageBox.classList.add('bg-red-100', 'text-red-800', 'border', 'border-red-200');
                break;
            case 'warning':
                messageBox.classList.add('bg-yellow-100', 'text-yellow-800', 'border', 'border-yellow-200');
                break;
            default:
                messageBox.classList.add('bg-blue-100', 'text-blue-800', 'border', 'border-blue-200');
        }

        messageBox.textContent = message;
        messageBox.classList.remove('hidden');

        // Auto hide after 5 seconds for non-error messages
        if (type !== 'error') {
            setTimeout(() => {
                messageBox.classList.add('hidden');
            }, 5000);
        }
    }
}

// Initialize registration manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Make sure your form has an element with id="messageBox" for showing messages
    new RegistrationManager();
});