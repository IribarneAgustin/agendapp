class PasswordResetManager {
    constructor() {
        this.baseUrl = window.BASE_URL;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadTokenIfPresent();
    }

    setupEventListeners() {
        const emailForm = document.getElementById('resetForm');
        if (emailForm) {
            emailForm.addEventListener('submit', this.handlePasswordReset.bind(this));
        }

        const newPasswordForm = document.getElementById('resetPasswordForm');
        if (newPasswordForm) {
            newPasswordForm.addEventListener(
                'submit',
                this.handleNewPasswordSubmit.bind(this)
            );
        }
    }

    async handlePasswordReset(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        const credentials = {
            email: formData.get('email')?.trim()
        };

        if (!credentials.email) {
            this.showMessage('Por favor, ingresa tu correo electrónico', 'error');
            return;
        }

        const submitBtn = form.querySelector('button[type="submit"]');

        try {
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Enviando...';
            }

            const response = await fetch(
                `${this.baseUrl}/auth/forgot-password`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(credentials)
                }
            );

            // Always return generic success
            this.showMessage(
                'Si este correo electrónico está registrado, recibirás un enlace para restablecer tu contraseña en breve.',
                'success'
            );
        } catch (error) {
            console.error('Password reset connection error:', error);
            this.showMessage('Error de conexión. Inténtalo de nuevo.', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Enviar Instrucciones';
            }
        }
    }

    async handleNewPasswordSubmit(e) {
        e.preventDefault();

        const form = e.target;
        const password = form.querySelector('#password')?.value;
        const confirmPassword = form.querySelector('#confirmPassword')?.value;
        const token = form.querySelector('#token')?.value;

        if (!token) {
            this.showMessage('El enlace es inválido o ha expirado.', 'error');
            return;
        }

        if (!password || password.length < 8) {
            this.showMessage(
                'La contraseña debe tener al menos 8 caracteres.',
                'error'
            );
            return;
        }

        if (password !== confirmPassword) {
            this.showMessage('Las contraseñas no coinciden.', 'error');
            return;
        }

        const submitBtn = form.querySelector('button[type="submit"]');

        try {
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Guardando...';
            }

            const response = await fetch(
                `${this.baseUrl}/users/reset-password`,
                {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        token,
                        newPassword: password
                    })
                }
            );

            if (response.ok) {
                this.showMessage(
                    'Tu contraseña fue actualizada correctamente.',
                    'success'
                );
                setTimeout(() => {
                    form.reset();
                    window.location.href = this.baseUrl;
                }, 2500);
            } else {
                this.showMessage(
                    'El enlace es inválido o ha expirado.',
                    'error'
                );
            }
        } catch (error) {
            console.error('Reset password error:', error);
            this.showMessage('Error de conexión. Inténtalo de nuevo.', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Guardar contraseña';
            }
        }
    }

    /* ------------------------------------
     * TOKEN HANDLING
     * ------------------------------------ */
    loadTokenIfPresent() {
        const tokenInput = document.getElementById('token');
        if (!tokenInput) return;

        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');

        if (!token) {
            this.showMessage('Token inválido o ausente.', 'error');
            return;
        }

        tokenInput.value = token;
    }

    /* ------------------------------------
     * MESSAGE HANDLER (UNCHANGED)
     * ------------------------------------ */
    showMessage(message, type = 'info') {
        const messageBox = document.getElementById('messageBox');
        if (!messageBox) return;

        messageBox.className =
            'mt-6 p-4 rounded-lg text-sm text-center font-medium';

        switch (type) {
            case 'success':
                messageBox.classList.add(
                    'bg-green-100',
                    'text-green-800',
                    'border',
                    'border-green-200'
                );
                break;
            case 'error':
                messageBox.classList.add(
                    'bg-red-100',
                    'text-red-800',
                    'border',
                    'border-red-200'
                );
                break;
            case 'warning':
                messageBox.classList.add(
                    'bg-yellow-100',
                    'text-yellow-800',
                    'border',
                    'border-yellow-200'
                );
                break;
            default:
                messageBox.classList.add(
                    'bg-blue-100',
                    'text-blue-800',
                    'border',
                    'border-blue-200'
                );
        }

        messageBox.textContent = message;
        messageBox.classList.remove('hidden');

        if (type !== 'error') {
            setTimeout(() => {
                messageBox.classList.add('hidden');
            }, 8000);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new PasswordResetManager();
});