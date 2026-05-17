document.addEventListener('DOMContentLoaded', async () => {
    let subscriptionData = null;
    try {
        subscriptionData = await fetchSubscription();
        renderMainPlanMetrics(subscriptionData);
        window.userSubscriptionData = subscriptionData;
    } catch (e) {
        console.error('Error loading subscription:', e);
    }
    initDynamicPricing(subscriptionData);
});

async function fetchSubscription() {
    const baseUrl = typeof BASE_URL !== 'undefined' ? BASE_URL : '';
    const userDataStorage = JSON.parse(localStorage.getItem('userData'));

    if (!userDataStorage?.id) throw new Error('User not found');

    const response = await fetch(`${baseUrl}/subscription/user/${userDataStorage.id}`, {
        method: 'GET',
        credentials: 'include'
    });

    if (!response.ok) throw new Error('Error fetching subscription');

    return await response.json();
}

window.handleCheckout = async function(planCode) {
    const baseUrl = typeof BASE_URL !== 'undefined' ? BASE_URL : '';
    const userDataStorage = JSON.parse(localStorage.getItem('userData'));

    let selectedResources = 2; // Default limit for BASIC and FREE_TIER

    if (planCode === 'PROFESSIONAL') {
        const slider = document.getElementById('pro-slider');
        selectedResources = slider ? parseInt(slider.value) : 2;
    }

    if (window.userSubscriptionData && window.userSubscriptionData.features) {
        const resourceFeature = window.userSubscriptionData.features.find(f => f.featureName === 'RESOURCES');
        const currentlyUsed = resourceFeature ? (resourceFeature.used ?? 0) : 0;

        if (selectedResources < currentlyUsed) {
            const excess = currentlyUsed - selectedResources;

            Swal.fire({
                title: 'Acción requerida',
                text: `Actualmente tienes ${currentlyUsed} profesionales activos. Para cambiar a este límite (${selectedResources}), primero debes desactivar o eliminar ${excess} profesional(es).`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#4f46e5',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'Ir a Profesionales',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/admin/resources.html';
                }
            });
            return;
        }
    }

    const { isConfirmed } = await Swal.fire({
        title: '¿Confirmar cambio de plan?',
        text: `Serás redirigido para completar el pago.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4f46e5',
        cancelButtonColor: '#ef4444',
        confirmButtonText: 'Sí, continuar',
        cancelButtonText: 'Cancelar'
    });

    if (!isConfirmed) return;

    Swal.fire({
        title: 'Generando orden de pago...',
        text: 'Por favor, espera un momento.',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        const response = await fetch(`${baseUrl}/subscription/${planCode}/user/${userDataStorage.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ selectedResources: selectedResources }),
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Error al generar el pago');
        const checkoutUrl = await response.text();
        window.location.href = checkoutUrl;

    } catch (e) {
        console.error('Checkout error:', e);
        Swal.fire({
            title: 'Error',
            text: 'No pudimos conectar con la pasarela de pagos. Por favor, intenta de nuevo.',
            icon: 'error',
            confirmButtonColor: '#4f46e5'
        });
    }
};

function renderMainPlanMetrics(data) {
    const rawPlanName = (data.planName || '').toUpperCase();

    const planNames = {
        'BASIC': 'Plan Básico',
        'PROFESSIONAL': 'Plan Profesional',
        'FREE_TIER': 'Plan de Prueba GRATIS'
    };

    const displayPlanName = planNames[rawPlanName] || `Plan ${rawPlanName}`;

    setText('plan-title-display', displayPlanName);

    hide('plan-badge');

    const renewBtn = document.getElementById('btn-renew-subscription');
    if (data.checkoutLink && renewBtn) {
        renewBtn.classList.remove('hidden');
        renewBtn.onclick = () => { window.location.href = data.checkoutLink; };
    } else if (renewBtn) {
        renewBtn.classList.add('hidden');
    }

    renderFeatures(data.features || []);
    renderBilling(data.billing);
    markActivePlanInComparison(rawPlanName);
}

function renderFeatures(features) {
    features.forEach(feature => {
        switch (feature.featureName) {
            case "BOOKINGS":
                renderMetric('res', feature);
                break;
            case "RESOURCES":
                renderMetric('prof', feature);
                break;
        }
    });
}

function renderMetric(prefix, feature) {
    const used = feature.used ?? 0;
    const limit = feature.limit;

    setText(`${prefix}-current`, used);

    if (limit === null) {
        hide(`${prefix}-limit-separator`);
        hide(`${prefix}-progress-container`);
        show(`${prefix}-unlimited-label`);
        return;
    }

    show(`${prefix}-limit-separator`);
    show(`${prefix}-progress-container`);
    hide(`${prefix}-unlimited-label`);

    const pct = Math.min((used / limit) * 100, 100);
    setText(`${prefix}-max`, limit);
    setWidth(`${prefix}-bar`, pct);
    setText(`${prefix}-remain`, Math.max(0, limit - used));
    setText(`${prefix}-pct`, `${Math.round(pct)}%`);
}

