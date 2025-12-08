class PasswordResetManager {
  constructor() {
      this.baseUrl = window.BASE_URL;
      this.init();
  }

  init() {
      this.setupEventListeners();
  }

  setupEventListeners() {
      const form = document.getElementById('resetForm');
      if (form) {
          form.addEventListener('submit', this.handlePasswordReset.bind(this));
      }
  }

  async handlePasswordReset(e) {
      e.preventDefault();

      const form = e.target;
      const formData = new FormData(form);

      const credentials = {
          email: formData.get('email').trim()
      };

      // Basic validation
      if (!credentials.email) {
          this.showMessage('Por favor, ingresa tu correo electrónico', 'error');
          return;
      }

      try {
          // Disable submit button
          const submitBtn = form.querySelector('button[type="submit"]');
          if (submitBtn) {
              submitBtn.disabled = true;
              submitBtn.textContent = 'Enviando...';
          }

          // API Endpoint for initiating password reset
          const response = await fetch(`${this.baseUrl}/auth/forgot-password`, {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify(credentials)
          });

          if (response.ok) {
              this.showMessage(
                  'Si este correo electrónico está registrado, recibirás un enlace para restablecer tu contraseña en breve.',
                  'success'
              );
          } else {
              console.error('Password reset request failed on backend:', response.status);

              this.showMessage(
                  'Si este correo electrónico está registrado, recibirás un enlace para restablecer tu contraseña en breve.',
                  'success'
              );
          }
      } catch (error) {
          console.error('Password reset connection error:', error);
          this.showMessage('Error de conexión. Inténtalo de nuevo.', 'error');
      } finally {
          // Re-enable submit button
          const submitBtn = form.querySelector('button[type="submit"]');
          if (submitBtn) {
              submitBtn.disabled = false;
              submitBtn.textContent = 'Enviar Instrucciones';
          }
      }
  }

  // Reused showMessage utility from LoginManager
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
          }, 8000); // Increased duration for this important message
      }
  }
}

// Initialize the password reset manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new PasswordResetManager();
});