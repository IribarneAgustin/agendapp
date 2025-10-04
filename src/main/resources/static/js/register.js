class RegistrationManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.init();
    }

    init() {
        // Check if user is already logged in
        const token = localStorage.getItem('authToken');
        if (token) {
            window.location.href = 'dashboard.html';
            return;
        }

        this.setupEventListeners();
    }

    setupEventListeners() {
        const form = document.getElementById('registrationForm');
        if (form) {
            form.addEventListener('submit', this.handleRegistration.bind(this));
        }
    }

    async handleRegistration(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const userData = {
            name: formData.get('name').trim(),
            lastName: formData.get('lastName').trim(),
            email: formData.get('email').trim(),
            password: formData.get('password')
        };

        // Basic validation
        if (!userData.name || !userData.lastName || !userData.email || !userData.password) {
            this.showMessage('Por favor, completa todos los campos', 'error');
            return;
        }

        if (userData.password.length < 3) {
            this.showMessage('La contraseña debe tener al menos 3 caracteres', 'error');
            return;
        }

        // Email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userData.email)) {
            this.showMessage('Por favor, ingresa un email válido', 'error');
            return;
        }

        try {
            // Disable submit button
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Registrando...';
            }

            const response = await fetch(`${this.baseUrl}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                const registrationData = await response.json();
                
                this.showMessage('Registro exitoso. Redirigiendo al inicio de sesión...', 'success');
                
                // Redirect to login after a short delay
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 2000);

            } else {
                const errorData = await response.text();
                let errorMessage = 'Error al registrar usuario';
                
                if (response.status === 409) {
                    errorMessage = 'Este email ya está registrado';
                } else if (response.status === 400) {
                    errorMessage = 'Datos inválidos. Verifica la información ingresada';
                } else if (errorData) {
                    errorMessage = errorData;
                }
                
                this.showMessage(errorMessage, 'error');
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.showMessage('Error de conexión. Inténtalo de nuevo.', 'error');
        } finally {
            // Re-enable submit button
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Registrarse';
            }
        }
    }

    showMessage(message, type = 'info') {
        const messageBox = document.getElementById('messageBox');
        if (!messageBox) return;

        // Clear existing classes
        messageBox.className = 'mt-6 p-4 rounded-lg text-sm text-center font-medium';
        
        // Add type-specific classes
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
    new RegistrationManager();
});