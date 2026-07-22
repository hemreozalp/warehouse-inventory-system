# Warehouse Inventory Management System - Yapılacaklar

Bu dosya, Warehouse Inventory Management System projesini portfolyo seviyesinde tamamlamak için sprint bazlı yol haritasıdır.

## Proje Amacı

Birden fazla depodaki ürün stoklarını güvenli, tutarlı ve izlenebilir şekilde yönetmek.

Sistemin temel hedefleri:

- Stok giriş ve çıkışı yapmak
- Depolar arası transferi yönetmek
- Negatif stok oluşumunu engellemek
- Stok hareket geçmişini tutmak
- Kritik stok seviyelerini takip etmek
- Eş zamanlı işlemlerde veri tutarlılığını korumak
- Arama, filtreleme ve sayfalama sağlamak
- Yetkilendirme uygulamak
- Raporlama ve audit trail oluşturmak

## Önerilen Teknoloji Seti

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Bean Validation
- JUnit 5
- Mockito
- Testcontainers
- Docker
- Docker Compose
- Swagger / OpenAPI

---

## Sprint 1 - Foundation

### Amaç

Projeyi ayağa kaldırmak ve temel altyapıyı kurmak.

### Yapılacaklar

- Spring Boot projesini oluştur
- PostgreSQL bağlantısını yapılandır
- Docker Compose dosyasını hazırla
- Flyway migration yapısını kur
- Katmanlı package yapısını oluştur
- Global exception handler ekle
- Temel validation altyapısını kur
- Swagger / OpenAPI yapılandırmasını yap
- Ortak API error response formatı belirle

### Önemli Business Kuralları

- Uygulama tüm ortamlarda aynı temel yapı ile çalışmalı
- Hatalar standart bir response formatı ile dönmeli
- Veritabanı değişiklikleri migration ile yönetilmeli

### Sprint Çıktısı

- Proje çalışır durumda olur
- PostgreSQL ve Flyway entegrasyonu tamamlanır
- Swagger üzerinden API dokümantasyonu açılır
- Temel proje iskeleti oluşur

---

## Sprint 2 - Authentication & Authorization

### Amaç

Kullanıcı girişi ve rol bazlı yetkilendirme eklemek.

### Yapılacaklar

- Kullanıcı kayıt ve giriş akışını oluştur
- JWT tabanlı kimlik doğrulama ekle
- BCrypt ile şifreleme uygula
- Role-based authorization kur
- `@PreAuthorize` kullanımı için method security aç
- Custom user details yapısını oluştur
- Authentication filter ekle

### Önemli Business Kuralları

- Yetkisiz kullanıcı hassas endpointlere erişmemeli
- Şifreler düz metin olarak saklanmamalı
- Rol bazlı erişim proje boyunca tutarlı uygulanmalı

### Sprint Çıktısı

- Kullanıcı giriş yapabilir
- JWT alıp isteklerde kullanabilir
- Endpointler role göre korunur

---

## Sprint 3 - Product Catalog

### Amaç

Ürün, kategori ve tedarikçi yönetimini oluşturmak.

### Yapılacaklar

- Category CRUD oluştur
- Supplier CRUD oluştur
- Product CRUD oluştur
- DTO ve MapStruct yapılarını kur
- Sayfalama ve sıralama ekle
- Filtreleme için Specification yapısını hazırla
- Pasif ürün yönetimini tasarla

### Önemli Business Kuralları

- `sku` benzersiz olmalı
- Aynı kategori adı tekrar eklenmemeli
- Pasif ürünlerle stok işlemi yapılamamalı
- Minimum stok seviyesi negatif olamamalı

### Sprint Çıktısı

- Ürün kataloğu yönetilebilir hale gelir
- Kategori ve tedarikçi CRUD işlemleri çalışır
- Ürünler filtrelenebilir ve sayfalanabilir olur

---

## Sprint 4 - Warehouse & Stock Model

### Amaç

Depo yapısını ve ürünlerin depo bazlı stok modelini kurmak.

### Yapılacaklar

- Warehouse entity ve CRUD endpointlerini oluştur
- Stock entity tasarımını yap
- Ürün-depo için tekil stok kaydı kuralını uygula
- `@Version` ile optimistic locking desteğini ekle
- Stok listeleme endpointlerini oluştur
- İlişkiler ve index/constraint yapılarını düzenle

