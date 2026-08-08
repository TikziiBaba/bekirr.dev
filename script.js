// ============================================
// bekirr.dev — Main Application Script
// Portfolio + Supabase Auth + Dashboard
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // ── State ──
    let currentUser = null;
    let currentView = 'portfolio'; // 'portfolio' | 'dashboard'

    // ── DOM Cache ──
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    const DOM = {
        // Loader
        pageLoader: $('#pageLoader'),
        // Navbar
        navbar: $('#navbar'),
        navbarBrand: $('#navbarBrand'),
        navbarLinks: $('#navbarLinks'),
        navbarToggle: $('#navbarToggle'),
        // Auth UI
        authButtons: $('#authButtons'),
        userNav: $('#userNav'),
        userNavName: $('#userNavName'),
        userNavAvatar: $('#userNavAvatar'),
        btnOpenLogin: $('#btnOpenLogin'),
        btnOpenRegister: $('#btnOpenRegister'),
        btnLogout: $('#btnLogout'),
        // Auth Modal
        authOverlay: $('#authOverlay'),
        authModal: $('#authModal'),
        authClose: $('#authClose'),
        loginView: $('#loginView'),
        registerView: $('#registerView'),
        switchToRegister: $('#switchToRegister'),
        switchToLogin: $('#switchToLogin'),
        // Login Form
        loginForm: $('#loginForm'),
        loginEmail: $('#loginEmail'),
        loginPassword: $('#loginPassword'),
        loginSubmit: $('#loginSubmit'),
        loginMessage: $('#loginMessage'),
        loginEmailError: $('#loginEmailError'),
        loginPasswordError: $('#loginPasswordError'),
        // Register Form
        registerForm: $('#registerForm'),
        registerName: $('#registerName'),
        registerEmail: $('#registerEmail'),
        registerPassword: $('#registerPassword'),
        registerPasswordConfirm: $('#registerPasswordConfirm'),
        registerSubmit: $('#registerSubmit'),
        registerMessage: $('#registerMessage'),
        registerNameError: $('#registerNameError'),
        registerEmailError: $('#registerEmailError'),
        registerPasswordError: $('#registerPasswordError'),
        registerPasswordConfirmError: $('#registerPasswordConfirmError'),
        // Views
        portfolioView: $('#portfolioView'),
        dashboardView: $('#dashboardView'),
        // Dashboard
        dashboardUserName: $('#dashboardUserName'),
        profileName: $('#profileName'),
        profileEmail: $('#profileEmail'),
        profileCreatedAt: $('#profileCreatedAt'),
        profileLastSignIn: $('#profileLastSignIn'),
        profileSessionExpiry: $('#profileSessionExpiry'),
        btnBackToSite: $('#btnBackToSite'),
        btnDashboardLogout: $('#btnDashboardLogout'),
        // Contact form
        contactForm: $('#contactForm'),
        contactSubmit: $('#contactSubmit'),
        // Scroll
        scrollProgress: $('#scroll-progress'),
        // Toast
        toastContainer: $('#toastContainer'),
    };


    // ════════════════════════════════════════════════════════════
    //  1. PAGE LOADER
    // ════════════════════════════════════════════════════════════
    function hideLoader() {
        if (DOM.pageLoader) {
            DOM.pageLoader.classList.add('hidden');
            setTimeout(() => {
                DOM.pageLoader.style.display = 'none';
            }, 400);
        }
    }


    // ════════════════════════════════════════════════════════════
    //  2. TOAST NOTIFICATIONS
    // ════════════════════════════════════════════════════════════
    function showToast(message, type = 'success', duration = 4000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ';
        toast.innerHTML = `<span class="toast-icon">${icon}</span><span>${message}</span>`;

        DOM.toastContainer.appendChild(toast);

        // Trigger animation
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                toast.classList.add('visible');
            });
        });

        setTimeout(() => {
            toast.classList.remove('visible');
            setTimeout(() => toast.remove(), 400);
        }, duration);
    }


    // ════════════════════════════════════════════════════════════
    //  3. NAVBAR
    // ════════════════════════════════════════════════════════════
    // Scroll effect
    let lastScrollY = 0;
    window.addEventListener('scroll', () => {
        const scrollY = window.scrollY;

        // Navbar background on scroll
        if (scrollY > 20) {
            DOM.navbar.classList.add('scrolled');
        } else {
            DOM.navbar.classList.remove('scrolled');
        }

        // Scroll progress bar
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        const progress = docHeight > 0 ? (scrollY / docHeight) * 100 : 0;
        if (DOM.scrollProgress) {
            DOM.scrollProgress.style.width = progress + '%';
        }

        lastScrollY = scrollY;
    }, { passive: true });

    // Mobile toggle
    if (DOM.navbarToggle) {
        DOM.navbarToggle.addEventListener('click', () => {
            DOM.navbarToggle.classList.toggle('active');
            DOM.navbarLinks.classList.toggle('open');
        });
    }

    // Close mobile menu on link click
    DOM.navbarLinks.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', () => {
            DOM.navbarToggle.classList.remove('active');
            DOM.navbarLinks.classList.remove('open');
        });
    });

    // Brand click — go to portfolio home
    if (DOM.navbarBrand) {
        DOM.navbarBrand.addEventListener('click', (e) => {
            e.preventDefault();
            switchView('portfolio');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }


    // ════════════════════════════════════════════════════════════
    //  4. SCROLL REVEAL ANIMATIONS
    // ════════════════════════════════════════════════════════════
    function initScrollReveal() {
        const reveals = $$('.reveal');
        if (!reveals.length) return;

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -40px 0px'
        });

        reveals.forEach(el => observer.observe(el));
    }

    // Skill bar animation
    function initSkillBars() {
        const fills = $$('.skill-item-fill');
        if (!fills.length) return;

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const width = entry.target.getAttribute('data-width');
                    entry.target.style.width = width + '%';
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });

        fills.forEach(el => observer.observe(el));
    }


    // ════════════════════════════════════════════════════════════
    //  5. AUTH MODAL
    // ════════════════════════════════════════════════════════════
    function openAuthModal(view = 'login') {
        DOM.authOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';

        if (view === 'login') {
            DOM.loginView.style.display = 'block';
            DOM.registerView.style.display = 'none';
        } else {
            DOM.loginView.style.display = 'none';
            DOM.registerView.style.display = 'block';
        }

        clearAuthErrors();
        clearAuthMessages();
    }

    function closeAuthModal() {
        DOM.authOverlay.classList.remove('active');
        document.body.style.overflow = '';
        clearAuthErrors();
        clearAuthMessages();
        resetAuthForms();
    }

    // Open buttons
    DOM.btnOpenLogin?.addEventListener('click', () => openAuthModal('login'));
    DOM.btnOpenRegister?.addEventListener('click', () => openAuthModal('register'));

    // Close button & overlay click
    DOM.authClose?.addEventListener('click', closeAuthModal);
    DOM.authOverlay?.addEventListener('click', (e) => {
        if (e.target === DOM.authOverlay) closeAuthModal();
    });

    // Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && DOM.authOverlay.classList.contains('active')) {
            closeAuthModal();
        }
    });

    // Switch between login/register
    DOM.switchToRegister?.addEventListener('click', () => openAuthModal('register'));
    DOM.switchToLogin?.addEventListener('click', () => openAuthModal('login'));

    // Password toggles
    $$('.password-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (input) {
                const isPassword = input.type === 'password';
                input.type = isPassword ? 'text' : 'password';
                btn.textContent = isPassword ? '🔒' : '👁';
            }
        });
    });


    // ════════════════════════════════════════════════════════════
    //  6. FORM VALIDATION
    // ════════════════════════════════════════════════════════════
    function validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function showFieldError(input, errorEl, message) {
        input.classList.add('error');
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.add('visible');
        }
    }

    function clearFieldError(input, errorEl) {
        input.classList.remove('error');
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('visible');
        }
    }

    function clearAuthErrors() {
        $$('.auth-form .form-input').forEach(input => input.classList.remove('error'));
        $$('.auth-form .form-error').forEach(el => {
            el.textContent = '';
            el.classList.remove('visible');
        });
    }

    function clearAuthMessages() {
        [DOM.loginMessage, DOM.registerMessage].forEach(el => {
            if (el) {
                el.textContent = '';
                el.className = 'auth-message';
            }
        });
    }

    function resetAuthForms() {
        DOM.loginForm?.reset();
        DOM.registerForm?.reset();
    }

    function showAuthMessage(element, message, type = 'error') {
        if (!element) return;
        element.textContent = message;
        element.className = `auth-message ${type}`;
    }

    function setButtonLoading(btn, loading) {
        if (!btn) return;
        if (loading) {
            btn.dataset.originalText = btn.textContent;
            btn.innerHTML = '<div class="spinner spinner-dark"></div>';
            btn.disabled = true;
        } else {
            btn.textContent = btn.dataset.originalText || 'Gönder';
            btn.disabled = false;
        }
    }


    // ════════════════════════════════════════════════════════════
    //  7. SUPABASE AUTH
    // ════════════════════════════════════════════════════════════
    function isSupabaseConfigured() {
        return typeof supabaseClient !== 'undefined' &&
            typeof SUPABASE_URL !== 'undefined' &&
            !SUPABASE_URL.includes('YOUR_PROJECT_ID');
    }

    // — REGISTER —
    DOM.registerForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAuthErrors();
        clearAuthMessages();

        const name = DOM.registerName.value.trim();
        const email = DOM.registerEmail.value.trim();
        const password = DOM.registerPassword.value;
        const confirmPassword = DOM.registerPasswordConfirm.value;

        // Validate
        let hasError = false;

        if (!name) {
            showFieldError(DOM.registerName, DOM.registerNameError, 'Ad soyad gerekli.');
            hasError = true;
        }

        if (!email) {
            showFieldError(DOM.registerEmail, DOM.registerEmailError, 'E-posta adresi gerekli.');
            hasError = true;
        } else if (!validateEmail(email)) {
            showFieldError(DOM.registerEmail, DOM.registerEmailError, 'Geçerli bir e-posta girin.');
            hasError = true;
        }

        if (!password) {
            showFieldError(DOM.registerPassword, DOM.registerPasswordError, 'Şifre gerekli.');
            hasError = true;
        } else if (password.length < 6) {
            showFieldError(DOM.registerPassword, DOM.registerPasswordError, 'Şifre en az 6 karakter olmalı.');
            hasError = true;
        }

        if (!confirmPassword) {
            showFieldError(DOM.registerPasswordConfirm, DOM.registerPasswordConfirmError, 'Şifre tekrarı gerekli.');
            hasError = true;
        } else if (password !== confirmPassword) {
            showFieldError(DOM.registerPasswordConfirm, DOM.registerPasswordConfirmError, 'Şifreler eşleşmiyor.');
            hasError = true;
        }

        if (hasError) return;

        if (!isSupabaseConfigured()) {
            showAuthMessage(DOM.registerMessage, 'Supabase yapılandırılmamış. supabase-config.js dosyasını güncelleyin.', 'error');
            return;
        }

        setButtonLoading(DOM.registerSubmit, true);

        try {
            const { data, error } = await supabaseClient.auth.signUp({
                email: email,
                password: password,
                options: {
                    data: {
                        full_name: name
                    }
                }
            });

            if (error) throw error;

            // Check if email confirmation is required
            if (data.user && data.user.identities && data.user.identities.length === 0) {
                showAuthMessage(DOM.registerMessage, 'Bu e-posta zaten kayıtlı.', 'error');
            } else if (data.session) {
                // Auto-confirmed — logged in immediately
                closeAuthModal();
                showToast(`Hoş geldin, ${name}! Hesabın oluşturuldu.`, 'success');
            } else {
                // Email confirmation required
                showAuthMessage(DOM.registerMessage, 'Kayıt başarılı! E-posta adresine onay linki gönderildi.', 'success');
            }
        } catch (err) {
            const message = getAuthErrorMessage(err);
            showAuthMessage(DOM.registerMessage, message, 'error');
        } finally {
            setButtonLoading(DOM.registerSubmit, false);
        }
    });

    // — LOGIN —
    DOM.loginForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAuthErrors();
        clearAuthMessages();

        const email = DOM.loginEmail.value.trim();
        const password = DOM.loginPassword.value;

        // Validate
        let hasError = false;

        if (!email) {
            showFieldError(DOM.loginEmail, DOM.loginEmailError, 'E-posta adresi gerekli.');
            hasError = true;
        } else if (!validateEmail(email)) {
            showFieldError(DOM.loginEmail, DOM.loginEmailError, 'Geçerli bir e-posta girin.');
            hasError = true;
        }

        if (!password) {
            showFieldError(DOM.loginPassword, DOM.loginPasswordError, 'Şifre gerekli.');
            hasError = true;
        }

        if (hasError) return;

        if (!isSupabaseConfigured()) {
            showAuthMessage(DOM.loginMessage, 'Supabase yapılandırılmamış. supabase-config.js dosyasını güncelleyin.', 'error');
            return;
        }

        setButtonLoading(DOM.loginSubmit, true);

        try {
            const { data, error } = await supabaseClient.auth.signInWithPassword({
                email: email,
                password: password
            });

            if (error) throw error;

            closeAuthModal();
            showToast('Başarıyla giriş yaptın!', 'success');
        } catch (err) {
            const message = getAuthErrorMessage(err);
            showAuthMessage(DOM.loginMessage, message, 'error');
        } finally {
            setButtonLoading(DOM.loginSubmit, false);
        }
    });

    // — LOGOUT —
    async function logout() {
        if (!isSupabaseConfigured()) return;

        try {
            await supabaseClient.auth.signOut();
            showToast('Çıkış yapıldı.', 'success');
            switchView('portfolio');
        } catch (err) {
            showToast('Çıkış yapılırken hata oluştu.', 'error');
        }
    }

    DOM.btnLogout?.addEventListener('click', logout);
    DOM.btnDashboardLogout?.addEventListener('click', logout);

    // — AUTH STATE LISTENER —
    function initAuthListener() {
        if (!isSupabaseConfigured()) {
            hideLoader();
            return;
        }

        supabaseClient.auth.onAuthStateChange((event, session) => {
            if (session?.user) {
                currentUser = session.user;
                updateUIForAuth(true);
                updateDashboard(session);
            } else {
                currentUser = null;
                updateUIForAuth(false);
                if (currentView === 'dashboard') {
                    switchView('portfolio');
                }
            }

            hideLoader();
        });

        // Also check initial session
        supabaseClient.auth.getSession().then(({ data: { session } }) => {
            if (session?.user) {
                currentUser = session.user;
                updateUIForAuth(true);
                updateDashboard(session);
            }
            hideLoader();
        });
    }

    // — ERROR MESSAGES —
    function getAuthErrorMessage(err) {
        const msg = err?.message?.toLowerCase() || '';
        if (msg.includes('invalid login credentials') || msg.includes('invalid_credentials')) {
            return 'E-posta veya şifre hatalı.';
        }
        if (msg.includes('email not confirmed')) {
            return 'E-posta adresini henüz onaylamamışsın.';
        }
        if (msg.includes('user already registered') || msg.includes('already registered')) {
            return 'Bu e-posta zaten kayıtlı.';
        }
        if (msg.includes('rate limit') || msg.includes('too many requests')) {
            return 'Çok fazla deneme. Lütfen biraz bekle.';
        }
        if (msg.includes('weak password') || msg.includes('password')) {
            return 'Şifre yeterince güçlü değil. En az 6 karakter kullan.';
        }
        if (msg.includes('network') || msg.includes('fetch')) {
            return 'Bağlantı hatası. İnternet bağlantınızı kontrol edin.';
        }
        return err?.message || 'Bir hata oluştu. Lütfen tekrar dene.';
    }


    // ════════════════════════════════════════════════════════════
    //  8. UI STATE MANAGEMENT
    // ════════════════════════════════════════════════════════════
    function updateUIForAuth(isLoggedIn) {
        if (isLoggedIn && currentUser) {
            DOM.authButtons.style.display = 'none';
            DOM.userNav.style.display = 'flex';

            const name = currentUser.user_metadata?.full_name || currentUser.email?.split('@')[0] || 'Kullanıcı';
            const initial = name.charAt(0).toUpperCase();

            DOM.userNavName.textContent = name;
            DOM.userNavAvatar.textContent = initial;
        } else {
            DOM.authButtons.style.display = 'flex';
            DOM.userNav.style.display = 'none';
        }
    }

    function switchView(view) {
        currentView = view;

        if (view === 'dashboard') {
            DOM.portfolioView.classList.add('hidden');
            DOM.dashboardView.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            DOM.portfolioView.classList.remove('hidden');
            DOM.dashboardView.classList.remove('active');
        }
    }

    // Dashboard navigation
    DOM.userNavAvatar?.addEventListener('click', () => {
        if (currentUser) switchView('dashboard');
    });

    DOM.btnBackToSite?.addEventListener('click', () => {
        switchView('portfolio');
    });

    function updateDashboard(session) {
        if (!session?.user) return;
        const user = session.user;
        const name = user.user_metadata?.full_name || user.email?.split('@')[0] || 'Kullanıcı';

        DOM.dashboardUserName.textContent = name;
        DOM.profileName.textContent = name;
        DOM.profileEmail.textContent = user.email || '—';
        DOM.profileCreatedAt.textContent = formatDate(user.created_at);
        DOM.profileLastSignIn.textContent = formatDate(user.last_sign_in_at);

        if (session.expires_at) {
            const expiryDate = new Date(session.expires_at * 1000);
            DOM.profileSessionExpiry.textContent = formatDate(expiryDate.toISOString());
        } else {
            DOM.profileSessionExpiry.textContent = '—';
        }
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('tr-TR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return '—';
        }
    }


    // ════════════════════════════════════════════════════════════
    //  9. CONTACT FORM
    // ════════════════════════════════════════════════════════════
    DOM.contactForm?.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name = document.getElementById('contactName').value.trim();
        const email = document.getElementById('contactEmail').value.trim();
        const message = document.getElementById('contactMessage').value.trim();

        if (!name || !email || !message) {
            showToast('Lütfen tüm alanları doldurun.', 'error');
            return;
        }

        if (!validateEmail(email)) {
            showToast('Geçerli bir e-posta adresi girin.', 'error');
            return;
        }

        const submitBtn = DOM.contactSubmit;
        setButtonLoading(submitBtn, true);

        // If Supabase is configured, try saving to database
        if (isSupabaseConfigured()) {
            try {
                const { error } = await supabaseClient
                    .from('contact_messages')
                    .insert([{ name, email, message }]);

                if (error) {
                    // Table might not exist — fall back to mailto
                    console.warn('Contact form DB insert failed:', error.message);
                    window.location.href = `mailto:dedyusuf99@gmail.com?subject=İletişim: ${encodeURIComponent(name)}&body=${encodeURIComponent(message + '\n\nGönderen: ' + email)}`;
                    showToast('Mesajın e-posta ile yönlendirildi.', 'success');
                } else {
                    showToast('Mesajın başarıyla gönderildi!', 'success');
                    DOM.contactForm.reset();
                }
            } catch (err) {
                console.warn('Contact form error:', err);
                window.location.href = `mailto:dedyusuf99@gmail.com?subject=İletişim: ${encodeURIComponent(name)}&body=${encodeURIComponent(message + '\n\nGönderen: ' + email)}`;
                showToast('Mesajın e-posta ile yönlendirildi.', 'success');
            }
        } else {
            // No Supabase — fall back to mailto
            window.location.href = `mailto:dedyusuf99@gmail.com?subject=İletişim: ${encodeURIComponent(name)}&body=${encodeURIComponent(message + '\n\nGönderen: ' + email)}`;
            showToast('Mesajın e-posta ile yönlendirildi.', 'success');
        }

        setButtonLoading(submitBtn, false);
    });


    // ════════════════════════════════════════════════════════════
    //  10. GSAP SUBTLE ANIMATIONS (if loaded)
    // ════════════════════════════════════════════════════════════
    function initGSAP() {
        if (typeof gsap === 'undefined') return;

        // Hero entrance animation
        const heroElements = document.querySelectorAll('.hero .reveal');
        if (heroElements.length) {
            // Remove the CSS reveal class and animate with GSAP for hero only
            heroElements.forEach(el => {
                el.classList.remove('reveal');
                el.style.opacity = '0';
                el.style.transform = 'translateY(24px)';
            });

            gsap.to(heroElements, {
                opacity: 1,
                y: 0,
                duration: 0.8,
                stagger: 0.12,
                ease: 'power3.out',
                delay: 0.3,
                clearProps: 'transform'
            });
        }
    }


    // ════════════════════════════════════════════════════════════
    //  11. ACTIVE NAV LINK TRACKING
    // ════════════════════════════════════════════════════════════
    function initActiveNavTracking() {
        const sections = $$('.section[id]');
        if (!sections.length) return;

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const id = entry.target.getAttribute('id');
                    DOM.navbarLinks.querySelectorAll('a').forEach(link => {
                        link.classList.toggle('active', link.getAttribute('data-nav') === id);
                    });
                }
            });
        }, {
            threshold: 0.3,
            rootMargin: '-64px 0px -40% 0px'
        });

        sections.forEach(section => observer.observe(section));
    }


    // ════════════════════════════════════════════════════════════
    //  12. INTERACTIVE CARD TILT (Desktop only)
    // ════════════════════════════════════════════════════════════
    function initCardTilt() {
        if (window.innerWidth <= 900) return;

        const cards = $$('.dev-card, .project-card, .skill-card, .stat-card');
        cards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left - rect.width / 2;
                const y = e.clientY - rect.top - rect.height / 2;
                const tiltX = (y / (rect.height / 2)) * -3;
                const tiltY = (x / (rect.width / 2)) * 3;
                card.style.transform = `perspective(800px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) translateY(-2px)`;
            });

            card.addEventListener('mouseleave', () => {
                card.style.transform = '';
            });
        });
    }


    // ════════════════════════════════════════════════════════════
    //  INIT
    // ════════════════════════════════════════════════════════════
    initScrollReveal();
    initSkillBars();
    initActiveNavTracking();
    initCardTilt();
    initGSAP();
    initAuthListener();

    console.log('🚀 bekirr.dev — Portfolio + Auth Engine Active.');
});
