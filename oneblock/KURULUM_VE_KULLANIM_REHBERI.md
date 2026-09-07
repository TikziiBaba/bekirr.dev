# TekBlok (OneBlock Skyblock) 1.16.1 Kurulum ve Kullanım Rehberi

Bu paket, **Minecraft 1.16.1 (Spigot/Paper)** sunucuları için sıfırdan geliştirilmiş tam kapsamlı bir **Tek Blok (OneBlock) Eklentisi**, 15 farklı biyom & aşama sistemi, dahili **Ekonomi Motoru**, tarlaları ve spawner ganimetlerini toplayıp satan **Ada Çiftçisi (Farmer)**, interaktif sohbet butonları ve bu eklentiye özel hazırlanmış **Void Haritası** içermektedir.

---

## 📦 Paket İçeriği

* **`TekBlok-1.0.0.jar`**: Doğrudan sunucunuzun `plugins/` klasörüne atılacak hazır eklenti dosyası.
* **`TekBlokWorld/`**: 0, 65, 0 koordinatında tek bir çimen bloğu bulunan, altında hiçbir blok olmayan 1.16.1 Void Haritası (`level.dat` + `region/r.0.0.mca`).
* **`build.ps1`**: Java kaynak kodlarını `--release 8` parametresiyle Spigot 1.16.1 API üzerinde derleyip test sunucusuna ve konfigürasyonlara otomatik senkronize eden PowerShell derleme betiği.
* **`pom.xml`**: Standart Maven geliştirme dosyası (IntelliJ / Eclipse ile uyumlu).
* **`src/`**: Tüm Java kaynak kodları ve varsayılan konfigürasyonlar (`phases.yml`, `messages.yml`, `config.yml`).

---

## 🚀 Sunucuya Kurulum (3 Kolay Adım)

