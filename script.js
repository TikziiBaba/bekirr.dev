// ============================================
// bekirr.dev — Ana script
// Lenis kaydırma + GSAP geçişleri + 3D akropolis kamerası
// ============================================

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

gsap.registerPlugin(ScrollTrigger);

// 3D dünya ile paylaşılan durum.
// t: kamera durağı (0 giriş, 1 hakkımda, 2 yetenekler, 3–6 projeler, 7 iletişim)
const scene = { t: 0, intro: reduceMotion ? 1 : 0, skillRise: reduceMotion ? 1 : 0, skillHover: -1 };

// ---------- Yumuşak kaydırma ----------
let lenis = null;
if (!reduceMotion && window.Lenis) {
    lenis = new window.Lenis({ lerp: 0.09 });
    lenis.on('scroll', ScrollTrigger.update);
    gsap.ticker.add((time) => lenis.raf(time * 1000));
    gsap.ticker.lagSmoothing(0);
}

// ---------- Menü ----------
const menuBtn = document.getElementById('menuBtn');
const setMenu = (open) => {
    document.body.classList.toggle('menu-open', open);
    menuBtn.setAttribute('aria-expanded', String(open));
    menuBtn.textContent = open ? 'Kapat' : 'Menü';
    if (lenis) open ? lenis.stop() : lenis.start();
};
menuBtn.addEventListener('click', () => setMenu(!document.body.classList.contains('menu-open')));

document.querySelectorAll('a[href^="#"]').forEach((link) => {
    link.addEventListener('click', (e) => {
        const target = document.querySelector(link.getAttribute('href'));
        if (!target) return;
        e.preventDefault();
        setMenu(false);
        if (lenis) lenis.scrollTo(target, { duration: 2 });
        else target.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth' });
    });
});

// ---------- Projeler: masaüstünde sabitlenir, kaydırdıkça bir sonraki vazoya geçilir ----------
const projects = document.getElementById('projects');
const projectEls = [...projects.querySelectorAll('.project')];
const projectDots = [...projects.querySelectorAll('.projects-dots i')];
const projectNum = document.getElementById('projectNum');
const projectCount = projectEls.length;
let activeProject = 0;
let projectRange = null;

const setProject = (index) => {
    if (index === activeProject) return;
    activeProject = index;
    projectEls.forEach((el, i) => el.classList.toggle('is-active', i === index));
    projectDots.forEach((el, i) => el.classList.toggle('is-active', i === index));
    projectNum.textContent = String(index + 1).padStart(2, '0');
};

const mm = gsap.matchMedia();
// Pin, altındaki tetikleyicilerin konumunu etkilediği için önce ölçülür (refreshPriority)
mm.add('(min-width: 900px) and (prefers-reduced-motion: no-preference)', () => {
    projects.classList.add('is-pinned');
    projectRange = ScrollTrigger.create({
        trigger: projects,
        start: 'top top',
        end: () => `+=${(projectCount - 1) * window.innerHeight * 1.1}`,
        pin: true,
        refreshPriority: 1,
        onUpdate: (self) => setProject(Math.round(self.progress * (projectCount - 1))),
    });
    return () => {
        projects.classList.remove('is-pinned');
        projectRange = null;
    };
});
mm.add('(max-width: 899px), (prefers-reduced-motion: reduce)', () => {
    projectRange = ScrollTrigger.create({ trigger: projects, start: 'top top', end: 'bottom bottom' });
    return () => { projectRange = null; };
});

// ---------- Kamera durağı ----------
// Her geçiş bir bölümün ekrana girişine bağlı; projelerde ise her vazoda biraz durulur.
const leg = (id, end = 'top 20%') => ScrollTrigger.create({ trigger: id, start: 'top bottom', end });
const legs = [leg('#about'), leg('#skills'), leg('#projects', 'top top')];
const contactLeg = leg('#contact');
const smoothstep = (a, b, x) => {
    const k = Math.min(Math.max((x - a) / (b - a), 0), 1);
    return k * k * (3 - 2 * k);
};

gsap.ticker.add(() => {
    let t = legs.reduce((sum, st) => sum + st.progress, 0);
    if (projectRange) {
        const s = projectRange.progress * (projectCount - 1);
        const i = Math.min(Math.floor(s), projectCount - 2);
        t += i + smoothstep(0.25, 0.75, s - i);
    }
    scene.t = t + contactLeg.progress;
});

