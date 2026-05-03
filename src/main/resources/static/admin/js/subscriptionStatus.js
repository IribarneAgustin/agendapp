document.addEventListener('DOMContentLoaded', async () => {
    try {
        const data = await fetchSubscription();
        renderMainPlanMetrics(data);
        window.userSubscriptionData = data;
    } catch (e) {
        console.error('Error loading subscription:', e);
    }

    initDynamicPricing();
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

    let selectedResources = 0;
    if (planCode === 'PROFESSIONAL') {
        const slider = document.getElementById('pro-slider');
        selectedResources = slider ? parseInt(slider.value) : 2;
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
                btn.disabled = true;
                btn.className = "w-full py-2 bg-gray-100 text-gray-400 rounded-xl font-bold text-xs cursor-not-allowed";
                btn.textContent = "Plan Activo";

                card.classList.add('border-2', 'border-indigo-600');
            } else {
                if (code !== 'FREE_TIER') {
                    btn.disabled = false;
                    btn.onclick = () => window.handleCheckout(code);
                }
            }
        }
    });
}

function initDynamicPricing() {
    const BASE_PRICE = 29000;
    const PRICE_PER_PRO = 6000;
    const INCLUDED_PROS = 2;

    const slider = document.getElementById('pro-slider');
    const priceEl = document.getElementById('pro-price');
    const countEl = document.getElementById('pro-count');

    if (!slider) return;

    function updatePrice() {
        const pros = parseInt(slider.value);
        const extra = Math.max(0, pros - INCLUDED_PROS);
        const total = BASE_PRICE + (extra * PRICE_PER_PRO);

        if (priceEl) priceEl.textContent = formatPrice(total);
        if (countEl) countEl.textContent = pros;
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