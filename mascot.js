// ============================================
// bekirr.dev — Glauks: siteyi tanıtan 3D rehber
// Athena'nın küçük baykuşu: kocaman kehribar gözler, gerçek göz kapakları, kulak tüyleri,
// başında altın defne tacı. Kaydırdıkça bölümleri anlatır, imleci gözleriyle izler,
// fareden kaçar, tıklanan butona uçup dokunur ve yaprak saçar; uzun süre dokunulmazsa uyuklar.
// ============================================

import * as THREE from 'three';

// ── Konuşmalar ──
const STOPS = [
    { id: 'hero', text: "Khaire! 🦉 Ben Glauks, Athena'nın baykuşu. Bekir'in akropolisine hoş geldin, aşağı kaydır!" },
    { id: 'about', text: 'Bekir full-stack bir geliştirici: Node.js, Electron ve React ile sağlam temelli, zarif sistemler kuruyor.' },
    { id: 'skills', text: 'Her sütun bir yetenek. Listede bir satırın üzerine gel, sütunu tholosta parlatayım 🏛️' },
    { id: 'projects', text: 'Agoraya hoş geldin! Her vazoda bir proje saklı, kaydırdıkça sırayla göstereceğim 🏺' },
    { id: 'contact', text: "Bir fikrin mi var? Mesajını yaz, Hermes'ten hızlı ulaştırırım ✉️" },
];
const FLEE_LINES = ['Yakalayamazsın! 🦉', 'Hu-huu! Buradayım 💨', 'Bir baykuşu yakalamak kolay mı sandın?'];
const WAKE_LINES = ['Hu? Uyumuyordum! 😳', 'Gözlerim açık, gözlerim açık!'];
const CLICKABLE = 'button, a[href], [role="button"], input[type="submit"], summary';
const SLEEP_AFTER = 15000; // bu kadar ms hareketsizlikten sonra uyuklar
const TYPE_SPEED = 42; // konuşma balonu: saniyede harf

const pick = (list) => list[Math.floor(Math.random() * list.length)];
const clamp = THREE.MathUtils.clamp;
const damp = (a, b, k) => a + (b - a) * k;

// ── Palet ──
const C = { feather: [123, 90, 63], cream: [236, 224, 200], disc: '#e6d8c0', beak: '#bfa77e', talon: '#2a221c', gold: '#e3b55c' };

function heightToNormal(src, strength) {
    const w = src.width;
    const h = src.height;
    const data = src.getContext('2d').getImageData(0, 0, w, h).data;
    const out = document.createElement('canvas');
    out.width = w;
    out.height = h;
    const g = out.getContext('2d');
    const img = g.createImageData(w, h);
    const H = (x, y) => data[(Math.min(h - 1, Math.max(0, y)) * w + ((x + w) % w)) * 4] / 255;
    for (let y = 0; y < h; y++) {
        for (let x = 0; x < w; x++) {
            const nx = (H(x - 1, y) - H(x + 1, y)) * strength;
            const ny = (H(x, y + 1) - H(x, y - 1)) * strength;
            const len = Math.hypot(nx, ny, 1);
            const i = (y * w + x) * 4;
            img.data[i] = (nx / len * 0.5 + 0.5) * 255;
            img.data[i + 1] = (ny / len * 0.5 + 0.5) * 255;
            img.data[i + 2] = (1 / len * 0.5 + 0.5) * 255;
            img.data[i + 3] = 255;
        }
    }
    g.putImageData(img, 0, 0);
    return new THREE.CanvasTexture(out);
}

