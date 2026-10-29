// ============================================
// bekirr.dev — Ana script
// Bölümlerin belirmesi + iletişim formu
// ============================================

// ---------- Belirme ----------
const revealEls = document.querySelectorAll('[data-reveal]');
if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
    revealEls.forEach((el) => el.classList.add('is-visible'));
} else {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) return;
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
        });
    }, { rootMargin: '0px 0px -8% 0px' });
    revealEls.forEach((el) => observer.observe(el));
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
        return;
    }

    const subject = encodeURIComponent(`Web sitesinden mesaj: ${name}`);
    const body = encodeURIComponent(`${message}\n\n— ${name} (${email})`);
    contactNote.textContent = 'E-posta uygulaman açılıyor...';
    window.location.href = `mailto:dedyusuf99@gmail.com?subject=${subject}&body=${body}`;
});
