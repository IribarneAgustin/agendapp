const params = new URLSearchParams(window.location.search);

const checkoutLink = params.get("checkoutLink");
const expirationDate = params.get("expirationDate");

if (checkoutLink) {
    document.getElementById("checkout-btn").href = checkoutLink;
}

function formatDate(dateTimeString) {
    const date = new Date(dateTimeString);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
}

if (expirationDate) {
    document.getElementById("expiration-text").innerText =
        `Tu suscripción se encuentra vencida desde el ${formatDate(expirationDate)}.`;
} else {
    document.getElementById("expiration-text").innerText =
        "Tu suscripción se encuentra vencida.";
}