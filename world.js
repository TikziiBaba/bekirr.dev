// ============================================
// bekirr.dev — Akropolis
// Gün batımında gerçekçi, prosedürel bir antik Yunan dünyası: Dor tapınağı, yetenek
// sütunlarından bir tholos, agorada proje vazoları, mangallar, serviler ve Ege.
// Harici model ya da doku dosyası yok: yüzey dokuları açılışta GPU'da üretilir, yapılar
// dünya uzayında (triplanar) dokulanır; fiziksel gökyüzü, yansımalı su, GTAO ve bloom.
// Kamera, script.js'in verdiği durak değerine (state.t) göre süzülür.
// ============================================

import * as THREE from 'three';
import { Sky } from 'three/addons/objects/Sky.js';
import { Water } from 'three/addons/objects/Water.js';
import { EffectComposer } from 'three/addons/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/addons/postprocessing/RenderPass.js';
import { GTAOPass } from 'three/addons/postprocessing/GTAOPass.js';
import { UnrealBloomPass } from 'three/addons/postprocessing/UnrealBloomPass.js';
import { ShaderPass } from 'three/addons/postprocessing/ShaderPass.js';
import { OutputPass } from 'three/addons/postprocessing/OutputPass.js';
import { RoundedBoxGeometry } from 'three/addons/geometries/RoundedBoxGeometry.js';

const lowPower = window.matchMedia('(pointer: coarse)').matches || window.innerWidth < 900;

// Batan güneş: kameranın sol arkasında, ufkun ~7° üstünde; tapınağın önünü ve sol yüzünü yalar
const SUN_DIR = new THREE.Vector3(-0.8, 0.115, 0.45).normalize();

const PLATEAU = { x: 8, z: 22, r: 58 };
const THOLOS = new THREE.Vector3(30, 0, -6);
const VASE_Z = 34;
const vaseX = (i) => 18 + i * 6.5;

// Agoradaki vazolar: proje sırasıyla (Bekofy, LuckMC, Full-Stack, UI/UX)
const VASES = [
    { kind: 'amphora', motif: 'lyre' },
    { kind: 'krater', motif: 'helmet' },
    { kind: 'hydria', motif: 'bolt' },
    { kind: 'lekythos', motif: 'owl' },
];

// Kamera yolu. stop: true olanlar kaydırma durakları (0 giriş, 1 hakkımda, 2 yetenekler,
// 3–6 projeler, 7 iletişim); diğerleri yalnızca yolu biçimlendiren ara noktalar.
// Masaüstünde içerik bir yanda durur; bakış noktası (look), model öbür yarıda kalacak şekilde
// kaydırılmıştır. Dikey ekranda kamera doğrudan modele (focus) bakar.
const PATH = [
    { pos: [-22, 4.4, 56], look: [-16.9, 5.6, 16.3], focus: [1, -5, 2], stop: true },
    { pos: [-25, 3.4, 30], look: [-11.5, 5.2, -2], focus: [-7, 4.6, 3], stop: true },
    { pos: [-4, 7.5, 44], look: [0, 6, 0], focus: [0, 6, 0] },
    { pos: [14, 6.5, 14], look: [34.3, 3.2, -2.6], focus: [30, 3.2, -6], stop: true },
    ...VASES.map((_, i) => ({ pos: [vaseX(i) - 1, 2.9, 41.5], look: [vaseX(i) - 1.6, 2.1, VASE_Z], focus: [vaseX(i), 2.2, VASE_Z], stop: true })),
    { pos: [55, 24, 82], look: [-19, 2, 18], focus: [4, 2, 4], stop: true },
];
const INTRO = new THREE.Vector3(-70, 40, 150);

