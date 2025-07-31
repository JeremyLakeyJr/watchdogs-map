const coordinatesDiv = document.getElementById('coordinates');
const latInput = document.getElementById('lat-input');
const lngInput = document.getElementById('lng-input');
const goToCoordsButton = document.getElementById('go-to-coords');

// Initialize the map
const map = L.map('map').setView([36.2797, -95.8283], 18);

// Add a satellite tile layer
L.tileLayer('https://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
    maxZoom: 20,
    subdomains:['mt0','mt1','mt2','mt3']
}).addTo(map);

let marker = L.marker([36.2797, -95.8283]).addTo(map);

// Update coordinates on map click
map.on('click', function(e) {
    const { lat, lng } = e.latlng;
    coordinatesDiv.textContent = `Coordinates: (${lat.toFixed(6)}, ${lng.toFixed(6)})`;
    marker.setLatLng(e.latlng);
});

// Go to coordinates from input
goToCoordsButton.addEventListener('click', () => {
    const lat = parseFloat(latInput.value);
    const lng = parseFloat(lngInput.value);

    if (!isNaN(lat) && !isNaN(lng)) {
        const newLatLng = L.latLng(lat, lng);
        map.setView(newLatLng, 18);
        marker.setLatLng(newLatLng);
        coordinatesDiv.textContent = `Coordinates: (${lat.toFixed(6)}, ${lng.toFixed(6)})`;
    } else {
        alert('Please enter valid coordinates.');
    }
});