// ============================================
// bekirr.dev — Bekir Kaplan Universal GSAP Scroll Presentation Engine
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // 1. Cursor Glow Tracking
    const cursorGlow = document.getElementById('cursorGlow');
    if (cursorGlow) {
        let mouseX = window.innerWidth / 2, mouseY = window.innerHeight / 2;
        let glowX = mouseX, glowY = mouseY;

        document.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;
        });

        function animateGlow() {
            glowX += (mouseX - glowX) * 0.08;
            glowY += (mouseY - glowY) * 0.08;
            cursorGlow.style.left = glowX + 'px';
            cursorGlow.style.top = glowY + 'px';
            requestAnimationFrame(animateGlow);
        }
        animateGlow();
    }

    // 2. Interactive Card Tilt on Mouse Move
    if (window.innerWidth > 900) {
        const tiltCards = document.querySelectorAll('.minimal-dev-card, .bento-card-master, .journey-card, .workflow-step-card');
        tiltCards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left - rect.width / 2;
                const y = e.clientY - rect.top - rect.height / 2;
                const tiltX = (y / (rect.height / 2)) * -6;
                const tiltY = (x / (rect.width / 2)) * 6;
                card.style.transform = `perspective(1000px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) translateY(-4px)`;
            });

            card.addEventListener('mouseleave', () => {
                card.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg) translateY(0px)`;
            });
        });
    }

    // 3. UNIVERSAL GSAP MASTER SCROLL PRESENTATION ENGINE (DEVICES & MOBILE)
    if (typeof gsap !== 'undefined' && typeof ScrollTrigger !== 'undefined') {
        gsap.registerPlugin(ScrollTrigger);

        // Top Scroll Progress Bar
        gsap.to('#gsap-scroll-bar', {
            width: '100%',
            ease: 'none',
            scrollTrigger: {
                trigger: 'body',
                start: 'top top',
                end: 'bottom bottom',
                scrub: 0.2
            }
        });

        // Responsive scrub duration
        const isMobile = window.innerWidth <= 900;
        const totalScrollDistance = isMobile ? '+=4500' : '+=6000';

        const masterTL = gsap.timeline({
            scrollTrigger: {
                trigger: '.master-scroll-viewport',
                start: 'top top',
                end: totalScrollDistance,
                scrub: 1,
                pin: true,
                anticipatePin: 1
            }
        });

        // SCENE 1: Hero Stage Exit (0 -> 1.0)
        masterTL.to('.hero-stage-layout', { autoAlpha: 0, y: -60, duration: 1.0, pointerEvents: 'none' }, 0);

        // SCENE 2: Spinning Thick Solid Emerald Bitcoin Coin & Feature Cards (1.2 -> 9.5)
        masterTL.to('.spinning-b-stage', { autoAlpha: 1, scale: isMobile ? 1.0 : 1.25, duration: 1.0, pointerEvents: 'auto' }, 1.2);

        // 🌀 1440-degree Z-Axis Face Rotation
        masterTL.to('.spinning-b-emblem', {
            rotation: 1440,
            duration: 8.5,
            ease: 'none'
        }, 1.2);

        // Story Feature Card 1
        masterTL.fromTo('#story-feature-1',
            { autoAlpha: 0, x: isMobile ? 0 : -160, y: isMobile ? 40 : 0, scale: 0.8 },
            { autoAlpha: 1, x: 0, y: 0, scale: 1, duration: 0.8, pointerEvents: 'auto' }, 1.8
        );
        masterTL.to('#story-feature-1', { autoAlpha: 0, x: isMobile ? 0 : -160, y: isMobile ? -40 : 0, duration: 0.6, pointerEvents: 'none' }, 3.0);

        // Story Feature Card 2
        masterTL.fromTo('#story-feature-2',
            { autoAlpha: 0, x: isMobile ? 0 : 160, y: isMobile ? 40 : 0, scale: 0.8 },
            { autoAlpha: 1, x: 0, y: 0, scale: 1, duration: 0.8, pointerEvents: 'auto' }, 3.8
        );
        masterTL.to('#story-feature-2', { autoAlpha: 0, x: isMobile ? 0 : 160, y: isMobile ? -40 : 0, duration: 0.6, pointerEvents: 'none' }, 5.0);

        // Story Feature Card 3
        masterTL.fromTo('#story-feature-3',
            { autoAlpha: 0, x: isMobile ? 0 : -160, y: isMobile ? 40 : 0, scale: 0.8 },
            { autoAlpha: 1, x: 0, y: 0, scale: 1, duration: 0.8, pointerEvents: 'auto' }, 5.8
        );
        masterTL.to('#story-feature-3', { autoAlpha: 0, x: isMobile ? 0 : -160, y: isMobile ? -40 : 0, duration: 0.6, pointerEvents: 'none' }, 7.0);

        // Story Feature Card 4
        masterTL.fromTo('#story-feature-4',
            { autoAlpha: 0, x: isMobile ? 0 : 160, y: isMobile ? 40 : 0, scale: 0.8 },
            { autoAlpha: 1, x: 0, y: 0, scale: 1, duration: 0.8, pointerEvents: 'auto' }, 7.8
        );
        masterTL.to('#story-feature-4', { autoAlpha: 0, x: isMobile ? 0 : 160, y: isMobile ? -40 : 0, duration: 0.6, pointerEvents: 'none' }, 9.0);

        // Coin stage exit
        masterTL.to('.spinning-b-stage', { autoAlpha: 0, scale: 0.5, duration: 0.8, pointerEvents: 'none' }, 9.2);

        // SCENE 3: Tech Journey Stage (10.2 -> 12.8)
        masterTL.fromTo('.master-journey-stage',
            { autoAlpha: 0, y: 80, scale: 0.9 },
            { autoAlpha: 1, y: 0, scale: 1, duration: 1.0, pointerEvents: 'auto' }, 10.2
        );

        masterTL.fromTo('.journey-card',
            { autoAlpha: 0, y: 25 },
            { autoAlpha: 1, y: 0, duration: 0.6, stagger: 0.1 }, 10.6
        );

        masterTL.to('.master-journey-stage', { autoAlpha: 0, y: -80, scale: 0.9, duration: 0.8, pointerEvents: 'none' }, 12.2);

        // SCENE 4: Workflow Stage (13.0 -> 15.6)
        masterTL.fromTo('.master-workflow-stage',
            { autoAlpha: 0, y: 80, scale: 0.9 },
            { autoAlpha: 1, y: 0, scale: 1, duration: 1.0, pointerEvents: 'auto' }, 13.0
        );

        masterTL.fromTo('.workflow-step-card',
            { autoAlpha: 0, y: 25 },
            { autoAlpha: 1, y: 0, duration: 0.6, stagger: 0.1 }, 13.4
        );

        masterTL.to('.master-workflow-stage', { autoAlpha: 0, y: -80, scale: 0.9, duration: 0.8, pointerEvents: 'none' }, 15.0);

        // SCENE 5: Bento Grid Stage / Projects (15.8 -> 18.4)
        masterTL.fromTo('.master-bento-stage',
            { autoAlpha: 0, y: 80, scale: 0.9 },
            { autoAlpha: 1, y: 0, scale: 1, duration: 1.0, pointerEvents: 'auto' }, 15.8
        );

        masterTL.fromTo('.bento-card-master',
            { autoAlpha: 0, y: 25 },
            { autoAlpha: 1, y: 0, duration: 0.6, stagger: 0.1 }, 16.2
        );

        masterTL.to('.master-bento-stage', { autoAlpha: 0, y: -80, scale: 0.9, duration: 0.8, pointerEvents: 'none' }, 17.8);

        // SCENE 6: Grand Finale & Contact Stage (18.6 -> 20.6)
        masterTL.fromTo('.master-finale-stage',
            { autoAlpha: 0, y: 80, scale: 0.9 },
            { autoAlpha: 1, y: 0, scale: 1, duration: 1.0, pointerEvents: 'auto' }, 18.6
        );

        masterTL.fromTo('.tech-stack-card',
            { autoAlpha: 0, y: 25 },
            { autoAlpha: 1, y: 0, duration: 0.6, stagger: 0.1 }, 19.0
        );
    }

    console.log("🚀 bekirr.dev — Universal GSAP Presentation Engine Active.");
});
