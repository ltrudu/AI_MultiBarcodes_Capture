// WMS Application JavaScript
class WMSApp {
    constructor() {
        this.apiBaseUrl = '/api';
        this.currentView = 'sessions';
        this.sessions = [];
        this.currentSession = null;
        this.lastUpdateTime = null;
        this.existingSessionIds = new Set();
        this.selectedSessions = new Set();
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

        // Settings button
        document.getElementById('btn-settings').addEventListener('click', () => {
            this.showSettingsModal();
        });

        // Settings modal buttons
        document.getElementById('settings-btn-endpoint').addEventListener('click', () => {
            this.closeSettingsModal();
            this.showEndpointModal();
        });

        document.getElementById('settings-btn-reset').addEventListener('click', () => {
            this.closeSettingsModal();
            this.resetAllData();
        });

        // Note: Event listeners are now attached in attachSessionEventListeners() method

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
                if (silent && this.sessions.length > 0) {
                    // Dynamic update mode - only add new sessions and update changed ones
                    this.dynamicUpdateSessions(data.sessions);
                } else {
                    // Full refresh mode - initial load or forced refresh
                    this.sessions = data.sessions;
                    this.renderSessions();
                }
                this.updateStats();
                this.lastUpdateTime = new Date();
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

    dynamicUpdateSessions(newSessions) {
        const existingSessionMap = new Map();
        this.sessions.forEach(session => {
            existingSessionMap.set(session.id, session);
        });

        const newSessionsToAdd = [];
        const sessionsToUpdate = [];

        newSessions.forEach(newSession => {
            const existingSession = existingSessionMap.get(newSession.id);

            if (!existingSession) {
                // This is a new session
                newSessionsToAdd.push(newSession);
                this.sessions.unshift(newSession); // Add to beginning of array
            } else {
                // Check if session data has changed
                if (this.hasSessionChanged(existingSession, newSession)) {
                    sessionsToUpdate.push({
                        old: existingSession,
                        new: newSession
                    });
                    // Update the session in our array
                    const index = this.sessions.findIndex(s => s.id === newSession.id);
                    if (index !== -1) {
                        this.sessions[index] = newSession;
                    }
                }
            }
        });

        // Apply dynamic updates to the DOM
        if (newSessionsToAdd.length > 0) {
            this.addNewSessionsToDOM(newSessionsToAdd);
        }

        if (sessionsToUpdate.length > 0) {
            this.updateExistingSessionsInDOM(sessionsToUpdate);
        }
    }

    hasSessionChanged(oldSession, newSession) {
        // Check if key properties have changed
        return oldSession.total_barcodes !== newSession.total_barcodes ||
               oldSession.processed_count !== newSession.processed_count ||
               oldSession.pending_count !== newSession.pending_count ||
               oldSession.last_scan !== newSession.last_scan;
    }

    addNewSessionsToDOM(newSessions) {
        const tableBody = document.querySelector('.table tbody');
        if (!tableBody) return;

        newSessions.forEach(session => {
            const sessionRow = this.createSessionRow(session);

            // Add new session animation class
            sessionRow.classList.add('new-session');

            // Insert at the beginning (most recent first)
            tableBody.insertBefore(sessionRow, tableBody.firstChild);

            // Add a subtle highlight effect after the slide-in animation
            setTimeout(() => {
                sessionRow.style.backgroundColor = '#e3f2fd';
                setTimeout(() => {
                    sessionRow.style.backgroundColor = '';
                    sessionRow.style.transition = 'background-color 1s ease-in-out';
                    // Remove the animation class after effects are complete
                    sessionRow.classList.remove('new-session');
                }, 2000);
            }, 500);
        });
    }

    updateExistingSessionsInDOM(sessionsToUpdate) {
        sessionsToUpdate.forEach(({ old: oldSession, new: newSession }) => {
            const existingRow = document.querySelector(`tr[data-session-id="${newSession.id}"]`);
            if (existingRow) {
                // Add update animation class
                existingRow.classList.add('updated-session');

                // Update the row content
                const newRow = this.createSessionRow(newSession);
                existingRow.innerHTML = newRow.innerHTML;

                // Ensure the data-session-id is preserved
                existingRow.setAttribute('data-session-id', newSession.id);

                // Remove animation class after animation completes
                setTimeout(() => {
                    existingRow.classList.remove('updated-session');
                }, 1000);
            }
        });
    }

    createSessionRow(session) {
        const sessionTime = new Date(session.session_timestamp).toLocaleString();
        const duration = this.calculateSessionDuration(session.first_scan, session.last_scan);
        const processedPercent = session.total_barcodes > 0
            ? Math.round((session.processed_count / session.total_barcodes) * 100)
            : 0;

        const row = document.createElement('tr');
        row.className = 'session-row';
        row.setAttribute('data-session-id', session.id);

        const isSelected = this.selectedSessions.has(session.id.toString());

        row.innerHTML = `
            <td class="checkbox-cell" onclick="event.stopPropagation();">
                <input type="checkbox" class="session-checkbox" value="${session.id}" ${isSelected ? 'checked' : ''}>
            </td>
            <td onclick="app.showSessionDetails(${session.id})">${sessionTime}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.device_info || 'Unknown Device'}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.device_ip || 'N/A'}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.total_barcodes}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.unique_symbologies}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.processed_count}/${session.total_barcodes}</td>
            <td onclick="app.showSessionDetails(${session.id})">
                <span class="status ${processedPercent === 100 ? 'processed' : 'pending'}">
                    ${processedPercent}% Complete
                </span>
            </td>
            <td onclick="app.showSessionDetails(${session.id})">${duration}</td>
        `;

        return row;
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
                <div class="selection-actions" id="selection-actions" style="display: none; margin-bottom: 1rem;">
                    <button id="bulk-delete-btn" class="btn btn-danger">
                        🗑️ Delete Selected
                    </button>
                    <button id="bulk-merge-btn" class="btn btn-primary">
                        🔗 Merge Selected
                    </button>
                    <span id="selection-count" class="selection-info">0 sessions selected</span>
                </div>
                <div class="table-container">
                    <table class="table">
                        <thead>
                            <tr>
                                <th class="checkbox-header">
                                    <input type="checkbox" id="select-all-sessions" title="Select/Unselect All">
                                </th>
                                <th>Session Time</th>
                                <th>Device</th>
                                <th>Device IP</th>
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

                const isSelected = this.selectedSessions.has(session.id.toString());
                html += `
                    <tr class="session-row" data-session-id="${session.id}">
                        <td class="checkbox-cell" onclick="event.stopPropagation();">
                            <input type="checkbox" class="session-checkbox" value="${session.id}" ${isSelected ? 'checked' : ''}>
                        </td>
                        <td onclick="app.showSessionDetails(${session.id})">${sessionTime}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.device_info || 'Unknown Device'}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.device_ip || 'N/A'}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.total_barcodes}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.unique_symbologies}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.processed_count}/${session.total_barcodes}</td>
                        <td onclick="app.showSessionDetails(${session.id})">
                            <span class="status ${processedPercent === 100 ? 'processed' : 'pending'}">
                                ${processedPercent}% Complete
                            </span>
                        </td>
                        <td onclick="app.showSessionDetails(${session.id})">${duration}</td>
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

        // Clear any previous selections when rendering sessions
        this.selectedSessions.clear();
        this.updateSelectionUI();

        // Re-attach event listeners after DOM update
        this.attachSessionEventListeners();
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
                        <strong>Device IP:</strong> ${session.device_ip || 'N/A'}<br>
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
                            <button class="btn btn-primary" style="margin-left: 0.5rem;"
                                    onclick="app.editBarcode(${barcode.id})">
                                ✏️ Edit
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

            <!-- Edit Barcode Modal -->
            <div id="edit-barcode-modal" class="modal" style="display: none;">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3><i class="icon">✏️</i> Edit Barcode</h3>
                        <button class="modal-close" onclick="app.closeEditBarcodeModal()" aria-label="Close">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div id="modal-status" style="display: none;"></div>
                        <form id="edit-barcode-form">
                            <div class="form-group">
                                <label for="edit-barcode-value">Barcode Value</label>
                                <input type="text" id="edit-barcode-value" class="form-control" required placeholder="Enter barcode value">
                            </div>
                            <div class="form-group">
                                <label for="edit-barcode-symbology">Symbology</label>
                                <select id="edit-barcode-symbology" class="form-control" required>
                                    <option value="-1">UNKNOWN</option>
                                    <option value="0">EAN 8</option>
                                    <option value="1">EAN 13</option>
                                    <option value="2">UPC A</option>
                                    <option value="3">UPC E</option>
                                    <option value="4">AZTEC</option>
                                    <option value="5">CODABAR</option>
                                    <option value="6">CODE128</option>
                                    <option value="7">CODE39</option>
                                    <option value="8">I2OF5</option>
                                    <option value="9">GS1 DATABAR</option>
                                    <option value="10">DATAMATRIX</option>
                                    <option value="11">GS1 DATABAR EXPANDED</option>
                                    <option value="12">MAILMARK</option>
                                    <option value="13">MAXICODE</option>
                                    <option value="14">PDF417</option>
                                    <option value="15">QRCODE</option>
                                    <option value="16">DOTCODE</option>
                                    <option value="17">GRID MATRIX</option>
                                    <option value="18">GS1 DATAMATRIX</option>
                                    <option value="19">GS1 QRCODE</option>
                                    <option value="20">MICROQR</option>
                                    <option value="21">MICROPDF</option>
                                    <option value="22">USPOSTNET</option>
                                    <option value="23">USPLANET</option>
                                    <option value="24">UK POSTAL</option>
                                    <option value="25">JAPANESE POSTAL</option>
                                    <option value="26">AUSTRALIAN POSTAL</option>
                                    <option value="27">CANADIAN POSTAL</option>
                                    <option value="28">DUTCH POSTAL</option>
                                    <option value="29">US4STATE</option>
                                    <option value="30">US4STATE FICS</option>
                                    <option value="31">MSI</option>
                                    <option value="32">CODE93</option>
                                    <option value="33">TRIOPTIC39</option>
                                    <option value="34">D2OF5</option>
                                    <option value="35">CHINESE 2OF5</option>
                                    <option value="36">KOREAN 3OF5</option>
                                    <option value="37">CODE11</option>
                                    <option value="38">TLC39</option>
                                    <option value="39">HANXIN</option>
                                    <option value="40">MATRIX 2OF5</option>
                                    <option value="41">UPCE1</option>
                                    <option value="42">GS1 DATABAR LIM</option>
                                    <option value="43">FINNISH POSTAL 4S</option>
                                    <option value="44">COMPOSITE AB</option>
                                    <option value="45">COMPOSITE C</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="edit-barcode-quantity">Quantity</label>
                                <input type="number" id="edit-barcode-quantity" class="form-control" min="1" value="1" required placeholder="Enter quantity">
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="app.closeEditBarcodeModal()">
                            <i class="icon">✕</i> Cancel
                        </button>
                        <button type="button" class="btn btn-primary" onclick="app.updateBarcode()">
                            <i class="icon">💾</i> Update Barcode
                        </button>
                    </div>
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

    async showEndpointModal() {
        const modal = document.getElementById('endpoint-modal');

        // Get current port
        const currentPort = window.location.port || '3500';

        // Show modal first with loading state
        modal.classList.remove('hide');
        modal.classList.add('show');

        // Set loading state
        document.getElementById('local-endpoint').value = 'Loading server information...';
        document.getElementById('internet-endpoint').value = 'Loading server information...';

        try {
            // Get server IP addresses from server-side endpoint
            const response = await fetch(`${this.apiBaseUrl}/server-info.php`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const serverInfo = await response.json();

            if (serverInfo.success) {
                // Use server's actual IP addresses
                const localEndpoint = `http://${serverInfo.local_ip}:${currentPort}/api/barcodes.php`;
                const internetEndpoint = `http://${serverInfo.external_ip}:${currentPort}/api/barcodes.php`;

                document.getElementById('local-endpoint').value = localEndpoint;
                document.getElementById('internet-endpoint').value = internetEndpoint;
            } else {
                throw new Error(serverInfo.error || 'Failed to get server information');
            }

        } catch (error) {
            console.error('Error getting server IP addresses:', error);
            // Fallback to hostname if server endpoint fails
            const fallbackHost = window.location.hostname;
            document.getElementById('local-endpoint').value = `http://${fallbackHost}:${currentPort}/api/barcodes.php`;
            document.getElementById('internet-endpoint').value = `http://YOUR-PUBLIC-IP:${currentPort}/api/barcodes.php (Error: ${error.message})`;
        }

