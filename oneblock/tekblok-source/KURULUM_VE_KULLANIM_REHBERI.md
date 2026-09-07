# TekBlok (OneBlock Skyblock) 1.16.1 Kurulum ve Kullanım Rehberi

Bu paket, **Minecraft 1.16.1 (Spigot/Paper)** sunucuları için sıfırdan geliştirilmiş tam kapsamlı bir **Tek Blok (OneBlock) Eklentisi**, 15 farklı biyom & aşama sistemi, **Zorlaştırılmış Ekonomi Motoru**, tarlaları ve spawner yaratıklarını otomatik öldürüp toplayan **Ada Çiftçisi (Farmer)**, **54 Slotluk Hızlı Satış Sandığı (`/sat`)**, 1:1 kömür tüketen **Maden Eritici**, havada süzülen **5 Farklı Kasa (Özel Spawner Kasası dahil)**, lobi karşılama hologramı, ada kaptan NPC'si ve 1.16.1 için özel dönüştürülmüş **Lobi Haritası** içermektedir.

---

## 📦 Paket İçeriği

* **`TekBlok-1.0.0.jar`**: Doğrudan sunucunuzun `plugins/` klasörüne atılacak hazır eklenti dosyası.
* **`TekBlokWorld/`**: 0, 65, 0 koordinatında tek bir çimen bloğu bulunan, altında hiçbir blok olmayan 1.16.1 Void Haritası (`level.dat` + `region/`).
* **`spawn/`**: Paper 1.16.1 uyumlu (DataVersion 2567) lobi haritası.
* **`build.ps1`**: Java kaynak kodlarını derleyen ve `oneblock/plugins/` dizinine otomatik senkronize eden PowerShell derleme betiği.
* **`pom.xml`**: Standart Maven geliştirme dosyası.
* **`src/`**: Tüm Java kaynak kodları ve varsayılan konfigürasyonlar (`phases.yml`, `crates.yml`, `market.yml`, `config.yml`, `messages.yml`).

---

## 🚀 Sunucuya Kurulum

1. **`TekBlok-1.0.0.jar`** dosyasını sunucunuzun `plugins` klasörüne kopyalayın.
2. **`TekBlokWorld`** ve **`spawn`** klasörlerini sunucu ana dizinine kopyalayın.
3. Sunucunuzun `server.properties` dosyasında `level-name=spawn` ve `spawn-protection=0` yapın.
4. Sunucunuzu başlatın. Oyuncular lobide doğacak, `/ada` veya Kaptan NPC ile adalarına geçebilecektir!

---

## 🎮 Tüm Komutlar Listesi

| Komut | Kısayol / Takma Adlar | Açıklama |
|---|---|---|
| `/ada` | `/tekblok`, `/ob` | Etkileşimli ana ada menüsünü (GUI) açar. |
| `/ada git` | `/tekblok git` | Adanızın tek bloğuna ışınlar (Yoksa yeni ada oluşturur). |
| `/ada bilgi` | `/tekblok bilgi` | Kırılan blok, aşama adı, biyom ve kalan blok sayısını gösterir. |
| `/sat` | `/sell`, `/hizlisat` | **54 Slotluk Hızlı Satış Sandığı** açar. İçine koyulan her şey satılır. |
| `/spawn` | `/lobi`, `/lobby` | **Lobi / Başlangıç** noktasına ışınlar. |
| `/setspawn` | `/spawn ayarla` | *(Yönetici)* Bulunulan konumu lobi doğuş noktası yapar. |
| `/spawnholo yenile` | - | *(Yönetici)* Lobi karşılama hologramını yeniler. |
| `/koylu olustur <tip>`| `/npc olustur` | *(Yönetici)* Özel NPC kurar: `eritici` (Maden), `takasci` (Takas), `ada` (Kaptan). |
| `/koylu sil` | `/npc sil` | *(Yönetici)* En yakındaki özel köylüyü/NPC'yi kaldırır. |
| `/koylu liste` | `/npc liste` | *(Yönetici)* Aktif özel köylülerin konumlarını listeler. |
| `/kasa olustur <tip>` | `/crate create` | *(Yönetici)* Baktığınız bloğun 1 blok üstüne havada duran kasa kurar (`cirak`, `maden`, `savasci`, `efsanevi`, `spawner`). |
| `/kasa sil` | `/crate remove` | *(Yönetici)* Baktığınız kasayı, efektlerini ve hologramlarını kaldırır. |
| `/kasa anahtarver` | `/crate key` | *(Yönetici)* Oyuncuya özel parıltılı kasa anahtarı verir. |
| `/kasa bilgi <tip>` | `/kasa incele` | Kasa içerisindeki ödülleri ve düşme şanslarını önizleme menüsünde açar. |
| `/ciftci` | `/ada ciftci` | **Ada Çiftçisi** menüsünü açar (5.000 ₺ lisans bedeli). |
| `/market` | `/shop`, `/magaza` | **Oyun İçi Zor Pazar** menüsünü açar (Bloklar, anahtarlar, madenler vb.). |
| `/takas <oyuncu>` | `/trade` | Oyuncular arası buton onaylı güvenli takas başlatır. |
| `/para` | `/bakiye` | Mevcut bakiyenizi görüntüler (Başlangıç 0.00 ₺). |
| `/para gonder <oyuncu> <miktar>` | `/pay` | Başka bir oyuncuya para transfer eder. |
| `/ada davet <oyuncu>` | `/tekblok davet`| Arkadaşınıza ada daveti gönderir. |
| `/ada kabul` / `/ada reddet` | - | Gelen ada davetini kabul/reddedir. |
| `/ada ziyaret <oyuncu>`| - | Başka bir oyuncunun adasını ziyarete gider. |
| `/ada top` | `/ada siralama` | En çok blok kıran liderlerin Top 10 sıralaması. |
| `/ada sil` / `/ada sil onay`| - | Adayı ve adadaki tüm blokları güvenle siler. |
| `/ada sifirla` | - | Adayı ve aşamayı 1. seviyeye sıfırlar. |
| `/ada yardim` | `/ada help` | Renkli yardım kılavuzu. |

