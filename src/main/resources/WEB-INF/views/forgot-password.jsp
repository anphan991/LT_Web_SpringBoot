<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quên mật khẩu - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
</head>
<body>

<main class="auth-page">
    <section class="auth-card" aria-labelledby="forgot-title">
        <div class="auth-header">
            <h1 id="forgot-title">Quên mật khẩu</h1>
            <p>Nhập email đăng ký của bạn để nhận mã OTP đặt lại mật khẩu.</p>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form id="forgotForm" action="${pageContext.request.contextPath}/forgot-password" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="field-group">
                <label for="email">Email đăng ký</label>
                <input id="email" class="input-control ${not empty errors.email ? 'is-invalid' : ''}" type="email" name="email"
                       value="<c:out value='${email}' />" placeholder="email@domain.com" required maxlength="100" autofocus autocomplete="email">
                <div class="invalid-feedback ${not empty errors.email ? 'd-block' : ''}"><c:out value="${errors.email}" /></div>
            </div>

            <p class="form-note">Hệ thống sẽ gửi mã xác thực OTP 6 chữ số tới email của bạn (hiệu lực 5 phút).</p>

            <button class="button button-primary auth-submit" type="submit" style="margin-top: 14px;">Gửi mã OTP</button>
        </form>

        <div class="auth-note">
            Nhớ mật khẩu? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('forgotForm', function (form) {
        var email = form.email.value.trim();
        if (!email) {
            window.ShopValidation.setError(form.email, 'Vui lòng nhập email đã đăng ký.');
            return false;
        }
        if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
            window.ShopValidation.setError(form.email, 'Địa chỉ email không đúng định dạng.');
            return false;
        }
        return true;
    });
});
</script>
</body>
</html>