// ── Yardımcılar ──
function rng(seed) {
    return () => {
        seed = (seed + 0x6d2b79f5) | 0;
        let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
        t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
        return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
}

// Döşenebilir değer gürültüsü (CPU tarafı: arazi yüksekliği, kaya biçimleri)
function tileNoise(P) {
    const hash = (x, y) => {
        x = ((x % P) + P) % P;
        y = ((y % P) + P) % P;
        const s = Math.sin(x * 127.1 + y * 311.7) * 43758.5453;
        return s - Math.floor(s);
    };
    const value = (x, y) => {
        const xi = Math.floor(x);
        const yi = Math.floor(y);
        const xf = x - xi;
        const yf = y - yi;
        const u = xf * xf * (3 - 2 * xf);
        const v = yf * yf * (3 - 2 * yf);
        const a = hash(xi, yi);
        const b = hash(xi + 1, yi);
        const c = hash(xi, yi + 1);
        const d = hash(xi + 1, yi + 1);
        return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v;
    };
    return (x, y, octaves = 4) => {
        let sum = 0;
        let amp = 0.5;
        let f = 1;
        for (let i = 0; i < octaves; i++) {
            sum += amp * value(x * f, y * f);
            f *= 2;
            amp *= 0.5;
        }
        return sum;
    };
}

const smoothstep = (a, b, x) => {
    const k = Math.min(Math.max((x - a) / (b - a), 0), 1);
    return k * k * (3 - 2 * k);
};

function canvas2d(w, h) {
    const c = document.createElement('canvas');
    c.width = w;
    c.height = h;
    return c;
}

function canvasTexture(c, srgb = true) {
    const tex = new THREE.CanvasTexture(c);
    if (srgb) tex.colorSpace = THREE.SRGBColorSpace;
    tex.anisotropy = 8;
    return tex;
}

// Gri tonlu yükseklik tuvalinden normal haritası
function heightToNormal(src, strength) {
    const w = src.width;
    const h = src.height;
    const data = src.getContext('2d').getImageData(0, 0, w, h).data;
    const out = canvas2d(w, h);
    const g = out.getContext('2d');
    const img = g.createImageData(w, h);
    const H = (x, y) => data[(Math.min(h - 1, Math.max(0, y)) * w + Math.min(w - 1, Math.max(0, x))) * 4] / 255;
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
    return canvasTexture(out, false);
}

const mtx = (x, y, z, sx = 1, sy = 1, sz = 1, ry = 0, rz = 0) => new THREE.Matrix4().compose(
    new THREE.Vector3(x, y, z),
    new THREE.Quaternion().setFromEuler(new THREE.Euler(0, ry, rz)),
    new THREE.Vector3(sx, sy, sz)
);

function instanced(geo, mat, matrices, tint) {
    const mesh = new THREE.InstancedMesh(geo, mat, matrices.length);
    const color = new THREE.Color();
    matrices.forEach((m, i) => {
        mesh.setMatrixAt(i, m);
        if (tint) mesh.setColorAt(i, color.setScalar(1 - tint * Math.random()).lerp(new THREE.Color('#f3e2c4'), Math.random() * tint));
    });
    mesh.castShadow = mesh.receiveShadow = true;
    return mesh;
}

// ── GPU'da üretilen yüzey dokuları ──
// Her tarif, döşenebilir bir yüzeyin rengini, pürüzlülüğünü ve yüksekliğini verir;
// ikinci geçişte yükseklikten normal haritası çıkarılır.
const BAKE_COMMON = /* glsl */ `
    varying vec2 vUv;
    uniform float uPass, uSize, uStrength;
    vec2 hash2(vec2 p) {
        p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
        return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
    }
    float hash1(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
    float gnoise(vec2 p, vec2 per) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
        float a = dot(hash2(mod(i, per)), f);
        float b = dot(hash2(mod(i + vec2(1.0, 0.0), per)), f - vec2(1.0, 0.0));
        float c = dot(hash2(mod(i + vec2(0.0, 1.0), per)), f - vec2(0.0, 1.0));
        float d = dot(hash2(mod(i + vec2(1.0, 1.0), per)), f - vec2(1.0, 1.0));
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }
    float fbm(vec2 p, vec2 per, int oct) {
        float s = 0.0;
        float a = 0.5;
        for (int i = 0; i < oct; i++) {
            s += a * gnoise(p, per);
            p *= 2.0;
            per *= 2.0;
            a *= 0.5;
        }
        return s;
    }
    vec3 voronoi(vec2 p, vec2 per) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        float d1 = 8.0;
        float d2 = 8.0;
        float id = 0.0;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                vec2 g = vec2(float(x), float(y));
                vec2 c = mod(i + g, per);
                vec2 r = g + 0.5 + 0.5 * hash2(c) - f;
                float d = dot(r, r);
                if (d < d1) { d2 = d1; d1 = d; id = hash1(c); }
                else if (d < d2) { d2 = d; }
            }
        }
        return vec3(sqrt(d1), sqrt(d2), id);
    }
    vec3 lin(vec3 c) { return pow(c, vec3(2.2)); }
`;

const BAKE_MAIN = /* glsl */ `
    void main() {
        vec3 col;
        float rough;
        float h;
        if (uPass < 0.5) {
            surface(vUv, col, rough, h);
            gl_FragColor = vec4(col, rough);
        } else {
            float e = 1.0 / uSize;
            float hl, hr, hd, hu;
            surface(vUv - vec2(e, 0.0), col, rough, hl);
            surface(vUv + vec2(e, 0.0), col, rough, hr);
            surface(vUv - vec2(0.0, e), col, rough, hd);
            surface(vUv + vec2(0.0, e), col, rough, hu);
            vec2 slope = vec2(hl - hr, hd - hu) / (2.0 * e) * uStrength;
            gl_FragColor = vec4(normalize(vec3(slope, 1.0)) * 0.5 + 0.5, 1.0);
        }
    }
`;

const RECIPES = {
    // Krem, hafif sarımsı patinalı Pentelik mermer: ince damarlar, gözenekler
    marble: /* glsl */ `
        void surface(vec2 uv, out vec3 col, out float rough, out float h) {
            vec2 per = vec2(4.0);
            vec2 p = uv * 4.0;
            vec2 q = vec2(fbm(p, per, 5), fbm(p + vec2(5.2, 1.3), per, 5));
            float n = fbm(p + 2.0 * q, per, 6);
            float v1 = 1.0 - smoothstep(0.0, 0.05, abs(sin((p.x + p.y) * 1.5707963 + n * 7.0)));
            float v2 = 1.0 - smoothstep(0.0, 0.1, abs(sin((p.x - p.y) * 3.1415926 + n * 11.0)));
            col = mix(lin(vec3(0.93, 0.9, 0.84)), lin(vec3(0.84, 0.76, 0.63)), smoothstep(-0.15, 0.45, n) * 0.55);
            col *= 0.95 + 0.07 * fbm(p * 8.0, per * 8.0, 4);
            col = mix(col, lin(vec3(0.6, 0.56, 0.5)), v1 * 0.3 + v2 * 0.1);
            float pit = smoothstep(0.26, 0.4, fbm(p * 32.0, per * 32.0, 3));
            col *= 1.0 - pit * 0.2;
            rough = clamp(0.55 + 0.25 * fbm(p * 4.0, per * 4.0, 3) + pit * 0.25 - v1 * 0.08, 0.25, 1.0);
            h = fbm(p * 16.0, per * 16.0, 4) * 0.5 - pit * 0.35 + fbm(p * 3.0, per * 3.0, 3) * 0.6;
        }`,
    // Kuru Akdeniz toprağı: çakıl, kurumuş ot lekeleri, ince kum
    ground: /* glsl */ `
        void surface(vec2 uv, out vec3 col, out float rough, out float h) {
            vec2 per = vec2(4.0);
            vec2 p = uv * 4.0;
            float n = fbm(p * 2.0, per * 2.0, 6);
            vec3 dirt = mix(lin(vec3(0.7, 0.59, 0.45)), lin(vec3(0.55, 0.45, 0.33)), smoothstep(-0.3, 0.35, n));
            vec3 vo = voronoi(p * 5.0, per * 5.0);
            float peb = smoothstep(0.4, 0.26, vo.x) * step(0.84, vo.z);
            vec3 pebCol = mix(lin(vec3(0.7, 0.66, 0.58)), lin(vec3(0.5, 0.46, 0.41)), fract(vo.z * 13.0));
            float grass = smoothstep(0.0, 0.25, fbm(p + 7.3, per, 5));
            float blade = fbm(p * 40.0, per * 40.0, 3);
            vec3 grassCol = mix(lin(vec3(0.66, 0.61, 0.39)), lin(vec3(0.41, 0.42, 0.25)), clamp(blade + 0.5, 0.0, 1.0));
            col = mix(dirt, grassCol, grass * 0.7);
            vec3 grit = voronoi(p * 48.0, per * 48.0);
            col *= 0.92 + 0.12 * smoothstep(0.05, 0.6, grit.x);
            col = mix(col, pebCol * (0.85 + 0.3 * (0.5 - vo.x)), peb * 0.35);
            rough = 0.94 - peb * 0.22;
            h = n * 0.35 + peb * (0.45 - vo.x) * 0.6 + grass * blade * 0.3 + grit.x * 0.1;
        }`,
    // Kireçtaşı kaya: katmanlar, çatlaklar, liken
    rock: /* glsl */ `
        void surface(vec2 uv, out vec3 col, out float rough, out float h) {
            vec2 per = vec2(4.0);
            vec2 p = uv * 4.0;
            float n = fbm(p * 2.0, per * 2.0, 6);
            vec3 vo = voronoi(p * 3.0 + n * 0.6, per * 3.0);
            float crack = 1.0 - smoothstep(0.0, 0.07, vo.y - vo.x);
            float strata = sin(p.y * 3.1415926 * 3.0 + n * 4.0) * 0.5 + 0.5;
            col = mix(lin(vec3(0.76, 0.66, 0.53)), lin(vec3(0.56, 0.47, 0.37)), clamp(strata * 0.5 + n + 0.2, 0.0, 1.0));
            col *= 0.88 + 0.22 * fbm(p * 12.0, per * 12.0, 3);
            float lichen = smoothstep(0.22, 0.38, fbm(p * 3.0 + 11.0, per * 3.0, 4));
            col = mix(col, lin(vec3(0.66, 0.64, 0.48)), lichen * 0.35);
            col *= 1.0 - crack * 0.55;
            rough = 0.92;
            h = n * 0.9 + vo.x * 0.35 - crack * 0.6 + fbm(p * 20.0, per * 20.0, 3) * 0.25;
        }`,
    // Pişmiş toprak çatı kiremitleri: oluklu alt kiremitler ve bombeli kapaklar
    tiles: /* glsl */ `
        void surface(vec2 uv, out vec3 col, out float rough, out float h) {
            vec2 per = vec2(4.0);
            vec2 g = uv * vec2(6.0, 12.0);
            vec2 cell = floor(g);
            vec2 f = fract(g);
            float ridge = abs(f.y - 0.5) * 2.0;
            float cover = step(0.5, fract(cell.y * 0.5));
            float arc = sqrt(max(0.0, 1.0 - ridge * ridge));
            h = cover > 0.5 ? arc * 0.9 : 0.25 - arc * 0.12;
            h -= f.x * 0.18;
            float lip = smoothstep(0.86, 1.0, f.x);
            vec2 p = uv * 4.0;
            col = mix(lin(vec3(0.62, 0.3, 0.18)), lin(vec3(0.76, 0.45, 0.28)), hash1(cell));
            col *= 0.82 + 0.28 * (fbm(p * 6.0, per * 6.0, 4) + 0.5);
            col = mix(col, lin(vec3(0.42, 0.4, 0.31)), smoothstep(0.15, 0.45, fbm(p * 2.0 + 3.0, per * 2.0, 5)) * 0.4);
            col *= 1.0 - lip * 0.4;
            rough = 0.82;
        }`,
    // Su yüzeyi dalgacıkları (yalnızca normal)
    water: /* glsl */ `
        void surface(vec2 uv, out vec3 col, out float rough, out float h) {
            vec2 per = vec2(8.0);
            vec2 p = uv * 8.0;
            h = fbm(p, per, 6) + 0.5 * fbm(p * 3.0 + 1.7, per * 3.0, 4);
            col = vec3(0.5);
            rough = 0.0;
        }`,
};

function bakeSurface(renderer, recipe, size, strength, { color = true } = {}) {
    const material = new THREE.ShaderMaterial({
        uniforms: { uPass: { value: 0 }, uSize: { value: size }, uStrength: { value: strength } },
        vertexShader: 'varying vec2 vUv; void main() { vUv = uv; gl_Position = vec4(position.xy, 0.0, 1.0); }',
        fragmentShader: BAKE_COMMON + RECIPES[recipe] + BAKE_MAIN,
        depthTest: false,
        depthWrite: false,
    });
    const quad = new THREE.Mesh(new THREE.PlaneGeometry(2, 2), material);
    quad.frustumCulled = false;
    const scene = new THREE.Scene();
    scene.add(quad);
    const camera = new THREE.Camera();
    const target = (srgb) => new THREE.WebGLRenderTarget(size, size, {
        depthBuffer: false,
        generateMipmaps: true,
        minFilter: THREE.LinearMipmapLinearFilter,
        magFilter: THREE.LinearFilter,
        wrapS: THREE.RepeatWrapping,
        wrapT: THREE.RepeatWrapping,
        anisotropy: 8,
        // sRGB hedef: renk 8 bitte bantlanmadan saklanır, okurken donanım doğrusala çevirir
        colorSpace: srgb ? THREE.SRGBColorSpace : THREE.NoColorSpace,
    });
    const out = {};
    const prev = renderer.getRenderTarget();
    if (color) {
        const rt = target(true);
        renderer.setRenderTarget(rt);
        renderer.render(scene, camera);
        out.map = rt.texture;
    }
    const nrt = target(false);
    material.uniforms.uPass.value = 1;
    renderer.setRenderTarget(nrt);
    renderer.render(scene, camera);
    out.normal = nrt.texture;
    renderer.setRenderTarget(prev);
    material.dispose();
    quad.geometry.dispose();
    return out;
}

// ── Triplanar taş malzemesi ──
// UV yerine dünya koordinatlarından üç yönlü dokulama: ölçeklenmiş sütunlar, kutular ve arazi
// aynı doku yoğunluğunu taşır. İsteğe bağlı blok derzleri, zemine yakın kir ve eğimde kaya geçişi.
const TP_VERT = /* glsl */ `
    #include <worldpos_vertex>
    vec4 tpPos = vec4(transformed, 1.0);
    vec3 tpObjN = objectNormal;
    #ifdef USE_INSTANCING
        tpPos = instanceMatrix * tpPos;
        tpObjN = mat3(instanceMatrix) * tpObjN;
    #endif
    vTpPos = (modelMatrix * tpPos).xyz;
    vTpNormal = normalize(mat3(modelMatrix) * tpObjN);
`;

const TP_HEAD = /* glsl */ `
    uniform sampler2D tAlbedo;
    uniform sampler2D tNormal;
    uniform float uTpScale;
    uniform float uNormalScale;
    uniform float uMacro;
    uniform float uGrime;
    uniform vec4 uJoints;
    #ifdef TP_ROCK
        uniform sampler2D tAlbedo2;
        uniform sampler2D tNormal2;
        uniform float uTpScale2;
    #endif
    varying vec3 vTpPos;
    varying vec3 vTpNormal;
    vec4 tpSample(sampler2D t, vec3 w, vec2 ux, vec2 uy, vec2 uz) {
        return texture2D(t, ux) * w.x + texture2D(t, uy) * w.y + texture2D(t, uz) * w.z;
    }
    // "Whiteout" karışımlı triplanar normal (dünya uzayında)
    vec3 tpNormal(sampler2D t, vec3 w, vec3 n, vec2 ux, vec2 uy, vec2 uz, float s) {
        vec3 a = texture2D(t, ux).xyz * 2.0 - 1.0;
        vec3 b = texture2D(t, uy).xyz * 2.0 - 1.0;
        vec3 c = texture2D(t, uz).xyz * 2.0 - 1.0;
        a.xy *= s; b.xy *= s; c.xy *= s;
        a = vec3(a.xy + n.zy, abs(a.z) * n.x);
        b = vec3(b.xy + n.xz, abs(b.z) * n.y);
        c = vec3(c.xy + n.xy, abs(c.z) * n.z);
        return normalize(a.zyx * w.x + b.xzy * w.y + c.xyz * w.z);
    }
`;

const TP_MAP = /* glsl */ `
    vec3 tpN = normalize(vTpNormal);
    vec3 tpW = pow(abs(tpN), vec3(4.0));
    tpW /= dot(tpW, vec3(1.0));
    vec2 tpX = vTpPos.zy * uTpScale;
    vec2 tpY = vTpPos.xz * uTpScale;
    vec2 tpZ = vTpPos.xy * uTpScale;
    vec4 tpA = tpSample(tAlbedo, tpW, tpX, tpY, tpZ);
    // Döşeme tekrarını kırmak için geniş ölçekli ton farkı
    float tpMacro = tpSample(tAlbedo, tpW, tpX * 0.071, tpY * 0.071, tpZ * 0.071).g;
    tpA.rgb *= mix(1.0, 0.7 + tpMacro * 0.6, uMacro);
    #ifdef TP_ROCK
        float tpRockMix = 1.0 - smoothstep(0.6, 0.8, tpN.y);
        vec2 tpX2 = vTpPos.zy * uTpScale2;
        vec2 tpY2 = vTpPos.xz * uTpScale2;
        vec2 tpZ2 = vTpPos.xy * uTpScale2;
        tpA = mix(tpA, tpSample(tAlbedo2, tpW, tpX2, tpY2, tpZ2), tpRockMix);
    #endif
    // Taş blok derzleri: x = sıra yüksekliği, y = blok boyu (0: yalnızca yatay), z = derz kalınlığı
    float tpJoint = 0.0;
    if (uJoints.w > 0.5) {
        float yy = vTpPos.y / uJoints.x;
        float course = floor(yy);
        float dh = min(fract(yy), 1.0 - fract(yy)) * uJoints.x;
        tpJoint = 1.0 - smoothstep(0.0, uJoints.z, dh);
        if (uJoints.y > 0.0) {
            float along = tpW.x > tpW.z ? vTpPos.z : vTpPos.x;
            float xx = along / uJoints.y + course * 0.5;
            float dv = min(fract(xx), 1.0 - fract(xx)) * uJoints.y;
            tpJoint = max(tpJoint, 1.0 - smoothstep(0.0, uJoints.z, dv));
        }
        tpJoint *= 1.0 - tpW.y;
    }
    vec3 tpCol = tpA.rgb * (1.0 - tpJoint * 0.55);
    // Zemine yakın kısımlarda toz ve kir
    float tpLow = 1.0 - smoothstep(0.0, 2.2, vTpPos.y);
    tpCol = mix(tpCol, tpCol * vec3(0.76, 0.69, 0.6), tpLow * uGrime);
    diffuseColor.rgb *= tpCol;
`;

const TP_ROUGH = /* glsl */ `
    float roughnessFactor = roughness * tpA.a * (1.0 + tpJoint * 0.2);
`;

const TP_NORMAL = /* glsl */ `
    vec3 tpWN = tpNormal(tNormal, tpW, tpN, tpX, tpY, tpZ, uNormalScale);
    #ifdef TP_ROCK
        tpWN = normalize(mix(tpWN, tpNormal(tNormal2, tpW, tpN, tpX2, tpY2, tpZ2, uNormalScale * 1.3), tpRockMix));
    #endif
    normal = normalize((viewMatrix * vec4(tpWN, 0.0)).xyz);
`;

function stoneMaterial(tex, { scale = 0.33, tint = '#ffffff', roughness = 1, normalScale = 1, macro = 0.6, grime = 0.8, joints = null, rock = null, rockScale = 0.08 } = {}) {
    const mat = new THREE.MeshStandardMaterial({ color: tint, roughness, envMapIntensity: 1 });
    const uniforms = {
        tAlbedo: { value: tex.map },
        tNormal: { value: tex.normal },
        uTpScale: { value: scale },
        uNormalScale: { value: normalScale },
        uMacro: { value: macro },
        uGrime: { value: grime },
        uJoints: { value: joints ? new THREE.Vector4(joints[0], joints[1], joints[2], 1) : new THREE.Vector4(1, 0, 0, 0) },
    };
    if (rock) {
        mat.defines = { TP_ROCK: '' };
        Object.assign(uniforms, { tAlbedo2: { value: rock.map }, tNormal2: { value: rock.normal }, uTpScale2: { value: rockScale } });
    }
    mat.onBeforeCompile = (shader) => {
        Object.assign(shader.uniforms, uniforms);
        shader.vertexShader = shader.vertexShader
            .replace('#include <common>', '#include <common>\nvarying vec3 vTpPos;\nvarying vec3 vTpNormal;')
            .replace('#include <worldpos_vertex>', TP_VERT);
        shader.fragmentShader = shader.fragmentShader
            .replace('#include <common>', `#include <common>\n${TP_HEAD}`)
            .replace('#include <map_fragment>', TP_MAP)
            .replace('#include <roughnessmap_fragment>', TP_ROUGH)
            .replace('#include <normal_fragment_maps>', TP_NORMAL);
    };
    return mat;
}

// ── Yapraklar ──
// Alfa kesimli yaprak kartları; normalleri ağacın merkezinden dışa doğru yönlendirilir ki
// taç tek bir hacim gibi ışık alsın (gerçek zamanlı bitki örtüsünde yaygın yöntem).
function foliageMaterial(map, up = 0) {
    const mat = new THREE.MeshStandardMaterial({ map, alphaTest: 0.42, side: THREE.DoubleSide, roughness: 0.82, envMapIntensity: 0.7 });
    mat.onBeforeCompile = (shader) => {
        shader.vertexShader = shader.vertexShader
            .replace('#include <common>', '#include <common>\nattribute vec3 aCenter;')
            .replace('#include <defaultnormal_vertex>', `#include <defaultnormal_vertex>
                vec3 fwDir = (instanceMatrix * vec4(position, 1.0)).xyz - aCenter;
                fwDir.y *= 0.45;
                fwDir = normalize(mix(normalize(fwDir + vec3(0.0, 0.0001, 0.0)), vec3(0.0, 1.0, 0.0), ${up.toFixed(2)}));
                transformedNormal = normalize(normalMatrix * fwDir);`);
        // Arka yüzlerde normal ters çevrilmesin: taç her yönden aynı aydınlansın
        shader.fragmentShader = shader.fragmentShader
            .replace('#include <normal_fragment_maps>', '#include <normal_fragment_maps>\nnormal = normalize(vNormal);');
    };
    mat.customProgramCacheKey = () => `foliage${up}`;
    return mat;
}

function foliageMesh(texture, cards, up = 0) {
    const geo = new THREE.PlaneGeometry(1, 1);
    const centers = new Float32Array(cards.length * 3);
    cards.forEach((c, i) => centers.set(c.center, i * 3));
    geo.setAttribute('aCenter', new THREE.InstancedBufferAttribute(centers, 3));
    const mesh = new THREE.InstancedMesh(geo, foliageMaterial(texture, up), cards.length);
    const color = new THREE.Color();
    cards.forEach((c, i) => {
        mesh.setMatrixAt(i, c.matrix);
        mesh.setColorAt(i, color.setScalar(c.shade));
    });
    mesh.castShadow = true;
    mesh.receiveShadow = true;
    mesh.customDepthMaterial = new THREE.MeshDepthMaterial({ depthPacking: THREE.RGBADepthPacking, map: texture, alphaTest: 0.42 });
    mesh.userData.foliage = true;
    return mesh;
}

function foliageTexture(kind) {
    const c = canvas2d(256, 256);
    const g = c.getContext('2d');
    const rand = rng(kind === 'cypress' ? 5 : kind === 'olive' ? 6 : 8);
    if (kind === 'cypress') {
        // Sık, pul pul koyu yeşil sürgünler
        const greens = ['#25301b', '#2f3b21', '#3a4729', '#465532', '#56653c'];
        for (let i = 0; i < 900; i++) {
            const x = 20 + rand() * 216;
            const y = 14 + rand() * 228;
            const r = Math.hypot(x - 128, y - 128);
            if (r > 112 * (0.75 + rand() * 0.3)) continue;
            g.strokeStyle = greens[Math.floor(rand() * greens.length)];
            g.lineWidth = 2 + rand() * 3;
            g.lineCap = 'round';
            const a = -Math.PI / 2 + (rand() - 0.5) * 1.4;
            const l = 5 + rand() * 9;
            g.beginPath();
            g.moveTo(x, y);
            g.lineTo(x + Math.cos(a) * l, y + Math.sin(a) * l);
            g.stroke();
        }
    } else if (kind === 'olive') {
        // İnce, gümüşi yeşil zeytin yaprakları ve dallar
        g.strokeStyle = '#5a4c3d';
        g.lineWidth = 2;
        for (let i = 0; i < 7; i++) {
            g.beginPath();
            g.moveTo(128, 250);
            g.quadraticCurveTo(60 + rand() * 136, 150, 20 + rand() * 216, 20 + rand() * 120);
            g.stroke();
        }
        const greens = ['#6b7652', '#7f8a63', '#96a07a', '#5d6847', '#b0b596'];
        for (let i = 0; i < 520; i++) {
            const x = 18 + rand() * 220;
            const y = 12 + rand() * 210;
            if (Math.hypot(x - 128, (y - 118) * 1.15) > 118) continue;
            g.fillStyle = greens[Math.floor(rand() * greens.length)];
            g.beginPath();
            g.ellipse(x, y, 1.6 + rand() * 1.2, 7 + rand() * 5, rand() * Math.PI, 0, Math.PI * 2);
            g.fill();
        }
    } else {
        // Kuru ot öbeği: tabandan yelpaze gibi açılan saplar
        const tones = ['#a39462', '#8d8150', '#b8a874', '#7a7445', '#c4b583'];
        for (let i = 0; i < 160; i++) {
            const bx = 128 + (rand() - 0.5) * 60;
            const a = -Math.PI / 2 + (rand() - 0.5) * 1.5;
            const l = 90 + rand() * 140;
            g.strokeStyle = tones[Math.floor(rand() * tones.length)];
            g.lineWidth = 1.5 + rand() * 2;
            g.beginPath();
            g.moveTo(bx, 256);
            g.quadraticCurveTo(bx + Math.cos(a) * l * 0.5, 256 + Math.sin(a) * l * 0.5, bx + Math.cos(a) * l + (rand() - 0.5) * 30, 256 + Math.sin(a) * l);
            g.stroke();
        }
    }
    const tex = canvasTexture(c);
    tex.generateMipmaps = true;
    return tex;
}

// ── Sütun parçaları ──
// Yivli gövde: 20 yiv, yukarı doğru incelir ve ortada hafifçe şişer (entasis). Taban y=0, yükseklik 1.
function flutedGeometry({ flutes = 20, seg = 4, rings = 14, taper = 0.78, depth = 0.05 } = {}) {
    const radial = flutes * seg;
    const geo = new THREE.CylinderGeometry(0.5, 0.5, 1, radial, rings, true);
    geo.translate(0, 0.5, 0);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
        const ix = i % (radial + 1);
        const x = pos.getX(i);
        const y = pos.getY(i);
        const z = pos.getZ(i);
        const len = Math.hypot(x, z) || 1;
        const R = 0.5 * (1 - (1 - taper) * Math.pow(y, 1.3)) * (1 + 0.03 * Math.sin(y * Math.PI));
        const r = R - depth * Math.sin((Math.PI * (ix % seg)) / seg);
        pos.setXYZ(i, (x / len) * r, y, (z / len) * r);
    }
    geo.computeVertexNormals();
    return geo;
}

