// GSAP Scroll Reveal Animation without Lenis (Native Scroll)
// We rely on native scrolling to avoid "lag" or "delay" feelings.

// Register ScrollTrigger
gsap.registerPlugin(ScrollTrigger);

// GSAP Scroll Reveal Animation (Intro - Simple Reveal)
document.querySelectorAll('.intro-reveal .reveal-line').forEach((line) => {
    const imgSpan = line.querySelector('.reveal-img-span');

    if (imgSpan) {
        gsap.to(imgSpan, {
            width: 300,
            ease: "none",
            scrollTrigger: {
                trigger: line,
                start: "top 80%",
                end: "top 30%",
                scrub: 1,
            }
        });
    }
});

// GSAP Scroll Reveal Animation (Tech - Rotate & Reveal)
document.querySelectorAll('.tech-reveal .reveal-line').forEach((line) => {
    const imgSpan = line.querySelector('.reveal-img-span');
    const img = imgSpan.querySelector('img');

    if (imgSpan) {
        // Timeline for synchronized effects
        const tl = gsap.timeline({
            scrollTrigger: {
                trigger: line,
                start: "top 80%",
                end: "top 35%",
                scrub: 1,
            }
        });

        // Expand width
        tl.to(imgSpan, {
            width: 300,
            ease: "none"
        }, 0);

        // Rotate Image
        if (img) {
            tl.fromTo(img,
                { rotation: -180, scale: 0.5, opacity: 0.5 },
                { rotation: 0, scale: 1, opacity: 1, ease: "none" },
                0);
        }
    }
});



// Stacking Cards Scale Effect (Optional Enhancement)
const cards = document.querySelectorAll('.card');
cards.forEach((card, index) => {
    gsap.to(card, {
        scale: 0.95, // Scale down slightly as it goes up to create depth
        scrollTrigger: {
            trigger: card,
            start: "top 15%",
            end: "bottom top",
            scrub: true,
            markers: false
        }
    });
});

console.log("Animations Initialized.");
