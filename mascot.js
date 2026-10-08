// ============================================
// bekirr.dev — Byte: siteyi tanıtan 3D rehber
// Allay esinli ama özgün bir karakter: yüzü piksel bir ekran, kafasında parlayan anten,
// yarı saydam gövde ve dijital kanatlar. Kaydırdıkça bölümleri anlatır, fareden kaçar,
// tıklanan butona uçup dokunur ve nota saçar.
// ============================================

import * as THREE from 'three';

// ── Konuşmalar ──
const STOPS = [
    { id: 'hero', text: "Selam! Ben Byte 🤖 Bekir'in dijital yardımcısıyım. Aşağı kaydır, sana onu tanıtayım!" },
    { id: 'about', text: 'Bekir full-stack bir geliştirici: Node.js, Electron ve React ile hızlı ve şık sistemler kuruyor.' },
    { id: 'skills', text: "İşte araç çantası! Frontend'den backend'e, hepsi günlük işler 🛠️" },
    { id: 'projects', text: 'Projeler burada. Kaydırmaya devam et, Bekofy ve LuckMC seni bekliyor 🎧⚔️' },
    { id: 'contact', text: 'Bir fikrin mi var? Formu doldur, mesajını ben uçururum ✉️' },
];
const FLEE_LINES = ['Yakalayamazsın! 😄', 'Hop! Buradayım 💨', 'Bip bop, kaçtım! 🎶'];
const CLICKABLE = 'button, a[href], [role="button"], input[type="submit"], summary';

// ── Palet ──
const C = { base: '#62dcf5', light: '#a6f3ff', lighter: '#d4fbff', dark: '#38b3db', deep: '#0d5fa8', screen: '#0a1a3a', bezel: '#2b8fc4', pixel: '#7df0ff' };
const KEY = { L: C.light, W: C.lighter, B: C.base, D: C.dark, X: C.deep, S: C.screen, Z: C.bezel, P: C.pixel };

function pixelTexture(rows) {
    const c = document.createElement('canvas');
    c.width = c.height = rows.length;
    const g = c.getContext('2d');
    rows.forEach((row, y) => row.split('').forEach((ch, x) => {
        if (KEY[ch]) { g.fillStyle = KEY[ch]; g.fillRect(x, y, 1, 1); }
    }));
    const tex = new THREE.CanvasTexture(c);
    tex.magFilter = tex.minFilter = THREE.NearestFilter; // keskin, pikselli görünüm
    tex.colorSpace = THREE.SRGBColorSpace;
    return tex;
}

// Yüz: çerçeveli ekran, piksel gözler ve gülümseme
const FACE_OPEN = ['ZZZZZZZZ', 'ZSSSSSSZ', 'ZSPSSPSZ', 'ZSPSSPSZ', 'ZSSSSSSZ', 'ZSPSSPSZ', 'ZSSPPSSZ', 'ZZZZZZZZ'];
const FACE_BLINK = ['ZZZZZZZZ', 'ZSSSSSSZ', 'ZSSSSSSZ', 'ZSPSSPSZ', 'ZSSSSSSZ', 'ZSPSSPSZ', 'ZSSPPSSZ', 'ZZZZZZZZ'];
const SIDE = ['LLLLLLLL', 'LBBBLBBL', 'BBBBBBBB', 'BBLBBBDB', 'BBBBBBBB', 'BDBBLBBB', 'DBBBBBBD', 'DDDDDDDD'];
const TOP = ['WLLLLLLW', 'LLLWLLLL', 'LLLLLLWL', 'LWLLLLLL', 'LLLLLWLL', 'LLWLLLLL', 'LLLLLLLL', 'WLLLWLLW'];
// Dijital kanat: ızgara desenli buz mavisi
const WING = ['..WWWW..', '.WLWLWW.', 'WLWLWLLW', 'WWLWLWLW', 'WLWLWLLW', '.WLWLWW.', '..WLLW..', '...WW...'];

