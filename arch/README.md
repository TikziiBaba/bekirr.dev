Şiir Tanıtım Sitesi
===================

Bu küçük site açıldığında ekrana şiir metnini gösterir, arka planda 5 saniyede bir değişen fotoğraflar oynatır ve bir fon müziği çalmak için kontrol sunar.

Kullanım
-------

1. Proje klasöründe `images` adında bir klasör oluşturun ve `1.jpg`, `2.jpg`, `3.jpg`, `4.jpg` gibi resimler ekleyin (veya `script.js` içindeki `images` dizisini düzenleyin).
2. `audio` adında bir klasör oluşturun ve bir MP3 dosyasını `bg-music.mp3` adıyla ekleyin (veya `index.html` içindeki audio kaynağını değiştirin).
3. `index.html` dosyasını tarayıcıda açın. Bazı tarayıcılar otomatik ses oynatımını engelleyebilir; bu durumda sayfada bir defa tıklamanız veya "Oynat" düğmesine basmanız gerekir.

Notlar
------

- Arka plan resimleri `styles.css` tarafından kaplayacak şekilde ayarlanmıştır.
- Geçiş süresi 1s, resimler 5s aralıkla değişir. Bu değerleri `script.js` içindeki `intervalMs` ve CSS transition değerinden değiştirebilirsiniz.
- Daha fazla geliştirme: şiiri dinamik yükleme, farklı temalar veya mobil optimizasyon.
