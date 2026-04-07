/**
 * Kullanıcı kendi resimlerini proje klasörüne ekleyip,
 * isimlerini bu diziye yazacak.
 * Örneğin: 'benim-resmim.png', 'tatil.jpg' vb.
 */
const localImages = [
    'bekir1.jpeg',
    'bekir2.jpeg',
    'bekir3.jpeg',
    'bekir4.jpeg',
    'bekir5.jpeg',
    'bekir6.jpeg',
    'saliha1.jpeg',
    'saliha2.jpeg',
    'saliha3.jpeg',
    'saliha4.jpeg',
    'saliha5.jpeg',
    'saliha6.jpeg',
    'sura1.jpeg',
    'sura2.jpeg',
    'sura3.jpeg',
    'sura4.jpeg',
    'sura5.jpeg',
    'sura6.jpeg'
];

const gallery = document.getElementById('gallery');
const lightbox = document.getElementById('lightbox');
const lightboxImg = document.getElementById('lightbox-img');
const lightboxOverlay = document.getElementById('lightbox-overlay');
const closeBtn = document.getElementById('close-btn');
const prevBtn = document.getElementById('prev-btn');
const nextBtn = document.getElementById('next-btn');

let currentIndex = 0;

// Galeriyi oluştur
function initGallery() {
    localImages.forEach((src, index) => {
        const item = document.createElement('div');
        item.classList.add('gallery-item');
        
        const img = document.createElement('img');
        img.src = src;
        img.alt = `Galeri Resmi ${index + 1}`;
        img.loading = "lazy";
        
        // Hata durumunda (kullanıcı henüz resim eklememişse)
        // resmi görünmez yap, arkadaki "Resim Bekleniyor" metni görünsün.
        img.onerror = () => {
            img.style.opacity = '0';
        };
        img.onload = () => {
            img.style.opacity = '1';
        };

        item.appendChild(img);
        
        // Tıklanınca lightbox'ı aç
        item.addEventListener('click', () => openLightbox(index));
        
        gallery.appendChild(item);
    });
}

// Lightbox'ı aç
function openLightbox(index) {
    currentIndex = index;
    updateLightboxImage();
    lightbox.classList.add('active');
    // Arka planın kaydırılmasını (scrolling) engelle
    document.body.style.overflow = 'hidden'; 
}

// Lightbox'ı kapat
function closeLightbox() {
    lightbox.classList.remove('active');
    document.body.style.overflow = '';
    
    // Kapanma animasyonu bittikten sonra resmi temizle (opsiyonel)
    setTimeout(() => {
        if (!lightbox.classList.contains('active')) {
            lightboxImg.src = ''; 
        }
    }, 400);
}

// Resmi güncelle
function updateLightboxImage() {
    // Animasyon hissi için geçiş sırasında resmi küçültüp görünmez yapıyoruz
    lightboxImg.style.opacity = '0';
    lightboxImg.style.transform = 'scale(0.95)';
    
    setTimeout(() => {
        lightboxImg.src = localImages[currentIndex];
        
        // Resim başarılı yüklendiğinde göster
        lightboxImg.onload = () => {
            lightboxImg.style.opacity = '1';
            lightboxImg.style.transform = 'scale(1)';
        };
        
        // Resim bulunamadığında bile görünür yapıp tarayıcının kırık ikonunu gösterebiliriz
        // veya boş kalmasını sağlayabiliriz.
        lightboxImg.onerror = () => {
            lightboxImg.style.opacity = '1';
            lightboxImg.style.transform = 'scale(1)';
        };
    }, 200);
}

// Önceki resim
function showPrev(e) {
    if(e) e.stopPropagation();
    currentIndex = (currentIndex - 1 + localImages.length) % localImages.length;
    updateLightboxImage();
}

// Sonraki resim
function showNext(e) {
    if(e) e.stopPropagation();
    currentIndex = (currentIndex + 1) % localImages.length;
    updateLightboxImage();
}

// Event Listeners (Olay Dinleyicileri)
closeBtn.addEventListener('click', closeLightbox);
lightboxOverlay.addEventListener('click', closeLightbox);
prevBtn.addEventListener('click', showPrev);
nextBtn.addEventListener('click', showNext);

// Klavye ile gezinme desteği
document.addEventListener('keydown', (e) => {
    if (!lightbox.classList.contains('active')) return;
    
    if (e.key === 'Escape') closeLightbox();
    if (e.key === 'ArrowLeft') showPrev();
    if (e.key === 'ArrowRight') showNext();
});

// Sayfa yüklendiğinde galeriyi başlat
document.addEventListener('DOMContentLoaded', initGallery);
