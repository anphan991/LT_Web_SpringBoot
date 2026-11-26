USE bt_lt_web;

-- Mật khẩu của tài khoản admin bên dưới là: 123456
-- Hash được tạo bằng PBKDF2-HMAC-SHA256, 120000 vòng lặp, salt riêng.
INSERT INTO users (email, username, fullname, password, roleid, createdDate)
VALUES (
    'admin@example.com',
    'admin',
    'Quản trị viên',
    'pbkdf2$120000$Ua7LbXovTKy6btPkYokEOg==$+20PWbrYZc7NNqWAAZbdFZEgpmngmwT08CGrPvw4iHk=',
    1,
    CURRENT_DATE
)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT IGNORE INTO Category (cate_name, icons) VALUES
    ('Quần Áo Nam', 'default-men.svg'),
    ('Quần Áo Nữ', 'default-women.svg'),
    ('Giày', 'default-category.svg');

-- Dữ liệu mẫu 10 Videos YouTube gắn với tài khoản admin (quan hệ 1-N User - Videos)
INSERT INTO Videos (title, poster, video_path, description, views, active, created_at, user_id)
SELECT v.title, 'default-video-poster.svg', v.video_path, v.description, v.views, 1, v.created_at, u.id
FROM users u
CROSS JOIN (
    SELECT 'Java in 100 Seconds' AS title, 'https://www.youtube.com/watch?v=l9AzO1FMgM8' AS video_path, 'Tổng quan nhanh về ngôn ngữ lập trình Java, JVM và hệ sinh thái doanh nghiệp.' AS description, 340 AS views, DATE_SUB(NOW(), INTERVAL 10 HOUR) AS created_at
    UNION ALL SELECT 'HTML in 100 Seconds', 'https://www.youtube.com/watch?v=ok-plXXHlWw', 'Cấu trúc tài liệu HTML, các thẻ ngữ nghĩa cơ bản khi xây dựng trang web.', 215, DATE_SUB(NOW(), INTERVAL 9 HOUR)
    UNION ALL SELECT 'CSS in 100 Seconds', 'https://www.youtube.com/watch?v=OEV8gMkCHXQ', 'Cách định kiểu giao diện, Flexbox, Grid và bố cục responsive cho website.', 189, DATE_SUB(NOW(), INTERVAL 8 HOUR)
    UNION ALL SELECT 'JavaScript in 100 Seconds', 'https://www.youtube.com/watch?v=DHjqpvDnNGE', 'Giới thiệu nhanh về JavaScript, DOM, xử lý sự kiện và bất đồng bộ.', 412, DATE_SUB(NOW(), INTERVAL 7 HOUR)
    UNION ALL SELECT 'SQL in 100 Seconds', 'https://www.youtube.com/watch?v=zsjvFFKOm3c', 'Nguyên lý cơ sở dữ liệu quan hệ, khóa chính, khóa ngoại và truy vấn SQL.', 276, DATE_SUB(NOW(), INTERVAL 6 HOUR)
    UNION ALL SELECT 'Git Explained in 100 Seconds', 'https://www.youtube.com/watch?v=hwP7WQkmECE', 'Cách quản lý phiên bản mã nguồn với commit, branch, merge và push lên GitHub.', 198, DATE_SUB(NOW(), INTERVAL 5 HOUR)
    UNION ALL SELECT 'Docker in 100 Seconds', 'https://www.youtube.com/watch?v=Gjnup-PuquQ', 'Đóng gói và triển khai ứng dụng nhất quán bằng Container và Dockerfile.', 164, DATE_SUB(NOW(), INTERVAL 4 HOUR)
    UNION ALL SELECT 'REST API Concepts and Examples', 'https://www.youtube.com/watch?v=-MTSQjw5DrM', 'Tìm hiểu kiến trúc RESTful API, các phương thức GET/POST/PUT/DELETE và JSON.', 230, DATE_SUB(NOW(), INTERVAL 3 HOUR)
    UNION ALL SELECT 'HTTP Crash Course & Exploration', 'https://www.youtube.com/watch?v=iYM2zFP3Zn0', 'Cơ chế request/response, header, status code và HTTPS trong lập trình Web.', 152, DATE_SUB(NOW(), INTERVAL 2 HOUR)
    UNION ALL SELECT 'MySQL — The Basics in 100 Seconds', 'https://www.youtube.com/watch?v=Cz3WcZLRaWc', 'Thực hành tạo bảng, liên kết quan hệ 1-N và truy vấn dữ liệu trong MySQL.', 295, DATE_SUB(NOW(), INTERVAL 1 HOUR)
) v
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM (
          SELECT video_id FROM Videos WHERE video_path LIKE 'https://www.youtube.com/watch?v=%' LIMIT 1
      ) AS existing_yt
  );
