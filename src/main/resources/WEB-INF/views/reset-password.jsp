<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đặt lại mật khẩu - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
    <style>
        .reset-email-box {
            margin-bottom: 20px;
            padding: 12px 14px;
            background: #f7f9fc;
            border-left: 3px solid var(--accent);
            border-radius: 4px;
            font-size: 13.5px;
            color: #364152;
        }
        .otp-input-field {
            text-align: center;
            font-size: 22px;
            font-weight: 700;
            letter-spacing: 6px;
            font-family: monospace;
        }
    </style>
</head>
<body>

<main class="auth-page">
    <section class="auth-card" aria-labelledby="reset-title">
        <div class="auth-header">
            <h1 id="reset-title">Đặt lại mật khẩu</h1>
            <p>Nhập mật khẩu mới để bảo mật tài khoản của bạn.</p>
        </div>

        <div class="reset-email-box">
            Tài khoản: <strong><c:out value="${email}" /></strong>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form id="resetPasswordForm" action="${pageContext.request.contextPath}/reset-password" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="email" value="<c:out value='${email}' />">

            <c:if test="${!isOtpVerified}">
                <div class="field-group">
                    <label for="otp">Mã OTP (6 chữ số)</label>
                    <input id="otp" class="input-control otp-input-field ${not empty errors.otp ? 'is-invalid' : ''}" type="text" name="otp"
                           maxlength="6" pattern="[0-9]{6}" inputmode="numeric"
                           placeholder="000000" required autofocus autocomplete="one-time-code">
                    <div class="invalid-feedback ${not empty errors.otp ? 'd-block' : ''}"><c:out value="${errors.otp}" /></div>
                </div>
            </c:if>

            <div class="field-group">
                <label for="password">Mật khẩu mới</label>
                <input id="password" class="input-control ${not empty errors.password ? 'is-invalid' : ''}" type="password" name="password"
                       placeholder="Tối thiểu 6 ký tự" required maxlength="100" autocomplete="new-password">
                <div class="invalid-feedback ${not empty errors.password ? 'd-block' : ''}"><c:out value="${errors.password}" /></div>
            </div>

            <div class="field-group">
                <label for="repassword">Nhập lại mật khẩu mới</label>
                <input id="repassword" class="input-control ${not empty errors.repassword ? 'is-invalid' : ''}" type="password" name="repassword"
                       placeholder="Xác nhận mật khẩu mới" required maxlength="100" autocomplete="new-password">
                <div class="invalid-feedback ${not empty errors.repassword ? 'd-block' : ''}"><c:out value="${errors.repassword}" /></div>
            </div>

            <button class="button button-primary auth-submit" type="submit" style="margin-top: 14px;">Đặt lại mật khẩu</button>
        </form>

        <div class="auth-note">
            Quay lại <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('resetPasswordForm', function (form) {
        var valid = true;
        if (form.otp) {
            if (!/^[0-9]{6}$/.test(form.otp.value.trim())) {
                window.ShopValidation.setError(form.otp, 'Vui lòng nhập mã OTP gồm đúng 6 chữ số.');
                valid = false;
            }
        }
        var password = form.password.value;
        var repassword = form.repassword.value;
        if (!password || password.length < 6 || password.length > 100) {
            window.ShopValidation.setError(form.password, 'Mật khẩu mới phải từ 6 đến 100 ký tự.');
            valid = false;
        }
        if (!repassword) {
            window.ShopValidation.setError(form.repassword, 'Vui lòng nhập lại mật khẩu mới.');
            valid = false;
        } else if (password !== repassword) {
            window.ShopValidation.setError(form.repassword, 'Mật khẩu xác nhận không khớp.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
