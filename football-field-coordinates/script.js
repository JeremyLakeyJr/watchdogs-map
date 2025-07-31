document.addEventListener('DOMContentLoaded', () => {
    const field = document.getElementById('football-field');
    const marker = document.getElementById('marker');
    const coordsDisplay = document.getElementById('coordinates-display');

    // Input elements
    const sideInput = document.getElementById('side-input');
    const yardlineInput = document.getElementById('yardline-input');
    const stepsInput = document.getElementById('steps-input');
    const verticalInput = document.getElementById('vertical-input');
    const goButton = document.getElementById('go-to-coords');

    // --- Field Geometry ---
    const FIELD_WIDTH_PX = 1000;
    const FIELD_HEIGHT_PX = 533.3;
    const YARDS_PER_PIXEL = 100 / FIELD_WIDTH_PX;
    const STEPS_PER_PIXEL = (53 + 1/3) / (5/8) / FIELD_HEIGHT_PX;

    // --- Coordinate Conversion Functions ---

    function pixelToMarching(px, py) {
        const yardsFromSide1Goal = px * YARDS_PER_PIXEL;
        const side = yardsFromSide1Goal <= 50 ? 1 : 2;
        const yardLine = side === 1 ? yardsFromSide1Goal : 100 - yardsFromSide1Goal;

        const stepsFromSideline = py * STEPS_PER_PIXEL;
        // This is a simplified model. A real hash system is more complex.
        coordsDisplay.textContent = `Side ${side}, ${yardLine.toFixed(1)} yard line, ${stepsFromSideline.toFixed(1)} steps from sideline`;
    }

    function marchingToPixel(side, yardLine, steps) {
        const yardsFromSide1Goal = side === '1' ? yardLine : 100 - yardLine;
        const px = yardsFromSide1Goal / YARDS_PER_PIXEL;
        const py = steps / STEPS_PER_PIXEL;
        return { x: px, y: py };
    }

    function updateMarker(px, py) {
        marker.style.left = `${px}px`;
        marker.style.top = `${py}px`;
        marker.style.display = 'block';
    }

    // --- Event Listeners ---

    field.addEventListener('click', (event) => {
        const rect = field.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        updateMarker(x, y);
        pixelToMarching(x, y);
    });

    goButton.addEventListener('click', () => {
        const side = sideInput.value;
        const yardLine = parseFloat(yardlineInput.value);
        const steps = parseFloat(stepsInput.value);

        if (isNaN(yardLine) || isNaN(steps)) {
            alert("Please enter valid numbers for yard line and steps.");
            return;
        }

        const { x, y } = marchingToPixel(side, yardLine, steps);
        updateMarker(x, y);
        coordsDisplay.textContent = `Set to: Side ${side}, ${yardLine} yard line, ${steps} steps from sideline.`;
    });
});