// ============================================
// bekirr.dev — Ana script
// Lenis kaydırma + GSAP geçişleri + Three.js shader sahnesi
// ============================================

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const finePointer = window.matchMedia('(pointer: fine)').matches;
const isMobile = () => window.innerWidth < 900;

gsap.registerPlugin(ScrollTrigger);

// Bölüm arka planları: hepsi aynı koyu ailede, sadece alt tonları farklı (nazik geçiş için).
// Yazı rengi sabit kalır, böylece metin hiçbir zaman tersine dönmez.
const THEMES = {
    ink: '#0C0C0E',
    dusk: '#131219',
    warm: '#17130F',
    cool: '#0D1016',
    ember: '#1C120D',
};

// 3D şeklin her bölümdeki konumu: [x, y, ölçek] — x/y ekranın yarı genişliği/yüksekliği oranında
const BLOB_STATES = {
    hero: { desktop: [0.42, 0.08, 1], mobile: [0.3, 0.38, 0.58] },
    about: { desktop: [0.68, -0.42, 0.5], mobile: [0.65, 0.72, 0.32] },
    skills: { desktop: [0.74, 0.52, 0.4], mobile: [0.6, 0.78, 0.3] },
    projects: { desktop: [0.8, 0.68, 0.26], mobile: [0.6, 0.76, 0.3] },
    contact: { desktop: [0.58, 0.18, 0.95], mobile: [0.45, 0.55, 0.6] },
};

// 3D sahne ile paylaşılan durum
const blob = { x: 0.42, y: 0.08, s: 1, intro: reduceMotion ? 1 : 0 };
const sceneState = { progress: 0 };

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
        if (lenis) lenis.scrollTo(target, { duration: 1.6 });
        else target.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth' });
    });
});

// ---------- Projeler: masaüstünde sabitlenip yatay kayar ----------
// Pin, altındaki tetikleyicilerin konumunu etkilediği için önce ölçülür (refreshPriority)
const projects = document.getElementById('projects');
const projectsTrack = document.getElementById('projectsTrack');
const mm = gsap.matchMedia();

mm.add('(min-width: 900px) and (prefers-reduced-motion: no-preference)', () => {
    projects.classList.add('is-horizontal');
    const distance = () => projectsTrack.scrollWidth - window.innerWidth;

    const slide = gsap.to(projectsTrack, {
        x: () => -distance(),
        ease: 'none',
        scrollTrigger: {
            trigger: projects,
            start: 'top top',
            end: () => `+=${distance()}`,
            pin: true,
            scrub: 1,
            invalidateOnRefresh: true,
            refreshPriority: 1,
        },
    });

    // Kartlar sağdan 3D açıyla dönerek düzleşir
    projects.querySelectorAll('.project').forEach((card) => {
        gsap.fromTo(card,
            { rotationY: -28, z: -160, opacity: 0.35, transformPerspective: 1600, transformOrigin: 'left center' },
            {
                rotationY: 0,
                z: 0,
                opacity: 1,
                ease: 'none',
                scrollTrigger: { trigger: card, containerAnimation: slide, start: 'left 105%', end: 'left 45%', scrub: true },
            });
    });

    return () => projects.classList.remove('is-horizontal');
});

// ---------- Tema + 3D şekil geçişleri ----------
function enterSection(id) {
    const section = document.getElementById(id);
    gsap.to(document.documentElement, {
        '--bg': THEMES[section.dataset.theme],
        duration: reduceMotion ? 0 : 2.2,
        ease: 'sine.inOut',
        overwrite: 'auto',
    });

    const [x, y, s] = BLOB_STATES[id][isMobile() ? 'mobile' : 'desktop'];
    gsap.to(blob, { x, y, s, duration: reduceMotion ? 0 : 1.8, ease: 'expo.inOut', overwrite: 'auto' });

    document.querySelectorAll('[data-nav]').forEach((a) => a.classList.toggle('active', a.dataset.nav === id));
}

