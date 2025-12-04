document.addEventListener('DOMContentLoaded', () => {

    // --- CUSTOM CURSOR ---
    const cursor = document.createElement('div');
    cursor.className = 'custom-cursor';
    document.body.appendChild(cursor);

    const cursorDot = document.createElement('div');
    cursorDot.className = 'custom-cursor-dot';
    document.body.appendChild(cursorDot);

    let mouseX = 0;
    let mouseY = 0;
    let cursorX = 0;
    let cursorY = 0;
    let dotX = 0;
    let dotY = 0;

    // Update mouse position
    document.addEventListener('mousemove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    // Animate cursor
    function animateCursor() {
        // Smooth cursor ring
        cursorX += (mouseX - cursorX) * 0.15;
        cursorY += (mouseY - cursorY) * 0.15;
        cursor.style.left = cursorX + 'px';
        cursor.style.top = cursorY + 'px';

        // Immediate cursor dot
        dotX += (mouseX - dotX) * 0.5;
        dotY += (mouseY - dotY) * 0.5;
        cursorDot.style.left = dotX + 'px';
        cursorDot.style.top = dotY + 'px';

        requestAnimationFrame(animateCursor);
    }
    animateCursor();

    // Hover effects for interactive elements
    const hoverElements = document.querySelectorAll('a, button, .project-item, .theme-toggle, .brand');
    hoverElements.forEach(el => {
        el.addEventListener('mouseenter', () => {
            cursor.classList.add('hover');
        });
        el.addEventListener('mouseleave', () => {
            cursor.classList.remove('hover');
        });
    });

    // Click effect
    document.addEventListener('mousedown', () => {
        cursor.classList.add('click');
    });
    document.addEventListener('mouseup', () => {
        cursor.classList.remove('click');
    });

    // Hide cursor on mobile/touch devices
    if ('ontouchstart' in window || navigator.maxTouchPoints > 0) {
        cursor.style.display = 'none';
        cursorDot.style.display = 'none';
    }

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


    // --- SCROLL-BASED ANIMATIONS (Removed) ---
    function setupScrollAnimations() {
        // Animations removed
    }

    // --- SCROLL PROGRESS BAR ---
    const scrollProgress = document.querySelector('.scroll-progress');
    if (scrollProgress) {
        window.addEventListener('scroll', () => {
            const windowHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
            const scrolled = (window.scrollY / windowHeight) * 100;
            scrollProgress.style.width = scrolled + '%';
        });
    }

    // --- FLOATING PARTICLES ---
    function createParticles() {
        const particlesContainer = document.querySelector('.particles-container');
        if (!particlesContainer) return;

        const particleCount = 30;
        const colors = ['#2E7D32', '#4CAF50', '#66BB6A', '#81C784'];

        for (let i = 0; i < particleCount; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            
            // Random position
            particle.style.left = Math.random() * 100 + '%';
            particle.style.top = Math.random() * 100 + '%';
            
            // Random size
            const size = Math.random() * 3 + 2;
            particle.style.width = size + 'px';
            particle.style.height = size + 'px';
            
            // Random color
            particle.style.background = colors[Math.floor(Math.random() * colors.length)];
            
            // Random animation delay
            particle.style.animationDelay = Math.random() * 15 + 's';
            particle.style.animationDuration = (Math.random() * 10 + 10) + 's';
            
            particlesContainer.appendChild(particle);
        }
    }
    createParticles();

    // --- BACK TO TOP BUTTON ---
    const backToTopBtn = document.getElementById('back-to-top');
    if (backToTopBtn) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 300) {
                backToTopBtn.classList.add('visible');
            } else {
                backToTopBtn.classList.remove('visible');
            }
        });

        backToTopBtn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });

        // Add hover effect for cursor
        backToTopBtn.addEventListener('mouseenter', () => {
            cursor.classList.add('hover');
        });
        backToTopBtn.addEventListener('mouseleave', () => {
            cursor.classList.remove('hover');
        });
    }


    // --- SMOOTH SCROLL INDICATOR (Right Side) ---
    const scrollIndicator = document.createElement('div');
    scrollIndicator.className = 'scroll-indicator-right';
    document.body.appendChild(scrollIndicator);

    const scrollIndicatorDot = document.createElement('div');
    scrollIndicatorDot.className = 'scroll-indicator-dot';
    scrollIndicator.appendChild(scrollIndicatorDot);

    function updateScrollIndicator() {
        const windowHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrolled = (window.scrollY / windowHeight) * 100;
        scrollIndicatorDot.style.top = scrolled + '%';
    }

    window.addEventListener('scroll', updateScrollIndicator);
    updateScrollIndicator();

    // --- MAGNETIC BUTTONS/LINKS (Simplified) ---
    function initMagneticEffect() {
        const magneticElements = document.querySelectorAll('a, button, .project-item, .contact-links a, .cv-link');
        
        magneticElements.forEach(element => {
            element.addEventListener('mousemove', (e) => {
                const rect = element.getBoundingClientRect();
                const x = e.clientX - rect.left - rect.width / 2;
                const y = e.clientY - rect.top - rect.height / 2;
                
                const moveX = x * 0.3;
                const moveY = y * 0.3;
                
                element.style.transform = `translate(${moveX}px, ${moveY}px)`;
            });
            
            element.addEventListener('mouseleave', () => {
                element.style.transform = 'translate(0, 0)';
            });
        });
    }
    initMagneticEffect();

    // --- SECRET INPUT ---
    const secretInput = document.getElementById('secret-input');
    if (secretInput) {
        secretInput.addEventListener('input', (e) => {
            const value = e.target.value.toLowerCase().trim();
            if (value === 'bal') {
                setTimeout(() => {
                    window.location.href = 'page2.html';
                }, 300);
            }
        });

        secretInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const value = e.target.value.toLowerCase().trim();
                if (value === 'bal') {
                    window.location.href = 'page2.html';
                }
            }
        });
    }

    // --- PARALLAX SCROLLING EFFECTS ---
    function initParallax() {
        gsap.registerPlugin(ScrollTrigger);
        
        // Parallax for particles
        gsap.to('.particles-container', {
            scrollTrigger: {
                start: 'top top',
                end: 'bottom top',
                scrub: 1
            },
            y: 200
        });
    }
    initParallax();

    // --- CURSOR TRAIL EFFECT ---
    function initCursorTrail() {
        const trail = [];
        const trailLength = 10;
        
        // Create trail elements
        for (let i = 0; i < trailLength; i++) {
            const trailDot = document.createElement('div');
            trailDot.className = 'cursor-trail';
            trailDot.style.opacity = (trailLength - i) / trailLength * 0.3;
            trailDot.style.transform = `scale(${(trailLength - i) / trailLength})`;
            document.body.appendChild(trailDot);
            trail.push({ element: trailDot, x: 0, y: 0 });
        }
        
        let mouseX = 0;
        let mouseY = 0;
        let trailX = 0;
        let trailY = 0;
        
        document.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;
        });
        
        function animateTrail() {
            trailX += (mouseX - trailX) * 0.1;
            trailY += (mouseY - trailY) * 0.1;
            
            trail.forEach((dot, index) => {
                const delay = index * 0.05;
                const x = trailX - (trailX - mouseX) * delay;
                const y = trailY - (trailY - mouseY) * delay;
                
                dot.element.style.left = x + 'px';
                dot.element.style.top = y + 'px';
            });
            
            requestAnimationFrame(animateTrail);
        }
        
        animateTrail();
    }
    
    // Only init on desktop
    if (window.innerWidth > 768 && !('ontouchstart' in window)) {
        initCursorTrail();
    }

    // --- PAGE TRANSITIONS (Fade removed) ---
    function initPageTransitions() {
        // Link click smooth scroll
        document.querySelectorAll('a[href^="#"]').forEach(link => {
            link.addEventListener('click', (e) => {
                const href = link.getAttribute('href');
                if (href.startsWith('#')) {
                    e.preventDefault();
                    const target = document.querySelector(href);
                    if (target) {
                        const targetPosition = target.getBoundingClientRect().top + window.pageYOffset - 100;
                        window.scrollTo({
                            top: targetPosition,
                            behavior: 'smooth'
                        });
                    }
                }
            });
        });
    }
    initPageTransitions();

});
