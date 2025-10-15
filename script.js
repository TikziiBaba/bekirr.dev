document.addEventListener('DOMContentLoaded', () => {

    // --- THEME INITIALIZATION ---
    const themeToggle = document.getElementById('theme-toggle');

    function applyTheme(theme) {
        if (theme === 'dark') {
            document.body.classList.add('dark-theme');
            themeToggle.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><path d="M6.76 4.84l-1.8-1.79L3.17 4.84l1.79 1.8 1.8-1.8zM1 13h3v-2H1v2zm10-9h2V1h-2v3zm7.03 1.05l1.79-1.8-1.79-1.79-1.8 1.79 1.8 1.8zM20 11v2h3v-2h-3zM11 20v3h2v-3h-2zm6.24-2.76l1.8 1.79 1.79-1.79-1.79-1.8-1.8 1.8zM4.22 19.78l1.79-1.79-1.79-1.8-1.8 1.8 1.8 1.79zM12 6a6 6 0 100 12 6 6 0 000-12z" fill="currentColor"/></svg>';
            themeToggle.setAttribute('aria-pressed', 'true');
            themeToggle.setAttribute('aria-label', 'Açık tema');
        } else {
            document.body.classList.remove('dark-theme');
            themeToggle.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" fill="currentColor"/></svg>';
            themeToggle.setAttribute('aria-pressed', 'false');
            themeToggle.setAttribute('aria-label', 'Koyu tema');
        }
    }

    function detectPreferredTheme() {
        const saved = localStorage.getItem('site-theme');
        if (saved === 'dark' || saved === 'light') return saved;
        // fallback to system preference
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark';
        return 'light';
    }

    // initialize
    const initialTheme = detectPreferredTheme();
    applyTheme(initialTheme);

    // toggle handler
    themeToggle.addEventListener('click', () => {
        const now = document.body.classList.contains('dark-theme') ? 'light' : 'dark';
        applyTheme(now);
        localStorage.setItem('site-theme', now);
    });


    // No loader grid animation — show content immediately and set up scroll animations
    const mainContent = document.querySelector('.main-content');
    if (mainContent) mainContent.style.opacity = '1';
    // register scroll animations immediately
    setupScrollAnimations();


    // --- 2. KAYDIRMA ANİMASYONLARI ---
    function setupScrollAnimations() {
        gsap.registerPlugin(ScrollTrigger);
        ScrollTrigger.refresh();

        gsap.from(".hero h1, .hero p", {
            scrollTrigger: { trigger: ".hero", start: "top 80%", toggleActions: "play none none none" },
            opacity: 0, y: 50, duration: 1, stagger: 0.2
        });
        gsap.from(".intro h2", {
            scrollTrigger: { trigger: ".intro", start: "top 70%", toggleActions: "play none none none" },
            opacity: 0, y: 50, duration: 1
        });
        gsap.from(".project-item", {
            scrollTrigger: { trigger: ".projects", start: "top 70%", toggleActions: "play none none none" },
            opacity: 0, y: 50, duration: 0.8, stagger: 0.2
        });
        gsap.from(".contact h2, .contact-links a", {
            scrollTrigger: { trigger: ".contact", start: "top 70%", toggleActions: "play none none none" },
            opacity: 0, y: 50, duration: 1, stagger: 0.2
        });
    }

});
