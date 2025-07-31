const footballField = document.getElementById('football-field');
const coordinatesDiv = document.getElementById('coordinates');

footballField.addEventListener('click', (event) => {
    const x = event.offsetX;
    const y = event.offsetY;
    coordinatesDiv.textContent = `Coordinates: (${x}, ${y})`;
});