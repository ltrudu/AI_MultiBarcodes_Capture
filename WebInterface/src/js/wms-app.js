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
        this.currentLanguage = 'en';
        this.translations = {};
        this.availableLanguages = {}; // Will be populated dynamically
        this.init();
    }

    async init() {
        await this.discoverAvailableLanguages();
        await this.initializeLanguage();
        this.setupEventListeners();
        this.setupLanguageDropdown();
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

        // Certificate download buttons
        document.getElementById('download-windows-cert').addEventListener('click', () => {
            this.downloadCertificate('wms_ca.crt', 'wms-ca-certificate.crt');
        });

        document.getElementById('download-android-cert').addEventListener('click', () => {
            this.downloadCertificate('android_ca_system.pem', 'android-ca-certificate.pem');
        });

        document.getElementById('show-cert-instructions').addEventListener('click', () => {
            this.showCertificateInstructions();
        });

        document.getElementById('settings-btn-reset').addEventListener('click', () => {
            this.closeSettingsModal();
            this.resetAllData();
        });

        // Language selection
        document.getElementById('language-select').addEventListener('change', async (e) => {
            await this.changeLanguage(e.target.value);
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
                this.showLoading(this.t('loading_sessions', 'Loading capture sessions...'));
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
                throw new Error(data.error || this.t('failed_to_load_sessions', 'Failed to load sessions'));
            }
        } catch (error) {
            console.error('Error loading sessions:', error);
            if (!silent) {
                this.showError(this.t('failed_to_load_sessions', 'Failed to load capture sessions') + ': ' + error.message);
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
            <td onclick="app.showSessionDetails(${session.id})">${session.device_info || this.t('unknown_device', 'Unknown Device')}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.device_ip || this.t('not_available', 'N/A')}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.total_barcodes}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.unique_symbologies}</td>
            <td onclick="app.showSessionDetails(${session.id})">${session.processed_count}/${session.total_barcodes}</td>
            <td onclick="app.showSessionDetails(${session.id})">
                <span class="status ${processedPercent === 100 ? 'processed' : 'pending'}">
                    ${processedPercent}% ${this.t('complete', 'Complete')}
                </span>
            </td>
            <td onclick="app.showSessionDetails(${session.id})">${duration}</td>
        `;

        return row;
    }

    async loadSessionDetails(sessionId) {
        try {
            this.showLoading(this.t('loading_session_details', 'Loading session details...'));

            const response = await fetch(`${this.apiBaseUrl}/barcodes.php?session_id=${sessionId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.success) {
                this.currentSession = data;
                this.renderSessionDetails();
            } else {
                throw new Error(data.error || this.t('failed_to_load_session_details', 'Failed to load session details'));
            }
        } catch (error) {
            console.error('Error loading session details:', error);
            this.showError(this.t('failed_to_load_session_details', 'Failed to load session details') + ': ' + error.message);
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
                // Update UI dynamically without page reload
                this.updateBarcodeUIStatus(barcodeId, processed, notes);

                // Update session statistics
                if (this.currentSession) {
                    this.updateSessionStatistics(processed);
                }
            } else {
                throw new Error(data.error || this.t('failed_to_update_barcode_status', 'Failed to update barcode status'));
            }
        } catch (error) {
            console.error('Error updating barcode status:', error);
            this.showError(this.t('failed_to_update_barcode_status', 'Failed to update barcode status') + ': ' + error.message);
        }
    }

    updateBarcodeUIStatus(barcodeId, processed, notes = '') {
        // Find the barcode card in the DOM
        const barcodeCard = document.querySelector(`[data-barcode-id="${barcodeId}"]`);
        if (!barcodeCard) return;

        // Update the status span
        const statusSpan = barcodeCard.querySelector('.status');
        if (statusSpan) {
            statusSpan.className = `status ${processed ? 'processed' : 'pending'}`;
            statusSpan.textContent = processed ? this.t('processed', 'Processed') : this.t('pending', 'Pending');
        }

        // Update the button
        const button = barcodeCard.querySelector('button[onclick*="toggleBarcodeStatus"]');
        if (button) {
            button.className = `btn ${processed ? 'btn-secondary' : 'btn-success'}`;
            button.textContent = processed ? this.t('mark_as_pending', 'Mark as Pending') : this.t('mark_as_processed', 'Mark as Processed');
            button.setAttribute('onclick', `app.toggleBarcodeStatus(${barcodeId}, ${!processed})`);
        }

        // Update notes if provided
        if (notes) {
            let notesDiv = barcodeCard.querySelector('.barcode-notes');
            if (notesDiv) {
                notesDiv.innerHTML = `${this.t('notes', 'Notes')}: ${this.escapeHtml(notes)}`;
            } else {
                // Add notes div if it doesn't exist
                const buttonContainer = button ? button.parentElement : barcodeCard;
                notesDiv = document.createElement('div');
                notesDiv.className = 'barcode-notes';
                notesDiv.style.marginTop = '0.5rem';
                notesDiv.style.fontStyle = 'italic';
                notesDiv.innerHTML = `${this.t('notes', 'Notes')}: ${this.escapeHtml(notes)}`;
                buttonContainer.appendChild(notesDiv);
            }
        }

        // Update the barcode data in current session
        if (this.currentSession && this.currentSession.barcodes) {
            const barcode = this.currentSession.barcodes.find(b => b.id == barcodeId);
            if (barcode) {
                barcode.processed = processed;
                if (notes) barcode.notes = notes;
            }
        }
    }

    updateSessionStatistics(processed) {
        if (!this.currentSession || !this.currentSession.session) return;

        // Update processed count in session data
        if (processed) {
            this.currentSession.session.processed_count = Math.min(
                this.currentSession.session.processed_count + 1,
                this.currentSession.session.total_barcodes
            );
        } else {
            this.currentSession.session.processed_count = Math.max(
                this.currentSession.session.processed_count - 1,
                0
            );
        }

        // Update the processed count display in session header stats
        const processedStatCard = document.querySelector('#session-details .stat-card:nth-child(2) .stat-value');
        if (processedStatCard) {
            processedStatCard.textContent = this.currentSession.session.processed_count;
        }
    }

    async resetAllData() {
        // Show confirmation dialog
        if (!confirm(this.t('reset_all_warning', '⚠️ WARNING: This will permanently delete ALL barcode capture sessions and data.\n\nThis action cannot be undone. Are you sure you want to continue?'))) {
            return;
        }

        // Double confirmation for safety
        if (!confirm(this.t('reset_final_confirmation', '🔴 FINAL CONFIRMATION: This will delete ALL data permanently.\n\nClick OK to proceed with complete data reset.'))) {
            return;
        }

        try {
            this.showLoading(this.t('resetting_data', 'Resetting all data...'));

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
                throw new Error(data.error || this.t('failed_to_reset_data', 'Failed to reset data'));
            }
        } catch (error) {
            console.error('Error resetting data:', error);
            this.showError(this.t('failed_to_reset_data', 'Failed to reset data') + ': ' + error.message);
        }
    }

    renderSessions() {
        const container = document.getElementById('main-content');

        let html = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value" id="total-sessions">0</div>
                    <div class="stat-label">${this.t('total_sessions', 'Total Sessions')}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="total-barcodes">0</div>
                    <div class="stat-label">${this.t('total_barcodes', 'Total Barcodes')}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="processed-barcodes">0</div>
                    <div class="stat-label">${this.t('processed', 'Processed')}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="pending-barcodes">0</div>
                    <div class="stat-label">${this.t('pending', 'Pending')}</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>${this.t('recent_capture_sessions', 'Recent Capture Sessions')}</h2>
                </div>
                <div class="card-body">
        `;

        if (this.sessions.length === 0) {
            html += `
                <div class="text-center" style="padding: 2rem; color: #6c757d;">
                    <p>${this.t('no_sessions_message', 'No capture sessions found. Sessions will appear here when your Android app uploads barcode data.')}</p>
                </div>
            `;
        } else {
            html += `
                <div class="selection-actions" id="selection-actions" style="display: none; margin-bottom: 1rem;">
                    <button id="bulk-export-btn" class="btn btn-success">
                        📊 ${this.t('export', 'Export')}
                    </button>
                    <button id="bulk-delete-btn" class="btn btn-danger">
                        🗑️ ${this.t('delete_selected', 'Delete Selected')}
                    </button>
                    <button id="bulk-merge-btn" class="btn btn-primary">
                        🔗 ${this.t('merge_sessions', 'Merge Selected')}
                    </button>
                    <span id="selection-count" class="selection-info">0 ${this.t('sessions_selected', 'sessions selected')}</span>
                </div>
                <div class="table-container">
                    <table class="table">
                        <thead>
                            <tr>
                                <th class="checkbox-header">
                                    <input type="checkbox" id="select-all-sessions" title="Select/Unselect All">
                                </th>
                                <th>${this.t('session_timestamp', 'Session Time')}</th>
                                <th>${this.t('device', 'Device')}</th>
                                <th>${this.t('device_ip', 'Device IP')}</th>
                                <th>${this.t('total_barcodes', 'Total Barcodes')}</th>
                                <th>${this.t('symbology', 'Unique Types')}</th>
                                <th>${this.t('processed', 'Processed')}</th>
                                <th>${this.t('status', 'Status')}</th>
                                <th>${this.t('duration', 'Duration')}</th>
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
                        <td onclick="app.showSessionDetails(${session.id})">${session.device_info || this.t('unknown_device', 'Unknown Device')}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.device_ip || this.t('not_available', 'N/A')}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.total_barcodes}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.unique_symbologies}</td>
                        <td onclick="app.showSessionDetails(${session.id})">${session.processed_count}/${session.total_barcodes}</td>
                        <td onclick="app.showSessionDetails(${session.id})">
                            <span class="status ${processedPercent === 100 ? 'processed' : 'pending'}">
                                ${processedPercent}% ${this.t('complete', 'Complete')}
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
                    <h2>${this.t('session_details_title', 'Session Details')} - ${sessionTime}</h2>
                    <button id="btn-back" class="btn btn-secondary" style="float: right;">← ${this.t('back_to_sessions', 'Back to Sessions')}</button>
                </div>
                <div class="card-body">
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-value">${session.total_barcodes}</div>
                            <div class="stat-label">${this.t('total_barcodes', 'Total Barcodes')}</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${session.unique_symbologies}</div>
                            <div class="stat-label">${this.t('symbology', 'Unique Types')}</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${session.processed_count}</div>
                            <div class="stat-label">${this.t('processed', 'Processed')}</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">${duration}</div>
                            <div class="stat-label">${this.t('duration', 'Duration')}</div>
                        </div>
                    </div>

                    <div style="margin-bottom: 2rem;">
                        <strong>${this.t('device', 'Device')}:</strong> ${session.device_info || this.t('unknown_device', 'Unknown Device')}<br>
                        <strong>${this.t('device_ip', 'Device IP')}:</strong> ${session.device_ip || this.t('not_available', 'N/A')}<br>
                        <strong>${this.t('session_id', 'Session ID')}:</strong> ${session.id}<br>
                        <strong>${this.t('first_scan', 'First Scan')}:</strong> ${session.first_scan ? new Date(session.first_scan).toLocaleString() : this.t('not_available', 'N/A')}<br>
                        <strong>${this.t('last_scan', 'Last Scan')}:</strong> ${session.last_scan ? new Date(session.last_scan).toLocaleString() : this.t('not_available', 'N/A')}
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>${this.t('captured_barcodes', 'Captured Barcodes')} (${barcodes.length})</h2>
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
                    <div class="barcode-item" data-barcode-id="${barcode.id}">
                        <div class="barcode-value">${this.escapeHtml(barcode.value)}</div>
                        <div class="barcode-meta">
                            <div class="meta-item">
                                <span class="meta-label">${this.t('symbology', 'Symbology')}</span>
                                <span>${barcode.symbology_name} (${barcode.symbology})</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${this.t('quantity', 'Quantity')}</span>
                                <span>${barcode.quantity}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${this.t('scan_time', 'Scan Time')}</span>
                                <span>${scanTime}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${this.t('status', 'Status')}</span>
                                <span class="status ${barcode.processed ? 'processed' : 'pending'}">
                                    ${barcode.processed ? this.t('processed', 'Processed') : this.t('pending', 'Pending')}
                                </span>
                            </div>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button class="btn ${barcode.processed ? 'btn-secondary' : 'btn-success'}"
                                    onclick="app.toggleBarcodeStatus(${barcode.id}, ${!barcode.processed})">
                                ${barcode.processed ? this.t('mark_as_pending', 'Mark as Pending') : this.t('mark_as_processed', 'Mark as Processed')}
                            </button>
                            <button class="btn btn-primary" style="margin-left: 0.5rem;"
                                    onclick="app.editBarcode(${barcode.id})">
                                ✏️ ${this.t('edit', 'Edit')}
                            </button>
                            ${barcode.notes ? `<div class="barcode-notes" style="margin-top: 0.5rem; font-style: italic;">${this.t('notes', 'Notes')}: ${this.escapeHtml(barcode.notes)}</div>` : ''}
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
                        <h3><i class="icon">✏️</i> ${this.t('edit_barcode', 'Edit Barcode')}</h3>
                        <button class="modal-close" onclick="app.closeEditBarcodeModal()" aria-label="${this.t('close', 'Close')}">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div id="modal-status" style="display: none;"></div>
                        <form id="edit-barcode-form">
                            <div class="form-group">
                                <label for="edit-barcode-value">${this.t('value', 'Barcode Value')}</label>
                                <input type="text" id="edit-barcode-value" class="form-control" required placeholder="${this.t('value', 'Enter barcode value')}">
                            </div>
                            <div class="form-group">
                                <label for="edit-barcode-symbology">${this.t('symbology', 'Symbology')}</label>
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
                                <label for="edit-barcode-quantity">${this.t('quantity', 'Quantity')}</label>
                                <input type="number" id="edit-barcode-quantity" class="form-control" min="1" value="1" required placeholder="${this.t('quantity', 'Enter quantity')}">
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="app.closeEditBarcodeModal()">
                            <i class="icon">✕</i> ${this.t('cancel', 'Cancel')}
                        </button>
                        <button type="button" class="btn btn-primary" onclick="app.updateBarcode()">
                            <i class="icon">💾</i> ${this.t('save', 'Update Barcode')}
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
        if (!firstScan || !lastScan) return this.t('not_available', 'N/A');

        const start = new Date(firstScan);
        const end = new Date(lastScan);
        const diffMs = end - start;

        if (diffMs < 60000) { // Less than 1 minute
            return `${Math.round(diffMs / 1000)}s`;
        } else if (diffMs < 3600000) { // Less than 1 hour
            return `${Math.round(diffMs / 60000)}${this.t('minutes', 'm')}`;
        } else {
            const hours = Math.floor(diffMs / 3600000);
            const minutes = Math.round((diffMs % 3600000) / 60000);
            return `${hours}${this.t('hours', 'h')} ${minutes}${this.t('minutes', 'm')}`;
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

        // Detect current protocol and determine appropriate port
        const isHttps = window.location.protocol === 'https:';
        const currentPort = window.location.port || (isHttps ? '3543' : '3500');
        const protocol = isHttps ? 'https' : 'http';
        const apiPort = isHttps ? '3543' : '3500';

        // Show modal first with loading state
        modal.classList.remove('hide');
        modal.classList.add('show');

        // Set loading state
        document.getElementById('local-endpoint').value = this.t('loading_server_info', 'Loading server information...');
        document.getElementById('internet-endpoint').value = this.t('loading_server_info', 'Loading server information...');

        try {
            // Get server IP addresses from server-side endpoint
            const response = await fetch(`${this.apiBaseUrl}/server-info.php`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const serverInfo = await response.json();

            if (serverInfo.success) {
                // Use server's actual IP addresses with protocol-aware endpoints
                const localEndpoint = `${protocol}://${serverInfo.local_ip}:${apiPort}/api/barcodes.php`;
                const internetEndpoint = `${protocol}://${serverInfo.external_ip}:${apiPort}/api/barcodes.php`;

                document.getElementById('local-endpoint').value = localEndpoint;
                document.getElementById('internet-endpoint').value = internetEndpoint;
            } else {
                throw new Error(serverInfo.error || this.t('failed_to_get_server_info', 'Failed to get server information'));
            }

        } catch (error) {
            console.error('Error getting server IP addresses:', error);
            // Fallback to hostname if server endpoint fails with protocol-aware URLs
            const fallbackHost = window.location.hostname;
            document.getElementById('local-endpoint').value = `${protocol}://${fallbackHost}:${apiPort}/api/barcodes.php`;
            document.getElementById('internet-endpoint').value = `${protocol}://YOUR-PUBLIC-IP:${apiPort}/api/barcodes.php (Error: ${error.message})`;
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

        if (!url || url === this.t('loading', 'Loading...') || url === this.t('loading_server_info', 'Loading server information...')) {
            alert(this.t('wait_endpoint_load', 'Please wait for the endpoint to be loaded first.'));
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

    async downloadCertificate(filename, downloadName) {
        try {
            const response = await fetch(`certificates/${filename}`);
            if (!response.ok) {
                throw new Error(`Certificate not found: ${filename}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = downloadName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);

            this.showNotification(this.t('certificate_downloaded', 'Certificate downloaded successfully'), 'success');
        } catch (error) {
            console.error('Error downloading certificate:', error);
            this.showNotification(this.t('certificate_download_error', 'Error downloading certificate. Please ensure certificates are generated.'), 'error');
        }
    }

    showCertificateInstructions() {
        this.closeSettingsModal();
        this.showCertificateInstructionsModal();
    }

    showCertificateInstructionsModal() {
        const modal = document.getElementById('cert-instructions-modal');
        modal.classList.remove('hide');
        modal.classList.add('show');

        // Add click outside to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeCertificateInstructionsModal();
            }
        });

        // Add escape key handler
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.closeCertificateInstructionsModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    }

    closeCertificateInstructionsModal() {
        const modal = document.getElementById('cert-instructions-modal');
        modal.classList.remove('show');
        modal.classList.add('hide');
    }

    showExportModal() {
        const modal = document.getElementById('export-modal');

        // Update translations
        const elements = {
            'export-data-title': this.t('export_data', 'Export Data'),
            'export-description': this.t('choose_export_format', 'Choose the export format for the selected sessions:'),
            'txt-file-title': this.t('txt_file_title', 'Text File (.txt)'),
            'txt-file-desc': this.t('txt_file_desc', 'Simple text format with basic information'),
            'csv-file-title': this.t('csv_file_title', 'CSV File (.csv)'),
            'csv-file-desc': this.t('csv_file_desc', 'Comma-separated values for spreadsheet applications'),
            'excel-file-title': this.t('excel_file_title', 'Excel File (.xlsx)'),
            'excel-file-desc': this.t('excel_file_desc', 'Microsoft Excel format with advanced formatting'),
            'export-will-include': this.t('export_will_include', 'Export will include:'),
            'export-session-info': this.t('export_session_info', 'Session information (device, timestamps, etc.)'),
            'export-barcode-info': this.t('export_barcode_info', 'All barcodes with values, symbologies, and quantities'),
            'export-status-info': this.t('export_status_info', 'Processing status and notes')
        };

        Object.entries(elements).forEach(([id, text]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = text;
            }
        });

        modal.classList.remove('hide');
        modal.classList.add('show');

        // Add click outside to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeExportModal();
            }
        });

        // Add escape key to close
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.closeExportModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    }

    closeExportModal() {
        const modal = document.getElementById('export-modal');
        modal.classList.remove('show');
        modal.classList.add('hide');
    }

    async exportData(format) {
        if (this.selectedSessions.size === 0) {
            alert(this.t('no_sessions_selected', 'No sessions selected for export'));
            return;
        }

        try {
            this.closeExportModal();

            // Create the request body with selected session IDs
            const requestBody = {
                session_ids: Array.from(this.selectedSessions),
                format: format
            };

            console.log('Exporting sessions:', requestBody);

            const response = await fetch(`${this.apiBaseUrl}/export.php`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                throw new Error(`Export failed: ${response.status} ${response.statusText}`);
            }

            // Check if this is an error response (JSON) or actual export (file)
            const contentType = response.headers.get('Content-Type');
            if (contentType && contentType.includes('application/json')) {
                // This is an error response
                const errorData = await response.json();
                console.error('Export Error Response:', errorData);
                throw new Error(errorData.error || 'Export failed');
            }

            // Original file download logic for actual export
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = `barcode_export.${format}`;
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
                if (filenameMatch) {
                    filename = filenameMatch[1];
                }
            }

            // Create blob and download
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);

            console.log(`Export completed: ${filename}`);

        } catch (error) {
            console.error('Export failed:', error);
            alert(this.t('export_failed', 'Export failed') + ': ' + error.message);
        }
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
                    const key = selectedCount === 1 ? 'session_selected' : 'sessions_selected';
                    const fallback = selectedCount === 1 ? 'session selected' : 'sessions selected';
                    selectionCount.textContent = `${selectedCount} ${this.t(key, fallback)}`;
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
        const exportButton = document.getElementById('bulk-export-btn');

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

        if (exportButton) {
            exportButton.addEventListener('click', () => {
                console.log('Export button clicked');
                this.showExportModal();
            });
        }

        console.log('Event listeners attached:', {
            selectAll: !!selectAllCheckbox,
            sessionCheckboxes: sessionCheckboxes.length,
            deleteButton: !!deleteButton,
            mergeButton: !!mergeButton,
            exportButton: !!exportButton
        });
        console.log('=== attachSessionEventListeners completed ===');
    }

    async bulkDeleteSessions() {
        const selectedCount = this.selectedSessions.size;
        if (selectedCount === 0) return;

        // Show confirmation dialog
        const plural = selectedCount !== 1 ? 's' : '';
        const confirmMessage = this.t('delete_sessions_warning', '⚠️ WARNING: You are about to delete {count} session{plural} and all their barcode data.\n\nThis action cannot be undone. Are you sure you want to continue?')
            .replace('{count}', selectedCount)
            .replace('{plural}', plural);

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            this.showLoading(this.t('deleting_sessions', 'Deleting sessions...'));

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
                throw new Error(data.error || this.t('failed_to_delete_sessions', 'Failed to delete sessions'));
            }
        } catch (error) {
            console.error('Error deleting sessions:', error);
            this.showError(this.t('failed_to_delete_sessions', 'Failed to delete sessions') + ': ' + error.message);
        }
    }

    async bulkMergeSessions() {
        const selectedCount = this.selectedSessions.size;
        if (selectedCount < 2) {
            alert(this.t('merge_sessions_min_error', 'At least 2 sessions must be selected to merge. Please select more sessions.'));
            return;
        }

        // Show confirmation dialog
        const confirmMessage = this.t('merge_sessions_confirmation', '🔗 You are about to merge {count} sessions into a single new session.\n\nIdentical barcodes will be consolidated with combined quantities.\nDevice name will be set to "Merged by user" and timestamps updated to current time.\n\nContinue with merge?')
            .replace('{count}', selectedCount);

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            this.showLoading(this.t('merging_sessions', 'Merging sessions...'));

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
                throw new Error(data.error || this.t('failed_to_merge_sessions', 'Failed to merge sessions'));
            }
        } catch (error) {
            console.error('Error merging sessions:', error);
            this.showError(this.t('failed_to_merge_sessions', 'Failed to merge sessions') + ': ' + error.message);
        }
    }

    // Barcode Edit Functionality
    editBarcode(barcodeId) {
        console.log('Edit barcode requested for ID:', barcodeId);

        // Find the barcode in current session data
        const barcode = this.currentSession.barcodes.find(b => b.id === barcodeId);
        if (!barcode) {
            this.showError(this.t('barcode_not_found', 'Barcode not found'));
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
            this.showModalError(this.t('no_barcode_selected', 'No barcode selected for editing'));
            return;
        }

        // Get form values
        const newValue = document.getElementById('edit-barcode-value').value.trim();
        const newSymbology = parseInt(document.getElementById('edit-barcode-symbology').value);
        const newQuantity = parseInt(document.getElementById('edit-barcode-quantity').value);

        // Validate input
        if (!newValue) {
            this.showModalError(this.t('barcode_value_empty', 'Barcode value cannot be empty'));
            return;
        }

        if (newQuantity < 1) {
            this.showModalError(this.t('quantity_min_error', 'Quantity must be at least 1'));
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

            this.showModalLoading(this.t('updating_barcode', 'Updating barcode...'));

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
                throw new Error(data.error || this.t('failed_to_update_barcode', 'Failed to update barcode'));
            }
        } catch (error) {
            console.error('Error updating barcode:', error);
            this.showModalError(this.t('failed_to_update_barcode', 'Failed to update barcode') + ': ' + error.message);
        }
    }

    // Language Support Methods
    async discoverAvailableLanguages() {
        try {
            // Scan for available translation files
            const languageMap = this.getLanguageDisplayNames();
            const discoveredLanguages = { 'en': 'English' }; // Always include English

            // Test each possible language file
            const testPromises = Object.keys(languageMap).map(async (langCode) => {
                if (langCode === 'en') return; // Skip English, already included

                try {
                    // Try to fetch the language file
                    const response = await fetch(`src/lang/${langCode}.json`);
                    if (response.ok) {
                        discoveredLanguages[langCode] = languageMap[langCode];
                    }
                } catch (error) {
                    // File doesn't exist, skip
                }
            });

            await Promise.all(testPromises);

            this.availableLanguages = discoveredLanguages;
            console.log(`Discovered ${Object.keys(discoveredLanguages).length} available languages:`, Object.keys(discoveredLanguages).sort());

        } catch (error) {
            console.error('Error discovering languages:', error);
            // Fallback to minimal set
            this.availableLanguages = { 'en': 'English' };
        }
    }

    getLanguageDisplayNames() {
        // Complete mapping of language codes to display names
        return {
            'af': 'Afrikaans',
            'am': 'አማርኛ (Amharic)',
            'ar': 'العربية (Arabic)',
            'as': 'অসমীয়া (Assamese)',
            'az': 'Azərbaycan (Azerbaijani)',
            'be': 'Беларуская (Belarusian)',
            'bg': 'Български (Bulgarian)',
            'bn': 'বাংলা (Bengali)',
            'br': 'Brezhoneg (Breton)',
            'bs': 'Bosanski (Bosnian)',
            'ca': 'Català (Catalan)',
            'cs': 'Čeština (Czech)',
            'cy': 'Cymraeg (Welsh)',
            'da': 'Dansk (Danish)',
            'de': 'Deutsch (German)',
            'el': 'Ελληνικά (Greek)',
            'en': 'English',
            'es': 'Español (Spanish)',
            'et': 'Eesti (Estonian)',
            'eu': 'Euskera (Basque)',
            'fa': 'فارسی (Persian)',
            'fi': 'Suomi (Finnish)',
            'fr': 'Français (French)',
            'ga': 'Gaeilge (Irish)',
            'gl': 'Galego (Galician)',
            'gu': 'ગુજરાતી (Gujarati)',
            'he': 'עברית (Hebrew)',
            'hi': 'हिन्दी (Hindi)',
            'hr': 'Hrvatski (Croatian)',
            'hu': 'Magyar (Hungarian)',
            'id': 'Bahasa Indonesia (Indonesian)',
            'is': 'Íslenska (Icelandic)',
            'it': 'Italiano (Italian)',
            'ja': '日本語 (Japanese)',
            'ka': 'ქართული (Georgian)',
            'kk': 'Қазақша (Kazakh)',
            'km': 'ខ្មែរ (Khmer)',
            'kn': 'ಕನ್ನಡ (Kannada)',
            'ko': '한국어 (Korean)',
            'ky': 'Кыргызча (Kyrgyz)',
            'lb': 'Lëtzebuergesch (Luxembourgish)',
            'lo': 'ລາວ (Lao)',
            'lt': 'Lietuvių (Lithuanian)',
            'lv': 'Latviešu (Latvian)',
            'mk': 'Македонски (Macedonian)',
            'ml': 'മലയാളം (Malayalam)',
            'mr': 'मराठी (Marathi)',
            'ms': 'Bahasa Melayu (Malay)',
            'mt': 'Malti (Maltese)',
            'my': 'မြန်မာ (Myanmar)',
            'ne': 'नेपाली (Nepali)',
            'nl': 'Nederlands (Dutch)',
            'no': 'Norsk (Norwegian)',
            'or': 'ଓଡ଼ିଆ (Odia)',
            'pa': 'ਪੰਜਾਬੀ (Punjabi)',
            'pl': 'Polski (Polish)',
            'pt': 'Português (Portuguese)',
            'rm': 'Rumantsch (Romansh)',
            'ro': 'Română (Romanian)',
            'ru': 'Русский (Russian)',
            'si': 'සිංහල (Sinhala)',
            'sk': 'Slovenčina (Slovak)',
            'sl': 'Slovenščina (Slovenian)',
            'sq': 'Shqip (Albanian)',
            'sr': 'Српски (Serbian)',
            'sv': 'Svenska (Swedish)',
            'sw': 'Kiswahili (Swahili)',
            'ta': 'தமிழ் (Tamil)',
            'te': 'తెలుగు (Telugu)',
            'th': 'ไทย (Thai)',
            'tl': 'Filipino',
            'tr': 'Türkçe (Turkish)',
            'uk': 'Українська (Ukrainian)',
            'ur': 'اردو (Urdu)',
            'uz': 'O\'zbek (Uzbek)',
            'vi': 'Tiếng Việt (Vietnamese)',
            'zh': '中文 (简体) (Chinese Simplified)',
            'zu': 'IsiZulu (Zulu)'
        };
    }

    async initializeLanguage() {
        // Get saved language preference
        const savedLanguage = this.getLanguagePreference();

        if (savedLanguage === 'system' || !savedLanguage) {
            // Use system language
            this.currentLanguage = this.detectSystemLanguage();
        } else {
            this.currentLanguage = savedLanguage;
        }

        // Load translations for current language
        await this.loadTranslations(this.currentLanguage);
    }

    detectSystemLanguage() {
        // Get browser language
        const browserLang = navigator.language || navigator.userLanguage || 'en';
        const langCode = browserLang.split('-')[0];

        // Check if we have translations for this language
        if (this.availableLanguages[langCode]) {
            return langCode;
        }

        // Check for special cases like zh-TW
        if (browserLang === 'zh-TW' && this.availableLanguages['zh-rTW']) {
            return 'zh-rTW';
        }

        // Default to English
        return 'en';
    }

    setupLanguageDropdown() {
        const select = document.getElementById('language-select');

        if (!select) {
            return;
        }

        // Clear existing options except "System Language"
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }

        // Add language options sorted alphabetically by native name
        const sortedLanguages = Object.entries(this.availableLanguages)
            .sort((a, b) => a[1].localeCompare(b[1]));

        sortedLanguages.forEach(([code, nativeName]) => {
            const option = document.createElement('option');
            option.value = code;
            option.textContent = nativeName;
            select.appendChild(option);
        });

        // Set current selection
        const savedLanguage = this.getLanguagePreference();
        select.value = savedLanguage || 'system';
    }

    async changeLanguage(languageCode) {
        if (languageCode === 'system') {
            this.currentLanguage = this.detectSystemLanguage();
            this.saveLanguagePreference('system');
        } else {
            this.currentLanguage = languageCode;
            this.saveLanguagePreference(languageCode);
        }

        // Load new translations
        await this.loadTranslations(this.currentLanguage);

        // Update the interface
        this.updateInterface();
    }

    async loadTranslations(languageCode) {
        try {
            // Default to English if language code is 'en' or if file doesn't exist
            if (languageCode === 'en') {
                this.translations = {};
                return;
            }

            // Try multiple possible paths
            const possiblePaths = [
                `src/lang/${languageCode}.json`,
                `/src/lang/${languageCode}.json`,
                `./src/lang/${languageCode}.json`,
                `lang/${languageCode}.json`,
                `/lang/${languageCode}.json`
            ];

            for (const translationUrl of possiblePaths) {
                try {
                    const response = await fetch(translationUrl);

                    if (response.ok) {
                        this.translations = await response.json();
                        return;
                    }
                } catch (pathError) {
                    // Try next path
                    continue;
                }
            }

            // Fallback to English if all paths fail
            this.translations = {};

        } catch (error) {
            console.warn(`Error loading translations for ${languageCode}:`, error);
            this.translations = {};
        }
    }

    t(key, defaultText) {
        // Translation function
        return this.translations[key] || defaultText || key;
    }

    updateInterface() {

        // Update all translatable elements
        const elementsToTranslate = {
            // Page elements
            '#page-title': this.t('page_title', 'Barcode WMS - Zebra Technologies'),
            '#header-title': this.t('header_title', 'Barcode Warehouse Management System'),
            '#header-subtitle': this.t('header_subtitle', 'Zebra Technologies - AI MultiBarcode Capture Interface'),

            // Navigation
            '#nav-capture-sessions': this.t('nav_capture_sessions', 'Capture Sessions'),
            '#nav-refresh': this.t('refresh', 'Refresh'),
            '#initializing-text': this.t('initializing_wms', 'Initializing WMS...'),

            // Endpoint Modal
            '#endpoint-modal-title': '🔗 ' + this.t('api_endpoint_configuration', 'API Endpoint Configuration'),
            '#endpoint-instruction': this.t('use_endpoints_instruction', 'Use these endpoints to configure your Android AI MultiBarcode Capture application:'),
            '#local-endpoint-title': '🏠 ' + this.t('local_network_endpoint', 'Local Network Endpoint'),
            '#internet-endpoint-title': '🌐 ' + this.t('internet_endpoint', 'Internet Endpoint'),
            '#copy-btn-text': this.t('copy', 'Copy'),
            '#qr-btn-text': this.t('qr_code', 'QR Code'),
            '#copy-btn-text-2': this.t('copy', 'Copy'),
            '#qr-btn-text-2': this.t('qr_code', 'QR Code'),
            '#local-endpoint-desc': this.t('local_endpoint_description', 'Use this endpoint when both the Android device and this server are on the same local network.'),
            '#internet-endpoint-desc': this.t('internet_endpoint_description', 'Use this endpoint when accessing the server from the internet (requires port forwarding or public hosting).'),
            '#android-config-title': '📱 ' + this.t('android_app_configuration', 'Android App Configuration:'),
            '#config-step-1': this.t('config_step_1', 'Open the AI MultiBarcode Capture app'),
            '#config-step-2': this.t('config_step_2', 'Go to Settings → Server Configuration'),
            '#config-step-3': this.t('config_step_3', 'Paste one of the endpoints above'),
            '#config-step-4': this.t('config_step_4', 'Test the connection and save'),

            // QR Modal
            '#qr-modal-title': '📱 ' + this.t('qr_code_endpoint_url', 'QR Code - Endpoint URL'),
            '#endpoint-url-label': this.t('endpoint_url_label', 'Endpoint URL:'),
            '#qr-scan-instructions': this.t('qr_scan_instructions', 'Scan this QR code from your Android AI MultiBarcode Capture app Settings page to automatically configure the endpoint.'),

            // Settings modal
            '#settings-modal-title': this.t('settings', 'Settings'),
            '.section-title:first-of-type': '⚙️ ' + this.t('configuration', 'Configuration'),
            '.config-label[for="language-select"]': '🌐 ' + this.t('language', 'Language'),
            '#language-description': this.t('select_interface_language', 'Select the interface language'),
            '.config-item .section-description': this.t('endpoint_settings_description', 'Configure endpoint settings and connection parameters'),
            '#settings-btn-endpoint': '🔗 ' + this.t('endpoint_configuration', 'Endpoint Configuration'),
            '.danger-section .section-title': '⚠️ ' + this.t('danger_zone', 'Danger Zone'),
            '.danger-section .section-description': this.t('danger_zone_description', 'Irreversible actions that will permanently delete data'),
            '#settings-btn-reset': '🗑️ ' + this.t('erase_all_data', 'Erase All Data'),
            '.danger-warning': '⚠️ ' + this.t('action_cannot_be_undone', 'This action cannot be undone'),

            // Footer
            '#footer-copyright': this.t('footer_copyright', 'Barcode WMS © 2024 - Powered by Zebra AI Vision SDK'),
            '#footer-connection': this.t('footer_connection', 'Connected to Android AI MultiBarcode Capture Application'),

            // Other common elements
            '.loading p': this.t('loading', 'Loading...'),
        };

        Object.entries(elementsToTranslate).forEach(([selector, text]) => {
            const element = document.querySelector(selector);
            if (element) {
                element.textContent = text;
            }
        });

        // Update "System Language" option text
        const systemOption = document.querySelector('#language-select option[value="system"]');
        if (systemOption) {
            systemOption.textContent = this.t('system_language', 'System Language');
        }

        // Re-render current view to apply translations
        if (this.currentView === 'sessions') {
            this.renderSessions();
        } else if (this.currentSession) {
            this.renderSessionDetails(this.currentSession);
        }
    }

    // Language preference storage using W3C guidelines (localStorage)
    getLanguagePreference() {
        try {
            return localStorage.getItem('wms_language_preference');
        } catch (error) {
            console.warn('Unable to access localStorage for language preference:', error);
            return null;
        }
    }

    saveLanguagePreference(languageCode) {
        try {
            localStorage.setItem('wms_language_preference', languageCode);
        } catch (error) {
            console.warn('Unable to save language preference to localStorage:', error);
        }
    }
}

// Initialize the application when the page loads
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new WMSApp();
});