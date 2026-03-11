document.addEventListener('DOMContentLoaded', () => {

    // --- PARTICLE EXPLOSION REMOVED ---

    // --- SPOTLIGHT EFFECT (NEW) ---
    const spotlight = document.createElement('div');
    spotlight.className = 'spotlight-overlay';
    document.body.appendChild(spotlight);

    // Add spotlight CSS dynamically
    const spotlightStyle = document.createElement('style');
    spotlightStyle.innerHTML = `
        .spotlight-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            pointer-events: none;
            background: radial-gradient(circle at var(--x, 50%) var(--y, 50%), transparent 0%, rgba(0,0,0,0.85) 50%);
            z-index: 9998; /* Below cursor, above content */
            mix-blend-mode: multiply;
        }
    `;
    document.head.appendChild(spotlightStyle);

    document.addEventListener('mousemove', (e) => {
        spotlight.style.setProperty('--x', `${e.clientX}px`);
        spotlight.style.setProperty('--y', `${e.clientY}px`);
    });


    // --- TYPING EFFECT ---
    const textToType = "Genelde pek birşey yapmam. Yani gözüken kısımda.";
    const typingElement = document.querySelector('.hero-subtitle');

    if (typingElement) {
        typingElement.textContent = '';
        typingElement.style.minHeight = '1.6em';

        let charIndex = 0;
        function typeText() {
            if (charIndex < textToType.length) {
                typingElement.textContent += textToType.charAt(charIndex);
                charIndex++;
                setTimeout(typeText, 40);
            }
        }
        setTimeout(typeText, 300);
    }

    // --- GLITCH EFFECT ON TITLE (NEW) ---
    const title = document.querySelector('.hero-title');
    if (title) {
        title.setAttribute('data-text', title.innerText);
    }


    // --- MAGNETIC BUTTON & 3D TILT EFFECT ---
    const magneticBtns = document.querySelectorAll('.btn, .social-icon, .card-link');
    const cards = document.querySelectorAll('.card');

    magneticBtns.forEach(btn => {
        btn.addEventListener('mousemove', (e) => {
            const rect = btn.getBoundingClientRect();
            const x = e.clientX - rect.left - rect.width / 2;
            const y = e.clientY - rect.top - rect.height / 2;
            btn.style.transform = `translate(${x * 0.5}px, ${y * 0.5}px) scale(1.1)`;
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'translate(0, 0) scale(1)';
        });
    });

    // 3D Tilt for Cards
    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = ((y - centerY) / centerY) * -10;
            const rotateY = ((x - centerX) / centerX) * 10;

            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.05)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale(1)';
        });
    });


    // --- SCROLL ANIMATIONS ---
    const observerOptions = { threshold: 0.1, rootMargin: '0px 0px -50px 0px' };
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('in-view');
            } else {
                entry.target.classList.remove('in-view');
            }
        });
    }, observerOptions);

    const animatedElements = document.querySelectorAll('.card, .hero-title, .section-title, .contact-section, .hero-tag, p');
    animatedElements.forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(50px) scale(0.8) rotate(5deg)';
        el.style.transition = 'all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)';

        if (el.classList.contains('card')) {
            el.style.transitionDelay = `${index * 100}ms`;
        }
        observer.observe(el);
    });

    const style = document.createElement('style');
    style.innerHTML = `
        .in-view {
            opacity: 1 !important;
            transform: translateY(0) scale(1) rotate(0deg) !important;
        }
    `;
    document.head.appendChild(style);

    // --- CUSTOM CURSOR ---
    if (window.matchMedia("(pointer: fine)").matches) {
        const cursorDot = document.createElement('div');
        cursorDot.className = 'cursor-dot';
        document.body.appendChild(cursorDot);

        const cursorOutline = document.createElement('div');
        cursorOutline.className = 'cursor-outline';
        document.body.appendChild(cursorOutline);

        const cursorStyle = document.createElement('style');
        cursorStyle.innerHTML = `
            .cursor-dot, .cursor-outline {
                position: fixed;
                top: 0; left: 0; transform: translate(-50%, -50%);
                border-radius: 50%; z-index: 9999; pointer-events: none;
            }
            .cursor-dot { width: 10px; height: 10px; background: var(--accent); box-shadow: 0 0 10px var(--accent); }
            .cursor-outline {
                width: 50px; height: 50px; border: 2px solid var(--primary);
                transition: transform 0.1s;
            }
            body.hovering .cursor-outline { transform: translate(-50%, -50%) scale(1.5) rotate(45deg); border-color: var(--accent); border-style: dashed;}
            body.hovering .cursor-dot { transform: translate(-50%, -50%) scale(0.5); }
        `;
        document.head.appendChild(cursorStyle);

        let mouseX = 0, mouseY = 0, outlineX = 0, outlineY = 0;
        document.addEventListener('mousemove', (e) => { mouseX = e.clientX; mouseY = e.clientY; cursorDot.style.left = `${mouseX}px`; cursorDot.style.top = `${mouseY}px`; });

        const animateCursor = () => {
            outlineX += (mouseX - outlineX) * 0.15; outlineY += (mouseY - outlineY) * 0.15;
            cursorOutline.style.left = `${outlineX}px`; cursorOutline.style.top = `${outlineY}px`;
            requestAnimationFrame(animateCursor);
        };
        animateCursor();

        document.querySelectorAll('a, button, .card').forEach(el => {
            el.addEventListener('mouseenter', () => document.body.classList.add('hovering'));
            el.addEventListener('mouseleave', () => document.body.classList.remove('hovering'));
        });
    }

    // --- EASTER EGG ---
    let keystrokes = [];
    const secretCode = 'bal';
    document.addEventListener('keydown', (e) => {
        keystrokes.push(e.key);
        if (keystrokes.length > secretCode.length) keystrokes.shift();
        if (keystrokes.join('').toLowerCase() === secretCode) window.location.href = 'page2.html';
    });
});
