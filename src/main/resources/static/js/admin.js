// ========================================
// BMS RapidClean - Admin Dashboard JS
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    initializeAdmin();
});

// Initialize all admin features
function initializeAdmin() {
    initializeSidebar();
    initializeTooltips();
    initializeCharts();
    initializeDataTables();
    initializeNotifications();
    initializeRealTimeUpdates();
}

// Sidebar Management
function initializeSidebar() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    const mainContent = document.querySelector('.main-content');
    
    // Don't initialize sidebar toggle here - let the layout.html script handle it
    // This prevents conflicts with the mobile sidebar logic
    
    // Active menu item
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll('.menu-link');
    
    menuLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}

// Initialize Bootstrap Tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Initialize Charts (Placeholder - requires Chart.js)
function initializeCharts() {
    // Add Chart.js initialization here if needed
    console.log('Charts initialized');
}

// Data Tables Enhancement
function initializeDataTables() {
    const tables = document.querySelectorAll('table');
    
    tables.forEach(table => {
        // Add row click handlers
        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(row => {
            row.addEventListener('click', function(e) {
                // Don't trigger if clicking on buttons
                if (!e.target.closest('button') && !e.target.closest('a') && !e.target.closest('form')) {
                    const viewButton = this.querySelector('[data-bs-toggle="modal"]');
                    if (viewButton) {
                        viewButton.click();
                    }
                }
            });
        });
    });
}

// Notifications Management
function initializeNotifications() {
    checkNewNotifications();
    
    // Check for new notifications every 30 seconds
    setInterval(checkNewNotifications, 30000);
}

function checkNewNotifications() {
    // Fetch new notifications from API
    // This is a placeholder - implement your API call here
    fetch('/api/notifications/count')
        .then(response => response.json())
        .then(data => {
            updateNotificationBadge(data.count);
        })
        .catch(error => {
            console.error('Error fetching notifications:', error);
        });
}

function updateNotificationBadge(count) {
    const badge = document.querySelector('.notification-icon .badge');
    if (badge) {
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = 'inline';
        } else {
            badge.style.display = 'none';
        }
    }
}

// Real-time Updates
function initializeRealTimeUpdates() {
    // Implement WebSocket or polling for real-time updates
    console.log('Real-time updates initialized');
}

// Utility Functions

// Show loading spinner
function showLoading(element) {
    const originalContent = element.innerHTML;
    element.innerHTML = '<span class="loading"></span>';
    element.disabled = true;
    
    return function hideLoading() {
        element.innerHTML = originalContent;
        element.disabled = false;
    };
}

// Show toast notification
function showToast(message, type = 'info') {
    const toastContainer = document.querySelector('.toast-container') || createToastContainer();
    
    const toast = document.createElement('div');
    toast.className = `toast-custom toast-${type}`;
    
    const icon = {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-circle',
        'warning': 'fa-exclamation-triangle',
        'info': 'fa-info-circle'
    }[type] || 'fa-info-circle';
    
    const color = {
        'success': '#28a745',
        'error': '#dc3545',
        'warning': '#ffc107',
        'info': '#17a2b8'
    }[type] || '#17a2b8';
    
    toast.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas ${icon} me-3" style="font-size: 1.5rem; color: ${color};"></i>
            <div class="flex-grow-1">${message}</div>
            <button type="button" class="btn-close ms-3" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        toast.style.animation = 'slideOutToRight 0.3s ease-in';
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
}

// Confirm dialog with custom styling
function confirmAction(message, onConfirm) {
    if (confirm(message)) {
        onConfirm();
    }
}

// Format number with thousands separator
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: 'EUR'
    }).format(amount);
}

// Format date
function formatDate(date) {
    return new Intl.DateTimeFormat('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(date));
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Export data to CSV
function exportToCSV(tableId, filename) {
    const table = document.getElementById(tableId);
    let csv = [];
    
    // Get headers
    const headers = [];
    table.querySelectorAll('thead th').forEach(th => {
        headers.push(th.textContent.trim());
    });
    csv.push(headers.join(','));
    
    // Get rows
    table.querySelectorAll('tbody tr').forEach(row => {
        const rowData = [];
        row.querySelectorAll('td').forEach(td => {
            rowData.push('"' + td.textContent.trim().replace(/"/g, '""') + '"');
        });
        csv.push(rowData.join(','));
    });
    
    // Download
    const csvContent = csv.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename + '.csv';
    link.click();
}

// Print section
function printSection(sectionId) {
    const section = document.getElementById(sectionId);
    const printWindow = window.open('', '', 'height=600,width=800');
    
    printWindow.document.write('<html><head><title>Impression</title>');
    printWindow.document.write('<link rel="stylesheet" href="/css/admin.css">');
    printWindow.document.write('</head><body>');
    printWindow.document.write(section.innerHTML);
    printWindow.document.write('</body></html>');
    
    printWindow.document.close();
    printWindow.print();
}

// Global error handler
window.addEventListener('error', function(e) {
    console.error('Global error:', e.error);
    // You can send this to your error tracking service
});

// Export functions for global use
window.AdminDashboard = {
    showToast,
    showLoading,
    confirmAction,
    formatNumber,
    formatCurrency,
    formatDate,
    exportToCSV,
    printSection
};

console.log('Admin Dashboard JS loaded successfully');