// Her bölüm, başlangıcı geçilince devralır; geri dönülünce bir öncekine bırakır.
// (Bitiş noktası kullanılmaz: sabitlenen projeler bölümünün yüksekliği pin süresini kapsamaz.)
// Not: pin, projeler bölümünü bir pin-spacer'a sardığı için "main > section" kullanılmaz
const themedSections = [...document.querySelectorAll('section[data-theme]')];
themedSections.forEach((section, i) => {
    ScrollTrigger.create({
        trigger: section,
        start: 'top 55%',
        onEnter: () => enterSection(section.id),
        onLeaveBack: () => { if (i > 0) enterSection(themedSections[i - 1].id); },
    });
});

ScrollTrigger.create({
    start: 0,
    end: 'max',
    onUpdate: (self) => { sceneState.progress = self.progress; },
});

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

    gsap.utils.toArray('[data-fade]').forEach((el) => {
        gsap.from(el, {
            y: 50,
            opacity: 0,
            duration: 1.2,
            ease: 'expo.out',
            scrollTrigger: { trigger: el, start: 'top 88%', once: true },
        });
    });

    document.querySelectorAll('[data-words]').forEach((el) => {
        gsap.fromTo(el.querySelectorAll('.w'), { opacity: 0.12 }, {
            opacity: 1,
            ease: 'none',
            stagger: 0.05,
            scrollTrigger: { trigger: el, start: 'top 80%', end: 'bottom 45%', scrub: true },
        });
    });

    // Hero içeriği kaydırınca yukarı akar
    gsap.to('.hero-title', {
        yPercent: -30,
        ease: 'none',
        scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom top', scrub: true },
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

// ---------- Kayan teknoloji şeridi (kaydırma hızına tepki verir) ----------
const marqueeTrack = document.getElementById('marqueeTrack');
marqueeTrack.innerHTML += marqueeTrack.innerHTML;
if (!reduceMotion) {
    const loop = gsap.to(marqueeTrack, { xPercent: -50, duration: 28, ease: 'none', repeat: -1 });
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
        gsap.set(marqueeTrack, { skewX: -direction * boost * 1.4 });
    });
}

