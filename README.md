# BT Lập trình Web - ShopSpringBoot (Spring Boot 3 + Spring MVC + Spring Data JPA + SiteMesh 3 + Cloudinary)

Ứng dụng web Java theo mô hình MVC được viết lại toàn bộ bằng **Spring Boot 3.4 & Spring Framework** (Spring Web MVC, Spring Data JPA / Hibernate, Spring IoC/DI, Spring Transaction `@Transactional`), kết nối **MySQL** và sử dụng **SiteMesh Decorator 3** với giao diện **Bootstrap 5**, đảm bảo đầy đủ mọi chức năng của phiên bản cũ:
- **CRUD Danh mục (`Category`) và Thư viện Video (`Videos`)** có tìm kiếm, phân trang và phân quyền.
- **Quản lý Hồ sơ cá nhân (`/profile`)**: cập nhật thông tin, **xác nhận OTP qua email mới**, upload ảnh đại diện **`MultipartFile` lên Cloudinary**.
- **Validation Form** (Client-side + Server-side với Jakarta Bean Validation).
- **Xác thực & Bảo mật**: Đăng ký/kích hoạt OTP qua Email SMTP, đăng nhập hash password (`PBKDF2WithHmacSHA256`), Remember-Me cookie, quên mật khẩu OTP, bảo vệ CSRF và phân quyền Admin/Manager/Member.

## Cấu trúc thư mục chính

```text
BT_SpringBoot/
├── database/
│   ├── schema.sql                  # Tạo DB bt_lt_web và các bảng users, Category, remember_tokens, otp_tokens, Videos
│   └── seed.sql                    # Dữ liệu mẫu ban đầu
├── src/
│   ├── main/
│   │   ├── java/lt_web/
│   │   │   ├── BtSpringBootApplication.java  # Điểm khởi chạy @SpringBootApplication
│   │   │   ├── ServletInitializer.java       # Hỗ trợ deploy WAR lên external Tomcat
│   │   │   ├── config/
│   │   │   │   └── WebMvcConfig.java         # Cấu hình ViewResolver (/WEB-INF/views/*.jsp), ResourceHandler, FilterRegistrationBean & SiteMesh 3
│   │   │   ├── controllers/        # Spring MVC @Controller & OncePerRequestFilter
│   │   │   │   └── admin/          # Spring MVC @Controller cho Quản trị Danh mục & Tài khoản
│   │   │   ├── entity/             # JPA Entities: Category, User, RememberToken, OtpToken, Video
│   │   │   ├── exceptions/         # DataAccessException
│   │   │   ├── models/             # DTO/Form models: LoginModel, UserModel
│   │   │   ├── repository/         # JpaConfig (@Component seeding) & Repository interfaces
│   │   │   │   └── impl/           # Spring @Repository + @PersistenceContext + @Transactional
│   │   │   ├── services/           # Service interfaces & @Service beans
│   │   │   │   └── impl/           # Spring @Service + @Transactional implementations
│   │   │   └── utils/              # CloudinaryService (@Service), Constant, CsrfToken, FileStorage (MultipartFile), PasswordHasher, TokenUtil, ValidationUtil, AdminAccess
│   │   └── resources/
│   │       ├── static/
│   │       │   └── assets/css/     # site.css, admin.css
│   │       ├── templates/          # Thư mục chuẩn Spring Boot templates
│   │       ├── WEB-INF/
│   │       │   ├── commons/        # info.jsp, taglib.jsp, commons/web/*, commons/admin/*
│   │       │   ├── decorators/     # Layout SiteMesh 3 nền Bootstrap 5 (web.jsp, admin.jsp, login.jsp)
│   │       │   ├── views/          # index.jsp, home.jsp, profile.jsp, login.jsp, register.jsp, verify-otp.jsp, forgot-password.jsp, reset-password.jsp, video-*.jsp, views/admin/*
│   │       │   └── sitemesh3.xml   # Mapping layout SiteMesh 3
│   │       ├── application.properties        # Cấu hình chuẩn Spring Boot
│   │       └── db.properties.example         # Mẫu cấu hình kết nối MySQL, SMTP và Cloudinary cục bộ
│   └── test/java/lt_web/           # Unit Tests & @SpringBootTest Integration Test
└── pom.xml
```