---

## 🏛️ Lobi, Karşılama Hologramı ve Giriş / Boşluk (Void) Kuralları

1. **Sunucuya Giriş Yönlendirmesi:**
   * Oyuncu sunucuya her bağlandığında kontrol edilir. Eğer oyuncu bir adada (`TekBlokWorld`) değilse, otomatik olarak **Lobi Doğuş Noktasına (`spawn`)** ışınlanır.
2. **Adadan Boşluğa Düşüş ve Eşya Kaybı:**
   * **`TekBlokWorld` (Ada Dünyası):** Oyuncu adadan boşluğa (Y < 0) düştüğünde anında ölür ve eşyaları boşluğa saçılır. Yeniden doğduğunda (`Respawn`) doğrudan kendi **adasındaki tek bloğun üzerinde doğar**.
   * **`spawn` (Lobi Dünyası):** Lobiden aşağı düşen oyuncular can kaybetmeden ve eşyalarını kaybetmeden anında spawn merkezine geri ışınlanır.
   * **Kalıcı Spawn Koruması:** `/setspawn` ile belirlenen konum hem `config.yml` hem de `data/spawn.yml` dosyasına kalıcı olarak kaydedilir ve Vanilla dünya spawn'ı ile senkronize edilir. Sunucu açılıp kapansa dahi spawn konumu asla silinmez.
3. **Lobi Karşılama Hologramı:**
   * Lobi doğuş noktasının tam **10 blok ilerisine**, göz hizasına (`Y + 0.2` taban) süzülen 3 boyutlu karşılama hologramı konuşlandırılmıştır:
     * `&a&l★ TEKBLOK SKYBLOCK ★`
     * `&fSunucumuza Hoş Geldiniz!`
     * `&a/ada &fkomutu ile adana gidebilirsin.`
     * `&a/sat &fkomutu ile eşyalarını hızlıca sat!`
     * `&a/market &fkomutu ile alışveriş yapabilirsin.`
     * `&aKaptan &fköylüsüne sağ tıklayarak da adana gidebilirsin.`
   * Önemli komut ve başlıklar **Yeşil (`&a`)**, bilgilendirme metinleri **Beyaz (`&f`)** renktedir.
   * Sunucu kapandığında veya `/spawnholo yenile` yazıldığında güvenle tazelenir.

---

## ⛵ Adaya Gitmek İçin Kaptan NPC'si (`/koylu olustur ada`)

* Yöneticiler `/koylu olustur ada` komutu ile lobiye veya istedikleri konuma bir **Balıkçı Kaptan Köylüsü** ekleyebilir:
  * İsim: `§b§lKaptan | §3Ada Rehberi`
  * Sağ tıklayan oyuncu: `[TekBlok] Kaptan sizi adanıza götürüyor...` mesajı, portal sesleri ve parçacıklarıyla doğrudan kendi adasına güvenle ışınlanır!

---

## 🔨 Kömür Yakıtlı Maden Eritici (`1 Demir = 1 Kömür`)

