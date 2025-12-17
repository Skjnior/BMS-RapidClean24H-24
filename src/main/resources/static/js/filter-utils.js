/**
 * Utility functions for filter management across admin pages
 * Provides consistent filter reset and active filter indicators
 */

/**
 * Creates a filter reset button and active filters indicator
 * @param {string} pagePrefix - Prefix for page-specific IDs (e.g., 'Services', 'Requests')
 * @param {Object} config - Configuration object with filter elements
 */
function initializeFilterUtils(pagePrefix, config) {
    const resetBtnId = `resetFilters${pagePrefix}`;
    const activeFiltersDivId = `activeFilters${pagePrefix}`;
    const activeFiltersCountId = `activeFiltersCount${pagePrefix}`;
    
    // Create reset button if it doesn't exist
    let resetBtn = document.getElementById(resetBtnId);
    if (!resetBtn && config.cardHeader) {
        resetBtn = document.createElement('button');
        resetBtn.type = 'button';
        resetBtn.className = 'btn btn-sm btn-outline-secondary';
        resetBtn.id = resetBtnId;
        resetBtn.style.cssText = 'display: none; border-radius: 20px; padding: 5px 15px;';
        resetBtn.innerHTML = '<i class="fas fa-redo me-1"></i>Réinitialiser';
        resetBtn.onclick = () => window[`resetFilters${pagePrefix}`]();
        
        if (config.cardHeader) {
            config.cardHeader.style.display = 'flex';
            config.cardHeader.style.justifyContent = 'space-between';
            config.cardHeader.style.alignItems = 'center';
            config.cardHeader.appendChild(resetBtn);
        }
    }
    
    // Create active filters indicator if it doesn't exist
    let activeFiltersDiv = document.getElementById(activeFiltersDivId);
    if (!activeFiltersDiv && config.cardBody) {
        activeFiltersDiv = document.createElement('div');
        activeFiltersDiv.id = activeFiltersDivId;
        activeFiltersDiv.className = 'mt-3';
        activeFiltersDiv.style.display = 'none';
        activeFiltersDiv.innerHTML = `
            <small class="text-muted">
                <i class="fas fa-info-circle me-1"></i>
                Filtres actifs: <span id="${activeFiltersCountId}">0</span>
            </small>
        `;
        if (config.cardBody) {
            config.cardBody.appendChild(activeFiltersDiv);
        }
    }
}

/**
 * Updates the active filters indicator
 * @param {string} pagePrefix - Prefix for page-specific IDs
 * @param {Function} getActiveCount - Function that returns the count of active filters
 */
function updateActiveFiltersIndicator(pagePrefix, getActiveCount) {
    const resetBtnId = `resetFilters${pagePrefix}`;
    const activeFiltersDivId = `activeFilters${pagePrefix}`;
    const activeFiltersCountId = `activeFiltersCount${pagePrefix}`;
    
    const activeCount = getActiveCount();
    
    const resetBtn = document.getElementById(resetBtnId);
    const activeFiltersDiv = document.getElementById(activeFiltersDivId);
    const activeFiltersCount = document.getElementById(activeFiltersCountId);
    
    if (activeCount > 0) {
        if (resetBtn) resetBtn.style.display = 'inline-block';
        if (activeFiltersDiv) activeFiltersDiv.style.display = 'block';
        if (activeFiltersCount) activeFiltersCount.textContent = activeCount;
    } else {
        if (resetBtn) resetBtn.style.display = 'none';
        if (activeFiltersDiv) activeFiltersDiv.style.display = 'none';
    }
}



