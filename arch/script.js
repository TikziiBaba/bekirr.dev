// Configuration
const images = [
	'img/1.jpg',
	'img/2.jpg',
	
	
];
const intervalMs = 5000; // 5 seconds

const slideshow = document.getElementById('slideshow');
const audio = document.getElementById('bg-audio');
const playPauseBtn = document.getElementById('playPause');
const muteToggleBtn = document.getElementById('muteToggle');

let idx = 0;
let timer = null;

function setBackground(src) {
	// fade out -> change -> fade in
	slideshow.style.opacity = '0';
	setTimeout(() => {
		slideshow.style.backgroundImage = `url('${src}')`;
		slideshow.style.opacity = '1';
	}, 400);
}

function nextImage() {
	idx = (idx + 1) % images.length;
	setBackground(images[idx]);
}

function startSlideshow() {
	if (timer) return;
	timer = setInterval(nextImage, intervalMs);
}

function stopSlideshow() {
	if (!timer) return;
	clearInterval(timer);
	timer = null;
}

// Preload images
images.forEach(src => {
	const img = new Image();
	img.src = src;
});

// Initialize
if (images.length) {
	slideshow.style.backgroundImage = `url('${images[0]}')`;
	slideshow.style.opacity = '1';
}
startSlideshow();

// Audio controls: note browsers block autoplay sound; user may need to click play.
let isPlaying = false;

playPauseBtn.addEventListener('click', async () => {
	if (!isPlaying) {
		try {
			await audio.play();
			isPlaying = true;
			playPauseBtn.textContent = 'Durdur';
		} catch (err) {
			// autoplay blocked
			isPlaying = false;
			playPauseBtn.textContent = 'Oynat';
			console.warn('Ses otomatik oynatılamadı. Kullanıcı etkileşimi gereklidir.', err);
		}
	} else {
		audio.pause();
		isPlaying = false;
		playPauseBtn.textContent = 'Oynat';
	}
});

muteToggleBtn.addEventListener('click', () => {
	audio.muted = !audio.muted;
	muteToggleBtn.textContent = audio.muted ? 'Ses Aç' : 'Sessiz';
});

// If user interacts with page (click anywhere) try to start audio if not playing
function tryStartOnInteraction() {
	if (!isPlaying) {
		audio.play().then(() => {
			isPlaying = true;
			playPauseBtn.textContent = 'Durdur';
		}).catch(() => {});
	}
	window.removeEventListener('click', tryStartOnInteraction);
}
window.addEventListener('click', tryStartOnInteraction);
