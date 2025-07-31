document.addEventListener('DOMContentLoaded', () => {
    const fieldImage = document.getElementById('football-field');
    const marker = document.getElementById('marker');
    const coordsDisplay = document.getElementById('coordinates-display');

    // Input elements
    const sideInput = document.getElementById('side-input');
    const yardlineInput = document.getElementById('yardline-input');
    const stepsInput = document.getElementById('steps-input');
    const verticalInput = document.getElementById('vertical-input');
    const goButton = document.getElementById('go-to-coords');

    // --- Field Geometry (in pixels and yards) ---
    // These values are based on the specific image being used.
    // Image dimensions: 1280x597
    const PIXELS_PER_YARD_HORIZONTAL = 10.66; // (1280px - 2 * 10-yard endzones) / 100 yards is not quite right, need to measure image.
                                              // Let's measure from goal line to goal line in the image. It's roughly 1067px. So 10.67 px/yd.
    const FIELD_WIDTH_PIXELS = 1067;
    const FIELD_HEIGHT_PIXELS = 318; // The playable area between sidelines.
    const HORIZONTAL_OFFSET_PIXELS = 107; // Distance from left edge to left goal line.
    const VERTICAL_OFFSET_PIXELS = 140; // Distance from top edge to the front sideline.

    const YARDS_PER_STEP = 5 / 8; // 8 steps to 5 yards
    const FIELD_WIDTH_YARDS = 53 + 1/3;
    const STEPS_ACROSS_FIELD = FIELD_WIDTH_YARDS / YARDS_PER_STEP; // Approx 85.33 steps

    // --- Coordinate Conversion Functions ---

    function pixelToMarching(px, py) {
        // Clamp coordinates to be within the field boundaries
        const clampedX = Math.max(HORIZONTAL_OFFSET_PIXELS, Math.min(px, HORIZONTAL_OFFSET_PIXELS + FIELD_WIDTH_PIXELS));
        const clampedY = Math.max(VERTICAL_OFFSET_PIXELS, Math.min(py, VERTICAL_OFFSET_PIXELS + FIELD_HEIGHT_PIXELS));

        // Horizontal Calculation (Yard Line)
        const yardsFromGoalLine = (clampedX - HORIZONTAL_OFFSET_PIXELS) / (FIELD_WIDTH_PIXELS / 100);
        let side = yardsFromGoalLine <= 50 ? 1 : 2;
        let yardLine = yardsFromGoalLine <= 50 ? yardsFromGoalLine : 100 - yardsFromGoalLine;

        // Vertical Calculation (Steps)
        const stepsFromCenter = (clampedY - (VERTICAL_OFFSET_PIXELS + FIELD_HEIGHT_PIXELS / 2)) / (FIELD_HEIGHT_PIXELS / STEPS_ACROSS_FIELD);
        
        // For this simplified model, we'll just use steps from center
        coordsDisplay.textContent = `Side ${side}, ${yardLine.toFixed(1)} yard line, ${stepsFromCenter.toFixed(1)} steps from center`;
        
        return { x: clampedX, y: clampedY };
    }

    function marchingToPixel(side, yardLine, steps, verticalPos) {
        // Horizontal Calculation
        const yardsFromGoalLine = side === '1' ? yardLine : 100 - yardLine;
        const px = HORIZONTAL_OFFSET_PIXELS + (yardsFromGoalLine * (FIELD_WIDTH_PIXELS / 100));

        // Vertical Calculation
        // This is a simplification. A real hash system is more complex.
        // 'On Hash' is 1/3 of the way from the sideline.
        const HASH_OFFSET_YARDS = FIELD_WIDTH_YARDS / 3;
        const HASH_OFFSET_STEPS = HASH_OFFSET_YARDS / YARDS_PER_STEP;
        
        let stepsFromCenter;
        if (verticalPos === 'on') {
            stepsFromCenter = -HASH_OFFSET_STEPS; // Front hash
        } else if (verticalPos === 'inside') {
            stepsFromCenter = -HASH_OFFSET_STEPS + steps;
        } else { // outside
            stepsFromCenter = -HASH_OFFSET_STEPS - steps;
        }
        
        const py = (VERTICAL_OFFSET_PIXELS + FIELD_HEIGHT_PIXELS / 2) + (stepsFromCenter * (FIELD_HEIGHT_PIXELS / STEPS_ACROSS_FIELD));

        return { x: px, y: py };
    }

    // --- Event Listeners ---

    fieldImage.addEventListener('click', (event) => {
        const rect = fieldImage.getBoundingClientRect();
        // Adjust for the image's rendered size vs. its natural size
        const scaleX = fieldImage.naturalWidth / rect.width;
        const scaleY = fieldImage.naturalHeight / rect.height;

        const imageX = (event.clientX - rect.left) * scaleX;
        const imageY = (event.clientY - rect.top) * scaleY;

        const markerPos = pixelToMarching(imageX, imageY);

        // Position marker based on scaled image dimensions
        marker.style.left = `${(markerPos.x / fieldImage.naturalWidth) * 100}%`;
        marker.style.top = `${(markerPos.y / fieldImage.naturalHeight) * 100}%`;
        marker.style.display = 'block';
    });
    
    goButton.addEventListener('click', () => {
        const side = sideInput.value;
        const yardLine = parseFloat(yardlineInput.value);
        const steps = parseFloat(stepsInput.value);
        const verticalPos = verticalInput.value;

        if (isNaN(yardLine) || isNaN(steps)) {
            alert("Please enter valid numbers for yard line and steps.");
            return;
        }

        const markerPos = marchingToPixel(side, yardLine, steps, verticalPos);

        marker.style.left = `${(markerPos.x / fieldImage.naturalWidth) * 100}%`;
        marker.style.top = `${(markerPos.y / fieldImage.naturalHeight) * 100}%`;
        marker.style.display = 'block';
        
        coordsDisplay.textContent = `Set to: Side ${side}, ${yardLine} yard line, ${steps} steps ${verticalPos} hash.`;
    });
});