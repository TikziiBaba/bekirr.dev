document.addEventListener('DOMContentLoaded', () => {

    // --- 1. AÇILIŞ GRİD ANİMASYONU ---
    const loaderGrid = document.querySelector('.loader-grid');
    const loaderText = document.querySelector('.loader-text'); // YENİ
    const mainContent = document.querySelector('.main-content');

    // 100 tane grid kutusu oluştur
    for (let i = 0; i < 100; i++) {
        const item = document.createElement('div');
        item.classList.add('grid-item');
        loaderGrid.appendChild(item);
    }

    // GSAP ile animasyonu tanımla
    const gridTl = gsap.timeline({
        onComplete: setupScrollAnimations 
    });

    gridTl
        .from(".grid-item", {
            duration: 1,
            scale: 0,
            opacity: 0,
            delay: 0.2,
            ease: "power3.inOut",
            stagger: {
                amount: 1.5,
                from: "center",
                grid: "auto"
            }
        })
        // YENİ: Metni canlandır
        .to(loaderText, { duration: 1, opacity: 1, ease: "power2.inOut" }, "-=1.5")
        .to(loaderText, { duration: 0.5, opacity: 0, ease: "power2.inOut" }, "+=0.5")
        .to(".grid-item", {
            duration: 1,
            scale: 0,
            opacity: 0,
            ease: "power3.inOut",
            stagger: {
                amount: 1.5,
                from: "center",
                grid: "auto"
            }
        }, "-=0.5") // Metin kaybolurken grid de kaybolmaya başlasın
        .to(loaderGrid, {
            duration: 0.1,
            display: 'none'
        })
        .to(mainContent, {
            duration: 0.5,
            opacity: 1,
            onComplete: () => {
                window.scrollTo(0, 0);
            }
        }, "-=0.5");


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
