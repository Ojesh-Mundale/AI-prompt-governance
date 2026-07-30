/**
 * AI Prompt Governance System - Main JavaScript
 * Handles sidebar toggle, auto-dismiss alerts, and form enhancements.
 */

document.addEventListener('DOMContentLoaded', function() {

    // ============================================================
    // Sidebar Toggle
    // ============================================================
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.sidebar');

    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function(e) {
            e.preventDefault();
            sidebar.classList.toggle('active');
        });
    }

    // ============================================================
    // Auto-dismiss Alerts after 5 seconds
    // ============================================================
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // ============================================================
    // Form Validation Enhancement
    // ============================================================
    const forms = document.querySelectorAll('form');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // ============================================================
    // Confirm Delete Actions
    // ============================================================
    const deleteLinks = document.querySelectorAll('[data-confirm]');
    deleteLinks.forEach(function(link) {
        link.addEventListener('click', function(event) {
            if (!confirm(link.getAttribute('data-confirm') || 'Are you sure you want to proceed?')) {
                event.preventDefault();
            }
        });
    });

    // ============================================================
    // Character Counter for Textarea
    // ============================================================
    const textareas = document.querySelectorAll('textarea[maxlength]');
    textareas.forEach(function(textarea) {
        const maxLength = textarea.getAttribute('maxlength');
        const counter = document.createElement('small');
        counter.className = 'form-text text-muted float-end';
        counter.textContent = '0 / ' + maxLength + ' characters';

        textarea.parentNode.appendChild(counter);

        textarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            counter.textContent = currentLength + ' / ' + maxLength + ' characters';

            if (currentLength >= maxLength) {
                counter.classList.add('text-danger');
                counter.classList.remove('text-muted');
            } else {
                counter.classList.remove('text-danger');
                counter.classList.add('text-muted');
            }
        });
    });

    // ============================================================
    // Table Row Click Handler (for view links)
    // ============================================================
    const tableRows = document.querySelectorAll('.table-hover tbody tr[data-href]');
    tableRows.forEach(function(row) {
        row.addEventListener('click', function() {
            window.location.href = row.getAttribute('data-href');
        });
        row.style.cursor = 'pointer';
    });

    // ============================================================
    // Search Form Auto-submit on Enter
    // ============================================================
    const searchInput = document.querySelector('input[name="keyword"]');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                const form = this.closest('form');
                if (form) {
                    form.submit();
                }
            }
        });
    }

    // ============================================================
    // Tooltip Initialization
    // ============================================================
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    console.log('AI Prompt Governance System initialized successfully.');
});

