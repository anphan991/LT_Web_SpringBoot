<%@ page pageEncoding="UTF-8" %>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<style>
    :root {
        --ink: #0f172a;
        --muted: #64748b;
        --line: #e2e8f0;
        --surface: #ffffff;
        --canvas: #f4f7fb;
        --accent: #2563eb;
        --accent-dark: #1d4ed8;
        --accent-soft: #eff6ff;
        --admin-ink: #0f172a;
        --admin-muted: #64748b;
        --admin-line: #e2e8f0;
        --admin-canvas: #f4f7fb;
        --admin-accent: #2563eb;
        --admin-accent-soft: #eff6ff;
    }

    html, body { height: 100%; }

    body {
        font-family: "Plus Jakarta Sans", Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif !important;
        background-color: var(--canvas);
        color: var(--ink);
        -webkit-font-smoothing: antialiased;
    }

    .sitemesh-navbar {
        background: rgba(255, 255, 255, .92) !important;
        backdrop-filter: blur(14px);
        -webkit-backdrop-filter: blur(14px);
        border-bottom: 1px solid rgba(226, 232, 240, .9);
        box-shadow: 0 1px 2px rgba(15, 23, 42, .03);
        z-index: 1030;
    }

    .sitemesh-brand-mark {
        width: 30px;
        height: 30px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
        color: #ffffff;
        border-radius: 9px;
        font-size: 15px;
        box-shadow: 0 4px 10px -2px rgba(37, 99, 235, .42);
    }

    .sitemesh-nav-link {
        color: #475569 !important;
        font-weight: 600;
        font-size: 13.5px;
        padding: 7px 13px !important;
        border-radius: 9px;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        transition: background-color .16s ease, color .16s ease;
    }

    .sitemesh-nav-link:hover {
        color: #0f172a !important;
        background: #f1f5f9;
    }

    .sitemesh-nav-link.active {
        color: #1d4ed8 !important;
        background: #eff6ff;
    }

    .sitemesh-user-pill {
        display: inline-flex;
        align-items: center;
        gap: 9px;
        padding: 4px 11px 4px 5px;
        border-radius: 999px;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        text-decoration: none;
        color: #0f172a;
        transition: border-color .15s ease, background-color .15s ease, box-shadow .15s ease;
    }

    .sitemesh-user-pill:hover,
    .sitemesh-user-pill.active {
        background: #eff6ff;
        border-color: #bfdbfe;
        box-shadow: 0 2px 8px rgba(37, 99, 235, .1);
    }

    .sitemesh-avatar {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: linear-gradient(135deg, #1e293b 0%, #2563eb 100%);
        color: #ffffff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 700;
        text-transform: uppercase;
        overflow: hidden;
        flex: 0 0 30px;
    }

    .sitemesh-avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .sitemesh-main-wrap {
        flex: 1 0 auto;
        display: flex;
        flex-direction: column;
    }

    .landing-page .home-hero,
    .auth-page,
    .error-page,
    .admin-shell {
        flex: 1 0 auto;
        min-height: 0 !important;
    }

    .home-hero {
        min-height: 0 !important;
    }

    .auth-page,
    .error-page {
        display: flex;
        flex-direction: column;
        padding: 24px 20px !important;
        background:
            radial-gradient(circle at 12% 15%, rgba(37, 99, 235, .07), transparent 40%),
            radial-gradient(circle at 88% 82%, rgba(79, 70, 229, .06), transparent 40%),
            var(--canvas);
    }

    .auth-page .auth-card,
    .error-page .error-card {
        margin: auto;
        border-radius: 16px !important;
        border: 1px solid #e2e8f0 !important;
        box-shadow: 0 12px 32px -12px rgba(15, 23, 42, .12), 0 2px 6px rgba(15, 23, 42, .04) !important;
    }

    .home-hero-inner {
        padding: 40px 0 !important;
    }

    .sitemesh-footer {
        background: #ffffff;
        border-top: 1px solid #e2e8f0;
        color: #64748b;
        font-size: 13px;
    }

    body.landing-page .sitemesh-footer {
        background: #0b1220;
        border-top-color: rgba(148, 163, 184, .16);
        color: #94a3b8;
    }

    .input-control.is-invalid,
    .textarea-control.is-invalid,
    .admin-field input.is-invalid,
    .admin-field select.is-invalid {
        border-color: #dc3545 !important;
        box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, .15) !important;
    }

    .invalid-feedback {
        font-size: 12.5px;
        margin-top: 5px;
        font-weight: 500;
    }
</style>
<script>
window.ShopValidation = {
    setError: function (input, message) {
        if (!input) return;
        input.setCustomValidity(message || '');
        if (message) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
        } else {
            input.classList.remove('is-invalid');
        }
        var parent = input.parentElement;
        var feedback = parent ? parent.querySelector('.invalid-feedback') : null;
        if (!feedback && parent) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            parent.appendChild(feedback);
        }
        if (feedback) {
            feedback.textContent = message || '';
            if (message) {
                feedback.classList.add('d-block');
            } else {
                feedback.classList.remove('d-block');
            }
        }
    },
    clearAll: function (form) {
        form.querySelectorAll('.is-invalid').forEach(function (el) {
            el.classList.remove('is-invalid');
        });
        form.querySelectorAll('.invalid-feedback').forEach(function (el) {
            el.classList.remove('d-block');
        });
    },
    attach: function (formId, validatorFn) {
        var form = document.getElementById(formId);
        if (!form) return;
        form.setAttribute('novalidate', 'novalidate');
        form.querySelectorAll('input, textarea, select').forEach(function (field) {
            field.addEventListener('input', function () {
                window.ShopValidation.setError(field, '');
            });
            field.addEventListener('change', function () {
                window.ShopValidation.setError(field, '');
            });
        });
        form.addEventListener('submit', function (event) {
            window.ShopValidation.clearAll(form);
            var isValid = validatorFn(form);
            if (!isValid) {
                event.preventDefault();
                event.stopPropagation();
                var firstInvalid = form.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            }
        });
    }
};
</script>
