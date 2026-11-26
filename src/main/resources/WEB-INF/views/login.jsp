<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
</head>
<body>

<main class="auth-page">
    <section class="auth-card" aria-labelledby="login-title">
        <div class="auth-header">
            <h1 id="login-title">Đăng nhập</h1>
            <p>Sử dụng tài khoản của bạn để tiếp tục.</p>
        </div>

        <c:if test="${param.registered == '1'}">
            <div class="alert alert-success">Tạo tài khoản thành công. Vui lòng đăng nhập.</div>
        </c:if>
        <c:if test="${param.activated == '1'}">
            <div class="alert alert-success">Kích hoạt tài khoản thành công! Vui lòng đăng nhập.</div>
        </c:if>
        <c:if test="${param.resetSuccess == '1'}">
            <div class="alert alert-success">Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.</div>
        </c:if>
        <c:if test="${param.signedOut == '1'}">
            <div class="alert alert-success">Đã đăng xuất thành công khỏi hệ thống.</div>
        </c:if>
        <c:if test="${param.requireAuth == '1'}">
            <div class="alert alert-warning">Vui lòng đăng nhập để truy cập trang này.</div>
        </c:if>
        <c:if test="${not empty unactivatedEmail}">
            <div class="alert alert-warning">
                Tài khoản chưa kích hoạt.
                <a href="${pageContext.request.contextPath}/verify-otp?email=<c:out value='${unactivatedEmail}' />&type=activation" style="font-weight:700;text-decoration:underline;">
                    Bấm vào đây để nhập mã OTP kích hoạt
                </a>
            </div>
        </c:if>
        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="field-group">
                <label for="username">Tên đăng nhập</label>
                <input id="username" class="input-control ${not empty errors.username ? 'is-invalid' : ''}" type="text" name="username" value="<c:out value='${rememberedUsername}' />" placeholder="Nhập tên đăng nhập" required maxlength="50" autocomplete="username">
                <div class="invalid-feedback ${not empty errors.username ? 'd-block' : ''}"><c:out value="${errors.username}" /></div>
            </div>

            <div class="field-group">
                <label for="password">Mật khẩu</label>
                <input id="password" class="input-control ${not empty errors.password ? 'is-invalid' : ''}" type="password" name="password" placeholder="Nhập mật khẩu" required autocomplete="current-password">
                <div class="invalid-feedback ${not empty errors.password ? 'd-block' : ''}"><c:out value="${errors.password}" /></div>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin: 5px 0 22px;">
                <label class="checkbox-row" style="margin: 0;">
                    <input type="checkbox" name="remember" ${remembered ? 'checked' : ''}>
                    <span>Ghi nhớ đăng nhập</span>
                </label>
                <a href="${pageContext.request.contextPath}/forgot-password" style="font-size: 13px; color: var(--accent); text-decoration: none; font-weight: 600;">Quên mật khẩu?</a>
            </div>

            <button class="button button-primary auth-submit" type="submit">Đăng nhập</button>
        </form>

        <p class="demo-note">Tài khoản mẫu: <strong>admin</strong> / <strong>123456</strong></p>

        <div class="auth-note">
            Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('loginForm', function (form) {
        var valid = true;
        var username = form.username.value.trim();
        var password = form.password.value;
        if (!username) {
            window.ShopValidation.setError(form.username, 'Vui lòng nhập tên đăng nhập.');
            valid = false;
        } else if (username.length > 50) {
            window.ShopValidation.setError(form.username, 'Tên đăng nhập không được vượt quá 50 ký tự.');
            valid = false;
        }
        if (!password || !password.trim()) {
            window.ShopValidation.setError(form.password, 'Vui lòng nhập mật khẩu.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