* Maden eritici köylüde artık **1:1 kömür yakıtı** zorunludur:
  * **1 Demir/Altın/Netherit/Cam/Tuğla/Taş** eritmek için envanterinizde **1 Kömür veya Odun Kömürü** bulunmalıdır.
  * Eğer kömürünüz yoksa işlem gerçekleşmez ve `§cYeterli kömürün yok! 1 maden eritmek için 1 kömür gerekir.` uyarısı verilir.
  * **🔥 TÜM ÇANTAYI ERİT (Slot 22):** Çantadaki madenleri mevcut kömür miktarı kadar orantılı olarak tek tıkla eritir ve harcanan kömürleri envanterden düşer.

---

## 🌌 14. Aşamada Otomatik Aktif End Portalı Kurulumu

* Adanızda tek bloğu kırarak **14. Seviye (End Boyutu - `THE_END`)** aşamasına ulaştığınızda:
  * Tek bloğun **4 blok altına** (`tekblok.Y - 4`) eksiksiz ve otomatik olarak **Aktif Bir End Portalı** inşa edilir!
  * **Portal Yapısı:**
    * 12 adet aktif `END_PORTAL_FRAME` (hepsi merkeze bakacak şekilde yönlendirilmiş ve `Ender Gözü` takılmış).
    * Ortada 3x3 aktif `END_PORTAL` geçiş bloğu.
    * Altında 5x5 sağlam `END_STONE_BRICKS` (End Taşı Tuğlası) emniyet tabanı.
  * Oyuncuya sesli ve görsel kutlama mesajı iletilir. Oyuncular End boyutuna bu portaldan dalarak girebilir!

---

## 📦 54 Slotluk Hızlı Satış Sandığı (`/sat`)

* Çiftçi satın alınana kadar (veya istendiği her an) oyuncuların eşyalarını hızlıca paraya dönüştürmesini sağlar.
* `/sat` yazıldığında 54 slotluk dev bir sandık menüsü açılır.
* Sandığa **akla gelen her türlü eşya** konulabilir:
  * **Madenler (Düşük/Zor Fiyat):** Kömür (0.50 ₺), Demir (1.50 ₺), Altın (2.50 ₺), Elmas (6.00 ₺), Zümrüt (4.00 ₺), Netherit (15.00 ₺).
  * **Diğer Tüm Eşyalar:** Değerine göre **0.10 ₺ ile 0.40 ₺** arasında para verir.
  * **Varsayılan Taban:** Listelenmeyen herhangi bir blok veya eşya da en az **0.15 ₺** taban fiyattan satılır. **Hiçbir eşya çöpe gitmez!**
* Sandık kapatıldığı anda içindeki tüm eşyalar hesaplanır, oyuncunun bakiyesine eklenir ve detaylı kazanç bildirimi gönderilir.

---

## 🎁 Havada Duran Kasalar & Özel Spawner Kasası (`/kasa`)

1. **Havada Süzülen Kasalar & Abartılı Parçacık Gösterisi:**
   * Kasalar `/kasa olustur <tip>` ile kurulduğunda bloğun 1 blok yukarısında **havada asılı** durur.
   * **Görsel Efektler:**
     * **Double Helix:** Kasanın etrafında yukarı doğru süzülen çift sarmal DNA helezon parçacıkları.
     * **Halo Ring:** Kasanın çevresinde yatay dönen ışık halkası.
     * **Zemin Girdabı:** Kasanın altında dönen vortex/girdap parçacıkları.
     * **Büyü Patlamaları:** Büyü masası rünleri ve alev parıltıları.
2. **5 Kasa Türü:**
   * 🟢 **Çırak Kasası (`cirak`):** Odun, demir, kömür, altın elma, 100 ₺. (Anahtar: 500 ₺)
   * 🔵 **Maden Kasası (`maden`):** Maden blokları, elmas, zümrüt, netherit parçası, 400 ₺. (Anahtar: 2.000 ₺)
   * 🔴 **Savaşçı Kasası (`savasci`):** P4 Netherit zırh, Keskinlik V kılıç, Totem, 1.000 ₺. (Anahtar: 5.000 ₺)
   * 🟡 **Efsanevi Kasa (`efsanevi`):** 2x Netherit Külçesi, Notch Elması, Elytra, Fener, 4.000 ₺. (Anahtar: 18.000 ₺)
   * 🔮 **Spawner Kasası (`spawner`):**
     * **%1 Şansla Ultra Nadir Demir Golem (Iron Golem) Spawnerı!**
     * Blaze, Wither İskeleti, Enderman, Creeper, İskelet, Zombi, Mağara Örümceği, Balçık, İnek, Domuz, Tavuk, Koyun spawnerları. (Anahtar: 25.000 ₺)