### Önemli Business Kuralları

- Bir ürün-depo çifti için yalnızca bir stok satırı olmalı
- Stok miktarı negatif olamaz
- Pasif depo üzerinde işlem yapılamaz
- Pasif ürün üzerinde stok tutulmamalı

### Sprint Çıktısı

- Depolar yönetilebilir olur
- Her ürünün depo bazlı güncel stoğu görülebilir
- Stok modeli iş kurallarına uygun hale gelir

---

## Sprint 5 - Stock In / Out

### Amaç

Stok giriş ve çıkış işlemlerini güvenli şekilde yönetmek.

### Yapılacaklar

- Stock movement yapısını oluştur
- Stock in endpointi ekle
- Stock out endpointi ekle
- Adjustment işlemi için temel destek ekle
- İşlem öncesi ve sonrası stok değerlerini kaydet
- Audit amaçlı hareket geçmişi oluştur

### Önemli Business Kuralları

- Quantity sıfırdan büyük olmalı
- Stok çıkışı mevcut miktardan fazla olamaz
- Her başarılı işlem bir hareket kaydı oluşturmalı
- İşlem başarısız olursa stok ve hareket kaydı birlikte geri alınmalı

### Sprint Çıktısı

- Stok artırma ve azaltma işlemleri çalışır
- Negatif stok oluşmaz
- Her değişiklik hareket geçmişine yazılır

---

## Sprint 6 - Warehouse Transfer

### Amaç

Bir depodan başka bir depoya atomik stok transferi yapmak.

### Yapılacaklar

- Transfer entity tasarla
- Kaynak depo ve hedef depo kontrolünü ekle
- Transfer endpointi oluştur
- Kaynak stok azaltma ve hedef stok artırmayı tek transaction içinde yönet
- Transfer için iki ayrı movement kaydı oluştur
- Transfer status yapısını ekle

### Önemli Business Kuralları

- Kaynak ve hedef depo aynı olamaz
- Kaynak stok yeterli olmalı
- İşlemin tamamı tek transaction içinde çalışmalı
- Hata durumunda tüm işlem rollback olmalı

### Sprint Çıktısı

- Depolar arası transfer yapılabilir
- Transfer süreci tutarlı kalır
- Yarım kalmış stok hareketi oluşmaz

---

## Sprint 7 - Concurrency Control

### Amaç

Aynı stok üzerinde eş zamanlı işlemlerde veri tutarlılığını korumak.

### Yapılacaklar

- Optimistic locking senaryosunu uygula
- Gerekirse pessimistic lock alternatifini hazırla
- Yarış durumu testleri yaz
- Concurrency hatalarını domain exception'a çevir
- Global exception handler ile uygun response dön

### Önemli Business Kuralları

- Aynı anda yapılan işlemler stok tutarsızlığı yaratmamalı
- Lost update problemi engellenmeli
- İki kullanıcının aynı stoğu yanlışlıkla aşırı tüketmesi önlenmeli

### Sprint Çıktısı

- Eş zamanlı işlem güvenliği sağlanır
- Concurrency kaynaklı veri kaybı engellenir
- Bu alanda test senaryosu bulunur

---

## Sprint 8 - Movement History & Reporting

### Amaç

Stok hareketlerini takip etmek ve anlamlı raporlar sunmak.

### Yapılacaklar

- Stock movement listeleme endpointleri oluştur
- Ürün bazlı hareket geçmişi ekle
- Depo bazlı hareket geçmişi ekle
- Tarih aralığı filtrelerini uygula
- Low stock raporu oluştur
- Temel dashboard metriklerini hazırla

### Önemli Business Kuralları

- Her stok değişikliği geçmişte izlenebilir olmalı
- Kritik stok seviyesi ürün bazında değerlendirilmeli
- Filtreleme sonuçları doğru ve tutarlı olmalı

### Sprint Çıktısı

- Hareket geçmişi sorgulanabilir
- Kritik stoklar raporlanabilir
- Yönetim için temel özet metrikler oluşur

---

## Sprint 9 - Idempotency & Audit (Opsiyonel)

### Amaç

Aynı işlemin yanlışlıkla iki kez uygulanmasını engellemek ve audit izini güçlendirmek.

### Yapılacaklar

