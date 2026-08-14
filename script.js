// ============================================
// bekirr.dev — Main Application Script
// Portfolio + Supabase Auth + Dashboard
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // ── State ──
    let currentUser = null;
    let currentView = 'portfolio'; // 'portfolio' | 'dashboard'

    // ── Admin Config ──
    const ADMIN_EMAIL = 'dedyusuf99@gmail.com';
    function isAdmin() {
        return currentUser?.email === ADMIN_EMAIL;
    }

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
        chatView: $('#chatView'),
        // Dashboard
        dashboardUserName: $('#dashboardUserName'),
        profileName: $('#profileName'),
        profileEmail: $('#profileEmail'),
        profileCreatedAt: $('#profileCreatedAt'),
        profileLastSignIn: $('#profileLastSignIn'),
        profileSessionExpiry: $('#profileSessionExpiry'),
        profileBio: $('#profileBio'),
        btnBackToSite: $('#btnBackToSite'),
        btnDashboardLogout: $('#btnDashboardLogout'),
        // Profile Edit Form
        profileEditForm: $('#profileEditForm'),
        profileEditName: $('#profileEditName'),
        profileEditBioInput: $('#profileEditBioInput'),
        avatarPicker: $('#avatarPicker'),
        btnSaveProfile: $('#btnSaveProfile'),
        // Password Update Form
        passwordUpdateForm: $('#passwordUpdateForm'),
        profileNewPassword: $('#profileNewPassword'),
        profileConfirmPassword: $('#profileConfirmPassword'),
        btnUpdatePassword: $('#btnUpdatePassword'),
        // Contact form
        contactForm: $('#contactForm'),
        contactSubmit: $('#contactSubmit'),
        // Scroll
        scrollProgress: $('#scroll-progress'),
        // Toast
        toastContainer: $('#toastContainer'),
        // Live Chat
        navChatBtn: $('#navChatBtn'),
        chatFab: $('#chatFab'),
        chatFabIcon: $('#chatFabIcon'),
        chatPanel: $('#chatPanel'),
        chatPanelClose: $('#chatPanelClose'),
        chatMessages: $('#chatMessages'),
        chatWelcome: $('#chatWelcome'),
        chatInputArea: $('#chatInputArea'),
        chatInput: $('#chatInput'),
        chatSendBtn: $('#chatSendBtn'),
        chatCharCount: $('#chatCharCount'),
        chatLoginPrompt: $('#chatLoginPrompt'),
        chatLoginBtn: $('#chatLoginBtn'),
        chatOnlineText: $('#chatOnlineText'),
        chatUnreadBadge: $('#chatUnreadBadge'),
        btnExpandChat: $('#btnExpandChat'),
        // Full Page Chat View
        btnChatBackToSite: $('#btnChatBackToSite'),
        chatPageOnlineCount: $('#chatPageOnlineCount'),
        chatPageOnlineText: $('#chatPageOnlineText'),
        chatSearchInput: $('#chatSearchInput'),
        btnToggleSound: $('#btnToggleSound'),
        soundIcon: $('#soundIcon'),
        chatPageClearBtn: $('#chatPageClearBtn'),
        chatPageMessages: $('#chatPageMessages'),
        chatPageWelcome: $('#chatPageWelcome'),
        chatPageInputArea: $('#chatPageInputArea'),
        chatPageInput: $('#chatPageInput'),
        chatPageSendBtn: $('#chatPageSendBtn'),
        chatPageCharCount: $('#chatPageCharCount'),
        chatPageLoginPrompt: $('#chatPageLoginPrompt'),
        chatPageLoginBtn: $('#chatPageLoginBtn'),
        chatPageRegisterBtn: $('#chatPageRegisterBtn'),
        chatUsersList: $('#chatUsersList'),
        // Admin
        adminPanel: $('#adminPanel'),
        adminTotalUsers: $('#adminTotalUsers'),
        adminTotalMessages: $('#adminTotalMessages'),
        adminTodayMessages: $('#adminTodayMessages'),
        adminUserTableBody: $('#adminUserTableBody'),
        adminUserSearch: $('#adminUserSearch'),
        adminClearChat: $('#adminClearChat'),
        adminEditOverlay: $('#adminEditOverlay'),
        adminEditForm: $('#adminEditForm'),
        adminEditClose: $('#adminEditClose'),
        adminEditCancel: $('#adminEditCancel'),
        adminEditUserId: $('#adminEditUserId'),
        adminEditName: $('#adminEditName'),
        adminEditEmail: $('#adminEditEmail'),
        adminEditRole: $('#adminEditRole'),
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
            const avatar = currentUser.user_metadata?.avatar_icon || name.charAt(0).toUpperCase();

            DOM.userNavName.textContent = name;
            DOM.userNavAvatar.textContent = avatar;

            // Show chat inputs, hide login prompts
            if (DOM.chatInputArea) DOM.chatInputArea.style.display = 'block';
            if (DOM.chatLoginPrompt) DOM.chatLoginPrompt.style.display = 'none';
            if (DOM.chatPageInputArea) DOM.chatPageInputArea.style.display = 'block';
            if (DOM.chatPageLoginPrompt) DOM.chatPageLoginPrompt.style.display = 'none';
        } else {
            DOM.authButtons.style.display = 'flex';
            DOM.userNav.style.display = 'none';

            // Hide chat inputs, show login prompts
            if (DOM.chatInputArea) DOM.chatInputArea.style.display = 'none';
            if (DOM.chatLoginPrompt) DOM.chatLoginPrompt.style.display = 'flex';
            if (DOM.chatPageInputArea) DOM.chatPageInputArea.style.display = 'none';
            if (DOM.chatPageLoginPrompt) DOM.chatPageLoginPrompt.style.display = 'flex';
        }
    }

    function switchView(view) {
        currentView = view;

        DOM.portfolioView?.classList.add('hidden');
        DOM.dashboardView?.classList.remove('active');
        DOM.chatView?.classList.remove('active');

        if (DOM.navChatBtn) {
            DOM.navChatBtn.classList.toggle('active', view === 'chat');
        }

        if (view === 'dashboard') {
            DOM.dashboardView?.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else if (view === 'chat') {
            DOM.chatView?.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            if (window._initFullChatPage) {
                window._initFullChatPage();
            }
        } else {
            DOM.portfolioView?.classList.remove('hidden');
        }
    }

    // Navigation Events
    DOM.navChatBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        switchView('chat');
    });

    DOM.userNavAvatar?.addEventListener('click', () => {
        if (currentUser) switchView('dashboard');
    });

    DOM.btnBackToSite?.addEventListener('click', () => {
        switchView('portfolio');
    });

    DOM.btnChatBackToSite?.addEventListener('click', () => {
        switchView('portfolio');
    });

    DOM.chatPageLoginBtn?.addEventListener('click', () => {
        openAuthModal('login');
    });

    DOM.chatPageRegisterBtn?.addEventListener('click', () => {
        openAuthModal('register');
    });

    function updateDashboard(session) {
        if (!session?.user) return;
        const user = session.user;
        const name = user.user_metadata?.full_name || user.email?.split('@')[0] || 'Kullanıcı';
        const bio = user.user_metadata?.bio || 'Geliştirici';
        const avatar = user.user_metadata?.avatar_icon || '👨‍💻';

        DOM.dashboardUserName.textContent = name;
        DOM.profileName.textContent = name;
        DOM.profileEmail.textContent = user.email || '—';
        DOM.profileCreatedAt.textContent = formatDate(user.created_at);
        DOM.profileLastSignIn.textContent = formatDate(user.last_sign_in_at);
        if (DOM.profileBio) DOM.profileBio.textContent = bio;

        if (DOM.profileEditName) DOM.profileEditName.value = name;
        if (DOM.profileEditBioInput) DOM.profileEditBioInput.value = bio;

        if (DOM.avatarPicker) {
            DOM.avatarPicker.querySelectorAll('.avatar-opt').forEach(opt => {
                opt.classList.toggle('active', opt.getAttribute('data-avatar') === avatar);
            });
        }

        if (session.expires_at) {
            const expiryDate = new Date(session.expires_at * 1000);
            DOM.profileSessionExpiry.textContent = formatDate(expiryDate.toISOString());
        } else {
            DOM.profileSessionExpiry.textContent = '—';
        }

        if (isAdmin() && DOM.adminPanel) {
            DOM.adminPanel.style.display = 'block';
            if (DOM.chatPageClearBtn) DOM.chatPageClearBtn.style.display = 'inline-block';
            loadAdminData();
        } else if (DOM.adminPanel) {
            DOM.adminPanel.style.display = 'none';
            if (DOM.chatPageClearBtn) DOM.chatPageClearBtn.style.display = 'none';
        }
    }

    // Avatar Picker Choice Listener
    let selectedAvatarIcon = '👨‍💻';
    DOM.avatarPicker?.querySelectorAll('.avatar-opt').forEach(opt => {
        opt.addEventListener('click', () => {
            DOM.avatarPicker.querySelectorAll('.avatar-opt').forEach(b => b.classList.remove('active'));
            opt.classList.add('active');
            selectedAvatarIcon = opt.getAttribute('data-avatar');
        });
    });

    // Profile Edit Form Submit
    DOM.profileEditForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!currentUser) return;

        const newName = DOM.profileEditName.value.trim();
        const newBio = DOM.profileEditBioInput.value.trim();

        if (!newName) {
            showToast('Lütfen ad ve soyadınızı girin.', 'error');
            return;
        }

        const activeAvatarBtn = DOM.avatarPicker?.querySelector('.avatar-opt.active');
        if (activeAvatarBtn) {
            selectedAvatarIcon = activeAvatarBtn.getAttribute('data-avatar');
        }

        const submitBtn = DOM.btnSaveProfile;
        setButtonLoading(submitBtn, true);

        try {
            const { data, error } = await supabaseClient.auth.updateUser({
                data: {
                    full_name: newName,
                    bio: newBio,
                    avatar_icon: selectedAvatarIcon
                }
            });

            if (error) {
                showToast('Profil güncellenemedi: ' + error.message, 'error');
            } else {
                if (data.user) currentUser = data.user;

                // Sync with profiles table
                try {
                    await supabaseClient.from('profiles').upsert({
                        id: currentUser.id,
                        full_name: newName,
                        bio: newBio,
                        avatar_icon: selectedAvatarIcon,
                        email: currentUser.email,
                        updated_at: new Date().toISOString()
                    });
                } catch (pErr) {
                    console.warn('Profiles table sync warning:', pErr);
                }

                updateUIForAuth(true);
                updateDashboard({ user: currentUser });
                if (window._renderChatParticipants) window._renderChatParticipants();

                showToast('Profiliniz başarıyla güncellendi!', 'success');
            }
        } catch (err) {
            console.error('Profile update error:', err);
            showToast('Bir hata oluştu.', 'error');
        }

        setButtonLoading(submitBtn, false);
    });

    // Password Update Form Submit
    DOM.passwordUpdateForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!currentUser) return;

        const newPass = DOM.profileNewPassword.value.trim();
        const confirmPass = DOM.profileConfirmPassword.value.trim();

        if (!newPass || newPass.length < 6) {
            showToast('Şifre en az 6 karakter olmalıdır.', 'error');
            return;
        }

        if (newPass !== confirmPass) {
            showToast('Şifreler birbiriyle eşleşmiyor.', 'error');
            return;
        }

        const submitBtn = DOM.btnUpdatePassword;
        setButtonLoading(submitBtn, true);

        try {
            const { error } = await supabaseClient.auth.updateUser({
                password: newPass
            });

            if (error) {
                showToast('Şifre güncellenemedi: ' + getAuthErrorMessage(error), 'error');
            } else {
                showToast('Şifreniz başarıyla değiştirildi!', 'success');
                DOM.passwordUpdateForm.reset();
            }
        } catch (err) {
            console.error('Password update error:', err);
            showToast('Şifre güncellenirken hata oluştu.', 'error');
        }

        setButtonLoading(submitBtn, false);
    });

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
    //  13. LIVE CHAT ENGINE & DEDICATED PAGE
    // ════════════════════════════════════════════════════════════
    function initLiveChat() {
        if (!DOM.chatFab || !DOM.chatPanel) return;

        let chatOpen = false;
        let chatChannel = null;
        let messagesLoaded = false;
        let unreadCount = 0;
        let chatSoundEnabled = true;
        const renderedMsgIds = new Set();

        // Audio chime notification
        function playMessageSound() {
            if (!chatSoundEnabled) return;
            try {
                const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.type = 'sine';
                osc.frequency.setValueAtTime(587.33, audioCtx.currentTime);
                osc.frequency.exponentialRampToValueAtTime(880, audioCtx.currentTime + 0.1);
                gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.25);
                osc.connect(gain);
                gain.connect(audioCtx.destination);
                osc.start();
                osc.stop(audioCtx.currentTime + 0.25);
            } catch (e) {
                console.warn('Audio synth failed:', e);
            }
        }

        // Sound toggle
        DOM.btnToggleSound?.addEventListener('click', () => {
            chatSoundEnabled = !chatSoundEnabled;
            if (DOM.soundIcon) DOM.soundIcon.textContent = chatSoundEnabled ? '🔊' : '🔇';
            showToast(`Sohbet sesleri ${chatSoundEnabled ? 'açıldı' : 'kapatıldı'}.`, 'info');
        });

        // Expand chat to full page
        DOM.btnExpandChat?.addEventListener('click', () => {
            if (chatOpen) toggleChat();
            switchView('chat');
        });

        // Admin clear chat button in full page
        DOM.chatPageClearBtn?.addEventListener('click', async () => {
            if (!confirm('Tüm sohbet mesajlarını silmek istediğinden emin misin?')) return;
            await clearAllChatMessages();
        });

        // Toggle Floating Chat Panel
        function toggleChat() {
            chatOpen = !chatOpen;

            if (chatOpen) {
                DOM.chatPanel.classList.add('open');
                DOM.chatFab.classList.add('active');
                DOM.chatFabIcon.textContent = '✕';

                unreadCount = 0;
                if (DOM.chatUnreadBadge) DOM.chatUnreadBadge.style.display = 'none';

                if (!messagesLoaded) {
                    loadMessages();
                    subscribeToMessages();
                    messagesLoaded = true;
                }

                if (currentUser && DOM.chatInput) {
                    setTimeout(() => DOM.chatInput.focus(), 300);
                }
            } else {
                DOM.chatPanel.classList.remove('open');
                DOM.chatFab.classList.remove('active');
                DOM.chatFabIcon.textContent = '💬';
            }
        }

        DOM.chatFab.addEventListener('click', toggleChat);
        DOM.chatPanelClose?.addEventListener('click', toggleChat);

        DOM.chatLoginBtn?.addEventListener('click', () => {
            toggleChat();
            openAuthModal('login');
        });

        // Full Page Chat Initialization Expose
        window._initFullChatPage = function() {
            if (!messagesLoaded) {
                loadMessages();
                subscribeToMessages();
                messagesLoaded = true;
            }
            renderChatParticipants();
        };

        // Render Active Participants list
        function renderChatParticipants() {
            if (!DOM.chatUsersList) return;
            const participants = [
                { name: 'Bekir Kaplan', status: 'Geliştirici 👑', avatar: '👨‍💻' }
            ];

            if (currentUser) {
                const uName = currentUser.user_metadata?.full_name || currentUser.email?.split('@')[0] || 'Anonim';
                const uBio = currentUser.user_metadata?.bio || 'Üye';
                const uAvatar = currentUser.user_metadata?.avatar_icon || '⚡';
                if (uName !== 'Bekir Kaplan') {
                    participants.push({ name: uName, status: uBio, avatar: uAvatar });
                }
            }

            DOM.chatUsersList.innerHTML = participants.map(p => `
                <div class="chat-user-item">
                    <div class="chat-user-avatar">${p.avatar}</div>
                    <div class="chat-user-info">
                        <span class="chat-user-name">${escapeHTML(p.name)}</span>
                        <span class="chat-user-status">${escapeHTML(p.status)}</span>
                    </div>
                </div>
            `).join('');
        }
        window._renderChatParticipants = renderChatParticipants;

        // Character Counters
        function setupCharCounter(inputEl, counterEl, sendBtnEl) {
            if (!inputEl || !counterEl) return;
            inputEl.addEventListener('input', () => {
                const len = inputEl.value.length;
                counterEl.textContent = `${len} / 500`;
                if (sendBtnEl) sendBtnEl.disabled = len === 0;

                counterEl.className = 'chat-char-count';
                if (len >= 450) {
                    counterEl.classList.add('limit');
                } else if (len >= 350) {
                    counterEl.classList.add('warn');
                }
            });
        }

        setupCharCounter(DOM.chatInput, DOM.chatCharCount, DOM.chatSendBtn);
        setupCharCounter(DOM.chatPageInput, DOM.chatPageCharCount, DOM.chatPageSendBtn);

        // Emoji pills click handling
        document.querySelectorAll('.chat-emoji-pill').forEach(pill => {
            pill.addEventListener('click', () => {
                const emoji = pill.getAttribute('data-emoji');
                if (DOM.chatPageInput) {
                    DOM.chatPageInput.value += emoji;
                    DOM.chatPageInput.focus();
                    DOM.chatPageInput.dispatchEvent(new Event('input'));
                }
            });
        });

        // Search Filter in Full Page Chat
        DOM.chatSearchInput?.addEventListener('input', () => {
            const query = DOM.chatSearchInput.value.toLowerCase().trim();
            const msgs = DOM.chatPageMessages?.querySelectorAll('.chat-msg');
            msgs?.forEach(msgEl => {
                const text = msgEl.textContent.toLowerCase();
                msgEl.style.display = text.includes(query) ? 'flex' : 'none';
            });
        });

        // Send Message Handler
        async function sendMessage(inputEl, sendBtnEl, charCountEl) {
            if (!currentUser || !inputEl) return;
            const message = inputEl.value.trim();
            if (!message || message.length > 500) return;
            if (!isSupabaseConfigured()) return;

            if (message === '.clear' && isAdmin()) {
                inputEl.value = '';
                if (charCountEl) charCountEl.textContent = '0 / 500';
                if (sendBtnEl) sendBtnEl.disabled = true;
                await clearAllChatMessages();
                return;
            }

            const userName = currentUser.user_metadata?.full_name || currentUser.email?.split('@')[0] || 'Anonim';
            const userAvatar = currentUser.user_metadata?.avatar_icon || '👨‍💻';

            if (sendBtnEl) sendBtnEl.disabled = true;
            inputEl.value = '';
            if (charCountEl) {
                charCountEl.textContent = '0 / 500';
                charCountEl.className = 'chat-char-count';
            }

            const optimisticMsg = {
                id: 'temp-' + Date.now(),
                user_id: currentUser.id,
                user_name: userName,
                avatar_icon: userAvatar,
                message: message,
                created_at: new Date().toISOString()
            };

            if (DOM.chatWelcome) DOM.chatWelcome.style.display = 'none';
            if (DOM.chatPageWelcome) DOM.chatPageWelcome.style.display = 'none';
            renderMessage(optimisticMsg);
            scrollToBottom();

            try {
                const { data, error } = await supabaseClient
                    .from('chat_messages')
                    .insert([{
                        user_id: currentUser.id,
                        user_name: userName,
                        avatar_icon: userAvatar,
                        message: message
                    }])
                    .select()
                    .single();

                if (error) {
                    console.error('Chat send error:', error.message);
                    showToast('Mesaj gönderilemedi.', 'error');
                    removeTempMessage(optimisticMsg.id);
                    inputEl.value = message;
                    if (sendBtnEl) sendBtnEl.disabled = false;
                } else if (data) {
                    replaceTempMessageId(optimisticMsg.id, data.id);
                }
            } catch (err) {
                console.error('Chat send error:', err);
                showToast('Mesaj gönderilemedi.', 'error');
                removeTempMessage(optimisticMsg.id);
                inputEl.value = message;
                if (sendBtnEl) sendBtnEl.disabled = false;
            }
        }

        function removeTempMessage(tempId) {
            DOM.chatMessages?.querySelector(`[data-msg-id="${tempId}"]`)?.remove();
            DOM.chatPageMessages?.querySelector(`[data-msg-id="${tempId}"]`)?.remove();
            renderedMsgIds.delete(tempId);
        }

        function replaceTempMessageId(tempId, realId) {
            const el1 = DOM.chatMessages?.querySelector(`[data-msg-id="${tempId}"]`);
            if (el1) el1.setAttribute('data-msg-id', realId);
            const el2 = DOM.chatPageMessages?.querySelector(`[data-msg-id="${tempId}"]`);
            if (el2) el2.setAttribute('data-msg-id', realId);
            renderedMsgIds.delete(tempId);
            renderedMsgIds.add(realId);
        }

        DOM.chatSendBtn?.addEventListener('click', () => sendMessage(DOM.chatInput, DOM.chatSendBtn, DOM.chatCharCount));
        DOM.chatInput?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage(DOM.chatInput, DOM.chatSendBtn, DOM.chatCharCount);
            }
        });

        DOM.chatPageSendBtn?.addEventListener('click', () => sendMessage(DOM.chatPageInput, DOM.chatPageSendBtn, DOM.chatPageCharCount));
        DOM.chatPageInput?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage(DOM.chatPageInput, DOM.chatPageSendBtn, DOM.chatPageCharCount);
            }
        });

        // Load Messages
        async function loadMessages() {
            if (!isSupabaseConfigured()) {
                if (DOM.chatOnlineText) DOM.chatOnlineText.textContent = 'Bağlantı yok';
                if (DOM.chatPageOnlineText) DOM.chatPageOnlineText.textContent = 'Bağlantı yok';
                return;
            }

            const loadingHTML = `
                <div class="chat-loading">
                    <span class="chat-loading-dot"></span>
                    <span class="chat-loading-dot"></span>
                    <span class="chat-loading-dot"></span>
                </div>
            `;
            if (DOM.chatWelcome) DOM.chatWelcome.innerHTML = loadingHTML;
            if (DOM.chatPageWelcome) DOM.chatPageWelcome.innerHTML = loadingHTML;

            try {
                const { data, error } = await supabaseClient
                    .from('chat_messages')
                    .select('*')
                    .order('created_at', { ascending: true })
                    .limit(50);

                if (error) {
                    console.error('Chat load error:', error.message);
                    const errHTML = `
                        <span class="chat-welcome-icon">⚠️</span>
                        <p>Mesajlar yüklenemedi.<br><small style="color:var(--text-muted)">${error.message}</small></p>
                    `;
                    if (DOM.chatWelcome) DOM.chatWelcome.innerHTML = errHTML;
                    if (DOM.chatPageWelcome) DOM.chatPageWelcome.innerHTML = errHTML;
                    if (DOM.chatOnlineText) DOM.chatOnlineText.textContent = 'Bağlantı hatası';
                    if (DOM.chatPageOnlineText) DOM.chatPageOnlineText.textContent = 'Bağlantı hatası';
                    return;
                }

                if (data && data.length > 0) {
                    if (DOM.chatWelcome) DOM.chatWelcome.style.display = 'none';
                    if (DOM.chatPageWelcome) DOM.chatPageWelcome.style.display = 'none';
                    data.forEach(msg => renderMessage(msg));
                    scrollToBottom();
                } else {
                    const emptyHTML = `
                        <span class="chat-welcome-icon">🚀</span>
                        <p>Henüz mesaj yok. İlk mesajı sen gönder!</p>
                    `;
                    if (DOM.chatWelcome) DOM.chatWelcome.innerHTML = emptyHTML;
                    if (DOM.chatPageWelcome) DOM.chatPageWelcome.innerHTML = emptyHTML;
                }

                if (DOM.chatOnlineText) DOM.chatOnlineText.textContent = 'Aktif';
                if (DOM.chatPageOnlineText) DOM.chatPageOnlineText.textContent = 'Aktif';
            } catch (err) {
                console.error('Chat load error:', err);
                if (DOM.chatOnlineText) DOM.chatOnlineText.textContent = 'Hata';
                if (DOM.chatPageOnlineText) DOM.chatPageOnlineText.textContent = 'Hata';
            }
        }

        // Render Message in Dual Panels
        function renderMessage(msg, isRealtime = false) {
            if (msg.id && renderedMsgIds.has(msg.id)) return;
            if (msg.id) renderedMsgIds.add(msg.id);

            const isOwn = currentUser && msg.user_id === currentUser.id;
            const time = new Date(msg.created_at || Date.now()).toLocaleTimeString('tr-TR', {
                hour: '2-digit',
                minute: '2-digit'
            });

            const safeMsg = escapeHTML(msg.message || '');
            const safeName = escapeHTML(msg.user_name || 'Anonim');
            const avatar = msg.avatar_icon || (isOwn ? (currentUser?.user_metadata?.avatar_icon || '👨‍💻') : '👤');

            const contentHTML = `
                <div class="chat-msg-header">
                    <span class="chat-msg-user-avatar">${avatar}</span>
                    <span class="chat-msg-name">${isOwn ? 'Sen' : safeName}</span>
                    <span class="chat-msg-time">${time}</span>
                </div>
                <div class="chat-msg-bubble">${safeMsg}</div>
            `;

            const createMsgNode = () => {
                const el = document.createElement('div');
                el.className = `chat-msg ${isOwn ? 'own' : 'other'}`;
                if (msg.id) el.setAttribute('data-msg-id', msg.id);
                el.innerHTML = contentHTML;
                return el;
            };

            if (DOM.chatMessages) DOM.chatMessages.appendChild(createMsgNode());
            if (DOM.chatPageMessages) DOM.chatPageMessages.appendChild(createMsgNode());

            if (isRealtime && !isOwn) {
                playMessageSound();
            }
        }

        // Subscribe to Realtime Messages
        function subscribeToMessages() {
            if (!isSupabaseConfigured()) return;

            chatChannel = supabaseClient
                .channel('chat-room')
                .on(
                    'postgres_changes',
                    {
                        event: 'INSERT',
                        schema: 'public',
                        table: 'chat_messages'
                    },
                    (payload) => {
                        const msg = payload.new;
                        if (!msg) return;

                        if (msg.id && renderedMsgIds.has(msg.id)) return;

                        if (DOM.chatWelcome) DOM.chatWelcome.style.display = 'none';
                        if (DOM.chatPageWelcome) DOM.chatPageWelcome.style.display = 'none';

                        renderMessage(msg, true);
                        scrollToBottom();

                        if (!chatOpen && currentView !== 'chat') {
                            unreadCount++;
                            if (DOM.chatUnreadBadge) {
                                DOM.chatUnreadBadge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                                DOM.chatUnreadBadge.style.display = 'flex';
                            }
                        }
                    }
                )
                .subscribe((status) => {
                    console.log('Chat Realtime status:', status);
                    const statusText = status === 'SUBSCRIBED' ? 'Aktif' : 'Bağlantı koptu';
                    if (DOM.chatOnlineText) DOM.chatOnlineText.textContent = statusText;
                    if (DOM.chatPageOnlineText) DOM.chatPageOnlineText.textContent = statusText;
                });
        }

        function scrollToBottom() {
            requestAnimationFrame(() => {
                if (DOM.chatMessages) DOM.chatMessages.scrollTop = DOM.chatMessages.scrollHeight;
                if (DOM.chatPageMessages) DOM.chatPageMessages.scrollTop = DOM.chatPageMessages.scrollHeight;
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && chatOpen) {
                toggleChat();
            }
        });
    }


    // ════════════════════════════════════════════════════════════
    //  14. ADMIN PANEL
    // ════════════════════════════════════════════════════════════
    let allUsersCache = [];

    async function loadAdminData() {
        if (!isAdmin() || !isSupabaseConfigured()) return;

        // Load stats
        try {
            const [usersRes, msgsRes] = await Promise.all([
                supabaseClient.from('profiles').select('*', { count: 'exact' }),
                supabaseClient.from('chat_messages').select('*', { count: 'exact' })
            ]);

            if (usersRes.data) {
                DOM.adminTotalUsers.textContent = usersRes.count || usersRes.data.length;
                allUsersCache = usersRes.data;
                renderUserTable(allUsersCache);
            }

            if (msgsRes.data) {
                DOM.adminTotalMessages.textContent = msgsRes.count || msgsRes.data.length;
                // Count today's messages
                const today = new Date().toISOString().split('T')[0];
                const todayMsgs = msgsRes.data.filter(m => m.created_at?.startsWith(today));
                DOM.adminTodayMessages.textContent = todayMsgs.length;
            }
        } catch (err) {
            console.error('Admin data load error:', err);
        }
    }

    function renderUserTable(users) {
        if (!DOM.adminUserTableBody) return;

        if (!users || users.length === 0) {
            DOM.adminUserTableBody.innerHTML = `
                <tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:40px;">Kullanıcı bulunamadı.</td></tr>
            `;
            return;
        }

        DOM.adminUserTableBody.innerHTML = users.map(user => {
            const name = user.full_name || user.email?.split('@')[0] || 'Anonim';
            const initial = name.charAt(0).toUpperCase();
            const role = user.role || 'user';
            const isAdminUser = role === 'admin';
            const date = user.created_at ? new Date(user.created_at).toLocaleDateString('tr-TR', {
                year: 'numeric', month: 'short', day: 'numeric'
            }) : '—';

            return `
                <tr data-user-id="${user.id}">
                    <td>
                        <div class="admin-user-cell">
                            <div class="admin-user-avatar">${initial}</div>
                            <span class="admin-user-name">${escapeHTML(name)}</span>
                        </div>
                    </td>
                    <td>${escapeHTML(user.email || '—')}</td>
                    <td>${date}</td>
                    <td>
                        <span class="admin-role-badge ${isAdminUser ? 'role-admin' : 'role-user'}">
                            ${isAdminUser ? '🛡️ Admin' : 'Kullanıcı'}
                        </span>
                    </td>
                    <td>
                        <div class="admin-actions">
                            <button class="admin-btn" onclick="window._adminEditUser('${user.id}')">Düzenle</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function escapeHTML(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    // — Clear all chat messages (Admin) —
    async function clearAllChatMessages() {
        if (!isAdmin() || !isSupabaseConfigured()) return;

        try {
            const { error } = await supabaseClient
                .from('chat_messages')
                .delete()
                .neq('id', '00000000-0000-0000-0000-000000000000'); // Delete all rows

            if (error) {
                console.error('Clear chat error:', error.message);
                showToast('Sohbet temizlenemedi: ' + error.message, 'error');
                return;
            }

            // Clear UI
            if (DOM.chatMessages) {
                DOM.chatMessages.querySelectorAll('.chat-msg').forEach(el => el.remove());
                if (DOM.chatWelcome) {
                    DOM.chatWelcome.style.display = 'flex';
                    DOM.chatWelcome.innerHTML = `
                        <span class="chat-welcome-icon">🧹</span>
                        <p>Sohbet temizlendi.</p>
                    `;
                }
            }

            showToast('Tüm sohbet mesajları temizlendi.', 'success');
        } catch (err) {
            console.error('Clear chat error:', err);
            showToast('Sohbet temizlenirken hata oluştu.', 'error');
        }
    }

    // — Admin Edit User —
    window._adminEditUser = function(userId) {
        const user = allUsersCache.find(u => u.id === userId);
        if (!user) return;

        DOM.adminEditUserId.value = user.id;
        DOM.adminEditName.value = user.full_name || '';
        DOM.adminEditEmail.value = user.email || '';
        DOM.adminEditRole.value = user.role || 'user';

        DOM.adminEditOverlay.classList.add('active');
    };

    function closeAdminEditModal() {
        DOM.adminEditOverlay?.classList.remove('active');
    }

    DOM.adminEditClose?.addEventListener('click', closeAdminEditModal);
    DOM.adminEditCancel?.addEventListener('click', closeAdminEditModal);
    DOM.adminEditOverlay?.addEventListener('click', (e) => {
        if (e.target === DOM.adminEditOverlay) closeAdminEditModal();
    });

    // — Save edited user —
    DOM.adminEditForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!isAdmin() || !isSupabaseConfigured()) return;

        const userId = DOM.adminEditUserId.value;
        const newName = DOM.adminEditName.value.trim();
        const newRole = DOM.adminEditRole.value;

        if (!userId || !newName) {
            showToast('Ad soyad boş olamaz.', 'error');
            return;
        }

        try {
            const { error } = await supabaseClient
                .from('profiles')
                .update({ full_name: newName, role: newRole })
                .eq('id', userId);

            if (error) {
                showToast('Güncelleme hatası: ' + error.message, 'error');
                return;
            }

            showToast(`${newName} başarıyla güncellendi.`, 'success');
            closeAdminEditModal();
            loadAdminData(); // Refresh table
        } catch (err) {
            showToast('Güncelleme hatası.', 'error');
        }
    });

    // — Admin search filter —
    DOM.adminUserSearch?.addEventListener('input', () => {
        const query = DOM.adminUserSearch.value.toLowerCase().trim();
        if (!query) {
            renderUserTable(allUsersCache);
            return;
        }
        const filtered = allUsersCache.filter(u =>
            (u.full_name || '').toLowerCase().includes(query) ||
            (u.email || '').toLowerCase().includes(query)
        );
        renderUserTable(filtered);
    });

    // — Admin clear chat button —
    DOM.adminClearChat?.addEventListener('click', async () => {
        if (!confirm('Tüm sohbet mesajlarını silmek istediğinden emin misin?')) return;
        await clearAllChatMessages();
        loadAdminData(); // Refresh stats
    });


    // ════════════════════════════════════════════════════════════
    //  INIT
    // ════════════════════════════════════════════════════════════
    initScrollReveal();
    initSkillBars();
    initActiveNavTracking();
    initCardTilt();
    initGSAP();
    initAuthListener();
    initLiveChat();

    console.log('🚀 bekirr.dev — Portfolio + Auth + Live Chat + Admin Engine Active.');
});