3. **Spawner ve İpeksi Dokunuş:**
   * Yerleştirilen spawnerlar ilgili yaratığın gerçek mob spawnerına dönüşür.
   * **İpeksi Dokunuş (Silk Touch)** kazma ile kırıldığında spawner eşyası kırılmadan envantere düşer.

---

## 🌾 Ada Çiftçisi ve Spawner Otomasyonu (`/ciftci`)

* **Lisans Bedeli:** Zorlaştırılmış ekonomi kapsamında çiftçi lisans bedeli **`5.000.00 ₺`** olarak ayarlanmıştır.
* **Spawner Yaratıklarını Otomatik Kesme ve Toplama:**
  * Çiftçisi aktif olan adada, yerleştirilen spawnerlardan çıkan tüm canlılar (Demir Golem, Blaze, İskelet, Creeper vb.) **çiftçi tarafından otomatik olarak öldürülür**.
  * Düşen demirler, güller, blaze çubukları, kemikler, barutlar vb. doğrudan çiftçinin deposuna aktarılır.
* **Tüm Canlı & Tarla Toplayıcı:** Tarlalardaki olgunlaşmış ekinleri toplar ve yeniden eker.

---

## 🌍 Biyom Hedefleri (%15 Artırılmış) ve Biyoma Özel Canlılar

Her aşamanın blok hedefi dengeli bir oyun süresi için **%15 artırılmıştır**:

| Aşama | Seviye Adı | Biyom | Blok Aralığı | Doğabilecek Özel Canlılar |
|:---:|---|---|:---:|---|
| **1** | **Ova ve Çimenlik** | `PLAINS` | 0 - 290 | İnek, Koyun, Tavuk, Domuz, At |
| **2** | **Yeraltı Mağarası** | `MOUNTAINS` | 291 - 750 | Zombi, İskelet, Örümcek, Yarasa |
| **3** | **Karlı Tayga** | `SNOWY_TAIGA` | 751 - 1.320 | Kutup Ayısı, Kar Tilkisi, Kurt, Tavşan |
| **4** | **Okyanus & Bataklık**| `SWAMP` | 1.321 - 2.010 | Boğulmuş (Drowned), Balçık, Deniz Kaplumbağası, Cadı |
| **5** | **Tropikal Orman** | `JUNGLE` | 2.011 - 2.820 | Papağan, Oselo, Panda, Kedi |
| **6** | **Kızıl Çöl** | `DESERT` | 2.821 - 3.740 | Çöl Zombisi (Husk), Zehirli Mağara Örümceği, Tavşan |
| **7** | **Badlands & Kanyon** | `BADLANDS` | 3.741 - 4.770 | Kanyon Zombisi, Yabani At, İskelet, Örümcek |
| **8** | **Mercan Derinlikleri**| `WARM_OCEAN` | 4.771 - 5.920 | Yunus, Tropikal Balık, Kirpi Balığı, Boğulmuş |
| **9** | **Nether Cehennemi** | `NETHER_WASTES` | 5.921 - 7.190 | Piglin, Zombileşmiş Piglin, Blaze, Magma Küpü |
| **10**| **Çarpık Mantar Ormanı**| `WARPED_FOREST` | 7.191 - 8.570 | Mavi Gözlü Enderman, Lav Koşucusu (Strider) |
| **11**| **Kızıl Fungi Diyarı** | `CRIMSON_FOREST` | 8.571 - 10.060 | Vahşi Hoglin, Piglin Muhafızı, Kızıl Piglin |
| **12**| **Ruh Vadisi ve Bazalt**| `SOUL_SAND_VALLEY` | 10.061 - 11.670 | Ruh İskeleti, Ghast, Wither İskeleti |
| **13**| **Mantar Adası** | `MUSHROOM_FIELDS`| 11.671 - 13.400 | Mantar İneği (Mooshroom), Mantar Yarasası |
| **14**| **End Boyutu** *(Portal)*| `THE_END` | 13.401 - 15.300 | Enderman, Shulker, Endermite (4 blok alta **End Portalı** kurulur) |
| **15**| **Sonsuzluk & Hazine** | `PLAINS` | 15.301+ (Sonsuz) | Demir Golem, Wither İskeleti, Evoker, Ravager |

---

## 🛒 Zorlaştırılmış Market Fiyatları (`/market`)

* `/market` menüsüne tüm yapı taşlarını içeren **Bloklar Pazarı (`bloklar`)** ve **Spawner Kasa Anahtarı** eklenmiştir.
* Alış fiyatları zorlu emek sunucusuna uygun şekilde yüksek tutulmuş, haksız zenginleşme önlenmiştir.
* Tüm fiyatlar `plugins/TekBlok/market.yml` üzerinden düzenlenebilir ve `/market reload` ile anında yenilenebilir.