// Menüde etkin bölüm
['about', 'skills', 'projects', 'contact'].forEach((id) => {
    ScrollTrigger.create({
        trigger: `#${id}`,
        start: 'top 55%',
        end: 'bottom 45%',
        onToggle: (self) => {
            if (self.isActive) document.querySelectorAll('[data-nav]').forEach((a) => a.classList.toggle('active', a.dataset.nav === id));
        },
    });
});

// ---------- Yetenek sütunları ----------
document.querySelectorAll('[data-skill]').forEach((row) => {
    const index = Number(row.dataset.skill);
    row.addEventListener('pointerenter', () => { scene.skillHover = index; });
    row.addEventListener('pointerleave', () => { if (scene.skillHover === index) scene.skillHover = -1; });
});
if (!reduceMotion) {
    ScrollTrigger.create({
        trigger: '#skills',
        start: 'top 75%',
        once: true,
        onEnter: () => gsap.to(scene, { skillRise: 1, duration: 3, ease: 'power2.out' }),
    });
}

// ---------- Metin geçişleri ----------
// Hakkımda paragrafı: kelimeler kaydırdıkça aydınlanır
document.querySelectorAll('[data-words]').forEach((el) => {
    const words = el.textContent.trim().split(/\s+/);
    el.textContent = '';
    words.forEach((word, i) => {
        const span = document.createElement('span');
        span.className = 'w';
        span.textContent = i < words.length - 1 ? `${word} ` : word;
        el.appendChild(span);
    });
});

if (!reduceMotion) {
    // Başlık satırları maskenin altından kayar
    document.querySelectorAll('main .title').forEach((title) => {
        gsap.from(title.querySelectorAll('.ln > span'), {
            yPercent: 110,
            duration: 1.3,
            ease: 'expo.out',
            stagger: 0.09,
            scrollTrigger: { trigger: title, start: 'top 85%', once: true },
        });
    });

    // Mermer levhalar yerine oturur
    gsap.utils.toArray('.slab').forEach((slab) => {
        gsap.from(slab, {
            y: 80,
            opacity: 0,
            rotateX: 6,
            transformPerspective: 1400,
            transformOrigin: '50% 100%',
            duration: 1.5,
            ease: 'expo.out',
            scrollTrigger: { trigger: slab, start: 'top 85%', once: true },
        });
    });

    gsap.utils.toArray('[data-fade]').forEach((el) => {
        gsap.from(el, {
            y: 40,
            opacity: 0,
            duration: 1.2,
            ease: 'expo.out',
            scrollTrigger: { trigger: el, start: 'top 90%', once: true },
        });
    });

    document.querySelectorAll('[data-words]').forEach((el) => {
        gsap.fromTo(el.querySelectorAll('.w'), { opacity: 0.18 }, {
            opacity: 1,
            ease: 'none',
            stagger: 0.05,
            scrollTrigger: { trigger: el, start: 'top 80%', end: 'bottom 45%', scrub: true },
        });
    });

    // Giriş yazısı kaydırınca yukarı akıp söner
    gsap.to('.hero-inner', {
        yPercent: -18,
        opacity: 0,
        ease: 'none',
        scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom 20%', scrub: true },
    });
}

// Sayaçlar ve yetenek çizgileri
document.querySelectorAll('[data-count]').forEach((el) => {
    const target = Number(el.dataset.count);
    const rule = el.parentElement.querySelector('.rule');
    if (rule) rule.style.setProperty('--w', `${target}%`);

    if (reduceMotion) {
        el.textContent = target;
        return;
    }
    const counter = { value: 0 };
    ScrollTrigger.create({
        trigger: el,
        start: 'top 92%',
        once: true,
        onEnter: () => {
            gsap.to(counter, {
                value: target,
                duration: 1.8,
                ease: 'expo.out',
                onUpdate: () => { el.textContent = Math.round(counter.value); },
            });
            if (rule) gsap.from(rule, { scaleX: 0, duration: 1.8, ease: 'expo.out' });
        },
    });
});

