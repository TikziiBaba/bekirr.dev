// ============================================
// bekirr.dev — GSAP Animations & Logic
// ============================================

gsap.registerPlugin(ScrollTrigger);

// Supabase Init
const SUPABASE_URL = 'https://dtdsawyynetqlbosrvqo.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR0ZHNhd3l5bmV0cWxib3NydnFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ1NDU0MDUsImV4cCI6MjA5MDEyMTQwNX0.6rKxp51OOj_b1iKtz_21ZkHcvbThNF4w5sPdP7RAua4';

let supabaseClient = null;
if (typeof supabase !== 'undefined') {
    supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
}

// Fetch Stats from Supabase
async function loadStats() {
    if (!supabaseClient) return;
    
    try {
        const [profilesRes, songsRes, playlistsRes] = await Promise.all([
            supabaseClient.from('profiles').select('id', { count: 'exact', head: true }),
            supabaseClient.from('songs').select('id', { count: 'exact', head: true }),
            supabaseClient.from('playlists').select('id', { count: 'exact', head: true }),
        ]);

        const statUsers = document.getElementById('statUsers');
        const statSongs = document.getElementById('statSongs');
        const statPlaylists = document.getElementById('statPlaylists');

        if (statUsers && profilesRes.count) statUsers.setAttribute('data-target', profilesRes.count);
        if (statSongs && songsRes.count) statSongs.setAttribute('data-target', songsRes.count);
        if (statPlaylists && playlistsRes.count) statPlaylists.setAttribute('data-target', playlistsRes.count);
        
        initStatsAnimation();
    } catch (e) {
        console.error("Failed to fetch stats from Supabase", e);
        initStatsAnimation(); // fallback to default values
    }
}

// ===== CURSOR GLOW =====
const cursorGlow = document.getElementById('cursorGlow');
if (cursorGlow) {
    let mouseX = 0, mouseY = 0;
    let glowX = 0, glowY = 0;

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

// ===== HERO TEXT =====
const heroTitle = document.getElementById('heroTitle');
if (heroTitle) {
    gsap.from(heroTitle, {
        opacity: 0,
        y: 30,
        duration: 1,
        ease: "power3.out",
        delay: 0.2
    });
}

// Hero description fade
const heroDesc = document.getElementById('heroDesc');
if (heroDesc) {
    gsap.from(heroDesc, {
        opacity: 0,
        y: 20,
        duration: 0.8,
        delay: 0.6,
        ease: "power2.out"
    });
}

// ===== BEKOFY SHOWCASE PARALLAX =====
const bekofyShowcase = document.getElementById('bekofyShowcase');
if (bekofyShowcase) {
    gsap.from(bekofyShowcase.querySelector('.bekofy-badge'), {
        opacity: 0, y: 30, duration: 0.6,
        scrollTrigger: { trigger: bekofyShowcase, start: "top 80%" }
    });

    gsap.from(bekofyShowcase.querySelector('.bekofy-title'), {
        opacity: 0, scale: 0.8, duration: 1, ease: "power3.out",
        scrollTrigger: { trigger: bekofyShowcase, start: "top 70%" }
    });

    gsap.from(bekofyShowcase.querySelector('.bekofy-tagline'), {
        opacity: 0, y: 20, duration: 0.8,
        scrollTrigger: { trigger: bekofyShowcase, start: "top 65%" }
    });

    gsap.from(bekofyShowcase.querySelector('.bekofy-glass-card'), {
        opacity: 0, y: 50, duration: 1, ease: "power2.out",
        scrollTrigger: { trigger: bekofyShowcase, start: "top 55%" }
    });

    // Feature chips stagger
    const chips = bekofyShowcase.querySelectorAll('.bekofy-feature-chip');
    if (chips.length) {
        gsap.fromTo(chips, 
            { opacity: 0, y: 20, scale: 0.9 },
            { opacity: 1, y: 0, scale: 1, duration: 0.4, stagger: 0.08, scrollTrigger: { trigger: bekofyShowcase, start: "top 65%" } }
        );
    }
}

// ===== STATS COUNTER =====
function initStatsAnimation() {
    const statsSection = document.getElementById('statsSection');
    if (statsSection) {
        const statNumbers = statsSection.querySelectorAll('.stat-number');

        statNumbers.forEach((num) => {
            const target = parseInt(num.getAttribute('data-target'), 10) || 0;

            gsap.to(num, {
                innerHTML: target,
                duration: 2,
                ease: "power1.out",
                snap: { innerHTML: 1 },
                scrollTrigger: {
                    trigger: statsSection,
                    start: "top 85%",
                    once: true,
                }
            });
        });
    }
}

// Load stats first, then init animation
loadStats();

// ===== HORIZONTAL SCROLL FEATURES =====
const horizSection = document.getElementById('horizSection');
const horizTrack = document.getElementById('horizTrack');

if (horizSection && horizTrack) {
    // Calculate how far to scroll
    const getScrollAmount = () => {
        return -(horizTrack.scrollWidth - window.innerWidth + 100);
    };

    gsap.to(horizTrack, {
        x: getScrollAmount,
        ease: "none",
        scrollTrigger: {
            trigger: horizSection,
            start: "top top",
            end: () => `+=${horizTrack.scrollWidth - window.innerWidth + 100}`,
            pin: true,
            scrub: 1,
            invalidateOnRefresh: true,
        }
    });
}

// ===== SECTION ENTRANCE ANIMATIONS =====
document.querySelectorAll('.section-animate').forEach((section) => {
    gsap.to(section, {
        opacity: 1,
        y: 0,
        duration: 1,
        ease: "power3.out",
        scrollTrigger: {
            trigger: section,
            start: "top 85%",
            once: true,
        }
    });
});

// ===== MARQUEE HOVER PAUSE (UX) =====
document.querySelectorAll('.marquee-track').forEach((track) => {
    const content = track.querySelector('.marquee-content');
    if (content) {
        track.addEventListener('mouseenter', () => {
            content.style.animationPlayState = 'paused';
        });
        track.addEventListener('mouseleave', () => {
            content.style.animationPlayState = 'running';
        });
    }
});

console.log("✨ bekirr.dev — Animations Initialized.");

