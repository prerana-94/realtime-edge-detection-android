"use strict";
/**
 * Edge Detection Web Viewer
 * Displays processed frames from the Android application
 */
class FrameViewer {
    constructor(canvasId, statsId) {
        this.currentFrame = null;
        this.canvas = document.getElementById(canvasId);
        this.statsElement = document.getElementById(statsId);
        const context = this.canvas.getContext('2d');
        if (!context) {
            throw new Error('Could not get 2D context');
        }
        this.ctx = context;
        // Default stats
        this.stats = {
            width: 0,
            height: 0,
            fps: 0,
            processingTime: 0,
            mode: 'edge'
        };
        this.init();
    }
    init() {
        console.log('Frame Viewer initialized');
        this.updateStats();
        // Setup drag and drop for images
        this.setupDragDrop();
    }
    setupDragDrop() {
        const dropZone = document.getElementById('dropZone');
        if (!dropZone)
            return;
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('drag-over');
        });
        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('drag-over');
        });
        dropZone.addEventListener('drop', (e) => {
            var _a;
            e.preventDefault();
            dropZone.classList.remove('drag-over');
            const files = (_a = e.dataTransfer) === null || _a === void 0 ? void 0 : _a.files;
            if (files && files.length > 0) {
                this.loadImage(files[0]);
            }
        });
    }
    /**
     * Load an image file and display it
     */
    loadImage(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            var _a;
            const img = new Image();
            img.onload = () => {
                this.currentFrame = img;
                this.stats.width = img.width;
                this.stats.height = img.height;
                this.drawFrame();
                this.updateStats();
            };
            img.src = (_a = e.target) === null || _a === void 0 ? void 0 : _a.result;
        };
        reader.readAsDataURL(file);
    }
    /**
     * Load frame from base64 string
     */
    loadBase64Frame(base64Data, stats) {
        const img = new Image();
        img.onload = () => {
            this.currentFrame = img;
            if (stats) {
                this.stats = Object.assign(Object.assign({}, this.stats), stats);
            }
            this.stats.width = img.width;
            this.stats.height = img.height;
            this.drawFrame();
            this.updateStats();
        };
        img.src = base64Data.startsWith('data:') ? base64Data : `data:image/png;base64,${base64Data}`;
    }
    /**
     * Draw current frame on canvas
     */
    drawFrame() {
        if (!this.currentFrame)
            return;
        // Resize canvas to fit image
        this.canvas.width = this.currentFrame.width;
        this.canvas.height = this.currentFrame.height;
        // Draw image
        this.ctx.drawImage(this.currentFrame, 0, 0);
        // Add overlay text
        this.drawOverlay();
    }
    /**
     * Draw overlay information on canvas
     */
    drawOverlay() {
        this.ctx.save();
        // Semi-transparent background
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        this.ctx.fillRect(10, 10, 200, 80);
        // White text
        this.ctx.fillStyle = '#ffffff';
        this.ctx.font = '14px monospace';
        this.ctx.fillText(`Mode: ${this.stats.mode.toUpperCase()}`, 20, 30);
        this.ctx.fillText(`Resolution: ${this.stats.width}x${this.stats.height}`, 20, 50);
        this.ctx.fillText(`FPS: ${this.stats.fps}`, 20, 70);
        this.ctx.fillText(`Processing: ${this.stats.processingTime}ms`, 20, 90);
        this.ctx.restore();
    }
    /**
     * Update statistics display
     */
    updateStats() {
        this.statsElement.innerHTML = `
            <div class="stat-item">
                <span class="stat-label">Resolution:</span>
                <span class="stat-value">${this.stats.width} x ${this.stats.height}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">FPS:</span>
                <span class="stat-value">${this.stats.fps}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Processing Time:</span>
                <span class="stat-value">${this.stats.processingTime}ms</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Mode:</span>
                <span class="stat-value">${this.stats.mode}</span>
            </div>
        `;
    }
    /**
     * Update frame statistics
     */
    updateFrameStats(stats) {
        this.stats = Object.assign(Object.assign({}, this.stats), stats);
        this.updateStats();
        if (this.currentFrame) {
            this.drawFrame();
        }
    }
}
// Initialize viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new FrameViewer('frameCanvas', 'statsContainer');
    // Load sample processed frame (you'll replace this with actual frame from Android)
    const sampleBase64 = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
    viewer.loadBase64Frame(sampleBase64, {
        width: 1920,
        height: 1080,
        fps: 15,
        processingTime: 35,
        mode: 'edge'
    });
    // File input handler
    const fileInput = document.getElementById('fileInput');
    fileInput === null || fileInput === void 0 ? void 0 : fileInput.addEventListener('change', (e) => {
        var _a;
        const target = e.target;
        const file = (_a = target.files) === null || _a === void 0 ? void 0 : _a[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = () => {
                viewer.loadBase64Frame(reader.result);
            };
            reader.readAsDataURL(file);
        }
    });
    console.log('Edge Detection Viewer ready');
});