// ---------- Kayan friz (kaydırma hızına tepki verir) ----------
const friezeTrack = document.getElementById('friezeTrack');
friezeTrack.innerHTML += friezeTrack.innerHTML;
if (!reduceMotion) {
    const loop = gsap.to(friezeTrack, { xPercent: -50, duration: 30, ease: 'none', repeat: -1 });
    let boost = 0;
    let direction = 1;
    ScrollTrigger.create({
        onUpdate: (self) => {
            direction = self.direction;
            boost = Math.min(Math.max(boost, Math.abs(self.getVelocity()) / 250), 6);
        },
    });
    gsap.ticker.add(() => {
        boost *= 0.93;
        loop.timeScale(direction * (1 + boost));
    });
}

// ---------- İletişim formu (e-posta ile gönderim) ----------
const contactForm = document.getElementById('contactForm');
const contactNote = document.getElementById('contactNote');

contactForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('contactName').value.trim();
    const email = document.getElementById('contactEmail').value.trim();
    const message = document.getElementById('contactMessage').value.trim();

    if (!name || !message || !/^\S+@\S+\.\S+$/.test(email)) {
        contactNote.textContent = 'Lütfen tüm alanları doğru doldur.';
        if (!reduceMotion) gsap.fromTo(contactForm, { x: -10 }, { x: 0, duration: 0.8, ease: 'elastic.out(1, 0.3)' });
        return;
    }

    const subject = encodeURIComponent(`Web sitesinden mesaj: ${name}`);
    const body = encodeURIComponent(`${message}\n\n— ${name} (${email})`);
    contactNote.textContent = 'E-posta uygulaman açılıyor...';
    window.location.href = `mailto:dedyusuf99@gmail.com?subject=${subject}&body=${body}`;
});

// ---------- 3D dünya ----------
// Dinamik yüklenir: CDN'den bir parça gelmezse sayfanın geri kalanı yine çalışır
const skills = [...document.querySelectorAll('[data-skill] b')].map((b) => Number(b.dataset.count));
const worldReady = import('./world.js').then(({ createWorld }) => createWorld(document.getElementById('world'), scene, { skills })).catch((err) => {
    // WebGL yoksa sayfa CSS gün batımı arka planıyla çalışmaya devam eder
    console.warn('3D sahne başlatılamadı:', err);
    document.getElementById('world').remove();
    document.body.classList.add('no-webgl');
});

// ---------- Açılış ----------
function heroIntro() {
    const tl = gsap.timeline();
    tl.from('.hero-title .ln > span', { yPercent: 115, duration: 1.6, ease: 'expo.out', stagger: 0.12 })
        .from('.hero-eyebrow, .hero-sub, .actions, .hero-foot > *', { y: 24, opacity: 0, duration: 1.2, ease: 'expo.out', stagger: 0.08 }, '-=1.2')
        .from('.nav', { y: -30, opacity: 0, duration: 1, ease: 'expo.out' }, '-=1.2')
        .to(scene, { intro: 1, duration: 3.6, ease: 'expo.out' }, 0);
    return tl;
}

function runLoader() {
    const loader = document.getElementById('loader');
    if (reduceMotion) {
        loader.style.display = 'none';
        return;
    }
    const countEl = document.getElementById('loaderCount');
    const count = { value: 0 };

    const showCount = () => { countEl.textContent = Math.round(count.value); };

    // Sayaç %90'a kadar çıkar; dünya hazır olmadan perde açılmaz
    const drawing = gsap.timeline()
        .to('.loader-temple path', { strokeDashoffset: 0, duration: 1.8, ease: 'power2.inOut' }, 0)
        .to(count, { value: 90, duration: 1.5, ease: 'power2.inOut', onUpdate: showCount }, 0);

    const timeout = new Promise((resolve) => { setTimeout(resolve, 6000); });
    Promise.all([drawing.then(), Promise.race([worldReady, timeout])]).then(() => {
        gsap.timeline()
            .to(count, { value: 100, duration: 0.4, onUpdate: showCount })
            .to('.loader-center', { opacity: 0, y: -10, duration: 0.4 })
            .addLabel('split')
            .to('.loader-top', { yPercent: -100, duration: 1.3, ease: 'expo.inOut' }, 'split')
            .to('.loader-bottom', { yPercent: 100, duration: 1.3, ease: 'expo.inOut' }, 'split')
            .add(heroIntro(), 'split+=0.3')
            .set(loader, { display: 'none' });
    });
}

runLoader();
