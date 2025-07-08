// BÖLÜM 1: İNTERAKTİF KANVAS (Değişiklik yok)
const canvas = document.getElementById('interactive-canvas');
const ctx = canvas.getContext('2d');
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

let particles = [];
const mouse = {
    x: null,
    y: null,
    radius: 100
};

window.addEventListener('mousemove', function(event) {
    mouse.x = event.x;
    mouse.y = event.y;
});

window.addEventListener('resize', function() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    init(); // Ekran boyutu değiştiğinde parçacıkları yeniden başlat
});

window.addEventListener('mouseout', function() {
    mouse.x = undefined;
    mouse.y = undefined;
});

class Particle {
    constructor(x, y, directionX, directionY, size, color) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;
        this.size = size;
        this.color = color;
        this.baseX = this.x;
        this.baseY = this.y;
        this.density = (Math.random() * 30) + 1;
    }
    draw() {
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2, false);
        ctx.fillStyle = 'rgba(0, 119, 255, 0.5)';
        ctx.fill();
    }
    update() {
        let dx = mouse.x - this.x;
        let dy = mouse.y - this.y;
        let distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < mouse.radius) {
            this.x -= dx / 15;
            this.y -= dy / 15;
        } else {
            if (this.x !== this.baseX) {
                let dx_base = this.x - this.baseX;
                this.x -= dx_base / 10;
            }
            if (this.y !== this.baseY) {
                let dy_base = this.y - this.baseY;
                this.y -= dy_base / 10;
            }
        }
        this.draw();
    }
}

function init() {
    particles = [];
    let numberOfParticles = (canvas.height * canvas.width) / 9000;
    for (let i = 0; i < numberOfParticles; i++) {
        let size = (Math.random() * 2) + 1;
        let x = (Math.random() * ((innerWidth - size * 2) - (size * 2)) + size * 2);
        let y = (Math.random() * ((innerHeight - size * 2) - (size * 2)) + size * 2);
        let directionX = (Math.random() * .2) - .1;
        let directionY = (Math.random() * .2) - .1;
        let color = 'rgba(0, 119, 255, 0.5)';
        particles.push(new Particle(x, y, directionX, directionY, size, color));
    }
}

function animate() {
    requestAnimationFrame(animate);
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (let i = 0; i < particles.length; i++) {
        particles[i].update();
    }
}

init();
animate();


// BÖLÜM 2: KAYDIRMA ANİMASYONU (Yeni eklenen kod)
const sections = document.querySelectorAll('.content-section');

const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
        }
    });
}, {
    threshold: 0.1 // Bölümün %10'u göründüğünde animasyon tetiklenir
});

sections.forEach(section => {
    observer.observe(section);
});