function createByte() {
    const disposables = [];
    const keep = (o) => (disposables.push(o), o);
    const group = new THREE.Group();

    // Kafa: her yüze piksel doku; parıltı dokudan gelir (koyu ekran koyu kalır)
    const faceOpen = keep(pixelTexture(FACE_OPEN));
    const faceBlink = keep(pixelTexture(FACE_BLINK));
    const side = keep(pixelTexture(SIDE));
    const top = keep(pixelTexture(TOP));
    const mat = (map) => keep(new THREE.MeshLambertMaterial({ map, emissiveMap: map, emissive: new THREE.Color('#ffffff'), emissiveIntensity: 0.45 }));
    const faceMat = mat(faceOpen);
    // BoxGeometry yüz sırası: +x, -x, +y, -y, +z (ön), -z
    const head = new THREE.Mesh(keep(new THREE.BoxGeometry(2.6, 2.4, 2.4)), [mat(side), mat(side), mat(top), mat(side), faceMat, mat(side)]);
    head.position.y = 1.35;
    group.add(head);

    // Anten + parlayan uç
    const antenna = new THREE.Mesh(keep(new THREE.BoxGeometry(0.16, 0.8, 0.16)), keep(new THREE.MeshLambertMaterial({ color: C.dark })));
    antenna.position.set(0.55, 2.95, 0);
    group.add(antenna);
    const tipMat = keep(new THREE.MeshBasicMaterial({ color: C.lighter }));
    const tip = new THREE.Mesh(keep(new THREE.SphereGeometry(0.24, 16, 16)), tipMat);
    tip.position.set(0.55, 3.45, 0);
    group.add(tip);

    // Gövde: yarı saydam, içinde parlak çekirdek
    const body = new THREE.Mesh(keep(new THREE.BoxGeometry(1.5, 1.8, 1)), keep(new THREE.MeshLambertMaterial({
        color: '#9ff0ff', emissive: new THREE.Color(C.light), emissiveIntensity: 0.55, transparent: true, opacity: 0.72,
    })));
    body.position.y = -0.8;
    group.add(body);
    const core = new THREE.Mesh(keep(new THREE.BoxGeometry(0.85, 1.1, 0.5)), keep(new THREE.MeshBasicMaterial({ color: C.lighter, transparent: true, opacity: 0.85 })));
    core.position.y = -0.75;
    group.add(core);

    // Kollar
    const armMat = keep(new THREE.MeshLambertMaterial({ color: C.base, emissive: new THREE.Color(C.base), emissiveIntensity: 0.3, transparent: true, opacity: 0.92 }));
    const armGeo = keep(new THREE.BoxGeometry(0.42, 1.4, 0.42));
    const arms = [-1, 1].map((s) => {
        const pivot = new THREE.Group();
        pivot.position.set(s * 0.97, -0.15, 0.1);
        const arm = new THREE.Mesh(armGeo, armMat);
        arm.position.y = -0.65;
        pivot.add(arm);
        group.add(pivot);
        return pivot;
    });

    // Kanatlar
    const wingMat = keep(new THREE.MeshBasicMaterial({ map: keep(pixelTexture(WING)), transparent: true, opacity: 0.78, side: THREE.DoubleSide, depthWrite: false }));
    const wingGeo = keep(new THREE.PlaneGeometry(2.4, 2.4));
    const wings = [-1, 1].map((s) => {
        const pivot = new THREE.Group();
        pivot.position.set(s * 0.25, -0.35, -0.55);
        const wing = new THREE.Mesh(wingGeo, wingMat);
        wing.position.x = s * 1.2;
        wing.rotation.y = s * -0.2;
        pivot.add(wing);
        group.add(pivot);
        return { pivot, s };
    });

    // Arkada yumuşak ışıltı
    const glowCanvas = document.createElement('canvas');
    glowCanvas.width = glowCanvas.height = 64;
    const gg = glowCanvas.getContext('2d');
    const grad = gg.createRadialGradient(32, 32, 0, 32, 32, 32);
    grad.addColorStop(0, 'rgba(140, 238, 255, 0.55)');
    grad.addColorStop(1, 'rgba(140, 238, 255, 0)');
    gg.fillStyle = grad;
    gg.fillRect(0, 0, 64, 64);
    const glow = new THREE.Sprite(keep(new THREE.SpriteMaterial({ map: keep(new THREE.CanvasTexture(glowCanvas)), blending: THREE.AdditiveBlending, depthWrite: false })));
    glow.scale.set(7.5, 7.5, 1);
    glow.position.set(0, 0.4, -1);
    group.add(glow);

    let blinkUntil = 0;
    let nextBlink = 2;
    const animate = (t, flap, swing) => {
        const beat = Math.sin(t * 14 * flap);
        wings.forEach(({ pivot, s }) => { pivot.rotation.y = s * (0.55 + beat * 0.55); });
        arms[0].rotation.set(Math.sin(t * 3) * 0.25 + swing, 0, -0.15);
        arms[1].rotation.set(Math.sin(t * 3 + Math.PI) * 0.25 + swing, 0, 0.15);
        head.rotation.z = Math.sin(t * 1.6) * 0.06;
        antenna.rotation.z = tip.rotation.z = Math.sin(t * 2.4) * 0.12;
        const pulse = 0.6 + Math.sin(t * 4) * 0.4;
        tipMat.color.setRGB(0.55 + pulse * 0.45, 0.95, 1);
        glow.material.opacity = 0.75 + Math.sin(t * 2.2) * 0.2;
        // Göz kırpma
        if (t > nextBlink) { blinkUntil = t + 0.14; nextBlink = t + 2.5 + Math.random() * 3; }
        const map = t < blinkUntil ? faceBlink : faceOpen;
        if (faceMat.map !== map) { faceMat.map = faceMat.emissiveMap = map; faceMat.needsUpdate = true; }
    };

    return { group, animate, dispose: () => disposables.forEach((d) => d.dispose()) };
}

