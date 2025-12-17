// Enhanced Form Validation and UI Improvements

document.addEventListener('DOMContentLoaded', function() {
    initializeLazyLoading();
    initializeFormValidation();
    initializeFormSpinners();
    initializeHoneypotFields();
    initializeAccessibility();
});

// Lazy Loading for Images
function initializeLazyLoading() {
    if ('loading' in HTMLImageElement.prototype) {
        // Native lazy loading supported
        const images = document.querySelectorAll('img[data-src]');
        images.forEach(img => {
            img.src = img.dataset.src;
            img.loading = 'lazy';
        });
    } else {
        // Fallback for browsers without native lazy loading
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }
}

// Enhanced Form Validation with Real-time Feedback
function initializeFormValidation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        const inputs = form.querySelectorAll('input, textarea, select');
        
        inputs.forEach(input => {
            // Real-time validation on blur
            input.addEventListener('blur', function() {
                validateField(this);
            });
            
            // Real-time validation on input (for some fields)
            if (input.type === 'email' || input.type === 'tel') {
                input.addEventListener('input', function() {
                    if (this.value.length > 0) {
                        validateField(this);
                    }
                });
            }
            
            // Clear errors on focus
            input.addEventListener('focus', function() {
                clearFieldError(this);
            });
        });
        
        // Form submission validation
        form.addEventListener('submit', function(e) {
            let isValid = true;
            inputs.forEach(input => {
                if (!validateField(input)) {
                    isValid = false;
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                showFormError(form, 'Veuillez corriger les erreurs avant de soumettre.');
            }
        });
    });
}

// Validate individual field
function validateField(field) {
    const value = field.value.trim();
    let isValid = true;
    let errorMessage = '';
    
    // Remove previous error state
    clearFieldError(field);
    
    // Required field validation
    if (field.hasAttribute('required') && !value) {
        isValid = false;
        errorMessage = 'Ce champ est obligatoire.';
    }
    
    // Email validation
    if (field.type === 'email' && value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            isValid = false;
            errorMessage = 'Veuillez entrer une adresse email valide.';
        }
    }
    
    // Phone validation
    if (field.type === 'tel' && value) {
        const phoneRegex = /^[\d\s\+\-\(\)]+$/;
        if (!phoneRegex.test(value) || value.replace(/\D/g, '').length < 10) {
            isValid = false;
            errorMessage = 'Veuillez entrer un numéro de téléphone valide.';
        }
    }
    
    // Min length validation
    if (field.hasAttribute('minlength') && value.length > 0) {
        const minLength = parseInt(field.getAttribute('minlength'));
        if (value.length < minLength) {
            isValid = false;
            errorMessage = `Ce champ doit contenir au moins ${minLength} caractères.`;
        }
    }
    
    // Max length validation
    if (field.hasAttribute('maxlength') && value.length > parseInt(field.getAttribute('maxlength'))) {
        isValid = false;
        errorMessage = `Ce champ ne peut pas dépasser ${field.getAttribute('maxlength')} caractères.`;
    }
    
    // Display error if invalid
    if (!isValid) {
        showFieldError(field, errorMessage);
    } else {
        showFieldSuccess(field);
    }
    
    return isValid;
}

// Show field error
function showFieldError(field, message) {
    field.classList.add('is-invalid');
    field.classList.remove('is-valid');
    field.setAttribute('aria-invalid', 'true');
    
    // Remove existing error message
    const existingError = field.parentElement.querySelector('.invalid-feedback');
    if (existingError) {
        existingError.remove();
    }
    
    // Create error message element
    const errorDiv = document.createElement('div');
    errorDiv.className = 'invalid-feedback';
    errorDiv.textContent = message;
    errorDiv.setAttribute('role', 'alert');
    field.parentElement.appendChild(errorDiv);
}

// Show field success
function showFieldSuccess(field) {
    field.classList.add('is-valid');
    field.classList.remove('is-invalid');
    field.setAttribute('aria-invalid', 'false');
    
    const existingError = field.parentElement.querySelector('.invalid-feedback');
    if (existingError) {
        existingError.remove();
    }
}

// Clear field error
function clearFieldError(field) {
    field.classList.remove('is-invalid', 'is-valid');
    const existingError = field.parentElement.querySelector('.invalid-feedback');
    if (existingError) {
        existingError.remove();
    }
}

// Show form error
function showFormError(form, message) {
    const existingAlert = form.querySelector('.form-error-alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger form-error-alert';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `<i class="fas fa-exclamation-circle me-2"></i>${message}`;
    form.insertBefore(alertDiv, form.firstChild);
    
    // Scroll to error
    alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Initialize form spinners
function initializeFormSpinners() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitButton = form.querySelector('button[type="submit"], input[type="submit"]');
            if (submitButton && !form.dataset.noSpinner) {
                showSpinner(submitButton);
            }
        });
    });
}

// Show spinner on button
function showSpinner(button) {
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    
    const originalText = button.innerHTML;
    button.dataset.originalText = originalText;
    
    button.innerHTML = `
        <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
        <span class="sr-only">Envoi en cours...</span>
        ${button.textContent || 'Envoi...'}
    `;
}

// Hide spinner on button
function hideSpinner(button) {
    button.disabled = false;
    button.removeAttribute('aria-busy');
    
    if (button.dataset.originalText) {
        button.innerHTML = button.dataset.originalText;
        delete button.dataset.originalText;
    }
}

// Initialize honeypot fields (anti-spam)
function initializeHoneypotFields() {
    const honeypotFields = document.querySelectorAll('input[name="website"]');
    honeypotFields.forEach(field => {
        // Hide honeypot field with CSS
        field.style.position = 'absolute';
        field.style.left = '-9999px';
        field.style.opacity = '0';
        field.setAttribute('tabindex', '-1');
        field.setAttribute('autocomplete', 'off');
        
        // Clear value on focus (if bot fills it)
        field.addEventListener('focus', function() {
            this.value = '';
        });
    });
}

// Accessibility improvements
function initializeAccessibility() {
    // Add aria-labels to icon-only buttons
    document.querySelectorAll('button, a').forEach(element => {
        if (!element.textContent.trim() && !element.getAttribute('aria-label')) {
            const icon = element.querySelector('i');
            if (icon) {
                const iconClass = Array.from(icon.classList).find(c => c.startsWith('fa-'));
                if (iconClass) {
                    const label = iconClass.replace('fa-', '').replace(/-/g, ' ');
                    element.setAttribute('aria-label', label);
                }
            }
        }
    });
    
    // Improve keyboard navigation
    document.querySelectorAll('a, button, input, textarea, select').forEach(element => {
        if (!element.hasAttribute('tabindex') && !element.disabled) {
            element.setAttribute('tabindex', '0');
        }
    });
    
    // Add skip to main content link
    if (!document.querySelector('#skip-to-main')) {
        const skipLink = document.createElement('a');
        skipLink.id = 'skip-to-main';
        skipLink.href = '#main-content';
        skipLink.className = 'sr-only sr-only-focusable btn btn-primary position-absolute';
        skipLink.style.cssText = 'top: 10px; left: 10px; z-index: 9999;';
        skipLink.textContent = 'Aller au contenu principal';
        document.body.insertBefore(skipLink, document.body.firstChild);
    }
}

// Export functions for use in other scripts
window.FormEnhancements = {
    validateField: validateField,
    showSpinner: showSpinner,
    hideSpinner: hideSpinner
};



