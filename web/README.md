# Edge Detection Web Viewer

TypeScript-based web viewer for displaying processed frames from the Android Edge Detection application.

## Features

- 📤 Drag and drop image loading
- 📊 Real-time frame statistics display
- 🎨 Visual overlay with processing information
- 📱 Responsive design

## Setup
```bash
cd web
npm install
npm run build
```

## Usage

1. Build the TypeScript code: `npm run build`
2. Open `index.html` in a web browser
3. Load a processed frame from the Android app

## Development

Watch mode for auto-compilation:
```bash
npm run watch
```

## Architecture

- **viewer.ts**: Main TypeScript logic for frame display and statistics
- **index.html**: UI and styling
- Canvas-based rendering with overlay graphics