function columnParts() {
    return {
        fluted: flutedGeometry(),
        echinus: new THREE.LatheGeometry(
            [[0.38, 0], [0.42, 0.04], [0.5, 0.12], [0.6, 0.22], [0.66, 0.3], [0, 0.3]].map(([r, y]) => new THREE.Vector2(r, y)), 48
        ),
        abacus: new RoundedBoxGeometry(1.36, 0.22, 1.36, 2, 0.03).translate(0, 0.11, 0),
    };
}

// ── Siyah figür çizimleri (vazolar ve alınlık için) ──
// Hepsi (0,0) merkezli, yaklaşık ±50 birimlik bir kutuya çizilir.
function drawOwl(g, ink, hole) {
    const PI2 = Math.PI * 2;
    g.fillStyle = ink;
    g.beginPath(); g.ellipse(0, 14, 24, 31, 0, 0, PI2); g.fill();
    g.beginPath(); g.ellipse(0, -21, 22, 19, 0, 0, PI2); g.fill();
    [-1, 1].forEach((s) => {
        g.beginPath(); g.moveTo(s * 13, -36); g.lineTo(s * 24, -47); g.lineTo(s * 21, -28); g.fill();
        g.beginPath(); g.moveTo(s * 6, 42); g.lineTo(s * 12, 50); g.lineTo(s * 2, 47); g.fill();
    });
    g.beginPath(); g.moveTo(-9, 42); g.lineTo(0, 54); g.lineTo(9, 42); g.fill();
    g.fillStyle = hole;
    [-1, 1].forEach((s) => { g.beginPath(); g.arc(s * 10, -22, 8, 0, PI2); g.fill(); });
    g.fillStyle = ink;
    [-1, 1].forEach((s) => { g.beginPath(); g.arc(s * 10, -22, 3.6, 0, PI2); g.fill(); });
    g.fillStyle = hole;
    g.beginPath(); g.moveTo(-3, -13); g.lineTo(3, -13); g.lineTo(0, -7); g.fill();
    // Kanat ve göğüs kazımaları
    g.strokeStyle = hole;
    g.lineWidth = 1.6;
    [-1, 1].forEach((s) => {
        g.beginPath(); g.moveTo(s * 20, -2); g.quadraticCurveTo(s * 26, 20, s * 12, 40); g.stroke();
    });
    for (let r = 0; r < 3; r++) {
        for (let c = -1; c <= 1; c++) {
            g.beginPath(); g.moveTo(c * 8 - 3, 6 + r * 9); g.lineTo(c * 8, 9 + r * 9); g.lineTo(c * 8 + 3, 6 + r * 9); g.stroke();
        }
    }
}

function drawLyre(g, ink, hole) {
    g.strokeStyle = ink;
    g.lineCap = 'round';
    g.lineWidth = 7;
    [-1, 1].forEach((s) => {
        g.beginPath(); g.moveTo(s * 12, 24); g.bezierCurveTo(s * 40, 8, s * 40, -30, s * 22, -44); g.stroke();
    });
    g.beginPath(); g.moveTo(-31, -33); g.lineTo(31, -33); g.stroke();
    g.fillStyle = ink;
    g.beginPath(); g.ellipse(0, 31, 23, 16, 0, 0, Math.PI * 2); g.fill();
    g.lineWidth = 1.6;
    for (let i = -2; i <= 2; i++) { g.beginPath(); g.moveTo(i * 5, -33); g.lineTo(i * 5, 24); g.stroke(); }
    g.strokeStyle = hole;
    g.lineWidth = 1.6;
    g.beginPath(); g.ellipse(0, 31, 15, 9, 0, 0, Math.PI * 2); g.stroke();
    g.beginPath(); g.moveTo(-15, 31); g.lineTo(15, 31); g.moveTo(0, 22); g.lineTo(0, 40); g.stroke();
}

function drawHelmet(g, ink, hole) {
    g.fillStyle = ink;
    // At kılı sorguç
    g.beginPath();
    g.moveTo(-36, -18); g.bezierCurveTo(-32, -64, 32, -64, 36, -18);
    g.lineTo(27, -17); g.bezierCurveTo(23, -48, -23, -48, -27, -17);
    g.closePath(); g.fill();
    // Korint miğferi: kubbe, yanak siperleri ve aradaki T yarığı
    g.beginPath();
    g.moveTo(-27, -12); g.bezierCurveTo(-29, -42, 29, -42, 27, -12);
    g.lineTo(29, 30); g.lineTo(11, 46); g.lineTo(6, 13); g.lineTo(-6, 13); g.lineTo(-11, 46); g.lineTo(-29, 30);
    g.closePath(); g.fill();
    g.fillStyle = hole;
    [-1, 1].forEach((s) => {
        g.beginPath();
        g.moveTo(s * 21, -5); g.quadraticCurveTo(s * 12, -14, s * 3, -6);
        g.lineTo(s * 3, 1); g.quadraticCurveTo(s * 12, 5, s * 21, -1);
        g.closePath(); g.fill();
    });
    g.strokeStyle = hole;
    g.lineWidth = 1.6;
    g.beginPath(); g.moveTo(-24, -20); g.bezierCurveTo(-12, -30, 12, -30, 24, -20); g.stroke();
    g.beginPath(); g.moveTo(-24, 24); g.lineTo(-12, 36); g.moveTo(24, 24); g.lineTo(12, 36); g.stroke();
    for (let i = -3; i <= 3; i++) { g.beginPath(); g.moveTo(i * 8, -50); g.lineTo(i * 7, -41); g.stroke(); }
}

// Zeus'un yıldırımı (keraunos)
function drawBolt(g, ink, hole) {
    g.fillStyle = ink;
    g.strokeStyle = ink;
    g.lineCap = 'round';
    g.beginPath(); g.ellipse(0, 0, 8, 13, 0, 0, Math.PI * 2); g.fill();
    [-1, 1].forEach((s) => {
        g.beginPath();
        g.moveTo(-5, s * 9); g.lineTo(-12, s * 23); g.lineTo(-3, s * 25); g.lineTo(-8, s * 50);
        g.lineTo(10, s * 19); g.lineTo(1, s * 18); g.lineTo(5, s * 9);
        g.closePath(); g.fill();
        g.lineWidth = 4;
        g.beginPath(); g.moveTo(-6, s * 10); g.quadraticCurveTo(-24, s * 20, -22, s * 42); g.stroke();
        g.beginPath(); g.moveTo(6, s * 10); g.quadraticCurveTo(24, s * 20, 22, s * 42); g.stroke();
    });
    g.strokeStyle = hole;
    g.lineWidth = 1.6;
    g.beginPath(); g.moveTo(-7, -5); g.lineTo(7, -5); g.moveTo(-7, 0); g.lineTo(7, 0); g.moveTo(-7, 5); g.lineTo(7, 5); g.stroke();
}