function renderBilling(billing) {
    if (!billing) return;

    const start = new Date(billing.startDate);
    const end = new Date(billing.endDate);
    const today = new Date();
    const diff = Math.max(0, Math.ceil((end - today) / (1000 * 60 * 60 * 24)));
    const format = (d) => d.toLocaleDateString('es-AR', { day: '2-digit', month: 'short' });

    setText('billing-range', `${format(start)} - ${format(end)} • ${diff} días restantes`);
    show('billing-period');
}

function markActivePlanInComparison(planCode) {
    const normalized = planCode.toUpperCase();

    ['FREE_TIER', 'BASIC', 'PROFESSIONAL'].forEach(code => {
        const card = document.getElementById(`plan-${code}-card`);
        if (!card) return;

        card.classList.remove('border-2', 'border-indigo-600');

        const tag = card.querySelector('.plan-actual-tag');
        if (tag) tag.remove();

        const btn = card.querySelector('button');
        if (btn) {
            if (code === normalized) {
                card.classList.add('border-2', 'border-indigo-600');

                if (code === 'PROFESSIONAL') {
                    // Initially disabled because slider matches current configuration on load
                    btn.disabled = true;
                    btn.className = "w-full py-2 bg-gray-100 text-gray-400 rounded-xl font-bold text-xs cursor-not-allowed transition-colors";
                    btn.textContent = "Plan Activo";
                    btn.onclick = () => window.handleCheckout(code);
                } else {
                    btn.disabled = true;
                    btn.className = "w-full py-2 bg-gray-100 text-gray-400 rounded-xl font-bold text-xs cursor-not-allowed";
                    btn.textContent = "Plan Activo";
                }
            } else {
                if (code !== 'FREE_TIER') {
                    btn.disabled = false;
                    if (code === 'PROFESSIONAL') {
                        btn.className = "w-full py-2 bg-indigo-600 text-white rounded-xl font-bold text-xs hover:bg-indigo-700 shadow-md shadow-indigo-200 transition-all";
                    } else {
                        btn.className = "w-full py-2 bg-gray-50 border border-gray-200 rounded-xl font-bold text-xs hover:bg-gray-100 transition-colors";
                    }
                    btn.textContent = "Elegir Plan";
                    btn.onclick = () => window.handleCheckout(code);
                }
            }
        }
    });
}

function initDynamicPricing(data) {
    const BASE_PRICE = 29000;
    const PRICE_PER_PRO = 6000;
    const INCLUDED_PROS = 2;

    const slider = document.getElementById('pro-slider');
    const priceEl = document.getElementById('pro-price');
    const countEl = document.getElementById('pro-count');
    const proCard = document.getElementById('plan-PROFESSIONAL-card');

    if (!slider) return;

    let currentLimit = 2;
    if (data && data.features) {
        const resourceFeature = data.features.find(f => f.featureName === 'RESOURCES');
        if (resourceFeature && resourceFeature.limit) {
            currentLimit = resourceFeature.limit;
            slider.value = currentLimit;
        }
    }

    const isInitiallyProfessional = data && (data.planName || '').toUpperCase() === 'PROFESSIONAL';

    function updatePrice() {
        const pros = parseInt(slider.value);
        const extra = Math.max(0, pros - INCLUDED_PROS);
        const total = BASE_PRICE + (extra * PRICE_PER_PRO);

        if (priceEl) priceEl.textContent = formatPrice(total);
        if (countEl) countEl.textContent = pros;

        if (isInitiallyProfessional && proCard) {
            const btn = proCard.querySelector('button');
            if (btn) {
                if (pros === currentLimit) {
                    btn.disabled = true;
                    btn.className = "w-full py-2 bg-gray-100 text-gray-400 rounded-xl font-bold text-xs cursor-not-allowed transition-colors";
                    btn.textContent = "Plan Activo";
                } else {
                    btn.disabled = false;
                    btn.className = "w-full py-2 bg-indigo-100 text-indigo-700 rounded-xl font-bold text-xs hover:bg-indigo-200 transition-colors";
                    btn.textContent = "Actualizar Cantidad";
                }
            }
        }
    }

    slider.addEventListener('input', updatePrice);
    updatePrice();
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function show(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('hidden');
}

function hide(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('hidden');
}

function setWidth(id, pct) {
    const el = document.getElementById(id);
    if (el) el.style.width = `${pct}%`;
}

function formatPrice(value) {
    return value.toLocaleString('es-AR');
}