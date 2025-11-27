class LoginManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        const form = document.getElementById('loginForm');
        if (form) {
            form.addEventListener('submit', this.handleLogin.bind(this));
        }
    }

    async handleLogin(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const credentials = {
            email: formData.get('email').trim(),
            password: formData.get('password')
        };

        // Basic validation
        if (!credentials.email || !credentials.password) {
            this.showMessage('Por favor, completa todos los campos', 'error');
            return;
        }

        try {
            // Disable submit button
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Iniciando sesión...';
            }

            const response = await fetch(`${this.baseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            });

            if (response.ok) {
                const loginData = await response.json();
                localStorage.setItem('authToken', loginData.token);

                const userData = {
                    id: loginData.id,
                    email: loginData.email,
                    brandName: loginData.brandName
                };

                localStorage.setItem('userData', JSON.stringify(userData));


                this.showMessage('Inicio de sesión exitoso', 'success');
                
                // Redirect to dashboard after a short delay
                setTimeout(() => {
                    window.location.href = './admin/dashboard.html';
                }, 500);

            } else {
                const errorData = await response.text();
                let errorMessage = 'Error al iniciar sesión';

                if (response.status === 401) {
                    errorMessage = 'Credenciales incorrectas';
                } else if (response.status === 404) {
                    errorMessage = 'Usuario no encontrado';
                } else if (errorData) {
                    errorMessage = errorData;
                }

                this.showMessage(errorMessage, 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showMessage('Error de conexión. Inténtalo de nuevo.', 'error');
        } finally {
            // Re-enable submit button
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Iniciar Sesión';
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

// Initialize login manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new LoginManager();
});