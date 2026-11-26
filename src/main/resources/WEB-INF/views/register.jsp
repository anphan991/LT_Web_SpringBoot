<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
</head>
<body>

<main class="auth-page">
    <section class="auth-card register-card" aria-labelledby="register-title">
        <div class="auth-header">
            <h1 id="register-title">Tạo tài khoản</h1>
            <p>Điền thông tin cơ bản để trở thành thành viên.</p>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="form-grid">
                <div class="field-group">
                    <label for="username">Tên đăng nhập</label>
                    <input id="username" class="input-control ${not empty errors.username ? 'is-invalid' : ''}" type="text" name="username" value="<c:out value='${username}' />" placeholder="Ví dụ: nguyenvana" required maxlength="50" autocomplete="username">
                    <div class="invalid-feedback ${not empty errors.username ? 'd-block' : ''}"><c:out value="${errors.username}" /></div>
                </div>
                <div class="field-group">
                    <label for="fullname">Họ và tên</label>
                    <input id="fullname" class="input-control ${not empty errors.fullname ? 'is-invalid' : ''}" type="text" name="fullname" value="<c:out value='${fullname}' />" placeholder="Nguyễn Văn A" required maxlength="100" autocomplete="name">
                    <div class="invalid-feedback ${not empty errors.fullname ? 'd-block' : ''}"><c:out value="${errors.fullname}" /></div>
                </div>
                <div class="field-group">
                    <label for="email">Email (nhận mã OTP)</label>
                    <input id="email" class="input-control ${not empty errors.email ? 'is-invalid' : ''}" type="email" name="email" value="<c:out value='${email}' />" placeholder="email@domain.com" required maxlength="100" autocomplete="email">
                    <div class="invalid-feedback ${not empty errors.email ? 'd-block' : ''}"><c:out value="${errors.email}" /></div>
                </div>
                <div class="field-group">
                    <label for="phone">Số điện thoại</label>
                    <input id="phone" class="input-control ${not empty errors.phone ? 'is-invalid' : ''}" type="text" name="phone" value="<c:out value='${phone}' />" placeholder="0901234567" required maxlength="20" autocomplete="tel">
                    <div class="invalid-feedback ${not empty errors.phone ? 'd-block' : ''}"><c:out value="${errors.phone}" /></div>
                </div>
                <div class="field-group">
                    <label for="password">Mật khẩu</label>
                    <input id="password" class="input-control ${not empty errors.password ? 'is-invalid' : ''}" type="password" name="password" placeholder="Tối thiểu 6 ký tự" required maxlength="100" autocomplete="new-password">
                    <div class="invalid-feedback ${not empty errors.password ? 'd-block' : ''}"><c:out value="${errors.password}" /></div>
                </div>
                <div class="field-group">
                    <label for="repassword">Nhập lại mật khẩu</label>
                    <input id="repassword" class="input-control ${not empty errors.repassword ? 'is-invalid' : ''}" type="password" name="repassword" placeholder="Xác nhận mật khẩu" required maxlength="100" autocomplete="new-password">
                    <div class="invalid-feedback ${not empty errors.repassword ? 'd-block' : ''}"><c:out value="${errors.repassword}" /></div>
                </div>
            </div>

            <p class="form-note">Sau khi bấm Tạo tài khoản, hệ thống sẽ gửi một mã OTP 6 chữ số tới email của bạn để kích hoạt tài khoản.</p>

            <button class="button button-primary auth-submit" type="submit">Tạo tài khoản</button>
        </form>

        <div class="auth-note">
            Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('registerForm', function (form) {
        var valid = true;
        var username = form.username.value.trim();
        var fullname = form.fullname.value.trim();
        var email = form.email.value.trim();
        var phone = form.phone.value.trim().replace(/[\s.-]/g, '');
        var password = form.password.value;
        var repassword = form.repassword.value;

        if (!/^[A-Za-z0-9._-]{3,50}$/.test(username)) {
            window.ShopValidation.setError(form.username, 'Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, ".", "_" hoặc "-".');
            valid = false;
        }
        if (fullname.length < 2 || fullname.length > 100) {
            window.ShopValidation.setError(form.fullname, 'Họ và tên phải từ 2 đến 100 ký tự.');
            valid = false;
        }
        if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
            window.ShopValidation.setError(form.email, 'Địa chỉ email không đúng định dạng.');
            valid = false;
        }
        if (!/^\+?[0-9]{9,15}$/.test(phone)) {
            window.ShopValidation.setError(form.phone, 'Số điện thoại phải gồm 9-15 chữ số hợp lệ.');
            valid = false;
        }
        if (!password || password.length < 6 || password.length > 100) {
            window.ShopValidation.setError(form.password, 'Mật khẩu phải từ 6 đến 100 ký tự.');
            valid = false;
        }
        if (!repassword) {
            window.ShopValidation.setError(form.repassword, 'Vui lòng nhập lại mật khẩu.');
            valid = false;
        } else if (password !== repassword) {
            window.ShopValidation.setError(form.repassword, 'Mật khẩu nhập lại không khớp.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