const MOTIFS = { owl: drawOwl, lyre: drawLyre, helmet: drawHelmet, bolt: drawBolt };

// Meander (Yunan anahtarı) bandı; s ölçeğiyle 20 birimlik karolar halinde
function drawMeander(g, x0, y0, w, s, ink) {
    g.save();
    g.strokeStyle = ink;
    g.lineWidth = 2 * s;
    g.lineCap = 'square';
    const P = (x, px, py) => [x + px * s, y0 + py * s];
    for (let x = x0; x < x0 + w; x += 20 * s) {
        g.beginPath();
        g.moveTo(...P(x, 0, 19)); g.lineTo(...P(x, 20, 19));
        g.moveTo(...P(x, 18, 19));
        [[18, 3], [2, 3], [2, 15], [14, 15], [14, 7], [6, 7], [6, 11], [10, 11]].forEach(([px, py]) => g.lineTo(...P(x, px, py)));
        g.stroke();
    }
    g.restore();
}

// Panel amforası düzeni: siyah sırlı gövdede iki pişmiş toprak pano, içinde motif.
// Aynı çizim üç kez yapılır: renk, pürüzlülük (sır parlak, kil mat) ve vernik maskesi.
function vasePaint(motif, mode) {
    const draw = MOTIFS[motif];
    const c = canvas2d(1280, 640);
    const g = c.getContext('2d');
    const W = c.width;
    const H = c.height;
    const pal = {
        color: { clay: null, ink: '#18110d', hole: '#c06538' },
        rough: { clay: '#c8c8c8', ink: '#4a4a4a', hole: '#c8c8c8' },
        coat: { clay: '#000000', ink: '#ffffff', hole: '#000000' },
    }[mode];
    if (pal.clay) g.fillStyle = pal.clay;
    else {
        const clay = g.createLinearGradient(0, 0, 0, H);
        clay.addColorStop(0, '#b0522c');
        clay.addColorStop(0.5, '#c06538');
        clay.addColorStop(1, '#a24a27');
        g.fillStyle = clay;
    }
    g.fillRect(0, 0, W, H);
    const { ink, hole } = pal;
    g.fillStyle = ink;
    g.fillRect(0, 0, W, H * 0.07);
    g.fillRect(0, H * 0.9, W, H);
    // Boyunda sarkan dil deseni
    for (let x = 0; x < W; x += 32) {
        g.beginPath(); g.moveTo(x + 3, H * 0.075); g.lineTo(x + 29, H * 0.075); g.lineTo(x + 16, H * 0.17); g.fill();
    }
    g.fillRect(0, H * 0.19, W, 4);
    drawMeander(g, 0, H * 0.2, W, 2, ink);
    g.fillRect(0, H * 0.27, W, 4);
    // Gövde: siyah, iki pano boş bırakılır
    g.fillRect(0, H * 0.29, W, H * 0.45);
    [0.25, 0.75].forEach((u) => {
        const cx = W * u;
        g.fillStyle = hole;
        g.fillRect(cx - W * 0.14, H * 0.305, W * 0.28, H * 0.42);
        g.save();
        g.translate(cx, H * 0.515);
        g.scale(2.2, 2.2);
        draw(g, ink, hole);
        g.restore();
        g.fillStyle = ink;
    });
    // Altta çizgiler ve ışın deseni
    g.fillRect(0, H * 0.76, W, 3);
    for (let x = 0; x < W; x += 28) {
        g.beginPath(); g.moveTo(x + 2, H * 0.885); g.lineTo(x + 26, H * 0.885); g.lineTo(x + 14, H * 0.78); g.fill();
    }
    // Yıllanma: sırda küçük dökülmeler, kilde toz lekeleri
    if (mode !== 'coat') {
        const rand = rng(motif.length * 17);
        for (let i = 0; i < 900; i++) {
            g.fillStyle = mode === 'color'
                ? (rand() < 0.5 ? 'rgba(196, 120, 80, 0.35)' : 'rgba(60, 40, 28, 0.18)')
                : 'rgba(200, 200, 200, 0.4)';
            g.beginPath();
            g.arc(rand() * W, rand() * H, 0.6 + rand() * 2.2, 0, Math.PI * 2);
            g.fill();
        }
    }
    return canvasTexture(c, mode === 'color');
}

// Alınlık: yıpranmış Mısır mavisi boya üzerinde altın varaklı baykuş kabartması
function tympanumMaps() {
    const W = 1200;
    const H = 170;
    const owlAt = (g, ink, hole, sprigs) => {
        g.save();
        g.translate(W / 2, H * 0.56);
        g.scale(1.2, 1.2);
        drawOwl(g, ink, hole);
        g.restore();
        g.strokeStyle = sprigs;
        g.fillStyle = sprigs;
        g.lineWidth = 3;
        [-1, 1].forEach((s) => {
            g.beginPath(); g.moveTo(W / 2 + s * 46, H * 0.86); g.quadraticCurveTo(W / 2 + s * 200, H * 0.72, W / 2 + s * 360, H * 0.84); g.stroke();
            for (let k = 0; k < 9; k++) {
                const t = (k + 0.5) / 9;
                const x = W / 2 + s * (46 + t * 314);
                const y = H * (0.86 - Math.sin(t * Math.PI) * 0.1);
                [-1, 1].forEach((side) => { g.beginPath(); g.ellipse(x, y + side * 9, 13, 4.5, s * side * 0.6, 0, Math.PI * 2); g.fill(); });
            }
        });
    };
    const rand = rng(21);

    const color = canvas2d(W, H);
    let g = color.getContext('2d');
    g.fillStyle = '#d9cdb7';
    g.fillRect(0, 0, W, H);
    g.fillStyle = '#3b5679';
    g.fillRect(0, 0, W, H);
    // Boya yer yer dökülmüş, altından mermer görünür
    for (let i = 0; i < 260; i++) {
        g.fillStyle = `rgba(214, 200, 176, ${0.25 + rand() * 0.5})`;
        g.beginPath();
        g.ellipse(rand() * W, rand() * H, 3 + rand() * 18, 2 + rand() * 8, rand() * 3, 0, Math.PI * 2);
        g.fill();
    }
    owlAt(g, '#d1a650', '#3b5679', '#c99a45');

    const height = canvas2d(W, H);
    g = height.getContext('2d', { willReadFrequently: true });
    g.fillStyle = '#000';
    g.fillRect(0, 0, W, H);
    g.filter = 'blur(2.5px)';
    owlAt(g, '#fff', '#777', '#ccc');

    const mr = canvas2d(W, H);
    g = mr.getContext('2d');
    g.fillStyle = 'rgb(0, 210, 0)';
    g.fillRect(0, 0, W, H);
    owlAt(g, 'rgb(0, 80, 255)', 'rgb(0, 210, 0)', 'rgb(0, 90, 255)');

    return { map: canvasTexture(color), normalMap: heightToNormal(height, 6), mr: canvasTexture(mr, false) };
}

// Arşitrav yazıtı: "ΒΕΚΙΡ · ΚΑΠΛΑΝ · ΕΠΟΙΕΙ" (yapan: Bekir Kaplan), taşa oyulmuş
function inscriptionMaps(onReady) {
    const text = 'ΒΕΚΙΡ · ΚΑΠΛΑΝ · ΕΠΟΙΕΙ';
    const W = 2048;
    const H = 110;
    const mask = canvas2d(W, H);
    const height = canvas2d(W, H);
    const draw = (c, bg, fg, blur) => {
        const g = c.getContext('2d', { willReadFrequently: c === height });
        g.filter = 'none';
        g.fillStyle = bg;
        g.fillRect(0, 0, W, H);
        g.filter = blur;
        g.font = '600 70px "EB Garamond", "Times New Roman", serif';
        g.textBaseline = 'middle';
        g.fillStyle = fg;
        const chars = Array.from(text);
        const gap = 22;
        const total = chars.reduce((sum, ch) => sum + g.measureText(ch).width + gap, -gap);
        let x = (W - total) / 2;
        chars.forEach((ch) => {
            g.fillText(ch, x, H / 2);
            x += g.measureText(ch).width + gap;
        });
    };
    const maps = { alphaMap: canvasTexture(mask, false), normalMap: null };
    const render = () => {
        draw(mask, '#000', '#fff', 'blur(1px)');
        draw(height, '#fff', '#000', 'blur(1.6px)');
        maps.normalMap = heightToNormal(height, 5);
        maps.alphaMap.needsUpdate = true;
        onReady?.(maps);
    };
    render();
    // Yunanca harfler web fontuyla yüklenince yeniden çiz
    document.fonts?.load('600 70px "EB Garamond"', text).then(render, () => {});
    return maps;
}

function doorTexture() {
    const c = canvas2d(64, 128);
    const g = c.getContext('2d');
    const grad = g.createRadialGradient(32, 80, 4, 32, 80, 90);
    grad.addColorStop(0, '#ffcf8a');
    grad.addColorStop(0.4, '#c8692f');
    grad.addColorStop(1, '#2a1208');
    g.fillStyle = grad;
    g.fillRect(0, 0, 64, 128);
    return canvasTexture(c);
}

function glowTexture() {
    const c = canvas2d(64, 64);
    const g = c.getContext('2d');
    const grad = g.createRadialGradient(32, 32, 0, 32, 32, 32);
    grad.addColorStop(0, 'rgba(255,255,255,1)');
    grad.addColorStop(0.35, 'rgba(255,255,255,0.45)');
    grad.addColorStop(1, 'rgba(255,255,255,0)');
    g.fillStyle = grad;
    g.fillRect(0, 0, 64, 64);
    return canvasTexture(c);
}

// ── Arazi ──
const terrainNoise = tileNoise(64);
const edgeNoise = tileNoise(16);

// Yapıların ve yolların altında düz; diğer yerlerde hafif dalgalı; kenarda falez
function flatMask(x, z) {
    const rect = (x0, x1, z0, z1) => {
        const dx = Math.max(x0 - x, 0, x - x1);
        const dz = Math.max(z0 - z, 0, z - z1);
        return 1 - smoothstep(0, 5, Math.hypot(dx, dz));
    };
    return Math.max(
        rect(-11.5, 11.5, -19.5, 22),
        rect(13, 42, 29, 39),
        1 - smoothstep(8.5, 13, Math.hypot(x - THOLOS.x, z - THOLOS.z))
    );
}

function heightAt(x, z) {
    const dx = x - PLATEAU.x;
    const dz = z - PLATEAU.z;
    const d = Math.hypot(dx, dz);
    const R = PLATEAU.r + (edgeNoise((Math.atan2(dz, dx) / Math.PI + 1) * 8, 3.7) - 0.5) * 16;
    const bumps = (terrainNoise(x * 0.035 + 10, z * 0.035 + 10) - 0.5) * 2.2 + (terrainNoise(x * 0.16, z * 0.16) - 0.5) * 0.35;
    const h = bumps * (1 - flatMask(x, z));
    const edge = smoothstep(R - 2, R + 12, d);
    const cliff = -22 + (terrainNoise(x * 0.12 + 3, z * 0.12) - 0.5) * 8;
    return h * (1 - edge) + cliff * edge + (terrainNoise(x * 0.4, z * 0.4) - 0.5) * 1.6 * edge * (1 - edge) * 4;
}

function buildTerrain(mat) {
    const size = 300;
    const seg = lowPower ? 180 : 260;
    const geo = new THREE.PlaneGeometry(size, size, seg, seg);
    geo.rotateX(-Math.PI / 2);
    geo.translate(PLATEAU.x, 0, PLATEAU.z);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) pos.setY(i, heightAt(pos.getX(i), pos.getZ(i)));
    geo.computeVertexNormals();
    const mesh = new THREE.Mesh(geo, mat);
    mesh.receiveShadow = true;
    mesh.castShadow = true;
    return mesh;
}

// Düzensiz kaya: gürültüyle bozulmuş küre
function rockGeometry(seed) {
    const geo = new THREE.IcosahedronGeometry(1, 4);
    const pos = geo.attributes.position;
    const v = new THREE.Vector3();
    const n = tileNoise(32);
    for (let i = 0; i < pos.count; i++) {
        v.fromBufferAttribute(pos, i);
        const k = 0.72 + n(v.x * 1.6 + seed, v.y * 1.6 + v.z * 1.3 + seed, 5) * 0.62;
        v.multiplyScalar(k);
        v.y *= 0.7;
        pos.setXYZ(i, v.x, v.y, v.z);
    }
    geo.computeVertexNormals();
    return geo;
}