- Idempotency key yapısını tasarla
- Aynı işlem tekrar geldiğinde ikinci kez uygulanmamasını sağla
- Unique constraint ekle
- CreatedBy / CreatedAt / UpdatedBy / UpdatedAt alanlarını yapılandır
- Spring Data Auditing desteğini ekle

### Önemli Business Kuralları

- Aynı işlem iki kez stok değiştirmemeli
- İşlemi yapan kullanıcı izlenebilir olmalı
- Audit kayıtları değişmez ve güvenilir olmalı

### Sprint Çıktısı

- Tekrarlanan istekler sistemde sorun yaratmaz
- İşlem geçmişi daha güvenilir hale gelir
- Proje gerçek dünya davranışına daha yakın olur

---

## Sprint 10 - Testing

### Amaç

Business logic'in güvenilirliğini testlerle garanti altına almak.

### Yapılacaklar

- Unit testler yaz
- Stock in / out senaryolarını test et
- Transfer rollback senaryosunu test et
- Concurrency testleri yaz
- Testcontainers ile gerçek PostgreSQL entegrasyonu kur
- Global exception response testleri ekle

### Önemli Business Kuralları

- Kritik stok kuralları testlerle korunmalı
- Transfer işlemleri rollback davranışı ile doğrulanmalı
- Concurrency senaryoları gerçekçi şekilde test edilmeli

### Sprint Çıktısı

- Temel iş kuralları testlerle güvence altına alınır
- PostgreSQL üzerinde integration testler çalışır
- Proje daha güvenilir hale gelir

---

## Sprint 11 - Docker / CI / Documentation

### Amaç

Projeyi portfolyoya ve sunuma hazır hale getirmek.

### Yapılacaklar

- Multi-stage Dockerfile hazırla
- Docker Compose ile tam çalışma ortamı oluştur
- GitHub Actions CI pipeline ekle
- README dosyasını yaz
- ER diyagramı ve mimari açıklamayı ekle
- API örnekleri ve çalışma adımlarını dokümante et
- Gelecek geliştirme maddelerini yaz

### Önemli Business Kuralları

- Proje tek komutla ayağa kalkabilmeli
- Dokümantasyon teknik ve iş mantığını birlikte anlatmalı
- CI pipeline temel kalite kapısı görevi görmeli

### Sprint Çıktısı

- Proje teslim edilebilir hale gelir
- Kurulum ve kullanım kolaylaşır
- GitHub ve CV için güçlü sunum paketi oluşur

---

## Zorunlu ve Opsiyonel Sprint Özeti

### Zorunlu Sprintler

- Sprint 1 - Foundation
- Sprint 2 - Authentication & Authorization
- Sprint 3 - Product Catalog
- Sprint 4 - Warehouse & Stock Model
- Sprint 5 - Stock In / Out
- Sprint 6 - Warehouse Transfer
- Sprint 7 - Concurrency Control
- Sprint 8 - Movement History & Reporting
- Sprint 10 - Testing
- Sprint 11 - Docker / CI / Documentation

### Opsiyonel Sprint

- Sprint 9 - Idempotency & Audit

## Geliştirme Öncelikleri

### 1. Önce Temel Doğruluk

İlk odak stok tutarlılığı ve transaction davranışı olmalı. Negatif stok, yarım kalmış transfer ve eş zamanlı güncelleme problemleri çözülmeden proje tamamlanmış sayılmamalı.

### 2. Sonra İş Kuralları

Ürün, depo, hareket ve transfer akışları gerçek iş mantığına uygun olmalı. Bu proje bir CRUD uygulaması gibi görünmemeli.

### 3. Ardından Gözlemlenebilirlik

Movement history, low stock raporu ve audit alanları sisteme şeffaflık kazandırmalı.

### 4. En Son Paketleme ve Sunum

Testler, Docker, CI ve README bölümleri projeyi portfolyo seviyesine taşır. Kod kadar anlatım da önemlidir.

## Kısa Yol Haritası

1. Foundation'ı kur
2. Authentication ve role kontrolünü ekle
3. Ürün, depo ve stok modelini tamamla
4. Stok giriş/çıkış ve transfer akışlarını bitir
5. Concurrency ve raporlama ekle
6. Test, Docker, CI ve dokümantasyon ile sonlandır