### Yöntem 1: Doğrudan Ana Dünya Olarak Kullanma (En Çok Tercih Edilen)
1. **`TekBlok-1.0.0.jar`** dosyasını sunucunuzun `plugins` klasörüne kopyalayın.
2. **`TekBlokWorld`** klasörünü sunucu ana dizinine (spigot.jar'ın yanına) kopyalayın.
3. Sunucunuzun `server.properties` dosyasını açıp şu ayarı yapın:
   ```properties
   level-name=TekBlokWorld
   spawn-protection=0
   ```
4. Sunucunuzu başlatın. Oyuncular doğrudan tek bloğun üzerinde doğacaktır!

### Yöntem 2: Mevcut Çok Dünyalı (Multiverse vb.) Sunucuya Kurma
1. `TekBlok-1.0.0.jar` dosyasını `plugins/` klasörüne atın ve sunucuyu başlatın.
2. Eklenti, kendi yerleşik `VoidChunkGenerator` jeneratörü sayesinde `TekBlokWorld` dünyasını otomatik olarak sıfır bloklu void formatında oluşturur.
3. Oyuncular `/tekblok git` veya `/ob go` yazdığında adalarına güvenle ışınlanır.

---

## 🎮 Oyuncu Komutları

| Komut | Kısayol | Açıklama |
|---|---|---|
| `/ada` | `/tekblok`, `/ob` | Etkileşimli ana sandık menüsünü (GUI) açar. |
| `/ada git` | `/tekblok git` | Adanızın tek bloğuna ışınlar (Yoksa yeni ada oluşturur). |
| `/ada bilgi` | `/tekblok bilgi` | Kırılan blok, aşama adı, biyom ve kalan blok sayısını gösterir. |
| `/spawn` | `/lobi`, `/lobby` | **Lobi / Başlangıç** noktasına ışınlar. |
| `/setspawn` | `/spawnset` | *(Yönetici)* Bulunulan konumu sunucu lobisi/spawn noktası yapar. |
| `/koylu olustur <tip>`| `/npc olustur` | *(Yönetici)* Maden Eritici (`eritici`) veya Takasçı (`takasci`) köylü koyar. |
| `/koylu sil` | `/npc sil` | *(Yönetici)* En yakındaki özel köylüyü kaldırır. |
| `/kasa olustur <tip>` | `/crate create` | *(Yönetici)* Baktığınız bloğu efektli ve hologramlı kasaya dönüştürür (`cirak`, `maden`, `savasci`, `efsanevi`). |
| `/kasa sil` | `/crate remove` | *(Yönetici)* Baktığınız kasayı ve hologramlarını kaldırır. |
| `/kasa anahtarver` | `/crate key` | *(Yönetici)* Oyuncuya özel parıltılı kasa anahtarı verir. |
| `/kasa bilgi <tip>` | `/kasa incele` | Kasa içerisindeki ödülleri ve düşme şanslarını önizleme menüsünde açar. |
| `/ciftci` | `/ada ciftci` | **Ada Çiftçisi** menüsünü açar (Satın alınmamışsa önce lisans menüsü açılır). |
| `/market` | `/shop`, `/magaza` | **Oyun İçi Market** menüsünü açar (Tohum, et, yün, maden ve **Kasa Anahtarları**). |
| `/takas <oyuncu>` | `/trade` | Oyuncular arası **buton onaylı** güvenli eşya takası başlatır. |
| `/para` | `/bakiye` | Mevcut bakiyenizi görüntüler (Başlangıç parası 0 TL). |
| `/para gonder <oyuncu> <miktar>` | `/pay` | Başka bir oyuncuya para transfer eder. |
| `/ada davet <oyuncu>` | `/tekblok davet`| Arkadaşınıza **tıklanabilir butonlu** ada daveti gönderir. |
| `/ada kabul` | `/tekblok kabul`| Gelen ada davetini kabul eder (Butonla da tıklanabilir). |
| `/ada reddet` | `/tekblok reddet`| Gelen ada davetini reddeder (Butonla da tıklanabilir). |
| `/ada ziyaret <oyuncu>`| `/tekblok ziyaret`| Başka bir oyuncunun TekBlok adasını ziyarete gider. |
| `/ada top` | `/ada siralama` | En çok blok kıran lider oyuncuların Top 10 sıralamasını listeler. |
| `/ada sil` | `/tekblok sil` | **Onay butonlu** ada silme diyaloğu açar (Onaylanırsa adayı ve blokları siler). |
| `/ada sil onay` | `/tekblok sil onay`| Ada silme işlemini doğrudan onaylayarak siler. |
| `/ada sifirla` | `/tekblok sifirla`| Adanızı ve aşamanızı 1. seviyeye sıfırlar. |
| `/ada yardim` | `/ada help` | Tüm komutları renkli sohbet listesi olarak gösterir. |

---

## 🏛️ Lobi / Spawn Dünyası ve Spawn Noktası (`/spawn`, `/setspawn`)

* **Lobi Dünyası (`spawn`):**
  * Proje içerisindeki `spawn/` harita klasörü sunucu açılışında otomatik olarak yüklenir (`spawn.auto-load-world: true`).
  * Lobi dünyasında yetkisi olmayan oyuncular için **blok kırma/koyma engeli**, **açlık kaybı engeli**, **hasar ve PvP engeli** ve **doğal yaratık doğma engeli** aktiftir.
* **Lobi Boşluk (Void) Koruması:**
  * Lobiden aşağıya boşluğa düşen oyuncular ölmeden ve eşyalarını kaybetmeden anında lobi doğuş noktasına geri ışınlanır.
* **Spawn Noktası Belirleme:**
  * Yönetici olarak dilediğiniz konuma geçip:
    ```text
    /setspawn
    veya
    /spawn ayarla
    ```
    yazdığınızda X, Y, Z, Yaw ve Pitch koordinatları kalıcı olarak `config.yml`'ye kaydedilir.
  * Tüm oyuncular `/spawn` yazdığında ışınlanma sesleri, başlık ve portaldan geçiş efektleriyle bu noktaya gelir.

---

## 🔨 Maden Eritici & Takasçı Köylü Sistemi (`/koylu`)

Lobiye veya adalara yerleştirilebilen, sabit duran ve hasar almayan özel tüccarlar:
1. **Maden Eritici (Demirci Köylü):**
   * Oluşturmak için: `/koylu olustur eritici`
   * Baktığı köylüye sağ tıklayan oyuncuya **Maden Eritici Menüsü** açılır.
   * **Eritme Dönüşümleri:**
     * Demir Cevheri ➔ Demir Külçesi
     * Altın Cevheri ➔ Altın Külçesi
     * Antik Kalıntı ➔ Netherit Parçası
     * Kırıktaş ➔ Düz Taş
     * Kum ➔ Cam
     * Kil Topu ➔ Tuğla
     * Nether Taşı ➔ Nether Tuğlası
   * **Kolay Tıklama:** Sol tık (1 adet), Sağ tık (16 adet), Shift+Sağ tık (64 adet).
   * **🔥 TÜM ÇANTAYI ERİT (Slot 22):** Çantadaki tüm eritilebilir madenleri tek tıkla anında külçeye dönüştürür, fırın alevleri ve örs çekiçleme ses efektleri çıkarır!
2. **Takasçı Köylü (Tüccar):**
   * Oluşturmak için: `/koylu olustur takasci`
   * Madenleri birbirine dönüştürür (16 Kömür ➔ 1 Demir, 8 Demir ➔ 1 Altın, 4 Altın ➔ 1 Elmas, 2 Elmas ➔ 1 Zümrüt, 4 Zümrüt ➔ 1 Netherit Parçası).
3. **Köylü Kaldırma:**
   * Yanına gidip `/koylu sil` yazıldığında en yakındaki özel köylü güvenle kaldırılır.

---

## 🎁 Efektli Kasa (Crate) & CS:GO Çarkıfelek Açılış Sistemi (`/kasa`)

Kasalar lobiye komutla kurulan, 3 boyutlu hologram başlıklı, spiral parçacık efektli ve CS:GO tarzı yatay çarkıfelek açılış animasyonuna sahip özel sandıklardır:
1. **4 Farklı Kasa Türü:**
   * 🟢 **Çırak Kasası (`cirak`):** Yeşil mutlu köylü efektli, kömür, demir, odun, altın elma ve 250 TL başlangıç para ödülleri.
   * 🔵 **Maden Kasası (`maden`):** Büyü masası rün efektli, demir/altın/elmas blokları, zümrüt, netherit parçası ve 1.000 TL bakiye.
   * 🔴 **Savaşçı Kasası (`savasci`):** Alev ve lav parçacıklı, Keskinlik V kılıç, Güç V yay, Ölümsüzlük Totemi, Koruma IV zırh ve 2.500 TL bakiye.
   * 🟡 **Efsanevi Kasa (`efsanevi`):** Portal ve ejderha nefesi parçacıklı, 2x Netherit Külçesi, 2x Büyülü Altın Elma (Notch), Fener (Beacon), Elytra, Netherit Kılıç ve 10.000 TL dev bakiye ödülü!
2. **Kasa Oluşturma & Silme:**
   * Baktığınız bir bloğu kasaya dönüştürmek için:
     ```text
     /kasa olustur cirak
     /kasa olustur maden
     /kasa olustur savasci
     /kasa olustur efsanevi
     ```
   * Kasayı kaldırmak için kasaya bakıp `/kasa sil` yazmanız yeterlidir.
3. **Görsel Efektler:**
   * **Hologram:** Kasanın üstünde 3 satır süzülen yazı (`★ KASA ADI ★`, `Açmak için Sağ Tıkla`, `Gereken Anahtar`).
   * **Spiral Parçacıklar:** Kasanın etrafında 7/24 dönen çift sarmal büyü ve alev parçacıkları.
   * **Sol Tık (Önizleme):** Kasaya sol tıklayan oyuncu, kasanın içindeki tüm ödülleri ve kazanma yüzdelerini inceleyebilir.
4. **CS:GO Tarzı Çarkıfelek Açılış Animasyonu:**
   * Kasaya uygun anahtarla sağ tıklandığında 27 slotlu açılış arayüzü açılır.
   * Orta şeritte ödüller sağdan sola hızla kaymaya başlar ve nota blokları tıkırdayarak kademeli yavaşlar.
   * Kazanan ödül 13. slottaki ibrede durur; havai fişekler patlar, tebrik sesleri çalar ve ödül oyuncunun envanterine verilir.
   * Nadir ödüllerde tüm sunucuya tebrik duyurusu geçer!
5. **Kasa Anahtarlarının Markette Satılması:**
   * `/market` menüsünde **Kasa Anahtarları** kategorisi mevcuttur:
     * Çırak Kasa Anahtarı: 150 ₺
     * Maden Kasa Anahtarı: 600 ₺
     * Savaşçı Kasa Anahtarı: 1.500 ₺
     * Efsanevi Kasa Anahtarı: 6.000 ₺
   * Yöneticiler dilerse `/kasa anahtarver <oyuncu> <kasa_tipi> [adet]` ile de oyunculara anahtar verebilir.

---

## 🌾 Ada Çiftçisi (Farmer / Canlı Ganimetleri, Yere Düşenleri Toplama & Gelişmiş Tıklamalar)

Çiftçi, adanızdaki tarım, spawner ve yerdeki eşya süreçlerini tamamen otomatikleştirir:
1. **Çiftçi Lisansı ve Satın Alma (`farmer.require-purchase: true`):**
   * Adada henüz çiftçi satın alınmamışsa `/ciftci` yazıldığında veya köylüye tıklandığında **Çiftçi Lisans Satın Alma Arayüzü** açılır.
   * Oyuncu belirlenen lisans bedelini (`farmer.buy-price: 500.0 TL`) ödeyerek çiftçiyi aktif eder.
2. **Otomatik Tarla & Her Türlü Canlı Ganimetleri:**
   * Buğday, Havuç, Patates, Pancar, Nether Siğili, Kakao, Şeker Kamışı, Kaktüs, Balkabağı, Karpuz, Bambu, Tatlı Meyveler, Kelp vb. otomatik toplanır (olgunlaşan tarlalar otomatik geri ekilir).
   * Adada ölen **her türlü canlıdan** (Zombi, İskelet, Örümcek, Creeper, Enderman, Blaze, Balçık, Hayvanlar vb.) düşen tüm ganimetler eksiksiz depoya aktarılır.
3. **Yere Atılan Eşyaları Toplama (Kullanıcı Ayarlı):**
   * Çiftçi menüsünde bulunan **Slot 51 (Huni butonu)** ile açılıp kapatılabilir.
   * Açık olduğunda birisi çiftçi ürünlerini yere atarsa veya yere düşerse, çiftçi bunları otomatik toplayıp deposuna çeker.
4. **Çiftçi Menüsü ve Tıklama Seçenekleri (`/ciftci`):**
   * **`Sağ Tık`**: Sadece **1 Adet (Tekli)** satar.
   * **`Shift + Sağ Tık`**: O ürünün **Hepsini** satar.
   * **`Sol Tık`**: Depodan **1 Adet (Tekli)** envanterinize çeker.
   * **`Q Tuşu (Drop)`**: Depodan **64 Adet (Stack)** envanterinize çeker.
   * **`Shift + Sol Tık`**: **Envanteriniz Dolana Kadar** depodan ürün çeker.
   * **`[💰 TÜM ÜRÜNLERİ SAT]`**: Depodaki her şeyi tek tuşla satar.
   * **`[🌾 ÇİFTÇİYİ BURAYA GETİR]`**: Çiftçi köylüyü bulunduğunuz konuma ışınlar.
   * **`[⚙ YERE DÜŞENLERİ TOPLA]` (Slot 51)**: Yerden otomatik toplamayı açar/kapatır.

---

## 🤝 Güvenli ve Buton Onaylı Takas Sistemi (`/takas`)

Oyuncuların sohbet ve menü üzerinden tamamen butonlarla güvenli takas yapmasını sağlar:
1. **İnteraktif Sohbet Butonları:**
   * `/takas <oyuncu>` yazıldığında, karşı oyuncuya tıklanabilir `[ ✔ TAKASI KABUL ET ]` ve `[ ✖ REDDET ]` butonları gönderilir.
2. **54 Slotlu Çift Taraflı Takas Menüsü:**
   * İki oyuncu aynı anda menüyü açar (Sol taraf Oyuncu 1, Sağ taraf Oyuncu 2).
   * Oyuncular sadece kendi alanlarına eşya koyabilir, karşı tarafın eşyalarını alamaz.
   * Eşya eklendiğinde veya değiştirildiğinde hazır durumu otomatik sıfırlanarak dolandırıcılık (scam) engellenir.
3. **Butonla Onay:**
   * Oyuncular kendi onay butonlarına (`[ ONAYLA ]` -> `[ ONAYLANDI ]`) tıklar.
   * İki taraf da onayladığında 3 saniyelik geri sayım başlar ve eşyalar güvenle takas edilir.
   * Menü kapatılırsa eşyalar anında sahiplerine iade edilir.

---

## 🛒 Oyun İçi Market Sistemi (`/market`)

Tüm eşyaların sadece satın alınabildiği (`buy-only`), kategorilere ayrılmış ve `market.yml` ile %100 özelleştirilebilir pazar sistemi:
1. **5 Ana Kategori:**
   * 🌾 **Tarım & Tohum Pazarı:** Tohumlar, fidanlar, şeker kamışı, bambu, kaktüs vb.
   * 🥩 **Et & Yiyecek Pazarı:** Pişmiş biftek, domuz pirzolası, balıklar, ekmek, altın elma vb.
   * 🧶 **Yün & Renkli Blok Pazarı:** 16 farklı renk yün bloku.
   * 💎 **Maden & Cevher Pazarı:** Kömür, demir, altın, elmas, zümrüt, netherit ve bloklar.
   * 💀 **Canavar & Avcı Pazarı:** Çürük et, kemik, ip, barut, ender incisi, blaze çubuğu vb.
2. **Satın Alma Kolaylığı:**
   * **Sol Tık:** 1 Adet Satın Al
   * **Sağ Tık:** 16 Adet Satın Al
   * **Shift + Sağ Tık:** 64 Adet Satın Al
3. **Konfigürasyon:** `plugins/TekBlok/market.yml` dosyası açılarak her ürünün ismi ve birim fiyatı değiştirilebilir. `/market reload` ile sunucuyu yeniden başlatmadan anında güncellenir.

---

## 💰 Dahili Ekonomi Sistemi (`/para`)

* Başlangıç bakiyesi: **`0.0 ₺`** (Sıfırdan emekle kasılma).
* Komutlar: `/para`, `/para gonder <oyuncu> <miktar>`, yöneticiler için `/para ver`, `/para al`, `/para ayarla`.
* Fiyatlandırma:
  * Çiftçi satış fiyatları: `plugins/TekBlok/config.yml`
  * Market alış fiyatları: `plugins/TekBlok/market.yml`

---

## 🛡️ Ada Silme ve Yeniden Oluşturma Güvenliği

* **Eski Blokların Geri Gelmesi Düzeltildi:**
  * Eski sistemde adalar `islandsByOwner.size()` üzerinden hesaplandığı için ada silinip tekrar açıldığında aynı koordinat denk gelebiliyor ve eski yapılar dünyada kalıyordu.
  * Artık **`next-island-index`** sayacı sayesinde her yeni ada boşluğun tamamen el değmemiş, taze bir koordinatında sıfırdan kurulur.
  * Ada silindiğinde (`/tekblok sil onay`), adanın 60 blok yarıçapındaki tüm fiziksel blokları, sandıkları ve canavarları otomatik olarak temizlenir.

---

## 🌍 15 Aşamalı Biyom & Seviye Sistemi (Dinamik Biyom Değişimi)

| Aşama | Seviye Adı | Biyom | Blok Sayısı | Öne Çıkan Bloklar | Beliren Canlılar |
|:---:|---|---|:---:|---|---|
| **1** | **Ova ve Çimenlik** | `PLAINS` | 0 - 250 | Çimen, Toprak, Meşe Odunu, Kil, Çakıl | İnek, Koyun, Tavuk, Domuz |
| **2** | **Yeraltı Mağarası** | `MOUNTAINS` | 251 - 650 | Taş, Kırıktaş, Kömür Cevheri, Demir Cevheri, Granit | Zombi, İskelet, Örümcek, Yarasa |
| **3** | **Karlı Tayga** | `SNOWY_TAIGA` | 651 - 1150 | Ladin Odunu, Kar Bloğu, Paketlenmiş Buz, Podzol | Kutup Kurdu, Kar Tilkisi, Tavşan |
| **4** | **Okyanus & Bataklık**| `SWAMP` | 1151 - 1750 | Prizmarin, Sünger, Deniz Feneri, Balçık, Nilüfer | Boğulmuş (Drowned), Balçık, Deniz Kaplumbağası |
| **5** | **Tropikal Orman** | `JUNGLE` | 1751 - 2450 | Orman Odunu, Karpuz, Bambu, Sarmaşık | Renkli Papağan, Oselo, Panda |
| **6** | **Kızıl Çöl** | `DESERT` | 2451 - 3250 | Kırmızı Kum, Kum, Kumtaşı, Kaktüs, Altın Cevheri | Çöl Zombisi (Husk), Zehirli Mağara Örümceği |
| **7** | **Badlands & Kanyon** | `BADLANDS` | 3251 - 4150 | Pişmiş Toprak (Terracotta), Sarı/Turuncu Terakota, Kırmızı Kum | Kanyon Zombisi, Yabani At, Örümcek |
| **8** | **Mercan Derinlikleri**| `WARM_OCEAN` | 4151 - 5150 | Tüp Mercanı, Beyin Mercanı, Ateş Mercanı, Deniz Hıyarı | Yunus Balığı, Tropikal Balık, Kirpi Balığı |
| **9** | **Nether Cehennemi** | `NETHER_WASTES` | 5151 - 6250 | Nether Taşı, Kuvars, Ruh Kumu, Bazalt, Antik Kalıntı | Piglin, Zombi Piglin, Blaze, Magma Küpü |
| **10**| **Çarpık Mantar Ormanı**| `WARPED_FOREST` | 6251 - 7450 | Çarpık Kütük, Çarpık Nylium, Çarpık Siğil Bloğu, Shroomlight | Mavi Gözlü Enderman, Lav Koşucusu (Strider) |
| **11**| **Kızıl Fungi Diyarı** | `CRIMSON_FOREST` | 7451 - 8750 | Kızıl Kütük, Kızıl Nylium, Kızıl Mantar, Ağlayan Sarmaşık | Vahşi Hoglin, Piglin Muhafızı, Kızıl Piglin |
| **12**| **Ruh Vadisi ve Bazalt**| `SOUL_SAND_VALLEY` | 8751 - 10150 | Ruh Toprağı, Ruh Kumu, Bazalt, Cilalı Bazalt, Kemik Bloğu | Ruh İskeleti, Ağlayan Ghast, Wither Savaşçısı |
| **13**| **Mantar Adası** | `MUSHROOM_FIELDS`| 10151 - 11650 | Miselyum, Kırmızı/Kahverengi Mantar Blokları, Koyu Meşe | Mantar İneği (Mooshroom), Mantar Yarasası |
| **14**| **End Boyutu** | `THE_END` | 11651 - 13300 | End Taşı, Purpur Bloğu, Obsidyen, Ağlayan Obsidyen, End Çubuğu | Enderman, Shulker, Endermite |
| **15**| **Sonsuzluk & Nihai Hazine** | `PLAINS` | 13301+ (Sonsuz) | Elmas Cevheri, Zümrüt, Antik Kalıntı, Fener, Netherite Bloğu | Wither İskeleti, Demir Golem, Ravager, Evoker |
