<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isEdit" value="${formMode == 'edit'}" />
<c:set var="formFullname" value="${draftFullname != null ? draftFullname : user.fullName}" />
<c:set var="backListUrl" value="${returnTo == 'users' ? '/admin/user/list' : '/admin/category/list#users-section'}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${isEdit ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản'} - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261128-v1">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />

    <div class="admin-workspace">

        <main class="admin-form-container">
            <section class="admin-form-card">
                <div class="admin-form-header">
                    <h1>${isEdit ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản'}</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}${backListUrl}">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <c:if test="${not empty alert}">
                        <div class="admin-alert"><c:out value="${alert}" /></div>
                    </c:if>

                    <form id="userForm" data-mode="${isEdit ? 'edit' : 'add'}" action="${pageContext.request.contextPath}${isEdit ? '/admin/user/edit' : '/admin/user/add'}" method="post" novalidate>
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <c:if test="${not empty returnTo}">
                            <input type="hidden" name="returnTo" value="<c:out value='${returnTo}' />">
                        </c:if>
                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${user.id}">
                        </c:if>

                        <div class="form-grid-admin">
                            <div class="admin-field">
                                <label for="username">Tên đăng nhập</label>
                                <input id="username" class="${not empty errors.username ? 'is-invalid' : ''}" type="text" name="username" value="<c:out value='${user.userName}' />" required maxlength="50" ${isEdit ? 'readonly' : ''}>
                                <div class="invalid-feedback ${not empty errors.username ? 'd-block' : ''}"><c:out value="${errors.username}" /></div>
                            </div>
                            <div class="admin-field">
                                <label for="fullname">Họ và tên</label>
                                <input id="fullname" class="${not empty errors.fullname ? 'is-invalid' : ''}" type="text" name="fullname" value="<c:out value='${formFullname}' />" required maxlength="100">
                                <div class="invalid-feedback ${not empty errors.fullname ? 'd-block' : ''}"><c:out value="${errors.fullname}" /></div>
                            </div>
                            <div class="admin-field">
                                <label for="email">Email</label>
                                <input id="email" class="${not empty errors.email ? 'is-invalid' : ''}" type="email" name="email" value="<c:out value='${user.email}' />" maxlength="100">
                                <div class="invalid-feedback ${not empty errors.email ? 'd-block' : ''}"><c:out value="${errors.email}" /></div>
                            </div>
                            <div class="admin-field">
                                <label for="phone">Số điện thoại</label>
                                <input id="phone" class="${not empty errors.phone ? 'is-invalid' : ''}" type="text" name="phone" value="<c:out value='${user.phone}' />" maxlength="20">
                                <div class="invalid-feedback ${not empty errors.phone ? 'd-block' : ''}"><c:out value="${errors.phone}" /></div>
                            </div>
                            <div class="admin-field">
                                <label for="password">Mật khẩu${isEdit ? ' mới' : ''}</label>
                                <input id="password" class="${not empty errors.password ? 'is-invalid' : ''}" type="password" name="password" ${isEdit ? '' : 'required'} maxlength="100" autocomplete="new-password">
                                <p class="admin-field-hint">${isEdit ? 'Để trống nếu không muốn đổi mật khẩu.' : 'Mật khẩu tối thiểu 6 ký tự và được mã hóa trước khi lưu.'}</p>
                                <div class="invalid-feedback ${not empty errors.password ? 'd-block' : ''}"><c:out value="${errors.password}" /></div>
                            </div>
                            <div class="admin-field">
                                <label for="roleid">Vai trò</label>
                                <select id="roleid" name="roleid">
                                    <option value="1" ${user.roleid == 1 ? 'selected' : ''}>Admin</option>
                                    <option value="2" ${user.roleid == 2 ? 'selected' : ''}>Manager</option>
                                    <option value="5" ${user.roleid == 5 || user.roleid == 0 ? 'selected' : ''}>Member</option>
                                </select>
                            </div>
                            <div class="admin-field">
                                <label for="status">Trạng thái tài khoản</label>
                                <select id="status" name="status" ${isEdit && user.id == sessionScope.account.id ? 'disabled' : ''}>
                                    <option value="1" ${user.status != 0 ? 'selected' : ''}>Đã kích hoạt</option>
                                    <option value="0" ${user.status == 0 ? 'selected' : ''}>Chờ kích hoạt</option>
                                </select>
                                <c:if test="${isEdit && user.id == sessionScope.account.id}">
                                    <input type="hidden" name="status" value="1">
                                </c:if>
                            </div>
                        </div>

                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-primary" type="submit">${isEdit ? 'Lưu thay đổi' : 'Thêm tài khoản'}</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}${backListUrl}">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('userForm', function (form) {
        var valid = true;
        var isEdit = form.getAttribute('data-mode') === 'edit';
        var username = form.username.value.trim();
        var fullname = form.fullname.value.trim();
        var email = form.email.value.trim();
        var phone = form.phone.value.trim().replace(/[\s.-]/g, '');
        var password = form.password.value;

        if (!isEdit && !/^[A-Za-z0-9._-]{3,50}$/.test(username)) {
            window.ShopValidation.setError(form.username, 'Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, ".", "_" hoặc "-".');
            valid = false;
        }
        if (fullname.length < 2 || fullname.length > 100) {
            window.ShopValidation.setError(form.fullname, 'Họ và tên phải từ 2 đến 100 ký tự.');
            valid = false;
        }
        if (email && !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
            window.ShopValidation.setError(form.email, 'Địa chỉ email không đúng định dạng.');
            valid = false;
        }
        if (phone && !/^\+?[0-9]{9,15}$/.test(phone)) {
            window.ShopValidation.setError(form.phone, 'Số điện thoại phải gồm 9-15 chữ số hợp lệ.');
            valid = false;
        }
        if (!isEdit) {
            if (!password || password.length < 6 || password.length > 100) {
                window.ShopValidation.setError(form.password, 'Mật khẩu phải từ 6 đến 100 ký tự.');
                valid = false;
            }
        } else if (password && (password.length < 6 || password.length > 100)) {
            window.ShopValidation.setError(form.password, 'Mật khẩu mới phải từ 6 đến 100 ký tự.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
