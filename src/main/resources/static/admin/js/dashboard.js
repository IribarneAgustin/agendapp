class DashboardManager {
    constructor() {
        this.baseUrl = BASE_URL;
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        
        if (!this.token || !this.user) {
            window.location.href = BASE_URL;
            return;
        }
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.loadSubscriptionStatus();
        this.setupShareableUrl();
        this.setupBookingDashboardLink();
        this.loadMpConnectionStatus();
        this.setupMpConnectButton();
        this.setupMpUnlinkButton();
    }

    getStoredUser() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }

    setupEventListeners() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', this.logout.bind(this));
        }

        const toastClose = document.getElementById('toastClose');
        if (toastClose) {
            toastClose.addEventListener('click', this.hideToast.bind(this));
        }

        const copyUrlBtn = document.getElementById('copyUrlBtn');
        if (copyUrlBtn) {
            copyUrlBtn.addEventListener('click', this.copyShareableUrl.bind(this));
        }
    }


    async loadDashboardData() {
        try {
            await this.loadOfferingsStats();
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showToast('Error al cargar los datos del panel', 'error');
        }
    }

    // --- NUEVA FUNCIÓN PARA CARGAR ESTADO DE SUSCRIPCIÓN ---
    async loadSubscriptionStatus() {
        const statusElement = document.getElementById('subscriptionStatusDisplay');
        if (!statusElement) return;

        statusElement.textContent = 'Cargando...';
        statusElement.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-gray-200 text-gray-700';

        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/subscription`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const subscription = await response.json();
                this.updateSubscriptionDisplay(subscription, statusElement);
            } else if (response.status === 404) {
                // No subscription found
                this.updateSubscriptionDisplay(null, statusElement);
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al consultar el estado de la suscripción');
            }
        } catch (error) {
            console.error('Error loading subscription status:', error);
            statusElement.textContent = 'Error de conexión';
            statusElement.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-700';
        }
    }

    updateSubscriptionDisplay(subscription, element) {
        if (!subscription || subscription.expired) {
            element.textContent = 'INACTIVA (¡Actívala!)';
            element.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-red-500 text-white';
        } else {
            const expirationDate = new Date(subscription.expiration);
            const statusText = `ACTIVA (Expira: ${expirationDate.toLocaleDateString()})`;
            element.textContent = statusText;
            element.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-green-500 text-white';
        }
    }
    // --- FIN NUEVA FUNCIÓN ---

    async loadOfferingsStats() {
        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/offerings`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const offerings = await response.json();
                this.updateStats(offerings);
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al cargar servicios');
            }
        } catch (error) {
            console.error('Error loading offerings:', error);
            this.updateStats([]);
        }
    }

    updateStats(offerings) {
        const activeOfferings = offerings.filter(o => o.enabled).length;
        const activeElement = document.getElementById('activeOfferings');
        if (activeElement) activeElement.textContent = activeOfferings;
    }

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = BASE_URL;
    }

    async logout() {
        await fetch(BASE_URL + "/auth/logout", {
            method: "POST",
            credentials: "include"
        });

        localStorage.removeItem("authToken");
        localStorage.removeItem("userData");

        window.location.href = BASE_URL;

    }

    showToast(message, type = 'info') {
        // ... (SweetAlert logic is recommended, but keeping current toast for consistency)
        const toast = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');

        if (!toast || !toastMessage || !toastIcon) return;

        toastMessage.textContent = message;

        let iconHTML = '';
        // Simplified icon logic for brevity, assuming the rest of the switch is correct
        switch (type) {
            case 'success':
                iconHTML = '<svg class="h-5 w-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
                break;
            case 'error':
                iconHTML = '<svg class="h-5 w-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
                break;
            case 'warning':
                iconHTML = '<svg class="h-5 w-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.664-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg>';
                break;
            default:
                iconHTML = '<svg class="h-5 w-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
        }
        toastIcon.innerHTML = iconHTML;

        toast.classList.remove('hidden');

        setTimeout(() => {
            this.hideToast();
        }, 5000);
    }

    hideToast() {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.classList.add('hidden');
        }
    }

    async setupShareableUrl() {
        try {
            const response = await fetch(`${this.baseUrl}/users/${this.user.id}/public-url`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const publicURL = await response.text();
                const shareableUrlElement = document.getElementById('shareableUrl');
                if (shareableUrlElement) {
                    shareableUrlElement.textContent = publicURL;
                }
            } else if (response.status === 401) {
                this.handleUnauthorized();
            } else {
                throw new Error('Error al obtener URL pública');
            }
        } catch (error) {
            console.error('Error loading public URL:', error);
        }
    }

    setupBookingDashboardLink() {
        if (this.user && this.user.id) {
            const bookingDashboardLink = document.getElementById('bookingDashboardLink');
            if (bookingDashboardLink) {
                bookingDashboardLink.href = `booking-dashboard.html`;
            }
        }
    }

    async copyShareableUrl() {
        const shareableUrlElement = document.getElementById('shareableUrl');
        const copyBtn = document.getElementById('copyUrlBtn');

        if (shareableUrlElement && copyBtn) {
            const url = shareableUrlElement.textContent;

            try {
                // Using document.execCommand('copy') as navigator.clipboard.writeText() might not work in some iframe environments
                const textarea = document.createElement('textarea');
                textarea.value = url;
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand('copy');
                document.body.removeChild(textarea);

                const originalText = copyBtn.innerHTML;
                copyBtn.innerHTML = `
                    <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    ¡Copiado!
                `;

                setTimeout(() => {
                    copyBtn.innerHTML = originalText;
                }, 2000);

                this.showToast('URL copiada al portapapeles', 'success');
            } catch (err) {
                console.error('Error copying to clipboard:', err);
                this.showToast('Error al copiar la URL', 'error');
            }
        }
    }

   async loadMpConnectionStatus() {
       const statusElement = document.getElementById('mpConnectionStatus');
       const connectBtn = document.getElementById('connectMpBtn');
       const unlinkBtn = document.getElementById('unlinkMpBtn');
       if (!statusElement || !connectBtn || !unlinkBtn) return;

       // --- show loading state for button ---
       const originalText = connectBtn.textContent;
       connectBtn.disabled = true;
       connectBtn.innerHTML = `
           <svg class="animate-spin h-4 w-4 mr-2 text-white" xmlns="http://www.w3.org/2000/svg" fill="none"
                viewBox="0 0 24 24">
               <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
               <path class="opacity-75" fill="currentColor"
                     d="M4 12a8 8 0 018-8v4l3-3-3-3v4a8 8 0 00-8 8z"></path>
           </svg>
           Cargando...
       `;
       connectBtn.classList.add('bg-sky-400', 'cursor-wait');

       try {
           const response = await fetch(`${this.baseUrl}/mercadopago/oauth/status/${this.user.id}`, {
               method: 'GET',
               headers: {
                   'Authorization': `Bearer ${this.token}`,
                   'Content-Type': 'application/json'
               }
           });

           if (response.ok) {
               const data = await response.json();
               if (data.connected) {
                   statusElement.innerHTML = `
                       <svg class="h-4 w-4 mr-1 text-green-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                           <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                 d="M5 13l4 4L19 7" />
                       </svg>
                       Cuenta vinculada
                   `;
                   statusElement.className =
                       "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700 border border-green-300 mb-3";
                   connectBtn.style.display = "none";

                   unlinkBtn.classList.remove("hidden");

               } else {
                   statusElement.innerHTML = `
                       <svg class="h-4 w-4 mr-1 text-red-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                           <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                 d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                       </svg>
                       No vinculada
                   `;
                   statusElement.className =
                       "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-700 border border-red-300 mb-3";
                   connectBtn.style.display = "inline-block";
                   connectBtn.textContent = "Vincular cuenta";
                   connectBtn.disabled = false;
                   connectBtn.classList.remove('bg-gray-400', 'cursor-not-allowed', 'cursor-wait', 'bg-sky-400');

                   unlinkBtn.classList.add("hidden");
               }
           } else {
               statusElement.innerHTML = `
                   <svg class="h-4 w-4 mr-1 text-yellow-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                       <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                             d="M12 9v2m0 4h.01M4.93 4.93l14.14 14.14" />
                   </svg>
                   Error al obtener el estado de conexión
               `;
               statusElement.className =
                   "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-yellow-100 text-yellow-700 border border-yellow-300 mb-3";
               connectBtn.style.display = "inline-block";

               unlinkBtn.classList.add("hidden");
           }
       } catch (err) {
           console.error(err);
           statusElement.innerHTML = `
               <svg class="h-4 w-4 mr-1 text-red-600 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                   <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                         d="M12 9v2m0 4h.01M4.93 4.93l14.14 14.14" />
               </svg>
               Error de conexión
           `;
           statusElement.className =
               "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-700 border border-red-300 mb-3";
           connectBtn.style.display = "inline-block";

           unlinkBtn.classList.add("hidden");
       } finally {
           if (connectBtn.style.display !== "none") {
               connectBtn.innerHTML = originalText;
               connectBtn.disabled = false;
               connectBtn.classList.remove('cursor-wait', 'bg-sky-400');
           }
       }
   }


   setupMpUnlinkButton() {
       const unlinkBtn = document.getElementById('unlinkMpBtn');
       if (!unlinkBtn) return;

       unlinkBtn.addEventListener('click', async () => {

           const result = await Swal.fire({
               title: "¿Desvincular cuenta?",
               text: "Tu negocio dejará de recibir pagos hasta que vuelvas a vincular.",
               icon: "warning",
               showCancelButton: true,
               confirmButtonText: "Sí, desvincular",
               cancelButtonText: "Cancelar"
           });

           if (!result.isConfirmed) return;

           try {
               const response = await fetch(`${this.baseUrl}/mercadopago/oauth/unlink/${this.user.id}`, {
                   method: 'DELETE',
                   headers: {
                       'Authorization': `Bearer ${this.token}`
                   }
               });

               if (response.ok) {
                   Swal.fire({
                       icon: "success",
                       title: "Cuenta desvinculada",
                       text: "Tu cuenta fue desconectada correctamente."
                   });

                   this.loadMpConnectionStatus();
               } else {
                   this.showToast('No se pudo desvincular la cuenta', 'error');
               }
           } catch (err) {
               console.error(err);
               this.showToast('Error al intentar desvincular la cuenta', 'error');
           }
       });
   }



    setupMpConnectButton() {
        const connectBtn = document.getElementById('connectMpBtn');
        if (!connectBtn) return;

        connectBtn.addEventListener('click', async () => {
            try {
                const response = await fetch(`${this.baseUrl}/mercadopago/oauth/link?userId=${this.user.id}`, {
                    headers: {
                        'Authorization': `Bearer ${this.token}`
                    }
                });
                if (response.ok) {
                    const { authUrl } = await response.json();
                    window.location.href = authUrl; // redirige al OAuth de MP
                } else {
                    this.showToast('No se pudo iniciar la vinculación con Mercado Pago', 'error');
                }
            } catch (err) {
                console.error(err);
                this.showToast('Error al conectar con Mercado Pago', 'error');
            }
        });
    }


}

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const linked = params.get("linked");

    if (linked === "true") {
      Swal.fire({
        icon: "success",
        title: "Cuenta vinculada",
        text: "Tu cuenta de Mercado Pago fue vinculada correctamente.",
      });
    } else if (linked === "false") {
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo vincular tu cuenta de Mercado Pago. Intentalo nuevamente.",
      });
    }


    new DashboardManager();
});

