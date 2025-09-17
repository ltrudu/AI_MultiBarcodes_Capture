// WMS Application JavaScript
class WMSApp {
    constructor() {
        this.apiBaseUrl = '/api';
        this.currentView = 'sessions';
        this.sessions = [];
        this.currentSession = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadSessions();
        this.startAutoRefresh();
    }

    setupEventListeners() {
        // Navigation buttons
        document.getElementById('btn-sessions').addEventListener('click', () => {
            this.showSessions();
        });

        document.getElementById('btn-refresh').addEventListener('click', () => {
            this.refresh();
        });

        document.getElementById('btn-reset').addEventListener('click', () => {
            this.resetAllData();
        });

        // Back button
        const backButton = document.getElementById('btn-back');
        if (backButton) {
            backButton.addEventListener('click', () => {
                this.showSessions();
            });
        }
    }

    async loadSessions(silent = false) {
        try {
            if (!silent) {
                this.showLoading('Loading capture sessions...');
            }

            const response = await fetch(`${this.apiBaseUrl}/barcodes.php`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.success) {
                this.sessions = data.sessions;
                this.renderSessions();
                this.updateStats();
            } else {
                throw new Error(data.error || 'Failed to load sessions');
            }
        } catch (error) {
            console.error('Error loading sessions:', error);
            if (!silent) {
                this.showError('Failed to load capture sessions: ' + error.message);
            }
        }
    }