// ── Yapılar ──
function buildTemple(M, parts) {
    const g = new THREE.Group();
    const box = (w, h, d, mat, x, y, z) => {
        const r = Math.min(0.045, Math.min(w, h, d) / 2 - 0.005);
        const mesh = new THREE.Mesh(new RoundedBoxGeometry(w, h, d, 2, r), mat);
        mesh.position.set(x, y, z);
        mesh.castShadow = mesh.receiveShadow = true;
        g.add(mesh);
        return mesh;
    };

    // Krepidoma: üç basamak; en üstteki stylobat
    [1.1, 0.55, 0].forEach((grow, k) => box((8 + grow) * 2, 0.4, (16 + grow) * 2, M.step, 0, 0.2 + k * 0.4, 0));

    // Peristil: 8 × 16 Dor sütunu; her biri biraz farklı tonda
    const spots = [];
    for (let x = -7; x <= 7; x += 2) spots.push([x, 15], [x, -15]);
    for (let z = -13; z <= 13; z += 2) spots.push([-7, z], [7, z]);
    const S = 1.2;
    const H = 6.4;
    g.add(
        instanced(parts.fluted, M.column, spots.map(([x, z]) => mtx(x, S, z, 1, H, 1, x * 0.37 + z)), 0.08),
        instanced(parts.echinus, M.plain, spots.map(([x, z]) => mtx(x, S + H, z)), 0.06),
        instanced(parts.abacus, M.plain, spots.map(([x, z]) => mtx(x, S + H + 0.3, z)), 0.06)
    );

    // Arşitrav, kırmızı taenia, friz ve geison: dört kenar boyunca
    const A = S + H + 0.52;
    const ring = (y, h, t, mat) => {
        box(14 + t, h, t, mat, 0, y + h / 2, 15);
        box(14 + t, h, t, mat, 0, y + h / 2, -15);
        box(t, h, 30 - t, mat, -7, y + h / 2, 0);
        box(t, h, 30 - t, mat, 7, y + h / 2, 0);
    };
    ring(A, 0.8, 1.3, M.plain);
    ring(A + 0.8, 0.08, 1.38, M.red);
    ring(A + 0.88, 0.86, 1.3, M.plain);
    ring(A + 1.74, 0.38, 2.1, M.plain);
    box(13.2, 0.12, 29.2, M.plain, 0, A + 0.86, 0);

    // Triglifler: boyası solmuş, üç dikey yarıklı bloklar
    const fy = A + 0.88 + 0.43;
    const tri = [];
    for (let x = -7; x <= 7; x += 1) tri.push(mtx(x, fy, 15.69), mtx(x, fy, -15.69));
    for (let z = -15; z <= 15; z += 1) tri.push(mtx(7.69, fy, z, 1, 1, 1, Math.PI / 2), mtx(-7.69, fy, z, 1, 1, 1, Math.PI / 2));
    const glyph = new RoundedBoxGeometry(0.44, 0.86, 0.12, 2, 0.02);
    g.add(instanced(glyph, M.blue, tri));
    const grooves = [];
    tri.forEach((m) => {
        [-0.11, 0, 0.11].forEach((dx) => {
            const offset = new THREE.Matrix4().makeTranslation(dx, -0.04, 0.062);
            grooves.push(m.clone().multiply(offset));
        });
    });
    g.add(instanced(new THREE.BoxGeometry(0.035, 0.72, 0.012), M.groove, grooves));

    // Arşitrav yazıtı
    const inscription = new THREE.Mesh(new THREE.PlaneGeometry(13, 0.7), M.inscription);
    inscription.position.set(0, A + 0.4, 15.656);
    g.add(inscription);

    // Alınlıklar, boyalı kabartmalı timpanon ve eğik geison
    const top = A + 2.12;
    const half = 8.1;
    const rise = 2.3;
    const triangle = new THREE.Shape([new THREE.Vector2(-half, 0), new THREE.Vector2(half, 0), new THREE.Vector2(0, rise)]);
    const pediment = new THREE.ExtrudeGeometry(triangle, { depth: 1.2, bevelEnabled: false });
    const tx0 = -half + 0.9;
    const tx1 = half - 0.9;
    const tympanum = new THREE.ShapeGeometry(new THREE.Shape([
        new THREE.Vector2(tx0, 0.16), new THREE.Vector2(tx1, 0.16), new THREE.Vector2(0, rise - 0.3),
    ]));
    const tpos = tympanum.attributes.position;
    const tuv = tympanum.attributes.uv;
    for (let i = 0; i < tpos.count; i++) tuv.setXY(i, (tpos.getX(i) - tx0) / (tx1 - tx0), (tpos.getY(i) - 0.16) / (rise - 0.46));
    const slope = Math.atan2(rise, half);
    const slopeLen = Math.hypot(half, rise) + 0.6;
    [1, -1].forEach((side) => {
        const ped = new THREE.Mesh(pediment, M.plain);
        ped.position.set(0, top, side > 0 ? 14.4 : -15.6);
        ped.castShadow = ped.receiveShadow = true;
        g.add(ped);
        const tym = new THREE.Mesh(tympanum, M.tympanum);
        tym.position.set(0, top, side * 15.61);
        if (side < 0) tym.rotation.y = Math.PI;
        tym.receiveShadow = true;
        g.add(tym);
        [-1, 1].forEach((s) => {
            const rake = box(slopeLen, 0.36, 1.5, M.plain, s * half * 0.5, top + rise / 2 + 0.12, side * 15.3);
            rake.rotation.z = -s * slope;
        });
        // Akroterler: bronz palmetler
        [[0, top + rise + 0.35], [-half, top + 0.3], [half, top + 0.3]].forEach(([x, y]) => {
            const acro = new THREE.Mesh(new THREE.LatheGeometry(
                [[0, 0], [0.22, 0.02], [0.14, 0.25], [0.24, 0.5], [0.08, 0.72], [0, 0.8]].map(([r, h]) => new THREE.Vector2(r, h)), 24
            ), M.gold);
            acro.position.set(x, y - 0.3, side * 15.5);
            acro.castShadow = true;
            g.add(acro);
        });
    });

    // Kiremit çatı
    [-1, 1].forEach((s) => {
        const roof = box(slopeLen, 0.22, 33.2, M.roof, s * half * 0.5, top + rise / 2 + 0.36, 0);
        roof.rotation.z = -s * slope;
    });

    // Cella ve içeriden sızan kandil ışığı
    box(10.4, A + 0.8 - S, 22, M.wall, 0, (S + A + 0.8) / 2, -0.5);
    const door = new THREE.Mesh(new THREE.PlaneGeometry(2.6, 4.6), M.door);
    door.position.set(0, S + 2.3, 10.53);
    g.add(door);
    const lamp = new THREE.PointLight('#ffa860', 14, 9, 2);
    lamp.position.set(0, S + 2.5, 12.2);
    g.add(lamp);

    return g;
}

function buildWreath(mat, radius, leaves) {
    const leaf = new THREE.SphereGeometry(1, 12, 8).scale(0.09 * radius, 0.24 * radius, 0.03 * radius);
    const mesh = new THREE.InstancedMesh(leaf, mat, leaves * 4);
    const q = new THREE.Quaternion();
    const e = new THREE.Euler();
    let i = 0;
    [-1, 1].forEach((side) => {
        for (let k = 0; k < leaves; k++) {
            const a = -Math.PI / 2 + side * (0.2 + (k / leaves) * (Math.PI - 0.55));
            [-1, 1].forEach((layer) => {
                const r = radius * (1 + layer * 0.08);
                const theta = (side > 0 ? a : a + Math.PI) - side * layer * 0.5;
                q.setFromEuler(e.set(layer * 0.35, 0, theta, 'ZYX'));
                mesh.setMatrixAt(i++, new THREE.Matrix4().compose(
                    new THREE.Vector3(Math.cos(a) * r, Math.sin(a) * r, layer * 0.03 * radius),
                    q,
                    new THREE.Vector3(1, 1, 1)
                ));
            });
        }
    });
    mesh.castShadow = true;
    return mesh;
}

function buildTholos(M, parts, skills, glow) {
    const g = new THREE.Group();
    g.position.copy(THOLOS);
    [7.4, 6.9, 6.4].forEach((r, k) => {
        const step = new THREE.Mesh(new THREE.CylinderGeometry(r, r, 0.3, 96), M.step);
        step.position.y = 0.15 + k * 0.3;
        step.castShadow = step.receiveShadow = true;
        g.add(step);
    });
    const base = 0.9;
    const columns = skills.map((pct, i) => {
        const a = (i / skills.length) * Math.PI * 2 + Math.PI / 2;
        const x = Math.cos(a) * 5.2;
        const z = Math.sin(a) * 5.2;
        // Her sütunun kendi malzemesi var: üzerine gelinince yalnız o parlar
        const mat = M.column.clone();
        mat.onBeforeCompile = M.column.onBeforeCompile;
        mat.emissive = new THREE.Color('#ffb648');
        mat.emissiveIntensity = 0;
        const shaft = new THREE.Mesh(parts.fluted, mat);
        shaft.position.set(x, base, z);
        shaft.scale.set(0.66, 0.001, 0.66);
        const cap = new THREE.Group();
        const echinus = new THREE.Mesh(parts.echinus, mat);
        const abacus = new THREE.Mesh(parts.abacus, mat);
        abacus.position.y = 0.3;
        cap.add(echinus, abacus);
        cap.scale.setScalar(0.66);
        cap.position.set(x, base, z);
        [shaft, echinus, abacus].forEach((m) => { m.castShadow = m.receiveShadow = true; });
        const halo = new THREE.Sprite(new THREE.SpriteMaterial({
            map: glow, color: '#ffcf73', transparent: true, opacity: 0, depthWrite: false, blending: THREE.AdditiveBlending,
        }));
        halo.scale.setScalar(2.6);
        halo.position.set(x, base, z);
        g.add(shaft, cap, halo);
        // %75 → 2.4 birim, %100 → 7 birim: farklar gözle seçilsin
        const height = 2.4 + (Math.max(0, pct - 75) / 25) * 4.6;
        return { shaft, cap, halo, mat, height, glow: 0 };
    });

    const pedestal = new THREE.Mesh(new THREE.CylinderGeometry(0.75, 0.95, 1.6, 48), M.plain);
    pedestal.position.y = base + 0.8;
    pedestal.castShadow = pedestal.receiveShadow = true;
    g.add(pedestal);
    const wreath = buildWreath(M.gold, 1.15, 14);
    wreath.position.y = base + 3.6;
    g.add(wreath);
    return { group: g, columns, wreath, base };
}

// Vazo profilleri: [yarıçap, yükseklik]; kulplar: [yükseklik merkezi, eksene uzaklık, boy, dışa taşma, açı]
const VASE_SHAPES = {
    amphora: {
        pts: [[0, 0], [0.24, 0], [0.26, 0.05], [0.2, 0.1], [0.19, 0.16], [0.3, 0.3], [0.45, 0.55], [0.55, 0.82], [0.57, 0.98], [0.52, 1.15], [0.38, 1.3], [0.22, 1.4], [0.19, 1.5], [0.19, 1.62], [0.27, 1.68], [0.28, 1.75], [0.2, 1.75], [0.16, 1.6]],
        handles: [[1.4, 0.19, 0.36, 0.3, 0], [1.4, 0.19, 0.36, 0.3, Math.PI]],
    },
    krater: {
        pts: [[0, 0], [0.3, 0], [0.31, 0.06], [0.2, 0.12], [0.16, 0.22], [0.2, 0.3], [0.42, 0.42], [0.52, 0.55], [0.6, 0.8], [0.68, 1.1], [0.76, 1.36], [0.8, 1.42], [0.78, 1.46], [0.72, 1.44], [0.62, 1.2]],
        handles: [[0.52, 0.48, 0.22, 0.24, 0], [0.52, 0.48, 0.22, 0.24, Math.PI]],
    },
    hydria: {
        pts: [[0, 0], [0.26, 0], [0.27, 0.05], [0.2, 0.1], [0.24, 0.2], [0.4, 0.38], [0.55, 0.66], [0.6, 0.86], [0.56, 1.04], [0.42, 1.18], [0.24, 1.3], [0.18, 1.4], [0.19, 1.5], [0.29, 1.56], [0.29, 1.6], [0.2, 1.6], [0.15, 1.45]],
        handles: [[0.92, 0.57, 0.24, 0.2, Math.PI / 2], [0.92, 0.57, 0.24, 0.2, -Math.PI / 2], [1.32, 0.2, 0.46, 0.34, Math.PI]],
    },
    lekythos: {
        pts: [[0, 0], [0.22, 0], [0.23, 0.05], [0.15, 0.1], [0.18, 0.2], [0.28, 0.42], [0.32, 0.9], [0.33, 1.12], [0.3, 1.2], [0.12, 1.26], [0.08, 1.32], [0.08, 1.52], [0.13, 1.56], [0.15, 1.7], [0.11, 1.78], [0.07, 1.7]],
        handles: [[1.36, 0.08, 0.3, 0.22, Math.PI]],
    },
};

