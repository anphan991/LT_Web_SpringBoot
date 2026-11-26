<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xác thực mã OTP - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
    <style>
        .otp-input-field {
            text-align: center;
            font-size: 26px;
            font-weight: 700;
            letter-spacing: 8px;
            font-family: monospace;
        }
        .otp-email-box {
            margin-bottom: 20px;
            padding: 12px 14px;
            background: #f7f9fc;
            border-left: 3px solid var(--accent);
            border-radius: 4px;
            font-size: 13.5px;
            color: #364152;
        }
        .otp-email-box strong {
            color: var(--ink);
        }
        .resend-box {
            margin-top: 18px;
            text-align: center;
            font-size: 13px;
        }
        .resend-box form {
            display: inline;
        }
        .resend-button {
            background: none;
            border: none;
            padding: 0;
            color: var(--accent);
            font-weight: 600;
            cursor: pointer;
            text-decoration: underline;
        }
        .resend-button:hover {
            color: var(--accent-dark);
        }
    </style>
</head>
<body>

<main class="auth-page">
    <section class="auth-card" aria-labelledby="otp-title">
        <div class="auth-header">
            <h1 id="otp-title">
                <c:choose>
                    <c:when test="${type == 'FORGOT_PASSWORD'}">Xác thực đặt lại mật khẩu</c:when>
                    <c:otherwise>Kích hoạt tài khoản</c:otherwise>
                </c:choose>
            </h1>
            <p>
                <c:choose>
                    <c:when test="${type == 'FORGOT_PASSWORD'}">Nhập mã OTP 6 chữ số đã gửi tới email để đặt lại mật khẩu.</c:when>
                    <c:otherwise>Nhập mã OTP 6 chữ số để kích hoạt tài khoản của bạn.</c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="otp-email-box">
            Mã OTP đã gửi tới: <strong><c:out value="${email}" /></strong>
        </div>

        <c:if test="${param.resent == '1'}">
            <div class="alert alert-success">Đã gửi lại mã OTP mới. Vui lòng kiểm tra hộp thư (hoặc console).</div>
        </c:if>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form id="verifyOtpForm" action="${pageContext.request.contextPath}/verify-otp" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="email" value="<c:out value='${email}' />">
            <input type="hidden" name="type" value="<c:out value='${type}' />">

            <div class="field-group">
                <label for="otp">Mã OTP (6 chữ số)</label>
                <input id="otp" class="input-control otp-input-field ${not empty errors.otp ? 'is-invalid' : ''}" type="text" name="otp"
                       maxlength="6" pattern="[0-9]{6}" inputmode="numeric"
                       placeholder="000000" required autofocus autocomplete="one-time-code">
                <div class="invalid-feedback ${not empty errors.otp ? 'd-block' : ''}"><c:out value="${errors.otp}" /></div>
            </div>

            <p class="form-note">Mã OTP có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này.</p>

            <button class="button button-primary auth-submit" type="submit" style="margin-top: 14px;">Xác nhận</button>
        </form>

        <div class="resend-box">
            Chưa nhận được mã?
            <form action="${pageContext.request.contextPath}/resend-otp" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                <input type="hidden" name="email" value="<c:out value='${email}' />">
                <input type="hidden" name="type" value="<c:out value='${type}' />">
                <button type="submit" class="resend-button">Gửi lại mã OTP</button>
            </form>
        </div>

        <div class="auth-note">
            Quay lại <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('verifyOtpForm', function (form) {
        var otp = form.otp.value.trim();
        if (!/^[0-9]{6}$/.test(otp)) {
            window.ShopValidation.setError(form.otp, 'Mã OTP phải gồm đúng 6 chữ số.');
            return false;
        }
        return true;
    });
});
</script>
</body>
</html>
