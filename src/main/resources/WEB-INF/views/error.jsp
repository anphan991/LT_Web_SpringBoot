<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập thất bại - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
</head>
<body>
<main class="error-page">
    <section class="error-card">
        <h1>Đăng nhập không thành công</h1>
        <p>Username hoặc password chưa đúng. Vui lòng thử lại.</p>
        <a class="button button-primary" href="${pageContext.request.contextPath}/login">Đăng nhập lại</a>
    </section>
</main>
</body>
</html>