// Profil noktalarını yumuşatır: lathe yüzeyi köşeli değil akışkan görünsün
function smoothProfile(pts) {
    const curve = new THREE.SplineCurve(pts.map(([r, y]) => new THREE.Vector2(r, y)));
    return curve.getPoints(pts.length * 6);
}

function buildVase({ kind, motif }, M) {
    const shape = VASE_SHAPES[kind];
    const H = Math.max(...shape.pts.map((p) => p[1]));
    const geo = new THREE.LatheGeometry(smoothProfile(shape.pts), 96);
    // UV'nin dikey ekseni profil sırasına değil yüksekliğe bağlansın: bantlar düz otursun
    const pos = geo.attributes.position;
    const uv = geo.attributes.uv;
    for (let i = 0; i < pos.count; i++) uv.setY(i, pos.getY(i) / H);
    geo.computeVertexNormals();
    const body = new THREE.Mesh(geo, new THREE.MeshPhysicalMaterial({
        map: vasePaint(motif, 'color'),
        roughnessMap: vasePaint(motif, 'rough'),
        clearcoatMap: vasePaint(motif, 'coat'),
        roughness: 1,
        clearcoat: 1,
        clearcoatRoughness: 0.22,
        envMapIntensity: 1,
        side: THREE.DoubleSide,
    }));
    body.castShadow = body.receiveShadow = true;
    const group = new THREE.Group();
    group.add(body);
    shape.handles.forEach(([y, dist, h, out, angle]) => {
        // Yarım simit, gövdeden dışa doğru "C" biçiminde kıvrılır
        const geoH = new THREE.TorusGeometry(h / 2, 0.035, 12, 32, Math.PI).rotateZ(-Math.PI / 2).rotateY(-Math.PI / 2);
        const handle = new THREE.Mesh(geoH, M.glaze);
        handle.position.set(Math.sin(angle) * dist, y, Math.cos(angle) * dist);
        handle.rotation.y = angle;
        handle.scale.z = out / (h / 2);
        handle.castShadow = true;
        group.add(handle);
    });
    return group;
}

function buildAgora(M, parts) {
    const g = new THREE.Group();
    // Agora döşemesi: ayrı ayrı yerleştirilmiş taş plakalar
    const slabs = [];
    const rand = rng(41);
    for (let x = vaseX(0) - 4; x < vaseX(3) + 4; x += 1.6) {
        for (let z = VASE_Z - 3.2; z < VASE_Z + 3.2; z += 1.6) {
            slabs.push(mtx(x + 0.8, 0.04 + rand() * 0.03, z + 0.8, 1.52, 1, 1.52, (rand() - 0.5) * 0.03));
        }
    }
    g.add(instanced(new RoundedBoxGeometry(1, 0.12, 1, 2, 0.03), M.paving, slabs, 0.12));
    const vases = VASES.map((v, i) => {
        const x = vaseX(i);
        const plinth = new THREE.Mesh(new RoundedBoxGeometry(1.6, 0.26, 1.6, 2, 0.04), M.step);
        plinth.position.set(x, 0.23, VASE_Z);
        const shaft = new THREE.Mesh(parts.fluted, M.column);
        shaft.position.set(x, 0.36, VASE_Z);
        shaft.scale.set(0.9, 0.95, 0.9);
        const top = new THREE.Mesh(new RoundedBoxGeometry(1.3, 0.16, 1.3, 2, 0.03), M.plain);
        top.position.set(x, 1.39, VASE_Z);
        [plinth, shaft, top].forEach((m) => { m.castShadow = m.receiveShadow = true; g.add(m); });
        const vase = buildVase(v, M);
        vase.position.set(x, 1.47, VASE_Z);
        vase.rotation.y = i * 0.9;
        g.add(vase);
        return vase;
    });
    return { group: g, vases };
}

// Mangal: mermer kaide, bronz üç ayak ve çanak
function buildBrazier(M) {
    const g = new THREE.Group();
    const base = new THREE.Mesh(new RoundedBoxGeometry(1.1, 0.9, 1.1, 2, 0.04), M.plain);
    base.position.y = 0.45;
    g.add(base);
    for (let k = 0; k < 3; k++) {
        const a = (k / 3) * Math.PI * 2;
        const leg = new THREE.Mesh(new THREE.CylinderGeometry(0.035, 0.05, 1.25, 12), M.bronze);
        leg.position.set(Math.cos(a) * 0.28, 1.5, Math.sin(a) * 0.28);
        leg.rotation.set(Math.sin(a) * 0.22, 0, -Math.cos(a) * 0.22);
        g.add(leg);
    }
    const bowl = new THREE.Mesh(new THREE.LatheGeometry(
        smoothProfile([[0, 0], [0.22, 0.02], [0.46, 0.16], [0.6, 0.4], [0.56, 0.44], [0.42, 0.24], [0, 0.2]]), 48
    ), M.bronze);
    bowl.position.y = 2.05;
    const coals = new THREE.Mesh(new THREE.SphereGeometry(0.4, 24, 12, 0, Math.PI * 2, 0, Math.PI / 2), M.coals);
    coals.scale.y = 0.35;
    coals.position.y = 2.28;
    g.add(bowl, coals);
    g.traverse((m) => { if (m.isMesh) m.castShadow = m.receiveShadow = true; });
    return g;
}

// Ateş ve duman: yükselip küçülen alev parçacıkları, üstte genişleyerek sönen is
function createFire(count, glow) {
    const make = (n, blending, size) => {
        const geo = new THREE.BufferGeometry();
        geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(n * 3), 3));
        geo.setAttribute('color', new THREE.BufferAttribute(new Float32Array(n * 4), 4));
        const points = new THREE.Points(geo, new THREE.PointsMaterial({
            size, map: glow, vertexColors: true, transparent: true, depthWrite: false, blending,
        }));
        points.frustumCulled = false;
        return points;
    };
    const flame = make(count, THREE.AdditiveBlending, 0.5);
    const smoke = make(Math.round(count * 0.4), THREE.NormalBlending, 1.4);
    const seed = (n, speed) => Array.from({ length: n }, () => ({
        life: Math.random(), speed: speed * (0.7 + Math.random() * 0.6), ox: (Math.random() - 0.5) * 0.5, oz: (Math.random() - 0.5) * 0.5,
    }));
    const flames = seed(count, 1.1);
    const smokes = seed(Math.round(count * 0.4), 0.22);
    const update = (dt, t) => {
        const fp = flame.geometry.attributes.position.array;
        const fc = flame.geometry.attributes.color.array;
        flames.forEach((p, i) => {
            p.life += dt * p.speed;
            if (p.life > 1) { p.life -= 1; p.ox = (Math.random() - 0.5) * 0.5; p.oz = (Math.random() - 0.5) * 0.5; }
            const L = p.life;
            const shrink = 1 - L * 0.85;
            fp[i * 3] = p.ox * shrink + Math.sin(t * 7 + i) * 0.06 * L;
            fp[i * 3 + 1] = L * L * 1.1 + L * 0.5;
            fp[i * 3 + 2] = p.oz * shrink;
            const fade = Math.sin(Math.PI * Math.min(L * 1.15, 1));
            fc[i * 4] = 1.6 * fade;
            fc[i * 4 + 1] = (0.9 - L * 0.7) * fade;
            fc[i * 4 + 2] = Math.max(0, 0.3 - L * 0.5) * fade;
            fc[i * 4 + 3] = fade;
        });
        const sp = smoke.geometry.attributes.position.array;
        const sc = smoke.geometry.attributes.color.array;
        smokes.forEach((p, i) => {
            p.life += dt * p.speed;
            if (p.life > 1) p.life -= 1;
            const L = p.life;
            sp[i * 3] = p.ox * (0.4 + L * 2) + Math.sin(t * 0.8 + i) * 0.3 * L;
            sp[i * 3 + 1] = 1.2 + L * 4.5;
            sp[i * 3 + 2] = p.oz * (0.4 + L * 2);
            sc[i * 4] = sc[i * 4 + 1] = sc[i * 4 + 2] = 0.32;
            sc[i * 4 + 3] = Math.sin(Math.PI * L) * 0.16;
        });
        flame.geometry.attributes.position.needsUpdate = true;
        flame.geometry.attributes.color.needsUpdate = true;
        smoke.geometry.attributes.position.needsUpdate = true;
        smoke.geometry.attributes.color.needsUpdate = true;
    };
    return { flame, smoke, update };
}

// Güneş ışığında süzülen ince toz
function createDust(count, glow) {
    const rand = rng(11);
    const geo = new THREE.BufferGeometry();
    const pos = new Float32Array(count * 3);
    const speed = new Float32Array(count);
    for (let i = 0; i < count; i++) {
        pos[i * 3] = -30 + rand() * 90;
        pos[i * 3 + 1] = 0.5 + rand() * 14;
        pos[i * 3 + 2] = -25 + rand() * 80;
        speed[i] = 0.08 + rand() * 0.2;
    }
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    const points = new THREE.Points(geo, new THREE.PointsMaterial({
        size: 0.06, map: glow, color: '#ffe2b0', transparent: true, opacity: 0.6, depthWrite: false, blending: THREE.AdditiveBlending,
    }));
    points.frustumCulled = false;
    const update = (dt, t) => {
        for (let i = 0; i < count; i++) {
            pos[i * 3 + 1] += dt * speed[i];
            pos[i * 3] += Math.sin(t * 0.4 + i) * dt * 0.12;
            if (pos[i * 3 + 1] > 14.5) pos[i * 3 + 1] = 0.5;
        }
        geo.attributes.position.needsUpdate = true;
    };
    return { points, update };
}