        // Add click outside to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeEndpointModal();
            }
        });

        // Add escape key to close
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.closeEndpointModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    }

    closeEndpointModal() {
        const modal = document.getElementById('endpoint-modal');
        modal.classList.remove('show');
        modal.classList.add('hide');
    }

    async copyToClipboard(inputId) {
        const input = document.getElementById(inputId);
        const text = input.value;

        try {
            await navigator.clipboard.writeText(text);

            // Visual feedback
            const button = input.nextElementSibling;
            const originalText = button.textContent;
            button.textContent = '✅ Copied!';
            button.style.background = '#28a745';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.background = '';
            }, 2000);

        } catch (err) {
            // Fallback for older browsers
            input.select();
            input.setSelectionRange(0, 99999);
            document.execCommand('copy');

            // Visual feedback
            const button = input.nextElementSibling;
            const originalText = button.textContent;
            button.textContent = '✅ Copied!';
            button.style.background = '#28a745';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.background = '';
            }, 2000);
        }
    }

    showQRCode(inputId) {
        const input = document.getElementById(inputId);
        const url = input.value;

        if (!url || url === 'Loading...' || url === 'Loading server information...') {
            alert('Please wait for the endpoint to be loaded first.');
            return;
        }

        // Show QR modal
        const qrModal = document.getElementById('qr-modal');
        qrModal.classList.remove('hide');
        qrModal.classList.add('show');

        // Update URL text (display the original URL for user reference)
        document.getElementById('qr-url-text').textContent = url;

        // Generate QR code with AIMultiBarcodeEndpoint prefix
        const qrCodeData = `AIMultiBarcodeEndpoint:${url}`;
        this.generateQRCode(qrCodeData);

        // Add event listeners for closing
        qrModal.addEventListener('click', (e) => {
            if (e.target === qrModal) {
                this.closeQRModal();
            }
        });

        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.closeQRModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    }

    generateQRCode(text) {
        const qrCodeDisplay = document.getElementById('qr-code-display');
        qrCodeDisplay.innerHTML = ''; // Clear previous QR code

        try {
            // Create QR code using qrcode-generator library
            const qr = qrcode(0, 'M'); // Type 0 (auto), Error correction level M
            qr.addData(text);
            qr.make();

            // Create canvas element
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');

            // QR code settings
            const modules = qr.getModuleCount();
            const cellSize = 8; // Size of each QR code cell
            const margin = 4; // Margin around QR code

            canvas.width = canvas.height = (modules * cellSize) + (margin * 2 * cellSize);

            // Fill background
            ctx.fillStyle = '#FFFFFF';
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            // Draw QR code
            ctx.fillStyle = '#000000';
            for (let row = 0; row < modules; row++) {
                for (let col = 0; col < modules; col++) {
                    if (qr.isDark(row, col)) {
                        ctx.fillRect(
                            (col * cellSize) + (margin * cellSize),
                            (row * cellSize) + (margin * cellSize),
                            cellSize,
                            cellSize
                        );
                    }
                }
            }

            qrCodeDisplay.appendChild(canvas);

        } catch (error) {
            console.error('Error generating QR code:', error);
            qrCodeDisplay.innerHTML = '<p style="color: red;">Error generating QR code</p>';
        }
    }

    closeQRModal() {
        const qrModal = document.getElementById('qr-modal');
        qrModal.classList.remove('show');
        qrModal.classList.add('hide');

        // Clear QR code display
        document.getElementById('qr-code-display').innerHTML = '';
        document.getElementById('qr-url-text').textContent = '';
    }

    showSettingsModal() {
        const modal = document.getElementById('settings-modal');
        modal.classList.remove('hide');
        modal.classList.add('show');

        // Add click outside to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeSettingsModal();
            }
        });

        // Add escape key to close
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.closeSettingsModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    }

    closeSettingsModal() {
        const modal = document.getElementById('settings-modal');
        modal.classList.remove('show');
        modal.classList.add('hide');
    }

    toggleSelectAll() {
        const selectAllCheckbox = document.getElementById('select-all-sessions');
        const sessionCheckboxes = document.querySelectorAll('.session-checkbox');

        if (selectAllCheckbox.checked) {
            // Select all
            this.selectedSessions.clear();
            sessionCheckboxes.forEach(checkbox => {
                checkbox.checked = true;
                this.selectedSessions.add(checkbox.value);
                checkbox.closest('tr').classList.add('selected');
            });
        } else {
            // Unselect all
            this.selectedSessions.clear();
            sessionCheckboxes.forEach(checkbox => {
                checkbox.checked = false;
                checkbox.closest('tr').classList.remove('selected');
            });
        }

        this.updateSelectionUI();
    }

    toggleSessionSelection(sessionId) {
        console.log('toggleSessionSelection called with:', sessionId);
        const sessionIdStr = sessionId.toString();
        const checkbox = document.querySelector(`.session-checkbox[value="${sessionId}"]`);

        if (!checkbox) {
            console.error('Checkbox not found for session:', sessionId);
            return;
        }

        const row = checkbox.closest('tr');

        if (checkbox.checked) {
            this.selectedSessions.add(sessionIdStr);
            row.classList.add('selected');
            console.log('Added session to selection:', sessionIdStr);
        } else {
            this.selectedSessions.delete(sessionIdStr);
            row.classList.remove('selected');
            console.log('Removed session from selection:', sessionIdStr);
        }

        console.log('Selected sessions:', Array.from(this.selectedSessions));
        this.updateSelectionUI();
    }

    updateSelectionUI() {
        const selectedCount = this.selectedSessions.size;
        const totalSessions = this.sessions.length;
        const selectAllCheckbox = document.getElementById('select-all-sessions');
        const selectionActions = document.getElementById('selection-actions');
        const selectionCount = document.getElementById('selection-count');
        const deleteButton = document.getElementById('bulk-delete-btn');
        const mergeButton = document.getElementById('bulk-merge-btn');

        console.log('🔍 updateSelectionUI called:', {
            selectedCount,
            totalSessions,
            selectedSessions: Array.from(this.selectedSessions),
            selectionActions: !!selectionActions,
            selectionCount: !!selectionCount,
            deleteButton: !!deleteButton,
            mergeButton: !!mergeButton
        });

        // Update select all checkbox state
        if (selectAllCheckbox) {
            if (selectedCount === 0) {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = false;
            } else if (selectedCount === totalSessions) {
                selectAllCheckbox.checked = true;
                selectAllCheckbox.indeterminate = false;
            } else {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = true;
            }
        }

        // Show/hide selection actions and buttons based on selection count
        if (selectionActions) {
            console.log('🎯 Selection actions element found');
            if (selectedCount > 0) {
                console.log('📋 Showing selection actions for', selectedCount, 'selected sessions');
                selectionActions.style.display = 'block';
                if (selectionCount) {
                    selectionCount.textContent = `${selectedCount} session${selectedCount !== 1 ? 's' : ''} selected`;
                }

                // Show delete button when at least 1 session is selected
                if (deleteButton) {
                    const shouldShowDelete = selectedCount >= 1;
                    console.log('🗑️ Delete button decision:', {
                        selectedCount,
                        shouldShowDelete,
                        currentDisplay: deleteButton.style.display
                    });
                    deleteButton.style.display = shouldShowDelete ? 'inline-flex' : 'none';
                    console.log('🗑️ Delete button display set to:', deleteButton.style.display);
                } else {
                    console.log('❌ Delete button not found in DOM');
                }

                // Show merge button only when at least 2 sessions are selected
                if (mergeButton) {
                    const shouldShowMerge = selectedCount >= 2;
                    console.log('🔗 Merge button decision:', {
                        selectedCount,
                        shouldShowMerge,
                        currentDisplay: mergeButton.style.display
                    });
                    mergeButton.style.display = shouldShowMerge ? 'inline-flex' : 'none';
                    console.log('🔗 Merge button display set to:', mergeButton.style.display);
                } else {
                    console.log('❌ Merge button not found in DOM');
                }
            } else {
                console.log('📋 Hiding selection actions (no sessions selected)');
                selectionActions.style.display = 'none';
            }
        } else {
            console.log('❌ Selection actions element not found in DOM');
        }
    }

    attachSessionEventListeners() {
        console.log('=== attachSessionEventListeners called ===');

        // Use event delegation instead of individual listeners to avoid accumulation issues
        // Remove any existing delegated listener first
        const mainContent = document.getElementById('main-content');
        if (mainContent) {
            // Create a new event handler function and store it as a property
            this.sessionCheckboxHandler = (e) => {
                if (e.target.classList.contains('session-checkbox')) {
                    console.log('🔥 Session checkbox clicked via delegation!', {
                        value: e.target.value,
                        checked: e.target.checked,
                        sessionId: e.target.value
                    });
                    this.toggleSessionSelection(e.target.value);
                }
            };

            // Remove existing delegated listener if it exists
            if (this.previousCheckboxHandler) {
                mainContent.removeEventListener('change', this.previousCheckboxHandler);
                console.log('🧹 Removed previous delegated listener');
            }

            // Add new delegated listener
            mainContent.addEventListener('change', this.sessionCheckboxHandler);
            this.previousCheckboxHandler = this.sessionCheckboxHandler;
            console.log('✅ Added delegated event listener to main-content');
        }

        // Attach event listeners to select all checkbox (direct listener since it's always present)
        const selectAllCheckbox = document.getElementById('select-all-sessions');
        if (selectAllCheckbox) {
            // Remove existing listener first
            if (this.selectAllHandler) {
                selectAllCheckbox.removeEventListener('change', this.selectAllHandler);
            }

            this.selectAllHandler = () => {
                console.log('Select all checkbox clicked');
                this.toggleSelectAll();
            };

            selectAllCheckbox.addEventListener('change', this.selectAllHandler);
            console.log('✅ Select all checkbox listener attached');
        } else {
            console.log('❌ Select all checkbox not found');
        }

        // Log how many checkboxes are currently in the DOM
        const sessionCheckboxes = document.querySelectorAll('.session-checkbox');
        console.log(`Found ${sessionCheckboxes.length} session checkboxes for event delegation`);

        // Attach event listeners to bulk action buttons
        const deleteButton = document.getElementById('bulk-delete-btn');
        const mergeButton = document.getElementById('bulk-merge-btn');

        if (deleteButton) {
            deleteButton.addEventListener('click', () => {
                console.log('Delete button clicked');
                this.bulkDeleteSessions();
            });
        }

        if (mergeButton) {
            mergeButton.addEventListener('click', () => {
                console.log('Merge button clicked');
                this.bulkMergeSessions();
            });
        }

        console.log('Event listeners attached:', {
            selectAll: !!selectAllCheckbox,
            sessionCheckboxes: sessionCheckboxes.length,
            deleteButton: !!deleteButton,
            mergeButton: !!mergeButton
        });
        console.log('=== attachSessionEventListeners completed ===');
    }

    async bulkDeleteSessions() {
        const selectedCount = this.selectedSessions.size;
        if (selectedCount === 0) return;

        // Show confirmation dialog
        const confirmMessage = `⚠️ WARNING: You are about to delete ${selectedCount} session${selectedCount !== 1 ? 's' : ''} and all their barcode data.\n\nThis action cannot be undone. Are you sure you want to continue?`;

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            this.showLoading(`Deleting ${selectedCount} session${selectedCount !== 1 ? 's' : ''}...`);

            const sessionIds = Array.from(this.selectedSessions);
            const response = await fetch(`${this.apiBaseUrl}/barcodes.php`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    action: 'bulk_delete',
                    session_ids: sessionIds
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            if (data.success) {
                this.showSuccess(`Successfully deleted ${selectedCount} session${selectedCount !== 1 ? 's' : ''}!`);

                // Clear selection and reload
                this.selectedSessions.clear();
                setTimeout(() => {
                    this.loadSessions();
                }, 2000);
            } else {
                throw new Error(data.error || 'Failed to delete sessions');
            }
        } catch (error) {
            console.error('Error deleting sessions:', error);
            this.showError('Failed to delete sessions: ' + error.message);
        }
    }

    async bulkMergeSessions() {
        const selectedCount = this.selectedSessions.size;
        if (selectedCount < 2) {
            alert('At least 2 sessions must be selected to merge. Please select more sessions.');
            return;
        }

        // Show confirmation dialog
        const confirmMessage = `🔗 You are about to merge ${selectedCount} sessions into a single new session.\n\nIdentical barcodes will be consolidated with combined quantities.\nDevice name will be set to "Merged by user" and timestamps updated to current time.\n\nContinue with merge?`;

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            this.showLoading(`Merging ${selectedCount} sessions...`);

            const sessionIds = Array.from(this.selectedSessions);
            const response = await fetch(`${this.apiBaseUrl}/barcodes.php`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    action: 'bulk_merge',
                    session_ids: sessionIds
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            if (data.success) {
                this.showSuccess(`Successfully merged ${selectedCount} sessions into session #${data.new_session_id}!`);

                // Clear selection and reload
                this.selectedSessions.clear();
                setTimeout(() => {
                    this.loadSessions();
                }, 2000);
            } else {
                throw new Error(data.error || 'Failed to merge sessions');
            }
        } catch (error) {
            console.error('Error merging sessions:', error);
            this.showError('Failed to merge sessions: ' + error.message);
        }
    }

    // Barcode Edit Functionality
    editBarcode(barcodeId) {
        console.log('Edit barcode requested for ID:', barcodeId);

        // Find the barcode in current session data
        const barcode = this.currentSession.barcodes.find(b => b.id === barcodeId);
        if (!barcode) {
            this.showError('Barcode not found');
            return;
        }

        // Store the barcode being edited
        this.editingBarcode = barcode;

        // Populate the modal with current values
        document.getElementById('edit-barcode-value').value = barcode.value;
        document.getElementById('edit-barcode-symbology').value = barcode.symbology;
        document.getElementById('edit-barcode-quantity').value = barcode.quantity;

        // Show the modal with proper centering
        const modal = document.getElementById('edit-barcode-modal');
        modal.style.display = 'flex';

        // Add click outside to close functionality
        modal.onclick = (e) => {
            if (e.target === modal) {
                this.closeEditBarcodeModal();
            }
        };

        console.log('Edit modal opened for barcode:', {
            id: barcode.id,
            value: barcode.value,
            symbology: barcode.symbology,
            quantity: barcode.quantity
        });
    }

    closeEditBarcodeModal() {
        const modal = document.getElementById('edit-barcode-modal');
        modal.style.display = 'none';
        this.editingBarcode = null;
        console.log('Edit modal closed');
    }

    showModalLoading(message) {
        const statusDiv = document.getElementById('modal-status');
        if (statusDiv) {
            statusDiv.innerHTML = `<div class="modal-loading"><span class="spinner"></span> ${message}</div>`;
            statusDiv.style.display = 'block';
        }
    }

    showModalSuccess(message) {
        const statusDiv = document.getElementById('modal-status');
        if (statusDiv) {
            statusDiv.innerHTML = `<div class="modal-success">✅ ${message}</div>`;
            statusDiv.style.display = 'block';
        }
    }

    showModalError(message) {
        const statusDiv = document.getElementById('modal-status');
        if (statusDiv) {
            statusDiv.innerHTML = `<div class="modal-error">❌ ${message}</div>`;
            statusDiv.style.display = 'block';
        }
    }

    async updateBarcode() {
        if (!this.editingBarcode) {
            this.showModalError('No barcode selected for editing');
            return;
        }

        // Get form values
        const newValue = document.getElementById('edit-barcode-value').value.trim();
        const newSymbology = parseInt(document.getElementById('edit-barcode-symbology').value);
        const newQuantity = parseInt(document.getElementById('edit-barcode-quantity').value);

        // Validate input
        if (!newValue) {
            this.showModalError('Barcode value cannot be empty');
            return;
        }

        if (newQuantity < 1) {
            this.showModalError('Quantity must be at least 1');
            return;
        }

        try {
            console.log('Updating barcode:', {
                id: this.editingBarcode.id,
                oldValue: this.editingBarcode.value,
                newValue: newValue,
                oldSymbology: this.editingBarcode.symbology,
                newSymbology: newSymbology,
                oldQuantity: this.editingBarcode.quantity,
                newQuantity: newQuantity
            });

            this.showModalLoading('Updating barcode...');

            const response = await fetch(`${this.apiBaseUrl}/barcodes.php`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    action: 'update_barcode',
                    barcode_id: this.editingBarcode.id,
                    value: newValue,
                    symbology: newSymbology,
                    quantity: newQuantity
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            if (data.success) {
                this.showModalSuccess('Barcode updated successfully!');
                setTimeout(() => {
                    this.closeEditBarcodeModal();
                    // Reload session details to reflect changes
                    this.loadSessionDetails(this.currentSession.session.id);
                }, 1500);
            } else {
                throw new Error(data.error || 'Failed to update barcode');
            }
        } catch (error) {
            console.error('Error updating barcode:', error);
            this.showModalError('Failed to update barcode: ' + error.message);
        }
    }
}

// Initialize the application when the page loads
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new WMSApp();
});