// Tüy dokusu (renk + yükseklik): pul pul üst üste binen tüyler, sırtta beyaz benekler, karında
// krem zemin üzerinde kahverengi damla çizgiler; başta küçük baykuşa özgü beyaz kaş çizgileri.
// SphereGeometry'de ön yüz u = 0.25'e denk gelir.
function featherMaps({ belly = false, brows = false, seed = 7 }) {
    const W = 512;
    const H = 256;
    const color = document.createElement('canvas');
    const height = document.createElement('canvas');
    color.width = height.width = W;
    color.height = height.height = H;
    const g = color.getContext('2d');
    const h = height.getContext('2d', { willReadFrequently: true });
    let s = seed;
    const rand = () => ((s = (s * 16807) % 2147483647) / 2147483647);
    g.fillStyle = `rgb(${C.feather})`;
    g.fillRect(0, 0, W, H);
    h.fillStyle = '#000';
    h.fillRect(0, 0, W, H);
    const inBelly = (x, y) => belly && Math.hypot((x - W * 0.25) / (W * 0.15), (y - H * 0.66) / (H * 0.36)) < 1;
    for (let row = 0; row < 31; row++) {
        for (let col = 0; col < 64; col++) {
            const x = col * 8 + (row % 2) * 4 + (rand() - 0.5) * 2;
            const y = row * 8.5 + (rand() - 0.5) * 2;
            const r = 6 + rand() * 2.5;
            const base = inBelly(x, y) ? C.cream : C.feather;
            const j = (rand() - 0.5) * 24;
            g.fillStyle = `rgb(${base[0] + j}, ${base[1] + j * 0.9}, ${base[2] + j * 0.8})`;
            g.beginPath();
            g.arc(x, y, r, 0, Math.PI);
            g.fill();
            g.strokeStyle = inBelly(x, y) ? 'rgba(150, 120, 90, 0.3)' : 'rgba(40, 24, 14, 0.4)';
            g.lineWidth = 1;
            g.stroke();
            const hg = h.createRadialGradient(x, y + r * 0.2, 0, x, y, r);
            hg.addColorStop(0, '#fff');
            hg.addColorStop(1, '#333');
            h.fillStyle = hg;
            h.beginPath();
            h.arc(x, y, r, 0, Math.PI);
            h.fill();
        }
    }
    for (let i = 0; i < 280; i++) {
        const x = rand() * W;
        const y = rand() * H;
        if (inBelly(x, y)) {
            g.fillStyle = 'rgba(112, 78, 52, 0.7)';
            g.beginPath(); g.ellipse(x, y, 1.6 + rand(), 4.5 + rand() * 3, 0, 0, Math.PI * 2); g.fill();
        } else if (rand() < 0.7) {
            g.fillStyle = 'rgba(240, 230, 212, 0.85)';
            g.beginPath(); g.ellipse(x, y, 2 + rand() * 2, 1.6 + rand() * 1.6, 0, 0, Math.PI * 2); g.fill();
        }
    }
    if (brows) {
        g.strokeStyle = 'rgba(244, 236, 220, 0.95)';
        g.lineCap = 'round';
        g.lineWidth = 7;
        [-1, 1].forEach((side) => {
            g.beginPath();
            g.moveTo(W * 0.25 + side * 6, H * 0.5);
            g.quadraticCurveTo(W * 0.25 + side * 30, H * 0.38, W * 0.25 + side * 58, H * 0.44);
            g.stroke();
        });
    }
    const map = new THREE.CanvasTexture(color);
    map.colorSpace = THREE.SRGBColorSpace;
    map.anisotropy = 4;
    return { map, normalMap: heightToNormal(height, 2.5) };
}

// Kehribar iris: göz küresinin ön kutbunun çevresinde ışınsal lifler, dışta koyu halka
function irisTexture() {
    const W = 256;
    const H = 128;
    const c = document.createElement('canvas');
    c.width = W;
    c.height = H;
    const g = c.getContext('2d');
    g.fillStyle = '#20150d';
    g.fillRect(0, 0, W, H);
    const band = H * 0.3;
    const grad = g.createLinearGradient(0, 0, 0, band);
    grad.addColorStop(0, '#f8cf55');
    grad.addColorStop(0.55, '#eeae2e');
    grad.addColorStop(0.88, '#b56f17');
    grad.addColorStop(1, '#2e1b0b');
    g.fillStyle = grad;
    g.fillRect(0, 0, W, band);
    let s = 11;
    const rand = () => ((s = (s * 16807) % 2147483647) / 2147483647);
    for (let i = 0; i < 320; i++) {
        const x = rand() * W;
        g.strokeStyle = rand() < 0.5 ? 'rgba(255, 236, 150, 0.35)' : 'rgba(140, 72, 10, 0.3)';
        g.lineWidth = 1;
        g.beginPath();
        g.moveTo(x, rand() * band * 0.3);
        g.lineTo(x + (rand() - 0.5) * 3, band * (0.6 + rand() * 0.4));
        g.stroke();
    }
    const tex = new THREE.CanvasTexture(c);
    tex.colorSpace = THREE.SRGBColorSpace;
    return tex;
}