// Serviler, zeytinler, kuru ot öbekleri, kayalar, yıkıntılar ve uzak adalar
function buildVegetation(M, parts, avoid) {
    const g = new THREE.Group();
    const rand = rng(3);
    const onPlateau = (x, z) => heightAt(x, z) > -0.8 && Math.hypot(x - PLATEAU.x, z - PLATEAU.z) < PLATEAU.r - 6;

    // Servi konumları: tapınağın arkasında bir sıra + platoya dağınık
    const cypressSpots = [];
    for (let x = -18; x <= 18; x += 4.5) cypressSpots.push([x + (rand() - 0.5), -24 + rand() * 1.5]);
    let tries = 0;
    while (cypressSpots.length < (lowPower ? 34 : 56) && tries++ < 5000) {
        const a = rand() * Math.PI * 2;
        const r = 14 + rand() * 44;
        const x = PLATEAU.x + Math.cos(a) * r;
        const z = PLATEAU.z + Math.sin(a) * r;
        if (onPlateau(x, z) && !avoid(x, z, 3)) cypressSpots.push([x, z]);
    }
    const cypressCards = [];
    const cores = [];
    const q = new THREE.Quaternion();
    const e = new THREE.Euler();
    cypressSpots.forEach(([x, z]) => {
        const y0 = heightAt(x, z);
        const Ht = 8 + rand() * 5;
        const Rm = 0.95 + rand() * 0.35;
        const center = [x, y0 + Ht * 0.45, z];
        const profile = (t) => (t < 0.22 ? 0.72 + t * 1.3 : 1) * Math.pow(1 - t, 0.9) * Rm;
        const n = lowPower ? 70 : 120;
        for (let k = 0; k < n; k++) {
            const t = Math.pow(rand(), 0.85) * 0.96;
            const a = rand() * Math.PI * 2;
            const r = profile(t) * (0.45 + 0.55 * Math.sqrt(rand()));
            const s = (0.9 + rand() * 0.6) * (1 - t * 0.45);
            q.setFromEuler(e.set((rand() - 0.5) * 1.2, rand() * Math.PI * 2, (rand() - 0.5) * 0.8));
            cypressCards.push({
                matrix: new THREE.Matrix4().compose(new THREE.Vector3(x + Math.cos(a) * r, y0 + 0.4 + t * Ht, z + Math.sin(a) * r), q, new THREE.Vector3(s, s * 1.2, s)),
                center,
                shade: 0.8 + rand() * 0.35,
            });
        }
        cores.push(mtx(x, y0, z, Rm * 0.75, Ht, Rm * 0.75));
    });
    const coreGeo = new THREE.LatheGeometry(
        [[0, 0], [0.5, 0.05], [0.75, 0.25], [0.6, 0.6], [0.3, 0.85], [0, 0.97]].map(([r, y]) => new THREE.Vector2(r, y)), 12
    );
    g.add(instanced(coreGeo, M.cypressCore, cores), foliageMesh(M.cypressLeaves, cypressCards));

    // Zeytin ağaçları: burgulu gövde + topak topak gümüşi taç
    const oliveSpots = [];
    tries = 0;
    while (oliveSpots.length < (lowPower ? 12 : 22) && tries++ < 4000) {
        const a = rand() * Math.PI * 2;
        const r = 16 + rand() * 40;
        const x = PLATEAU.x + Math.cos(a) * r;
        const z = PLATEAU.z + Math.sin(a) * r;
        if (onPlateau(x, z) && !avoid(x, z, 4)) oliveSpots.push([x, z]);
    }
    const trunkGeo = new THREE.CylinderGeometry(0.14, 0.32, 2.6, 10, 8).translate(0, 1.3, 0);
    const tp = trunkGeo.attributes.position;
    for (let i = 0; i < tp.count; i++) {
        const y = tp.getY(i);
        const a = y * 1.4;
        const x = tp.getX(i);
        const z = tp.getZ(i);
        tp.setXYZ(i, x * Math.cos(a) - z * Math.sin(a) + Math.sin(y * 2) * 0.12, y, x * Math.sin(a) + z * Math.cos(a));
    }
    trunkGeo.computeVertexNormals();
    const trunks = [];
    const oliveCards = [];
    oliveSpots.forEach(([x, z]) => {
        const y0 = heightAt(x, z);
        const s = 0.9 + rand() * 0.5;
        trunks.push(mtx(x, y0, z, s, s, s, rand() * 6));
        for (let c = 0; c < 4; c++) {
            const cx = x + (rand() - 0.5) * 2.4 * s;
            const cz = z + (rand() - 0.5) * 2.4 * s;
            const cy = y0 + (2.6 + rand() * 0.9) * s;
            const center = [cx, cy - 0.6, cz];
            for (let k = 0; k < (lowPower ? 26 : 44); k++) {
                const u = rand() * Math.PI * 2;
                const v = Math.acos(2 * rand() - 1);
                const r = Math.cbrt(rand()) * 1.25 * s;
                const sc = (0.9 + rand() * 0.5) * s;
                q.setFromEuler(e.set(rand() * 6, rand() * 6, rand() * 6));
                oliveCards.push({
                    matrix: new THREE.Matrix4().compose(new THREE.Vector3(cx + Math.sin(v) * Math.cos(u) * r, cy + Math.cos(v) * r * 0.65, cz + Math.sin(v) * Math.sin(u) * r), q, new THREE.Vector3(sc, sc, sc)),
                    center,
                    shade: 0.85 + rand() * 0.3,
                });
            }
        }
    });
    g.add(instanced(trunkGeo, M.bark, trunks), foliageMesh(M.oliveLeaves, oliveCards));

    // Kuru ot öbekleri: çapraz iki kart
    const grassCards = [];
    tries = 0;
    const grassCount = lowPower ? 450 : 1100;
    while (grassCards.length < grassCount * 2 && tries++ < grassCount * 8) {
        const x = PLATEAU.x + (rand() - 0.5) * 2 * PLATEAU.r;
        const z = PLATEAU.z + (rand() - 0.5) * 2 * PLATEAU.r;
        if (!onPlateau(x, z) || flatMask(x, z) > 0.2 || avoid(x, z, -3, false)) continue;
        const y0 = heightAt(x, z);
        const s = 0.45 + rand() * 0.6;
        const ry = rand() * Math.PI;
        [0, Math.PI / 2].forEach((off) => {
            grassCards.push({
                matrix: new THREE.Matrix4().compose(new THREE.Vector3(x, y0 + s * 0.42, z), q.setFromEuler(e.set(0, ry + off, 0)), new THREE.Vector3(s * 1.3, s, s)),
                center: [x, y0 - 2, z],
                shade: 0.8 + rand() * 0.35,
            });
        });
    }
    g.add(foliageMesh(M.grass, grassCards, 0.55));

    // Kayalar: falez kenarına ve platoya serpilmiş
    const boulders = [];
    tries = 0;
    while (boulders.length < (lowPower ? 26 : 48) && tries++ < 4000) {
        const a = rand() * Math.PI * 2;
        const r = 10 + rand() * 52;
        const x = PLATEAU.x + Math.cos(a) * r;
        const z = PLATEAU.z + Math.sin(a) * r;
        if (heightAt(x, z) < -1.5 || avoid(x, z, 2, false)) continue;
        const s = 0.4 + Math.pow(rand(), 2) * 1.8;
        boulders.push(mtx(x, heightAt(x, z) + s * 0.15, z, s * (1 + rand() * 0.6), s, s * (1 + rand() * 0.4), rand() * 6));
    }
    g.add(instanced(rockGeometry(1.3), M.rock, boulders));

    // Yıkıntılar: devrilmiş sütun tamburları ve kırık sütunlar
    const drums = [[22, -15, 0.4], [24.6, -13, 1.9], [39, 5, 0.9], [-13, -21, 2.3], [-15.5, -19, 0.2], [11, 46, 1.2], [44, 28, 2.8], [-30, 4, 1.1]];
    g.add(instanced(parts.fluted, M.column, drums.map(([x, z, ry]) => mtx(x, heightAt(x, z) + 0.46, z, 1, 1.15, 1, ry, Math.PI / 2)), 0.1));
    const stumps = [[26, -16, 2.2], [40, -1, 1.4], [-17, -17, 3.1], [45, 25, 1.8], [-33, 2, 2.6]];
    g.add(instanced(parts.fluted, M.column, stumps.map(([x, z, h]) => mtx(x, heightAt(x, z) - 0.1, z, 1, h, 1, x)), 0.1));

    // Uzak adalar: pusun içinde kalan kaya kütleleri; yalnızca dağlık tepeleri sudan çıkar
    const islands = [[-420, -290, 90, 44], [-180, -540, 120, 62], [300, -480, 95, 50], [540, -110, 70, 34], [-590, 110, 110, 52]];
    g.add(instanced(rockGeometry(7.1), M.island, islands.map(([x, z, s, h]) => mtx(x, -14 - h * 0.45, z, s, h, s * 0.7, x * 0.01))));
    return g;
}

// İnce film greni: kusursuz CG temizliğini kırar
const GrainShader = {
    uniforms: { tDiffuse: { value: null }, uTime: { value: 0 }, uAmount: { value: 0.035 } },
    vertexShader: 'varying vec2 vUv; void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }',
    fragmentShader: /* glsl */ `
        uniform sampler2D tDiffuse;
        uniform float uTime, uAmount;
        varying vec2 vUv;
        float rand(vec2 c) { return fract(sin(dot(c, vec2(12.9898, 78.233))) * 43758.5453); }
        void main() {
            vec4 c = texture2D(tDiffuse, vUv);
            float g = rand(vUv * 1000.0 + fract(uTime) * 100.0) - 0.5;
            c.rgb += g * uAmount * (0.25 + c.rgb);
            gl_FragColor = c;
        }`,
};

// Mobilde yansıma hesaplamayan hafif deniz
function simpleSeaMaterial(normals) {
    const material = new THREE.ShaderMaterial({
        fog: true,
        uniforms: THREE.UniformsUtils.merge([THREE.UniformsLib.fog, {
            uTime: { value: 0 },
            uDeep: { value: new THREE.Color('#123c52') },
            uSky: { value: new THREE.Color('#e9b98f') },
            uSun: { value: new THREE.Color('#ffd9a8') },
            uSunDir: { value: SUN_DIR },
        }]),
        vertexShader: /* glsl */ `
            #include <fog_pars_vertex>
            varying vec3 vWorld;
            void main() {
                vec4 w = modelMatrix * vec4(position, 1.0);
                vWorld = w.xyz;
                vec4 mvPosition = viewMatrix * w;
                gl_Position = projectionMatrix * mvPosition;
                #include <fog_vertex>
            }`,
        fragmentShader: /* glsl */ `
            #include <common>
            #include <fog_pars_fragment>
            uniform float uTime;
            uniform sampler2D tNormal;
            uniform vec3 uDeep, uSky, uSun, uSunDir;
            varying vec3 vWorld;
            void main() {
                vec2 p = vWorld.xz * 0.02;
                vec3 n1 = texture2D(tNormal, p + uTime * 0.01).xzy * 2.0 - 1.0;
                vec3 n2 = texture2D(tNormal, p * 2.3 - uTime * 0.013).xzy * 2.0 - 1.0;
                vec3 n = normalize(vec3(n1.x + n2.x, 6.0, n1.z + n2.z));
                vec3 v = normalize(cameraPosition - vWorld);
                float fres = pow(1.0 - max(dot(v, n), 0.0), 5.0);
                vec3 col = mix(uDeep, uSky, 0.08 + fres * 0.8);
                float s = max(dot(reflect(-v, n), uSunDir), 0.0);
                col += uSun * (pow(s, 300.0) * 6.0 + pow(s, 20.0) * 0.3);
                gl_FragColor = vec4(col, 1.0);
                #include <fog_fragment>
            }`,
    });
    // Render target dokuları UniformsUtils.merge ile kopyalanamaz; birleştirmeden sonra atanır
    material.uniforms.tNormal = { value: normals };
    return material;
}

