OTEL REZERVASYON SİSTEMİ - TEKNİK CASE ÇALIŞMASI
(Sr. Java Developer)

Bu case çalışmasında, otel rezervasyonlarını yöneten bir mikroservis mimarisi geliştirmen beklenmektedir.

Proje kapsamında aşağıdaki bileşenleri içeren bir sistem tasarlamanı bekliyoruz:
1. Otel Servisi → Otel ve oda yönetimi
2. Rezervasyon Servisi → Rezervasyon işlemleri
3. Bildirim Servisi → Kafka ile rezervasyon bildirimleri

Tüm servisler mikroservis mimarisine uygun, birbirinden bağımsız çalışabilir olmalı ve Docker ile ayağa kaldırılabilir
durumda olmalıdır.

İstenilen Özellikler

Otel ve Oda Yönetimi (Hotel Service)

Bir otel ve odalarını ekleyip yönetebileceğin bir REST API oluşturmalısın.
CRUD işlemleri eklenmeli.

Veri modeli olarak aşağıdaki modelleri örnek alabilirsin, dilersen kendi modellerini de oluşturabilirsin.

@Entity
public class Hotel {
@Id @GeneratedValue
private Long id;
private String name;
private String address;
private int starRating;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
}

@Entity
public class Room {
@Id @GeneratedValue
private Long id;
private Long hotelId;
private String roomNumber;
private int capacity;
private BigDecimal pricePerNight;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
}

Veri tabanı olarak Postgres kullanabilirsin
Spring Data JPA kullanmanı bekleyeceğiz.
Exception handling ve validation süreçlerini tasarlamalısın.

Rezervasyon Yönetimi (Reservation Service)

Kullanıcıların otellerde oda rezervasyonu yapmasını sağlayan bir REST API geliştirmelisin.
Aynı oda için çakışan rezervasyonları engelleyebilmesin (Locking mekanizmalarını kullanabilirsin).
Kafka ile rezervasyon durumlarının yönetimini yapmalısın.

Veri modeli olarak aşağıdaki modeli kullanabilirsin.

@Entity
public class Reservation {
@Id @GeneratedValue
private Long id;
private Long hotelId;
private Long roomId;
private String guestName;
private LocalDate checkInDate;
private LocalDate checkOutDate;
private LocalDateTime createdAt;
}

Rezervasyonların kontrolünü Kafka üzerinden ilerletebilirsin.
Her servis için ayrı ayrı unit ve integration testleri yazmalısın.

Bildirim Servisi (Notification Service)

Kafka Consumer olarak çalışmalı ve “Reservation Created” event’lerini dinlemeli.
Rezervasyon yapıldığında loglama veya bir e-posta bildirimi simülasyonu yap.

Bildirim mesajı için aşağıdaki modeli kullanabilirsin.

{
"reservationId": 1,
"hotelId": 10,
"roomId": 100,
"guestName": "John Doe",
"checkInDate": "2025-02-10",
"checkOutDate": "2025-02-15"
}

API Gateway & Security

API Gateway kullanarak tüm servisleri tek bir giriş noktası üzerinden sunmalısın.
JWT Authentication & Authorization eklemelisin.
Kullanıcıların sadece kendi rezervasyonlarını görüntüleyebilmesini sağlamalısın.

Docker & Deployment

Docker ve Docker Compose kullanarak projeyi ayağa kaldırmalısın.
Servisler birbirinden bağımsız çalışmalı.

Senden beklentilerimiz şöyle

Clean Code & SOLID Prensipleri → Kodun temiz, anlaşılır ve ölçeklenebilir olmalı.
Transaction Yönetimi → Aynı anda iki kişinin aynı odayı rezerve etmesi engellenmeli.
Event-Driven Mimari → Kafka ile event bazlı bir yapı kullanılmalı.
Testler → Unit ve integration testler eklenmeli.
Dokümantasyon → API’lerin nasıl kullanılacağını anlatan bir README.md dosyası hazırlanmalı.

Projeyi bitirdikten sonra IK üzerinden projenin kaynak kodlarını bizimle paylaşabilirsin veya github üzerinden bir repo
ile de sunabilirsin.
Docker ve veritabanı kurulumlarını içeren docker-compose.yaml dosyası muhakkak olmalı.
RestAPI’lerini test etmek için Postman Collectionlarını göndermelisin.

Soruların olursa IK üzerinden bize ulaşabilirsin