    async loadSessionDetails(sessionId) {
        try {
            this.showLoading('Loading session details...');

            const response = await fetch(`${this.apiBaseUrl}/barcodes.php?session_id=${sessionId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.success) {
                this.currentSession = data;
                this.renderSessionDetails();
            } else {
                throw new Error(data.error || 'Failed to load session details');
            }
        } catch (error) {
            console.error('Error loading session details:', error);
            this.showError('Failed to load session details: ' + error.message);
        }
    }

    async updateBarcodeStatus(barcodeId, processed, notes = '') {
        try {
            const response = await fetch(`${this.apiBaseUrl}/barcodes.php?barcode_id=${barcodeId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    processed: processed,
                    notes: notes
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            if (data.success) {
                // Reload current session details to reflect changes
                if (this.currentSession) {
                    await this.loadSessionDetails(this.currentSession.session.id);
                }
            } else {
                throw new Error(data.error || 'Failed to update barcode status');
            }
        } catch (error) {
            console.error('Error updating barcode status:', error);
            this.showError('Failed to update barcode status: ' + error.message);
        }
    }

    async resetAllData() {
        // Show confirmation dialog
        if (!confirm('⚠️ WARNING: This will permanently delete ALL barcode capture sessions and data.\n\nThis action cannot be undone. Are you sure you want to continue?')) {
            return;
        }

        // Double confirmation for safety
        if (!confirm('🔴 FINAL CONFIRMATION: This will delete ALL data permanently.\n\nClick OK to proceed with complete data reset.')) {
            return;
        }

        try {
            this.showLoading('Resetting all data...');

            const response = await fetch(`${this.apiBaseUrl}/barcodes.php?reset=all`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            if (data.success) {
                // Show success message and reload
                this.showSuccess('All data has been reset successfully!');

                // Clear local data
                this.sessions = [];
                this.currentSession = null;

                // Reload the sessions view
                setTimeout(() => {
                    this.loadSessions();
                }, 2000);
            } else {
                throw new Error(data.error || 'Failed to reset data');
            }
        } catch (error) {
            console.error('Error resetting data:', error);
            this.showError('Failed to reset data: ' + error.message);
        }
    }

    renderSessions() {
        const container = document.getElementById('main-content');

        let html = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value" id="total-sessions">0</div>
                    <div class="stat-label">Total Sessions</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="total-barcodes">0</div>
                    <div class="stat-label">Total Barcodes</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="processed-barcodes">0</div>
                    <div class="stat-label">Processed</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="pending-barcodes">0</div>
                    <div class="stat-label">Pending</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>Recent Capture Sessions</h2>
                </div>
                <div class="card-body">
        `;

        if (this.sessions.length === 0) {
            html += `
                <div class="text-center" style="padding: 2rem; color: #6c757d;">
                    <p>No capture sessions found. Sessions will appear here when your Android app uploads barcode data.</p>
                </div>
            `;
        } else {
            html += `
                <div class="table-container">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Session Time</th>
                                <th>Device</th>
                                <th>Total Barcodes</th>
                                <th>Unique Types</th>
                                <th>Processed</th>
                                <th>Status</th>
                                <th>Duration</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

            this.sessions.forEach(session => {
                const sessionTime = new Date(session.session_timestamp).toLocaleString();
                const duration = this.calculateSessionDuration(session.first_scan, session.last_scan);
                const processedPercent = session.total_barcodes > 0
                    ? Math.round((session.processed_count / session.total_barcodes) * 100)
                    : 0;

                html += `
                    <tr onclick="app.showSessionDetails(${session.id})" class="session-row">
                        <td>${sessionTime}</td>
                        <td>${session.device_info || 'Unknown Device'}</td>
                        <td>${session.total_barcodes}</td>
                        <td>${session.unique_symbologies}</td>
                        <td>${session.processed_count}/${session.total_barcodes}</td>
                        <td>
                            <span class="status ${processedPercent === 100 ? 'processed' : 'pending'}">
                                ${processedPercent}% Complete
                            </span>
                        </td>
                        <td>${duration}</td>
                    </tr>
                `;
            });

            html += `
                        </tbody>
                    </table>
                </div>
            `;
        }

        html += `
                </div>
            </div>
        `;

        container.innerHTML = html;
        this.currentView = 'sessions';
    }

    renderSessionDetails() {
        const container = document.getElementById('main-content');
        const session = this.currentSession.session;
        const barcodes = this.currentSession.barcodes;

        const sessionTime = new Date(session.session_timestamp).toLocaleString();
        const duration = this.calculateSessionDuration(session.first_scan, session.last_scan);

        let html = `
            <div class="card">
                <div class="card-header">
                    <h2>Session Details - ${sessionTime}</h2>
                    <button id="btn-back" class="btn btn-secondary" style="float: right;">← Back to Sessions</button>
                </div>
                <div class="card-body">
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-value">${session.total_barcodes}</div>
                            <div class="stat-label">Total Barcodes</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${session.unique_symbologies}</div>
                            <div class="stat-label">Unique Types</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${session.processed_count}</div>
                            <div class="stat-label">Processed</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${duration}</div>
                            <div class="stat-label">Duration</div>
                        </div>
                    </div>

                    <div style="margin-bottom: 2rem;">
                        <strong>Device:</strong> ${session.device_info || 'Unknown Device'}<br>
                        <strong>Session ID:</strong> ${session.id}<br>
                        <strong>First Scan:</strong> ${session.first_scan ? new Date(session.first_scan).toLocaleString() : 'N/A'}<br>
                        <strong>Last Scan:</strong> ${session.last_scan ? new Date(session.last_scan).toLocaleString() : 'N/A'}
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>Captured Barcodes (${barcodes.length})</h2>
                </div>
                <div class="card-body">
        `;

        if (barcodes.length === 0) {
            html += `
                <div class="text-center" style="padding: 2rem; color: #6c757d;">
                    <p>No barcodes found in this session.</p>
                </div>
            `;
        } else {
            barcodes.forEach(barcode => {
                const scanTime = new Date(barcode.timestamp).toLocaleString();

                html += `
                    <div class="barcode-item">
                        <div class="barcode-value">${this.escapeHtml(barcode.value)}</div>
                        <div class="barcode-meta">
                            <div class="meta-item">
                                <span class="meta-label">Symbology</span>
                                <span>${barcode.symbology_name} (${barcode.symbology})</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Quantity</span>
                                <span>${barcode.quantity}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Scan Time</span>
                                <span>${scanTime}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Status</span>
                                <span class="status ${barcode.processed ? 'processed' : 'pending'}">
                                    ${barcode.processed ? 'Processed' : 'Pending'}
                                </span>
                            </div>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button class="btn ${barcode.processed ? 'btn-secondary' : 'btn-success'}"
                                    onclick="app.toggleBarcodeStatus(${barcode.id}, ${!barcode.processed})">
                                ${barcode.processed ? 'Mark as Pending' : 'Mark as Processed'}
                            </button>
                            ${barcode.notes ? `<div style="margin-top: 0.5rem; font-style: italic;">Notes: ${this.escapeHtml(barcode.notes)}</div>` : ''}
                        </div>
                    </div>
                `;
            });
        }

        html += `
                </div>
            </div>
        `;

        container.innerHTML = html;
        this.currentView = 'session-details';

        // Re-attach back button event listener
        document.getElementById('btn-back').addEventListener('click', () => {
            this.showSessions();
        });
    }

    updateStats() {
        const totalSessions = this.sessions.length;
        const totalBarcodes = this.sessions.reduce((sum, session) => sum + session.total_barcodes, 0);
        const processedBarcodes = this.sessions.reduce((sum, session) => sum + session.processed_count, 0);
        const pendingBarcodes = totalBarcodes - processedBarcodes;

        const elements = {
            'total-sessions': totalSessions,
            'total-barcodes': totalBarcodes,
            'processed-barcodes': processedBarcodes,
            'pending-barcodes': pendingBarcodes
        };

        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });
    }