// ── Dünya ──
export async function createWorld(canvas, state, { skills = [] } = {}) {
    const renderer = new THREE.WebGLRenderer({ canvas, antialias: false, powerPreference: 'high-performance' });
    const dpr = Math.min(window.devicePixelRatio, lowPower ? 1.5 : 1.75);
    renderer.setPixelRatio(dpr);
    renderer.setSize(window.innerWidth, window.innerHeight, false);
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 0.5;
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    // Gölge haritası karede bir kez güncellenir (su yansıması ve AO geçişleri yeniden çizmesin)
    renderer.shadowMap.autoUpdate = false;

    const scene = new THREE.Scene();
    scene.fog = new THREE.FogExp2('#d9b8a2', 0.0031);
    const camera = new THREE.PerspectiveCamera(35, window.innerWidth / window.innerHeight, 0.5, 9000);

    // Fiziksel (Preetham) gün batımı gökyüzü; ortam ışığı da ondan üretilir
    const skyParams = (sky, rayleigh = 2.4) => {
        const u = sky.material.uniforms;
        u.turbidity.value = 9;
        u.rayleigh.value = rayleigh;
        u.mieCoefficient.value = 0.006;
        u.mieDirectionalG.value = 0.88;
        u.sunPosition.value.copy(SUN_DIR);
        return sky;
    };
    const sky = skyParams(new Sky());
    sky.scale.setScalar(4000);
    scene.add(sky);
    const pmrem = new THREE.PMREMGenerator(renderer);
    const envScene = new THREE.Scene();
    // Ortam ışığı için daha az Rayleigh: gölgeler maviye kaçmasın, gün batımının sıcaklığı korunsun
    const envSky = skyParams(new Sky(), 1.1);
    envSky.scale.setScalar(100);
    envScene.add(envSky);
    scene.environment = pmrem.fromScene(envScene, 0.03, 0.1, 1000).texture;
    pmrem.dispose();

    // Yüzey dokuları
    const big = lowPower ? 512 : 1024;
    const tex = {
        marble: bakeSurface(renderer, 'marble', big, 0.004),
        ground: bakeSurface(renderer, 'ground', big, 0.006),
        rock: bakeSurface(renderer, 'rock', lowPower ? 512 : 768, 0.008),
        tiles: bakeSurface(renderer, 'tiles', 512, 0.02),
        water: bakeSurface(renderer, 'water', 512, 0.012, { color: false }),
    };

    // Su: masaüstünde gerçek yansımalı, mobilde hafif gölgelendirici
    let water;
    if (lowPower) {
        water = new THREE.Mesh(new THREE.PlaneGeometry(6000, 6000), simpleSeaMaterial(tex.water.normal));
    } else {
        water = new Water(new THREE.PlaneGeometry(6000, 6000), {
            textureWidth: 512,
            textureHeight: 512,
            waterNormals: tex.water.normal,
            sunDirection: SUN_DIR.clone(),
            sunColor: 0xffd2a0,
            waterColor: 0x2a6782,
            distortionScale: 3.2,
            fog: true,
        });
        water.material.uniforms.size.value = 3;
    }
    water.rotation.x = -Math.PI / 2;
    water.position.y = -14;
    scene.add(water);

    // Işıklar: alçak, turuncu güneş; dolgu ışığını gökyüzü ortam haritası verir
    const sun = new THREE.DirectionalLight('#ffbd80', 5.2);
    sun.target.position.set(10, 0, 12);
    sun.position.copy(SUN_DIR).multiplyScalar(140).add(sun.target.position);
    scene.add(sun, sun.target);
    sun.castShadow = true;
    sun.shadow.mapSize.set(lowPower ? 1024 : 4096, lowPower ? 1024 : 4096);
    Object.assign(sun.shadow.camera, { left: -60, right: 60, top: 60, bottom: -60, near: 30, far: 300 });
    sun.shadow.bias = -0.0003;
    sun.shadow.normalBias = 0.03;

    // Malzemeler
    const glow = glowTexture();
    const tym = tympanumMaps();
    const M = {
        column: stoneMaterial(tex.marble, { joints: [1.07, 0, 0.012], grime: 0.7 }),
        plain: stoneMaterial(tex.marble, { grime: 0.5 }),
        step: stoneMaterial(tex.marble, { tint: '#f1e9dc', joints: [0.4, 1.45, 0.014], grime: 1 }),
        wall: stoneMaterial(tex.marble, { tint: '#efe4d0', joints: [0.62, 1.3, 0.012], grime: 0.9 }),
        paving: stoneMaterial(tex.marble, { tint: '#e2d6c2', scale: 0.4, grime: 0.6 }),
        blue: stoneMaterial(tex.marble, { tint: '#4f5f7a', roughness: 1.1, grime: 0 }),
        red: stoneMaterial(tex.marble, { tint: '#b4553b', grime: 0 }),
        roof: stoneMaterial(tex.tiles, { scale: 0.28, macro: 0.4, grime: 0, normalScale: 1.2 }),
        groove: new THREE.MeshStandardMaterial({ color: '#1d2433', roughness: 1 }),
        terrain: stoneMaterial(tex.ground, { scale: 0.11, macro: 1, grime: 0, normalScale: 1.2, rock: tex.rock, rockScale: 0.06 }),
        rock: stoneMaterial(tex.rock, { scale: 0.18, grime: 0.3, macro: 0.8 }),
        island: stoneMaterial(tex.rock, { scale: 0.02, grime: 0, macro: 1, tint: '#c9b3a6' }),
        bark: stoneMaterial(tex.rock, { scale: 0.9, grime: 0, tint: '#7a6a5a', normalScale: 1.6 }),
        tympanum: new THREE.MeshStandardMaterial({
            map: tym.map, normalMap: tym.normalMap, roughnessMap: tym.mr, metalnessMap: tym.mr, roughness: 1, metalness: 1, envMapIntensity: 1.1,
        }),
        inscription: new THREE.MeshStandardMaterial({ color: '#6f5e4b', roughness: 0.9, transparent: true, depthWrite: false }),
        door: new THREE.MeshBasicMaterial({ map: doorTexture(), color: new THREE.Color(1.6, 1.3, 1.1) }),
        gold: new THREE.MeshStandardMaterial({ color: '#e7b85f', metalness: 1, roughness: 0.24, envMapIntensity: 1.2 }),
        bronze: new THREE.MeshStandardMaterial({ color: '#8a5a33', metalness: 0.9, roughness: 0.42, envMapIntensity: 1 }),
        glaze: new THREE.MeshPhysicalMaterial({ color: '#17100c', roughness: 0.35, clearcoat: 1, clearcoatRoughness: 0.2 }),
        coals: new THREE.MeshBasicMaterial({ color: new THREE.Color(3, 1.1, 0.35) }),
        cypressCore: new THREE.MeshStandardMaterial({ color: '#1f2916', roughness: 1 }),
        cypressLeaves: foliageTexture('cypress'),
        oliveLeaves: foliageTexture('olive'),
        grass: foliageTexture('grass'),
    };
    inscriptionMaps((maps) => {
        M.inscription.alphaMap = maps.alphaMap;
        M.inscription.normalMap = maps.normalMap;
        M.inscription.needsUpdate = true;
    });

    const parts = columnParts();
    scene.add(buildTemple(M, parts));
    const tholos = buildTholos(M, parts, skills, glow);
    scene.add(tholos.group);
    const agora = buildAgora(M, parts);
    scene.add(agora.group);
    scene.add(buildTerrain(M.terrain));

    // Kamera yolu: duraklar ve ara noktalardan geçen yumuşak eğri
    const posCurve = new THREE.CatmullRomCurve3(PATH.map((p) => new THREE.Vector3(...p.pos)), false, 'centripetal');
    const lookCurve = new THREE.CatmullRomCurve3(PATH.map((p) => new THREE.Vector3(...p.look)), false, 'centripetal');
    const focusCurve = new THREE.CatmullRomCurve3(PATH.map((p) => new THREE.Vector3(...p.focus)), false, 'centripetal');
    const stopU = PATH.map((p, i) => (p.stop ? i / (PATH.length - 1) : -1)).filter((u) => u >= 0);

    // Ağaçlar yapılara, kamera yoluna ve giriş görünümünün önüne girmesin
    const pathSamples = posCurve.getPoints(120);
    // Her duraktaki görüş koridoru: kameradan bakış noktasına uzanan hat boyunca örnekler
    const sightSamples = PATH.filter((p) => p.stop).flatMap((p) => Array.from({ length: 12 }, (_, k) => {
        const f = k / 11;
        return { x: p.pos[0] + (p.look[0] - p.pos[0]) * f, z: p.pos[2] + (p.look[2] - p.pos[2]) * f, r: 7 + (1 - f) * 7 };
    }));
    // Giriş görünümünün sol yarısı açık kalsın: yazının arkasında deniz ve adalar görünsün
    const hero = PATH[0].pos;
    const heroDir = new THREE.Vector2(PATH[0].look[0] - hero[0], PATH[0].look[2] - hero[2]).normalize();
    const inHeroView = (x, z) => {
        const dx = x - hero[0];
        const dz = z - hero[2];
        const angle = Math.atan2(heroDir.x * dz - heroDir.y * dx, heroDir.x * dx + heroDir.y * dz);
        return Math.hypot(dx, dz) < 140 && angle > -0.66 && angle < 0.12;
    };
    const avoid = (x, z, pad, tall = true) => flatMask(x, z) > 0.05
        || (tall && inHeroView(x, z))
        || Math.hypot(x - hero[0], z - hero[2]) < 40 + pad
        || pathSamples.some((p) => Math.hypot(p.x - x, p.z - z) < 5 + pad)
        || (tall && sightSamples.some((p) => Math.hypot(p.x - x, p.z - z) < p.r));
    const vegetation = buildVegetation(M, parts, avoid);
    scene.add(vegetation);

    // Tapınak önünde iki mangal
    const fires = [-10.4, 10.4].map((x) => {
        const brazier = buildBrazier(M);
        brazier.position.set(x, 0, 19.2);
        scene.add(brazier);
        const fire = createFire(lowPower ? 40 : 70, glow);
        fire.flame.position.set(x, 2.3, 19.2);
        fire.smoke.position.set(x, 2.3, 19.2);
        scene.add(fire.flame, fire.smoke);
        const light = new THREE.PointLight('#ff9a4a', 30, 18, 2);
        light.position.set(x, 3, 19.2);
        scene.add(light);
        return { ...fire, light };
    });
    const dust = createDust(lowPower ? 200 : 400, glow);
    scene.add(dust.points);

    // Son işleme: MSAA → GTAO (masaüstü) → bloom → gren → ton eşleme
    const width = window.innerWidth;
    const height = window.innerHeight;
    const composer = new EffectComposer(renderer, new THREE.WebGLRenderTarget(width * dpr, height * dpr, { type: THREE.HalfFloatType, samples: lowPower ? 2 : 4 }));
    composer.setPixelRatio(dpr);
    composer.setSize(width, height);
    composer.addPass(new RenderPass(scene, camera));
    if (!lowPower) {
        const gtao = new GTAOPass(scene, camera, width * dpr, height * dpr);
        gtao.output = GTAOPass.OUTPUT.Default;
        gtao.updateGtaoMaterial({ radius: 1.2, distanceExponent: 2, thickness: 2, scale: 1.1, samples: 16 });
        gtao.updatePdMaterial({ lumaPhi: 10, depthPhi: 2, normalPhi: 3, radius: 6, rings: 2, samples: 16 });
        // AO geçişinde gökyüzü, su, sprite'lar ve yaprak kartları gizlenir (opak kare gibi görünmesinler)
        const hide = [sky, water];
        scene.traverse((o) => { if (o.userData.foliage || o.isSprite) hide.push(o); });
        const override = gtao.overrideVisibility.bind(gtao);
        gtao.overrideVisibility = () => {
            override();
            hide.forEach((o) => { o.visible = false; });
        };
        composer.addPass(gtao);
        // Su yansıması AO'nun normal geçişinde yeniden hesaplanmasın
        const renderMirror = water.onBeforeRender;
        water.onBeforeRender = (...args) => { if (!scene.overrideMaterial) renderMirror.apply(water, args); };
    }
    composer.addPass(new UnrealBloomPass(new THREE.Vector2(width, height), 0.3, 0.55, 0.9));
    const grain = new ShaderPass(GrainShader);
    composer.addPass(grain);
    composer.addPass(new OutputPass());

    // İmleç: kamerayı hafifçe kaydırır (paralaks)
    const pointer = { x: 0, y: 0, tx: 0, ty: 0 };
    window.addEventListener('pointermove', (e) => {
        if (e.pointerType !== 'mouse') return;
        pointer.tx = (e.clientX / window.innerWidth) * 2 - 1;
        pointer.ty = (e.clientY / window.innerHeight) * 2 - 1;
    }, { passive: true });

    const resize = () => {
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.fov = camera.aspect < 0.9 ? 48 : 35;
        camera.updateProjectionMatrix();
        renderer.setSize(window.innerWidth, window.innerHeight, false);
        composer.setSize(window.innerWidth, window.innerHeight);
    };
    window.addEventListener('resize', resize);
    resize();

    const camPos = new THREE.Vector3();
    const camLook = new THREE.Vector3();
    const right = new THREE.Vector3();
    const up = new THREE.Vector3();
    const clock = new THREE.Clock();
    const lastStop = stopU.length - 1;
    let cur = state.t;

    const frame = () => {
        const dt = Math.min(clock.getDelta(), 0.05);
        const t = clock.elapsedTime;
        cur += (state.t - cur) * (1 - Math.exp(-dt * 4.5));

        // Durak değerini eğri parametresine çevir: duraklar arası doğrusal
        const c = THREE.MathUtils.clamp(cur, 0, lastStop);
        const i = Math.min(Math.floor(c), lastStop - 1);
        const u = THREE.MathUtils.lerp(stopU[i], stopU[i + 1], c - i);
        posCurve.getPoint(u, camPos);
        // Dikey ekranda kamera modele döner ve geri çekilir ki model sığsın
        if (camera.aspect < 0.9) {
            focusCurve.getPoint(u, camLook);
            camPos.sub(camLook).multiplyScalar(1.35).add(camLook);
        } else lookCurve.getPoint(u, camLook);
        if (state.intro < 1) camPos.lerpVectors(INTRO, camPos, state.intro);

        pointer.x += (pointer.tx - pointer.x) * 0.04;
        pointer.y += (pointer.ty - pointer.y) * 0.04;
        right.setFromMatrixColumn(camera.matrixWorld, 0);
        up.setFromMatrixColumn(camera.matrixWorld, 1);
        camPos.addScaledVector(right, pointer.x * 0.8).addScaledVector(up, -pointer.y * 0.4 + Math.sin(t * 0.35) * 0.06);
        camera.position.copy(camPos);
        camera.lookAt(camLook);

        if (water.material.uniforms.time) water.material.uniforms.time.value = t * 0.5;
        else water.material.uniforms.uTime.value = t;

        // Yetenek sütunları: sırayla yükselir; üzerine gelinen sütun altınla parlar
        tholos.columns.forEach((col, k) => {
            const rise = THREE.MathUtils.clamp(state.skillRise * 1.5 - k * 0.04, 0, 1);
            const eased = 1 - Math.pow(1 - rise, 3);
            const h = Math.max(0.001, col.height * eased);
            col.shaft.scale.y = h;
            col.cap.position.y = tholos.base + h;
            col.cap.visible = eased > 0.02;
            col.glow += ((state.skillHover === k ? 1 : 0) - col.glow) * (1 - Math.exp(-dt * 8));
            col.mat.emissiveIntensity = col.glow * 0.6;
            col.halo.material.opacity = col.glow * 0.9;
            col.halo.position.y = tholos.base + h + 0.9;
        });
        tholos.wreath.rotation.y = t * 0.5;
        tholos.wreath.position.y = tholos.base + 3.6 + Math.sin(t * 1.2) * 0.12;

        agora.vases.forEach((vase, k) => { vase.rotation.y = k * 0.9 + t * 0.3; });

        fires.forEach((f, k) => {
            f.update(dt, t);
            f.light.intensity = 26 + Math.sin(t * 13 + k) * 4 + Math.sin(t * 7.3 + k * 2) * 3;
        });
        dust.update(dt, t);
        grain.uniforms.uTime.value = t;

        renderer.shadowMap.needsUpdate = true;
        composer.render(dt);
    };

    renderer.compile(scene, camera);
    frame();
    renderer.setAnimationLoop(() => { if (!document.hidden) frame(); });
}