function createOwl() {
    const disposables = [];
    const keep = (o) => (disposables.push(o), o);
    const group = new THREE.Group();

    // Tüyler: hafif "sheen" ile kumaş gibi yumuşak bir parlaklık
    const plumage = (maps, color = '#ffffff') => keep(new THREE.MeshPhysicalMaterial({
        map: keep(maps.map),
        normalMap: keep(maps.normalMap),
        normalScale: new THREE.Vector2(0.6, 0.6),
        color,
        roughness: 0.92,
        sheen: 0.6,
        sheenColor: new THREE.Color('#f3dcc0'),
        sheenRoughness: 0.7,
    }));
    const bodyMat = plumage(featherMaps({ belly: true, seed: 3 }));
    const headMat = plumage(featherMaps({ brows: true, seed: 9 }));
    const wingMat = plumage(featherMaps({ seed: 21 }), '#e8dccd');
    const discMat = keep(new THREE.MeshPhysicalMaterial({ color: C.disc, roughness: 0.95, sheen: 0.5, sheenColor: new THREE.Color('#ffffff') }));
    const beakMat = keep(new THREE.MeshPhysicalMaterial({ color: C.beak, roughness: 0.38, clearcoat: 0.5, clearcoatRoughness: 0.3 }));
    const talonMat = keep(new THREE.MeshPhysicalMaterial({ color: C.talon, roughness: 0.3, clearcoat: 0.6 }));
    const goldMat = keep(new THREE.MeshStandardMaterial({ color: C.gold, metalness: 1, roughness: 0.25, emissive: new THREE.Color('#5a3a0e'), emissiveIntensity: 0 }));
    const sphere = keep(new THREE.SphereGeometry(1, 48, 32));

    // Gövde ve tüylü ayaklar, koyu pençeler
    const body = new THREE.Mesh(sphere, bodyMat);
    body.scale.set(1.05, 1.25, 0.95);
    body.position.y = -0.35;
    group.add(body);
    const toeGeo = keep(new THREE.SphereGeometry(0.12, 16, 12));
    const clawGeo = keep(new THREE.ConeGeometry(0.035, 0.12, 8).rotateX(Math.PI / 2));
    [-1, 1].forEach((s) => {
        for (let k = -1; k <= 1; k++) {
            const toe = new THREE.Mesh(toeGeo, discMat);
            toe.scale.set(0.8, 0.6, 1.4);
            toe.position.set(s * 0.38 + k * 0.12, -1.55, 0.55 + (k === 0 ? 0.06 : 0));
            const claw = new THREE.Mesh(clawGeo, talonMat);
            claw.position.set(toe.position.x, -1.6, toe.position.z + 0.17);
            claw.rotation.x = 0.5;
            group.add(toe, claw);
        }
    });

    // Baş: imlece döner; tüm yüz ögeleri ona bağlı
    const head = new THREE.Group();
    head.position.y = 1.1;
    group.add(head);
    const skull = new THREE.Mesh(sphere, headMat);
    skull.scale.set(1.08, 0.9, 0.95);
    head.add(skull);

    // Yüz diski, gözler ve göz kapakları
    const irisMap = keep(irisTexture());
    const eyeGeo = keep(new THREE.SphereGeometry(0.27, 40, 24).rotateX(Math.PI / 2)); // kutup öne baksın
    const eyes = [-1, 1].map((s) => {
        const disc = new THREE.Mesh(sphere, discMat);
        disc.scale.set(0.46, 0.46, 0.2);
        disc.position.set(s * 0.42, -0.04, 0.74);
        disc.rotation.y = s * 0.35;
        head.add(disc);

        const eye = new THREE.Group();
        eye.position.set(s * 0.41, -0.02, 0.8);
        eye.rotation.y = s * 0.3;
        head.add(eye);
        const look = new THREE.Group(); // göz bebeği bu grupla döner
        eye.add(look);
        // Islak, parlak göz: vernik katmanı ortam yansımasını taşır
        const iris = new THREE.Mesh(eyeGeo, keep(new THREE.MeshPhysicalMaterial({
            map: irisMap, roughness: 0.35, clearcoat: 1, clearcoatRoughness: 0.04, emissive: new THREE.Color('#5a3405'), emissiveIntensity: 0.25,
        })));
        look.add(iris);
        const pupil = new THREE.Mesh(keep(new THREE.SphereGeometry(0.13, 24, 16)), keep(new THREE.MeshPhysicalMaterial({ color: '#050302', roughness: 0.2, clearcoat: 1, clearcoatRoughness: 0.03 })));
        pupil.scale.z = 0.35;
        pupil.position.z = 0.24;
        look.add(pupil);
        const shine = new THREE.Mesh(keep(new THREE.SphereGeometry(0.03, 10, 8)), keep(new THREE.MeshBasicMaterial({ color: '#ffffff' })));
        shine.position.set(0.06, 0.07, 0.258);
        look.add(shine);

        // Kapaklar: yarım küre kabuklar; x ekseninde dönerek gözü örter
        const upper = new THREE.Mesh(keep(new THREE.SphereGeometry(0.32, 32, 12, 0, Math.PI * 2, 0, Math.PI / 2)), headMat);
        const lower = new THREE.Mesh(keep(new THREE.SphereGeometry(0.315, 32, 12, 0, Math.PI * 2, Math.PI / 2, Math.PI / 2)), headMat);
        const upperPivot = new THREE.Group();
        upperPivot.add(upper);
        eye.add(upperPivot, lower);
        return { s, look, pupil, upper, upperPivot, lower, up: -1.0, low: 1.25 };
    });

    // Gaga: alt parça konuşurken açılır
    const beakTop = new THREE.Mesh(keep(new THREE.ConeGeometry(0.11, 0.32, 16)), beakMat);
    beakTop.rotation.x = Math.PI * 0.62;
    beakTop.position.set(0, -0.2, 0.95);
    head.add(beakTop);
    const jaw = new THREE.Group();
    jaw.position.set(0, -0.3, 0.88);
    head.add(jaw);
    const beakLow = new THREE.Mesh(keep(new THREE.ConeGeometry(0.07, 0.16, 12)), beakMat);
    beakLow.rotation.x = Math.PI * 0.6;
    beakLow.position.set(0, -0.02, 0.06);
    jaw.add(beakLow);

    // Altın defne tacı
    const leafGeo = keep(new THREE.SphereGeometry(1, 10, 8).scale(0.07, 0.025, 0.17));
    const wreath = new THREE.Group();
    wreath.position.y = 0.5;
    wreath.rotation.x = -0.15;
    head.add(wreath);
    for (let k = 0; k < 16; k++) {
        const a = (k / 16) * Math.PI * 2;
        const leaf = new THREE.Mesh(leafGeo, goldMat);
        leaf.position.set(Math.cos(a) * 0.86, 0, Math.sin(a) * 0.8);
        leaf.rotation.set(0, -a + (k % 2 ? 0.5 : -0.5), k % 2 ? 0.4 : -0.4);
        wreath.add(leaf);
    }

    // Kanatlar
    const wings = [-1, 1].map((s) => {
        const pivot = new THREE.Group();
        pivot.position.set(s * 0.95, 0.15, -0.05);
        const wing = new THREE.Mesh(sphere, wingMat);
        wing.scale.set(0.3, 0.95, 0.66);
        wing.position.set(s * 0.12, -0.6, 0);
        pivot.add(wing);
        group.add(pivot);
        return { pivot, s };
    });

    // Ruh hallerine göre göz kapağı açıları [üst, alt] ve göz bebeği boyu
    const MOODS = {
        idle: [-1.0, 1.25, 1],
        happy: [-0.55, 0.12, 1],
        mischief: [0.12, 1.0, 0.85],
        surprised: [-1.4, 1.45, 0.62],
        sleep: [1.45, -0.05, 1],
    };

    let flapPhase = 0;
    let blinkStart = -10;
    let nextBlink = 2;

    // st: { dt, flap, wave, sleep, vx, lookX, lookY, mood, mouth, excite, headSpin }
    const animate = (t, st) => {
        // Kanat fazı biriktirilir: hız değişince kanatlar zıplamaz
        flapPhase += st.dt * 13 * st.flap;
        const beat = Math.sin(flapPhase);
        const spread = 0.65 * (1 - st.sleep * 0.75);
        wings.forEach(({ pivot, s }, i) => {
            const waving = i === 1 ? st.wave * (1.6 + Math.sin(t * 13) * 0.4) : 0;
            pivot.rotation.z = s * (0.12 + (beat * 0.5 + 0.5) * spread + waving);
        });

        // Baş imlece döner, merakla yana yatar; uykuda öne düşer; kaçarken baykuşça tam tur atar
        head.rotation.set(st.lookY * 0.25 + st.sleep * 0.3, st.lookX * 0.55 + st.headSpin, Math.sin(t * 0.9) * 0.12 * (1 - st.sleep));

        // Göz kırpma (bazen çift)
        if (t > nextBlink) {
            blinkStart = t;
            nextBlink = t + (Math.random() < 0.2 ? 0.3 : 2.5 + Math.random() * 3);
        }
        const bp = (t - blinkStart) / 0.18;
        const blink = bp < 1 ? Math.sin(bp * Math.PI) : 0;
        const [up, low, pupil] = MOODS[st.mood];
        eyes.forEach((e) => {
            e.up = damp(e.up, up, 0.25);
            e.low = damp(e.low, low, 0.25);
            e.upperPivot.rotation.set(e.up + (1.45 - e.up) * blink, 0, st.mood === 'mischief' ? e.s * 0.35 : 0);
            e.lower.rotation.x = e.low;
            e.look.rotation.set(-st.lookY * 0.35, st.lookX * 0.5, 0);
            e.pupil.scale.x = e.pupil.scale.y = damp(e.pupil.scale.x, pupil, 0.2);
        });

        jaw.rotation.x = st.mouth * 0.45;

        // Heyecanlanınca defne tacı hafifçe parlar
        goldMat.emissiveIntensity = st.excite * (0.6 + Math.sin(t * 9) * 0.3);
    };

    return { group, animate, dispose: () => disposables.forEach((d) => d.dispose()) };
}

