document.addEventListener('DOMContentLoaded', () => {

    // Check for authentication token on page load
    const token = localStorage.getItem('agendapp_token');
    if (!token) {
        // If no token is found, redirect to the index page
        window.location.href = 'index.html';
    }

    // --- UI Elements and State ---
    const serviceForm = document.getElementById('serviceForm');
    const servicesList = document.getElementById('servicesList');
    const logoutBtn = document.getElementById('logoutBtn');
    const navDropdownButton = document.getElementById('navDropdownButton');
    const navDropdownMenu = document.getElementById('navDropdownMenu');

    let services = JSON.parse(localStorage.getItem('agendapp_services')) || [];

    // --- Event Listeners ---

    // Toggle dropdown visibility
    navDropdownButton.addEventListener('click', () => {
        navDropdownMenu.classList.toggle('hidden');
    });

    // Logout and redirect to index.html
    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('agendapp_token');
        window.location.href = 'index.html';
    });

    serviceForm.addEventListener('submit', handleServiceSubmit);
    document.getElementById('cancelServiceBtn').addEventListener('click', () => {
        serviceForm.reset();
        document.getElementById('serviceId').value = '';
    });

    // Close dropdown if clicked outside of it
    window.addEventListener('click', (event) => {
        if (!navDropdownButton.contains(event.target) && !navDropdownMenu.contains(event.target)) {
            navDropdownMenu.classList.add('hidden');
        }
    });

    // --- Service CRUD Logic ---

    function renderServices() {
        servicesList.innerHTML = '';
        services.forEach(service => {
            const listItem = document.createElement('li');
            listItem.className = 'bg-gray-50 p-4 rounded-lg flex items-center justify-between shadow-sm hover:shadow-md transition-shadow duration-200';
            listItem.innerHTML = `
                <div>
                    <p class="font-semibold text-lg">${service.name}</p>
                    <p class="text-gray-500 text-sm mt-1">${service.description || 'Sin descripción'}</p>
                    <p class="text-gray-700 font-medium mt-1">$${service.price.toFixed(2)}</p>
                </div>
                <div class="flex space-x-2">
                    <button data-id="${service.id}" data-action="manage" class="px-3 py-1 bg-blue-500 text-white rounded-md text-xs font-medium hover:bg-blue-600">Disponibilidad</button>
                    <button data-id="${service.id}" data-action="edit" class="px-3 py-1 bg-yellow-500 text-white rounded-md text-xs font-medium hover:bg-yellow-600">Editar</button>
                    <button data-id="${service.id}" data-action="delete" class="px-3 py-1 bg-red-500 text-white rounded-md text-xs font-medium hover:bg-red-600">Eliminar</button>
                </div>
            `;
            servicesList.appendChild(listItem);
        });
        localStorage.setItem('agendapp_services', JSON.stringify(services));
    }

    function handleServiceSubmit(e) {
        e.preventDefault();
        const id = document.getElementById('serviceId').value;
        const name = document.getElementById('serviceName').value;
        const description = document.getElementById('serviceDescription').value;
        const price = parseFloat(document.getElementById('servicePrice').value);

        if (id) {
            // Update existing service
            const index = services.findIndex(s => s.id === id);
            if (index !== -1) {
                services[index] = { ...services[index], name, description, price };
            }
        } else {
            // Add new service
            const newService = {
                id: crypto.randomUUID(),
                name,
                description,
                price,
                availability: [] // Initialize with an empty availability array
            };
            services.push(newService);
        }

        serviceForm.reset();
        document.getElementById('serviceId').value = '';
        renderServices();
    }

    servicesList.addEventListener('click', (e) => {
        const target = e.target;
        const id = target.dataset.id;
        const action = target.dataset.action;

        if (action === 'edit') {
            const serviceToEdit = services.find(s => s.id === id);
            document.getElementById('serviceId').value = serviceToEdit.id;
            document.getElementById('serviceName').value = serviceToEdit.name;
            document.getElementById('serviceDescription').value = serviceToEdit.description;
            document.getElementById('servicePrice').value = serviceToEdit.price;
        } else if (action === 'delete') {
            services = services.filter(s => s.id !== id);
            renderServices();
        } else if (action === 'manage') {
            // Redirect to the new settings page with the service ID
            window.location.href = `service-settings.html?id=${id}`;
        }
    });

    // Initial render
    renderServices();
});