    calculateSessionDuration(firstScan, lastScan) {
        if (!firstScan || !lastScan) return 'N/A';

        const start = new Date(firstScan);
        const end = new Date(lastScan);
        const diffMs = end - start;

        if (diffMs < 60000) { // Less than 1 minute
            return `${Math.round(diffMs / 1000)}s`;
        } else if (diffMs < 3600000) { // Less than 1 hour
            return `${Math.round(diffMs / 60000)}m`;
        } else {
            const hours = Math.floor(diffMs / 3600000);
            const minutes = Math.round((diffMs % 3600000) / 60000);
            return `${hours}h ${minutes}m`;
        }
    }

    showSessions() {
        this.loadSessions();
    }

    showSessionDetails(sessionId) {
        this.loadSessionDetails(sessionId);
    }

    async toggleBarcodeStatus(barcodeId, processed) {
        await this.updateBarcodeStatus(barcodeId, processed);
    }

    refresh() {
        if (this.currentView === 'sessions') {
            this.loadSessions();
        } else if (this.currentView === 'session-details' && this.currentSession) {
            this.loadSessionDetails(this.currentSession.session.id);
        }
    }

    startAutoRefresh() {
        // Auto-refresh every second for real-time updates - only on main sessions page
        this.refreshInterval = setInterval(() => {
            try {
                if (this.currentView === 'sessions') {
                    this.loadSessions(true); // Silent refresh to avoid flickering
                }
                // Note: No auto-refresh for session details to avoid disrupting user interaction
            } catch (error) {
                console.warn('Auto-refresh error:', error);
                // Continue refreshing despite errors
            }
        }, 1000);
    }

    showLoading(message = 'Loading...') {
        const container = document.getElementById('main-content');
        container.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
                <p>${message}</p>
            </div>
        `;
    }

    showError(message) {
        const container = document.getElementById('main-content');
        container.innerHTML = `
            <div class="error">
                <strong>Error:</strong> ${message}
                <br><br>
                <button class="btn" onclick="app.refresh()">Try Again</button>
            </div>
        `;
    }

    showSuccess(message) {
        const container = document.getElementById('main-content');
        container.innerHTML = `
            <div class="success">
                <strong>✅ Success:</strong> ${message}
                <br><br>
                <button class="btn" onclick="app.loadSessions()">Continue</button>
            </div>
        `;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize the application when the page loads
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new WMSApp();
});