// ── Rehber ──
function startGuide() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return startStatic();
    const finePointer = window.matchMedia('(pointer: fine)').matches;
    const isMobile = () => window.innerWidth < 768;
    let size = isMobile() ? 120 : 170;

    const fxLayer = document.createElement('div');
    fxLayer.className = 'owl-fx';
    // Balon: ekran okuyucuya tam metin bir kez; görünen metin daktilo gibi yazılır.
    // Henüz yazılmayan kısım görünmez yer tutar, böylece balon büyüyüp kaymaz.
    const bubble = document.createElement('div');
    bubble.className = 'owl-bubble';
    bubble.setAttribute('role', 'status');
    bubble.setAttribute('aria-live', 'polite');
    const srText = document.createElement('span');
    srText.className = 'owl-sr';
    const typed = document.createElement('span');
    typed.setAttribute('aria-hidden', 'true');
    const ghost = document.createElement('span');
    ghost.className = 'owl-ghost';
    ghost.setAttribute('aria-hidden', 'true');
    bubble.append(srText, typed, ghost);
    const wrap = document.createElement('div');
    wrap.className = 'owl';
    wrap.setAttribute('aria-hidden', 'true');
    const canvas = document.createElement('canvas');
    wrap.appendChild(canvas);
    document.body.append(fxLayer, bubble, wrap);

    let renderer;
    try {
        renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true, powerPreference: 'low-power' });
    } catch {
        wrap.remove(); bubble.remove(); fxLayer.remove();
        return;
    }
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.toneMapping = THREE.ACESFilmicToneMapping;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(30, 1, 0.1, 100);
    camera.position.set(0, 0.4, 14);
    // Ortam ışığı: sitedeki gün batımına uyan sıcak gökyüzü, toprak rengi zemin
    const pmrem = new THREE.PMREMGenerator(renderer);
    const envScene = new THREE.Scene();
    envScene.add(new THREE.Mesh(new THREE.SphereGeometry(10, 32, 16), new THREE.ShaderMaterial({
        side: THREE.BackSide,
        vertexShader: 'varying vec3 vDir; void main() { vDir = position; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }',
        fragmentShader: `varying vec3 vDir;
            void main() {
                float h = normalize(vDir).y;
                vec3 sky = mix(vec3(1.0, 0.72, 0.45), vec3(0.55, 0.6, 0.85), smoothstep(0.0, 0.8, h));
                gl_FragColor = vec4(h > 0.0 ? sky * 1.4 : vec3(0.35, 0.26, 0.2), 1.0);
            }`,
    })));
    scene.environment = pmrem.fromScene(envScene, 0.04).texture;
    pmrem.dispose();
    // Ana ışık soldan, akropolisteki güneşle aynı yönden; sağ arkadan soğuk kenar ışığı
    const sun = new THREE.DirectionalLight('#ffc890', 2.6);
    sun.position.set(-5, 3, 5);
    const rim = new THREE.DirectionalLight('#b9c8ff', 1.2);
    rim.position.set(4, 2, -5);
    scene.add(sun, rim);
    const rig = createOwl();
    scene.add(rig.group);

    const applySize = () => {
        size = isMobile() ? 120 : 170;
        renderer.setSize(size, size, false);
        canvas.style.width = canvas.style.height = `${size}px`;
    };
    applySize();
    window.addEventListener('resize', applySize);

    const pos = { x: window.innerWidth + size, y: window.innerHeight * 0.4, vx: 0, vy: 0 };
    const cursor = { x: 0, y: 0, seen: false };
    const look = { x: 0, y: 0 };
    const squash = { x: 0, v: 0 };
    let lastStop = -1;
    let talk = { chars: [], shown: 0, start: 0, until: 0 };
    let flee = null;
    let fleeCooldown = 0;
    let tap = null;
    let spin = null;
    let headSpin = null;
    let yaw = 0;
    let happyUntil = 0;
    let surprisedUntil = 0;
    let waveUntil = 0;
    let wave = 0;
    let excite = 0;
    let sleepAmt = 0;
    let sleeping = false;
    let lastActive = performance.now();
    let nextZ = 0;
    let lastSpark = 0;
    const clock = new THREE.Clock();

    const say = (text, ms) => {
        const now = performance.now();
        talk = { chars: Array.from(text), shown: -1, start: now, until: now + ms };
        srText.textContent = text;
    };

    const currentStop = () => {
        let index = 0;
        STOPS.forEach((stop, i) => {
            const el = document.getElementById(stop.id);
            if (el && el.getBoundingClientRect().top < window.innerHeight * 0.55) index = i;
        });
        return index;
    };

    // Her durakta içeriğin karşı tarafında durur (hakkımda ve projeler solda, yetenekler sağda);
    // mobilde sağ altta
    const SIDES = ['right', 'right', 'left', 'right', 'right'];
    const anchorFor = (index) => {
        const w = window.innerWidth;
        const h = window.innerHeight;
        if (isMobile()) return { x: w - size * 0.5 - 6, y: h - size * 0.5 - (index % 2 ? 150 : 90) };
        const margin = size * 0.7;
        const right = SIDES[index] === 'right';
        return { x: right ? w - margin : margin, y: h * (0.62 + (index % 3) * 0.06) };
    };

    // Uçuşan küçük öğeler (yaprak, kıvılcım, "ζ")
    const spawn = (className, x, y, text, { dx = 0, dy = 0, fontSize = 0, delay = 0, life = 1300, rot = 0 } = {}) => {
        const el = document.createElement('span');
        el.className = className;
        el.textContent = text;
        el.style.left = `${x}px`;
        el.style.top = `${y}px`;
        el.style.setProperty('--dx', `${dx}px`);
        el.style.setProperty('--dy', `${dy}px`);
        el.style.setProperty('--rot', `${rot}deg`);
        if (fontSize) el.style.fontSize = `${fontSize}px`;
        if (delay) el.style.animationDelay = `${delay}ms`;
        fxLayer.appendChild(el);
        setTimeout(() => el.remove(), life + delay);
    };

    // Dokunuşta altın defne yaprakları saçılır
    const leaves = (x, y) => {
        for (let i = 0; i < 8; i++) {
            spawn('owl-leaf', x, y, '', {
                dx: (Math.random() - 0.5) * 110,
                dy: -30 - Math.random() * 70,
                rot: (Math.random() - 0.5) * 360,
                delay: i * 35,
            });
        }
    };

    const touch = (el, x, y, t) => {
        leaves(x, y);
        happyUntil = performance.now() + 1300;
        spin = { start: t, dur: 0.7 };
        squash.v -= 0.14; // dokunuşta ezilip geri yaylanır
        el.classList.remove('owl-magic');
        void el.offsetWidth; // animasyonu yeniden başlat
        el.classList.add('owl-magic');
        setTimeout(() => el.classList.remove('owl-magic'), 700);
    };

    // Herhangi bir hareket uyandırır: irkilir, zıplar ve bir şey söyler
    const wake = () => {
        lastActive = performance.now();
        if (!sleeping) return;
        sleeping = false;
        surprisedUntil = lastActive + 700;
        squash.v += 0.12;
        pos.vy -= 6;
        say(pick(WAKE_LINES), 2200);
    };
    ['pointermove', 'pointerdown', 'keydown', 'wheel', 'scroll', 'touchstart'].forEach((type) => {
        window.addEventListener(type, wake, { passive: true });
    });

    window.addEventListener('pointermove', (e) => {
        cursor.x = e.clientX;
        cursor.y = e.clientY;
        cursor.seen = true;
    }, { passive: true });
    window.addEventListener('pointerdown', (e) => {
        if (e.button !== 0) return;
        const el = e.target instanceof Element ? e.target.closest(CLICKABLE) : null;
        if (!el) return;
        tap = { x: e.clientX, y: e.clientY, el, touched: false, until: performance.now() + 1100 };
    }, { capture: true, passive: true });

    const tick = () => {
        requestAnimationFrame(tick);
        if (document.hidden) return;
        const dt = Math.min(clock.getDelta(), 0.05);
        const t = clock.elapsedTime;
        const now = performance.now();
        const w = window.innerWidth;
        const h = window.innerHeight;

        const stop = currentStop();
        if (stop !== lastStop) {
            lastStop = stop;
            say(STOPS[stop].text, 7500);
            waveUntil = now + 1700;
        }

        if (!sleeping && now - lastActive > SLEEP_AFTER && !tap && !flee && now > talk.until) {
            sleeping = true;
            nextZ = now + 600;
        }

        const anchor = anchorFor(stop);
        let tx = anchor.x + Math.sin(t * 0.9) * 22;
        let ty = anchor.y + Math.cos(t * 1.3) * 14;
        let stiffness = 0.04;
        let damping = 0.84;
        if (sleeping) {
            // Uykuda yerinde, biraz aşağıda süzülür
            tx = anchor.x;
            ty = anchor.y + 24 + Math.sin(t * 0.8) * 6;
            stiffness = 0.02;
        }

        // Fare yaklaşınca kaç: önce şaşırır, başını tam tur çevirir, sonra sıvışır
        if (finePointer && !tap) {
            const dist = Math.hypot(cursor.x - pos.x, cursor.y - pos.y);
            if (cursor.seen && dist < size * 0.55 && now > fleeCooldown) {
                const ax = (pos.x - cursor.x) / (dist || 1);
                const ay = (pos.y - cursor.y) / (dist || 1);
                flee = { dx: ax * 230, dy: ay * 170 - 40, until: now + 1500 };
                fleeCooldown = now + 700;
                surprisedUntil = now + 260;
                headSpin = { start: t, dur: 0.9, dir: -(Math.sign(ax) || 1) };
                if (Math.random() < 0.4) say(pick(FLEE_LINES), 1600);
            }
        }
        if (flee && now < flee.until) {
            tx = anchor.x + flee.dx;
            ty = anchor.y + flee.dy;
            stiffness = 0.09;
            damping = 0.78;
        } else flee = null;

        // Butona dokunma
        if (tap) {
            tx = tap.x;
            ty = tap.y - size * 0.3;
            stiffness = 0.16;
            damping = 0.7;
            if (!tap.touched && Math.hypot(tx - pos.x, ty - pos.y) < 14) {
                tap.touched = true;
                tap.until = now + 650;
                touch(tap.el, tap.x, tap.y, t);
            }
            if (now > tap.until) tap = null;
        }

        // Ekrandan taşmasın (üstte menü payı)
        const half = size * 0.5;
        tx = Math.min(w - half, Math.max(half, tx));
        ty = Math.min(h - half, Math.max(half + 70, ty));

        pos.vx = (pos.vx + (tx - pos.x) * stiffness) * damping;
        pos.vy = (pos.vy + (ty - pos.y) * stiffness) * damping;
        pos.x += pos.vx;
        pos.y += pos.vy;
        wrap.style.transform = `translate3d(${pos.x - size / 2}px, ${pos.y - size / 2}px, 0)`;

        const showTalk = now < talk.until && !tap && !sleeping;
        const onRight = pos.x > w / 2;

        // Model: yöne döner, eğilir; dokunurken piruet atar
        const speed = Math.hypot(pos.vx, pos.vy);
        const wantYaw = Math.abs(pos.vx) > 0.8 ? Math.sign(pos.vx) * 0.7 : Math.sin(t * 0.7) * 0.22 * (1 - sleepAmt);
        yaw += (wantYaw - yaw) * 0.08;
        const ease = (p) => 1 - Math.pow(1 - p, 3);
        let spinY = 0;
        if (spin) {
            const p = (t - spin.start) / spin.dur;
            if (p >= 1) spin = null;
            else spinY = ease(p) * Math.PI * 2;
        }
        let headTurn = 0;
        if (headSpin) {
            const p = (t - headSpin.start) / headSpin.dur;
            if (p >= 1) headSpin = null;
            else headTurn = ease(p) * Math.PI * 2 * headSpin.dir;
        }
        rig.group.rotation.set(
            clamp(pos.vy * 0.02, -0.3, 0.3),
            yaw + spinY,
            clamp(-pos.vx * 0.035, -0.35, 0.35)
        );

        // Esneyip ezilme: dikey hızda uzar, dokunuş/uyanışta yay gibi seker
        squash.v = (squash.v - squash.x * 0.2) * 0.82;
        squash.x += squash.v;
        const stretch = Math.min(Math.abs(pos.vy) * 0.008, 0.1) + squash.x;
        rig.group.scale.set(1 - stretch * 0.5, 1 + stretch, 1 - stretch * 0.5);
        rig.group.position.y = Math.sin(t * (2.1 - sleepAmt)) * 0.2;

        // Bakış: imleci, dokunacağı butonu ya da konuşurken balonu izler
        let lx = 0;
        let ly = 0;
        if (sleeping) ly = 0.4;
        else if (tap) { lx = (tap.x - pos.x) / 200; ly = 1; }
        else if (finePointer && cursor.seen) { lx = (cursor.x - pos.x) / 380; ly = (cursor.y - pos.y) / 300; }
        else if (showTalk) lx = onRight ? -0.6 : 0.6;
        look.x += (clamp(lx, -1, 1) - look.x) * 0.15;
        look.y += (clamp(ly, -1, 1) - look.y) * 0.15;

        let mood = 'idle';
        if (sleeping) mood = 'sleep';
        else if (flee) mood = 'mischief';
        if (now < happyUntil) mood = 'happy';
        if (now < surprisedUntil) mood = 'surprised';

        // Daktilo: harf harf yazılır, yazarken gaga oynar
        const shown = Math.min(talk.chars.length, Math.floor(((now - talk.start) / 1000) * TYPE_SPEED));
        if (shown !== talk.shown) {
            talk.shown = shown;
            typed.textContent = talk.chars.slice(0, shown).join('');
            ghost.textContent = talk.chars.slice(shown).join('');
        }
        const mouth = showTalk && shown < talk.chars.length ? Math.abs(Math.sin(t * 16)) : 0;

        wave += ((now < waveUntil ? 1 : 0) - wave) * 0.12;
        excite += ((now < happyUntil ? 1 : flee ? 0.6 : 0) - excite) * 0.08;
        sleepAmt += ((sleeping ? 1 : 0) - sleepAmt) * 0.04;

        rig.animate(t, {
            dt,
            flap: (1 + Math.min(speed / 7, 1.3)) * (1 - sleepAmt * 0.7),
            wave,
            sleep: sleepAmt,
            vx: pos.vx,
            lookX: look.x,
            lookY: look.y,
            mood,
            mouth,
            excite,
            headSpin: headTurn,
        });
        renderer.render(scene, camera);

        // Hızlı uçarken arkasında altın kıvılcım izi
        if (speed > 6 && now - lastSpark > 40) {
            lastSpark = now;
            spawn('owl-spark', pos.x - pos.vx * 2 + (Math.random() - 0.5) * size * 0.3, pos.y + size * 0.1 - pos.vy * 2 + (Math.random() - 0.5) * size * 0.3, '', { life: 700 });
        }
        // Uykuda "ζ" çıkarır
        if (sleeping && now > nextZ) {
            nextZ = now + 1200;
            spawn('owl-z', pos.x + size * 0.18, pos.y - size * 0.3, 'ζ', {
                dx: 25 + Math.random() * 20,
                dy: -60,
                fontSize: 16 + Math.random() * 8,
                life: 1900,
            });
        }

        // Konuşma balonu: ekranın ortasına doğru açılır
        bubble.classList.toggle('owl-bubble-show', showTalk);
        bubble.classList.toggle('owl-bubble-left', onRight);
        const bw = bubble.offsetWidth;
        const bx = onRight ? pos.x - size * 0.38 - bw : pos.x + size * 0.38;
        const by = pos.y - size * 0.42;
        bubble.style.transform = `translate3d(${Math.max(8, Math.min(w - bw - 8, bx))}px, ${Math.max(8, by)}px, 0)`;
    };
    tick();
}

// "Hareketi azalt" tercihinde: uçan baykuş yerine dikkat dağıtmamak için
// yalnızca ilk karşılama balonu gösterilir.
function startStatic() {
    const bubble = document.createElement('div');
    bubble.className = 'owl-bubble owl-bubble-show';
    bubble.setAttribute('role', 'status');
    bubble.textContent = STOPS[0].text;
    bubble.style.transform = 'translate3d(16px, calc(100vh - 140px), 0)';
    document.body.appendChild(bubble);
    setTimeout(() => bubble.remove(), 8000);
}

startGuide();