// ── Rehber ──
function startGuide() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return startStatic();
    const finePointer = window.matchMedia('(pointer: fine)').matches;
    const isMobile = () => window.innerWidth < 768;
    let size = isMobile() ? 130 : 190;

    const noteLayer = document.createElement('div');
    noteLayer.className = 'nd-note-layer';
    const bubble = document.createElement('div');
    bubble.className = 'nd-bubble';
    bubble.setAttribute('role', 'status');
    bubble.setAttribute('aria-live', 'polite');
    const wrap = document.createElement('div');
    wrap.className = 'nd-allay';
    wrap.setAttribute('aria-hidden', 'true');
    const canvas = document.createElement('canvas');
    wrap.appendChild(canvas);
    document.body.append(noteLayer, bubble, wrap);

    let renderer;
    try {
        renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true, powerPreference: 'low-power' });
    } catch {
        wrap.remove(); bubble.remove(); noteLayer.remove();
        return;
    }
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(30, 1, 0.1, 100);
    camera.position.set(0, 0.6, 17);
    scene.add(new THREE.AmbientLight(0xffffff, 1.15));
    const sun = new THREE.DirectionalLight(0xffffff, 0.9);
    sun.position.set(3, 4, 6);
    scene.add(sun);
    const rig = createByte();
    scene.add(rig.group);

    const applySize = () => {
        size = isMobile() ? 130 : 190;
        renderer.setSize(size, size, false);
        canvas.style.width = canvas.style.height = `${size}px`;
    };
    applySize();
    window.addEventListener('resize', applySize);

    const pos = { x: window.innerWidth + size, y: window.innerHeight * 0.4, vx: 0, vy: 0 };
    const cursor = { x: -9999, y: -9999 };
    let lastStop = -1;
    let talk = { text: '', until: 0 };
    let flee = null;
    let fleeCooldown = 0;
    let tap = null;
    let spinStart = -1;
    let yaw = 0;
    const clock = new THREE.Clock();

    const say = (text, ms) => { talk = { text, until: performance.now() + ms }; };

    const currentStop = () => {
        let index = 0;
        STOPS.forEach((stop, i) => {
            const el = document.getElementById(stop.id);
            if (el && el.getBoundingClientRect().top < window.innerHeight * 0.55) index = i;
        });
        return index;
    };

    // Duraklar ekranın bir sağında bir solunda; mobilde sağ altta
    const anchorFor = (index) => {
        const w = window.innerWidth;
        const h = window.innerHeight;
        if (isMobile()) return { x: w - size * 0.5 - 6, y: h - size * 0.5 - (index % 2 ? 150 : 90) };
        const margin = size * 0.62;
        const right = index % 2 === 0;
        return { x: right ? w - margin : margin, y: h * (0.5 + (index % 3) * 0.08) };
    };

    const notes = (x, y) => {
        const glyphs = ['♪', '♫', '♩', '♬'];
        for (let i = 0; i < 7; i++) {
            const n = document.createElement('span');
            n.className = 'nd-note';
            n.textContent = glyphs[i % glyphs.length];
            n.style.left = `${x}px`;
            n.style.top = `${y}px`;
            n.style.setProperty('--dx', `${(Math.random() - 0.5) * 90}px`);
            n.style.setProperty('--dy', `${-40 - Math.random() * 60}px`);
            n.style.animationDelay = `${i * 40}ms`;
            n.style.fontSize = `${14 + Math.random() * 10}px`;
            noteLayer.appendChild(n);
            setTimeout(() => n.remove(), 1300);
        }
    };

    const touch = (el, x, y) => {
        notes(x, y);
        spinStart = clock.elapsedTime;
        el.classList.remove('nd-magic');
        void el.offsetWidth; // animasyonu yeniden başlat
        el.classList.add('nd-magic');
        setTimeout(() => el.classList.remove('nd-magic'), 700);
    };

    window.addEventListener('pointermove', (e) => { cursor.x = e.clientX; cursor.y = e.clientY; }, { passive: true });
    window.addEventListener('pointerdown', (e) => {
        if (e.button !== 0) return;
        const el = e.target instanceof Element ? e.target.closest(CLICKABLE) : null;
        if (!el) return;
        tap = { x: e.clientX, y: e.clientY, el, touched: false, until: performance.now() + 1100 };
    }, { capture: true, passive: true });

    const tick = () => {
        requestAnimationFrame(tick);
        if (document.hidden) return;
        const t = clock.getElapsedTime();
        const now = performance.now();
        const w = window.innerWidth;
        const h = window.innerHeight;

        const stop = currentStop();
        if (stop !== lastStop) {
            lastStop = stop;
            say(STOPS[stop].text, 7500);
        }

        const anchor = anchorFor(stop);
        let tx = anchor.x + Math.sin(t * 0.9) * 26;
        let ty = anchor.y + Math.cos(t * 1.3) * 16;
        let stiffness = 0.04;
        let damping = 0.84;

        // Fare yaklaşınca kaç
        if (finePointer && !tap) {
            const dist = Math.hypot(cursor.x - pos.x, cursor.y - pos.y);
            if (dist < size * 0.55 && now > fleeCooldown) {
                const ax = (pos.x - cursor.x) / (dist || 1);
                const ay = (pos.y - cursor.y) / (dist || 1);
                flee = { dx: ax * 230, dy: ay * 170 - 40, until: now + 1500 };
                fleeCooldown = now + 700;
                spinStart = t;
                if (Math.random() < 0.4) say(FLEE_LINES[Math.floor(Math.random() * FLEE_LINES.length)], 1600);
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
                touch(tap.el, tap.x, tap.y);
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

        // Model: yöne döner, eğilir; kaçarken/dokunurken bir tur atar
        const speed = Math.hypot(pos.vx, pos.vy);
        const wantYaw = Math.abs(pos.vx) > 0.8 ? Math.sign(pos.vx) * 0.75 : Math.sin(t * 0.7) * 0.25;
        yaw += (wantYaw - yaw) * 0.08;
        let spin = 0;
        if (spinStart >= 0) {
            const p = (t - spinStart) / 0.7;
            if (p >= 1) spinStart = -1;
            else spin = (1 - Math.pow(1 - p, 3)) * Math.PI * 2;
        }
        rig.group.rotation.set(
            Math.max(-0.3, Math.min(0.3, pos.vy * 0.02)),
            yaw + spin,
            Math.max(-0.35, Math.min(0.35, -pos.vx * 0.035))
        );
        rig.group.position.y = Math.sin(t * 2.1) * 0.22;
        rig.animate(t, 1 + Math.min(speed / 7, 1.3), tap ? -0.9 : 0);
        renderer.render(scene, camera);

        // Konuşma balonu: ekranın ortasına doğru açılır
        const showTalk = now < talk.until && !tap;
        if (bubble.dataset.text !== talk.text) {
            bubble.dataset.text = talk.text;
            bubble.textContent = talk.text;
        }
        const onRight = pos.x > w / 2;
        bubble.classList.toggle('nd-bubble-show', showTalk);
        bubble.classList.toggle('nd-bubble-left', onRight);
        const bw = bubble.offsetWidth;
        const bx = onRight ? pos.x - size * 0.38 - bw : pos.x + size * 0.38;
        const by = pos.y - size * 0.42;
        bubble.style.transform = `translate3d(${Math.max(8, Math.min(w - bw - 8, bx))}px, ${Math.max(8, by)}px, 0)`;
    };
    tick();
}

// "Hareketi azalt" tercihinde: uçmadan, sağ altta duran ve sadece konuşan Byte yerine
// dikkat dağıtmamak için yalnızca ilk karşılama balonu gösterilir.
function startStatic() {
    const bubble = document.createElement('div');
    bubble.className = 'nd-bubble nd-bubble-show';
    bubble.setAttribute('role', 'status');
    bubble.textContent = STOPS[0].text;
    bubble.style.transform = 'translate3d(16px, calc(100vh - 140px), 0)';
    document.body.appendChild(bubble);
    setTimeout(() => bubble.remove(), 8000);
}

startGuide();