// ---------- İmleç ve kapak eğimi ----------
if (finePointer && !reduceMotion) {
    const cursor = document.getElementById('cursor');
    const moveX = gsap.quickTo(cursor, 'x', { duration: 0.35, ease: 'power3' });
    const moveY = gsap.quickTo(cursor, 'y', { duration: 0.35, ease: 'power3' });
    document.body.classList.add('has-cursor');

    window.addEventListener('pointermove', (e) => { moveX(e.clientX); moveY(e.clientY); });
    document.addEventListener('pointerover', (e) => {
        const hovering = !!e.target.closest('a, button, input, textarea');
        gsap.to(cursor, { scale: hovering ? 3.2 : 1, duration: 0.5, ease: 'expo.out', overwrite: 'auto' });
    });

    document.querySelectorAll('.project-cover').forEach((cover) => {
        gsap.set(cover, { transformPerspective: 1000 });
        const glyph = cover.querySelector('.project-glyph');
        const rotX = gsap.quickTo(cover, 'rotationX', { duration: 0.7, ease: 'power3' });
        const rotY = gsap.quickTo(cover, 'rotationY', { duration: 0.7, ease: 'power3' });
        const glyphX = gsap.quickTo(glyph, 'x', { duration: 0.7, ease: 'power3' });
        const glyphY = gsap.quickTo(glyph, 'y', { duration: 0.7, ease: 'power3' });

        cover.addEventListener('pointermove', (e) => {
            const r = cover.getBoundingClientRect();
            const px = (e.clientX - r.left) / r.width - 0.5;
            const py = (e.clientY - r.top) / r.height - 0.5;
            rotY(px * 12);
            rotX(-py * 10);
            glyphX(px * 40);
            glyphY(py * 40);
        });
        cover.addEventListener('pointerleave', () => { rotX(0); rotY(0); glyphX(0); glyphY(0); });
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

// ---------- Açılış ----------
function heroIntro() {
    const tl = gsap.timeline();
    tl.from('.hero-title .ln > span', { yPercent: 115, duration: 1.5, ease: 'expo.out', stagger: 0.12 })
        .from('.hero-top > *, .hero-bottom > *', { y: 24, opacity: 0, duration: 1.1, ease: 'expo.out', stagger: 0.08 }, '-=1.1')
        .from('.nav', { y: -30, opacity: 0, duration: 1, ease: 'expo.out' }, '-=1.1')
        .to(blob, { intro: 1, duration: 2.4, ease: 'expo.out' }, 0);
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

    gsap.timeline()
        .to(count, {
            value: 100,
            duration: 1.6,
            ease: 'power2.inOut',
            onUpdate: () => { countEl.textContent = String(Math.round(count.value)).padStart(3, '0'); },
        })
        .to('.loader-count, .loader-label', { opacity: 0, duration: 0.3 })
        .addLabel('split')
        .to('.loader-top', { yPercent: -100, duration: 1.2, ease: 'expo.inOut' }, 'split')
        .to('.loader-bottom', { yPercent: 100, duration: 1.2, ease: 'expo.inOut' }, 'split')
        .add(heroIntro(), 'split+=0.45')
        .set(loader, { display: 'none' });
}

// Fontlar yüklenince (en geç 1.5 sn) başla; ölçüler değiştiği için tetikleyicileri yenile
Promise.race([document.fonts.ready, new Promise((resolve) => setTimeout(resolve, 1500))]).then(() => {
    ScrollTrigger.refresh();
    runLoader();
});

// ---------- 3D sahne: gürültüyle şekil değiştiren damla ----------
const NOISE_GLSL = /* glsl */ `
vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);
    vec3 i = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);
    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy;
    vec3 x3 = x0 - D.yyy;
    i = mod289(i);
    vec4 p = permute(permute(permute(
        i.z + vec4(0.0, i1.z, i2.z, 1.0))
        + i.y + vec4(0.0, i1.y, i2.y, 1.0))
        + i.x + vec4(0.0, i1.x, i2.x, 1.0));
    float n_ = 0.142857142857;
    vec3 ns = n_ * D.wyz - D.xzx;
    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);
    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);
    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);
    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);
    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));
    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;
    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x; p1 *= norm.y; p2 *= norm.z; p3 *= norm.w;
    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}`;

const VERTEX_SHADER = /* glsl */ `
uniform float uTime;
uniform float uDistort;
varying vec3 vNormal;
varying vec3 vViewPos;
varying float vNoise;
${NOISE_GLSL}
void main() {
    float n = snoise(normal * 1.1 + vec3(uTime * 0.2));
    n += 0.18 * snoise(normal * 2.4 - vec3(uTime * 0.25));
    vNoise = n;
    vec3 displaced = position + normal * n * uDistort;
    vNormal = normalize(normalMatrix * normal);
    vec4 mv = modelViewMatrix * vec4(displaced, 1.0);
    vViewPos = mv.xyz;
    gl_Position = projectionMatrix * mv;
}`;

const FRAGMENT_SHADER = /* glsl */ `
uniform float uTime;
uniform vec3 uColorA;
uniform vec3 uColorB;
uniform vec3 uColorC;
varying vec3 vNormal;
varying vec3 vViewPos;
varying float vNoise;
void main() {
    // Bükülmüş yüzeyin gerçek normali (ekran türevlerinden)
    vec3 normal = normalize(cross(dFdx(vViewPos), dFdy(vViewPos)));
    vec3 viewDir = normalize(-vViewPos);
    float fresnel = pow(1.0 - max(dot(viewDir, normal), 0.0), 2.5);

    float t = smoothstep(-0.9, 0.9, vNoise + normal.y * 0.35);
    vec3 color = mix(uColorA, uColorB, t);

    vec3 lightDir = normalize(vec3(-0.4, 0.7, 0.6));
    float diffuse = max(dot(normal, lightDir), 0.0);
    float spec = pow(max(dot(reflect(-lightDir, normal), viewDir), 0.0), 24.0);
    color *= 0.45 + 0.75 * diffuse;
    color += spec * 0.35;
    color = mix(color, uColorC, fresnel * 0.7);
    gl_FragColor = vec4(color, 1.0);
}`;

async function initScene() {
    let THREE;
    try {
        THREE = await import('three');
    } catch (err) {
        console.warn('Three.js yüklenemedi, 3D sahne atlanıyor.', err);
        return;
    }

    const canvas = document.getElementById('gl');
    let renderer;
    try {
        renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
    } catch (err) {
        console.warn('WebGL kullanılamıyor.', err);
        return;
    }
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.75));

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100);
    camera.position.z = 5;

    const uniforms = {
        uTime: { value: 0 },
        uDistort: { value: 0.24 },
        uColorA: { value: new THREE.Color('#3A35E8') },
        uColorB: { value: new THREE.Color('#FF6A2B') },
        uColorC: { value: new THREE.Color('#FFD9C7') },
    };
    const shape = new THREE.Mesh(
        new THREE.IcosahedronGeometry(1.15, 64),
        new THREE.ShaderMaterial({ uniforms, vertexShader: VERTEX_SHADER, fragmentShader: FRAGMENT_SHADER }),
    );
    scene.add(shape);

    // Hafif toz parçacıkları; rengi temaya uyar
    const dustCount = 500;
    const dustPositions = new Float32Array(dustCount * 3);
    for (let i = 0; i < dustCount; i++) {
        dustPositions[i * 3] = (Math.random() - 0.5) * 16;
        dustPositions[i * 3 + 1] = (Math.random() - 0.5) * 10;
        dustPositions[i * 3 + 2] = (Math.random() - 0.5) * 6 - 1;
    }
    const dustGeo = new THREE.BufferGeometry();
    dustGeo.setAttribute('position', new THREE.BufferAttribute(dustPositions, 3));
    const dustMat = new THREE.PointsMaterial({ size: 0.022, transparent: true, opacity: 0.45, depthWrite: false });
    dustMat.color.set('#F1EEE8');
    const dust = new THREE.Points(dustGeo, dustMat);
    scene.add(dust);

    const resize = () => {
        renderer.setSize(window.innerWidth, window.innerHeight, false);
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.updateProjectionMatrix();
    };
    resize();
    window.addEventListener('resize', resize);

    // Fare konumu ve hızı (hız, şeklin dalgalanmasını artırır)
    const pointer = { x: 0, y: 0, smoothX: 0, smoothY: 0, energy: 0 };
    window.addEventListener('pointermove', (e) => {
        const nx = (e.clientX / window.innerWidth) * 2 - 1;
        const ny = (e.clientY / window.innerHeight) * 2 - 1;
        pointer.energy = Math.min(pointer.energy + Math.hypot(nx - pointer.x, ny - pointer.y) * 2, 1);
        pointer.x = nx;
        pointer.y = ny;
    }, { passive: true });

    const halfHeight = Math.tan(THREE.MathUtils.degToRad(camera.fov / 2)) * camera.position.z;
    const clock = new THREE.Clock();

    const tick = () => {
        requestAnimationFrame(tick);
        const t = clock.getElapsedTime();
        const halfWidth = halfHeight * camera.aspect;

        pointer.smoothX += (pointer.x - pointer.smoothX) * 0.05;
        pointer.smoothY += (pointer.y - pointer.smoothY) * 0.05;
        pointer.energy *= 0.96;

        uniforms.uTime.value = reduceMotion ? t * 0.3 : t;
        uniforms.uDistort.value += ((0.24 + pointer.energy * 0.3) - uniforms.uDistort.value) * 0.08;

        shape.position.x = blob.x * halfWidth + pointer.smoothX * 0.18;
        shape.position.y = blob.y * halfHeight - pointer.smoothY * 0.12;
        shape.scale.setScalar(Math.max(blob.s * blob.intro, 0.0001));
        shape.rotation.y = t * 0.12 + sceneState.progress * 5 + pointer.smoothX * 0.4;
        shape.rotation.x = pointer.smoothY * 0.3;

        dust.rotation.y = t * 0.015 + sceneState.progress * 0.8;
        dust.position.y = sceneState.progress * 2;

        renderer.render(scene, camera);
    };
    tick();
}

initScene();
