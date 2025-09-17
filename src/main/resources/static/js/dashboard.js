class DashboardManager {
    constructor() {
        this.baseUrl = 'http://localhost:8080';
        this.token = localStorage.getItem('authToken');
        this.user = this.getStoredUser();
        
        if (!this.token || !this.user) {
            window.location.href = 'index.html';
            return;
        }
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.setupShareableUrl();
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
            // Set default values if there's an error
            this.updateStats([]);
        }
    }

    updateStats(offerings) {
        const totalOfferings = offerings.length;
        const activeOfferings = offerings.filter(o => o.active).length;

        // Update DOM elements
        const totalElement = document.getElementById('totalOfferings');
        const activeElement = document.getElementById('activeOfferings');

        if (totalElement) totalElement.textContent = totalOfferings;
        if (activeElement) activeElement.textContent = activeOfferings;
    }

    handleUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = 'index.html';
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        window.location.href = 'index.html';
    }

    showToast(message, type = 'info') {
        const toast = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');

        if (!toast || !toastMessage || !toastIcon) return;

        // Set message
        toastMessage.textContent = message;

        // Set icon based on type
        let iconHTML = '';
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

        // Show toast
        toast.classList.remove('hidden');

        // Auto hide after 5 seconds
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

    setupShareableUrl() {
        if (this.user && this.user.id) {
            const baseUrl = window.location.origin;
            const shareableUrl = `${baseUrl}/user-offerings.html?userId=${this.user.id}`;

            const shareableUrlElement = document.getElementById('shareableUrl');
            if (shareableUrlElement) {
                shareableUrlElement.textContent = shareableUrl;
            }
        }
    }

    async copyShareableUrl() {
        const shareableUrlElement = document.getElementById('shareableUrl');
        const copyBtn = document.getElementById('copyUrlBtn');

        if (shareableUrlElement && copyBtn) {
            const url = shareableUrlElement.textContent;

            try {
                await navigator.clipboard.writeText(url);

                // Visual feedback
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
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new DashboardManager();
});