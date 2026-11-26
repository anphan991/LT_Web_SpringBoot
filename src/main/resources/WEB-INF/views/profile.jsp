<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="user" value="${not empty profileUser ? profileUser : sessionScope.account}" />
<c:set var="pendingEmail" value="${sessionScope.profilePendingEmail}" />
<c:set var="formFullname" value="${not empty draftFullname ? draftFullname : user.fullName}" />
<c:set var="formPhone" value="${draftPhone != null ? draftPhone : user.phone}" />
<c:set var="formEmail" value="${not empty draftEmail ? draftEmail : (not empty pendingEmail ? pendingEmail : user.email)}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ cá nhân - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
</head>
<body>

<main class="page-container profile-main">
    <div class="profile-page-header">
        <div>
            <h1>Hồ sơ cá nhân</h1>
            <p>Cập nhật họ tên, số điện thoại, ảnh đại diện và xác thực email mới.</p>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <a class="button button-secondary" href="${pageContext.request.contextPath}/home">
                <i class="bi bi-arrow-left"></i> Về trang danh mục
            </a>
        </div>
    </div>

    <c:if test="${not empty successMsg}">
        <div class="alert alert-success d-flex align-items-center gap-2">
            <i class="bi bi-check-circle-fill"></i>
            <span><c:out value="${successMsg}" /></span>
        </div>
    </c:if>
    <c:if test="${not empty infoMsg}">
        <div class="alert alert-info d-flex align-items-center gap-2">
            <i class="bi bi-envelope-check-fill"></i>
            <span><c:out value="${infoMsg}" /></span>
        </div>
    </c:if>
    <c:if test="${not empty alert}">
        <div class="alert alert-danger d-flex align-items-center gap-2">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <span><c:out value="${alert}" /></span>
        </div>
    </c:if>

    <div class="profile-grid">
        <aside class="profile-card profile-summary-card">
            <div class="profile-avatar-frame">
                <div class="profile-avatar-inner" id="avatarPreviewBox">
                    <c:choose>
                        <c:when test="${not empty user.avatar}">
                            <c:choose>
                                <c:when test="${user.avatar.startsWith('http://') || user.avatar.startsWith('https://')}">
                                    <img id="avatarPreviewImg" src="<c:out value='${user.avatar}' />" alt="Ảnh đại diện">
                                </c:when>
                                <c:otherwise>
                                    <c:url value="/image" var="localAvatarUrl">
                                        <c:param name="fname" value="${user.avatar}" />
                                    </c:url>
                                    <img id="avatarPreviewImg" src="${localAvatarUrl}" alt="Ảnh đại diện">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <span id="avatarInitials"><c:out value="${not empty user.userName ? user.userName.substring(0, 1) : 'U'}" /></span>
                            <img id="avatarPreviewImg" src="" alt="Xem trước ảnh đại diện" style="display:none;">
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <h2 class="h5 fw-bold mb-1 text-dark"><c:out value="${not empty user.fullName ? user.fullName : user.userName}" /></h2>
            <p class="text-muted small mb-2">@<c:out value="${user.userName}" /></p>

            <div class="d-flex justify-content-center flex-wrap gap-2">
                <span class="badge rounded-pill text-bg-primary bg-opacity-10 text-primary border border-primary-subtle px-3 py-2">
                    <c:choose>
                        <c:when test="${user.roleid == 1}">Quản trị viên</c:when>
                        <c:when test="${user.roleid == 2}">Quản lý</c:when>
                        <c:otherwise>Thành viên</c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="profile-meta-list">
                <div class="profile-meta-row">
                    <span>Email hiện tại</span>
                    <strong><c:out value="${not empty user.email ? user.email : 'Chưa cập nhật'}" /></strong>
                </div>
                <div class="profile-meta-row">
                    <span>Số điện thoại</span>
                    <strong><c:out value="${not empty user.phone ? user.phone : 'Chưa cập nhật'}" /></strong>
                </div>
                <div class="profile-meta-row">
                    <span>Trạng thái email</span>
                    <c:choose>
                        <c:when test="${not empty pendingEmail}">
                            <strong class="text-primary">Chờ OTP cho email mới</strong>
                        </c:when>
                        <c:otherwise>
                            <strong class="text-success">Đã xác thực</strong>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${user.createdDate != null}">
                    <div class="profile-meta-row">
                        <span>Ngày tham gia</span>
                        <strong><c:out value="${user.createdDate}" /></strong>
                    </div>
                </c:if>
            </div>
        </aside>

        <div>
            <section class="profile-card" aria-labelledby="profile-form-heading">
                <div class="d-flex align-items-center justify-content-between mb-4 pb-3 border-bottom">
                    <div>
                        <h2 id="profile-form-heading" class="h5 fw-bold mb-1">Cập nhật thông tin tài khoản</h2>
                        <p class="text-muted small mb-0">Xác nhận OTP để xác nhận email mới.</p>
                    </div>
                </div>

                <form id="profileForm"
                      action="${pageContext.request.contextPath}/profile"
                      method="post"
                      enctype="multipart/form-data"
                      novalidate>
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                    <input type="hidden" name="action" value="updateProfile">

                    <div class="form-grid">
                        <div class="field-group">
                            <label for="usernameDisplay">Tên đăng nhập</label>
                            <input id="usernameDisplay" class="input-control bg-light text-muted" type="text"
                                   value="<c:out value='${user.userName}' />" readonly>
                        </div>

                        <div class="field-group">
                            <label for="fullname">Họ và tên</label>
                            <input id="fullname"
                                   class="input-control ${not empty errors.fullname ? 'is-invalid' : ''}"
                                   type="text"
                                   name="fullname"
                                   value="<c:out value='${formFullname}' />"
                                   placeholder="Nhập họ và tên"
                                   required
                                   maxlength="100">
                            <div class="invalid-feedback ${not empty errors.fullname ? 'd-block' : ''}"><c:out value="${errors.fullname}" /></div>
                        </div>
                    </div>

                    <div class="form-grid">
                        <div class="field-group">
                            <label for="phone">Số điện thoại</label>
                            <input id="phone"
                                   class="input-control ${not empty errors.phone ? 'is-invalid' : ''}"
                                   type="text"
                                   name="phone"
                                   value="<c:out value='${formPhone}' />"
                                   placeholder="Ví dụ: 0912345678"
                                   maxlength="20">
                            <div class="invalid-feedback ${not empty errors.phone ? 'd-block' : ''}"><c:out value="${errors.phone}" /></div>
                        </div>

                        <div class="field-group">
                            <label for="email">Địa chỉ Email</label>
                            <input id="email"
                                   class="input-control ${not empty errors.email ? 'is-invalid' : ''}"
                                   type="email"
                                   name="email"
                                   value="<c:out value='${formEmail}' />"
                                   placeholder="email@example.com"
                                   required
                                   maxlength="100">
                            <div class="invalid-feedback ${not empty errors.email ? 'd-block' : ''}"><c:out value="${errors.email}" /></div>
                            <div class="form-text mt-1">Email hiện đang lưu: <strong><c:out value="${user.email}" /></strong></div>
                        </div>
                    </div>

                    <div class="field-group">
                        <label for="images">Ảnh đại diện</label>
                        <div class="profile-upload-zone">
                            <input id="images"
                                   class="form-control ${not empty errors.images ? 'is-invalid' : ''}"
                                   type="file"
                                   name="images"
                                   accept="image/png,image/jpeg,image/gif,image/webp,image/svg+xml"
                                   onchange="previewProfileAvatar(this)">
                            <div class="invalid-feedback ${not empty errors.images ? 'd-block' : ''}"><c:out value="${errors.images}" /></div>
                            <p class="small text-muted mb-0 mt-2">
                                Hỗ trợ JPG, PNG, GIF, WEBP, SVG (tối đa 5MB).
                            </p>
                        </div>
                    </div>

                    <div class="d-flex flex-wrap gap-2 mt-3">
                        <button class="button button-primary" type="submit">
                            <i class="bi bi-check2-circle"></i> Lưu thay đổi hồ sơ
                        </button>
                    </div>
                </form>
            </section>

            <c:if test="${not empty pendingEmail}">
                <section class="profile-card profile-otp-card" aria-labelledby="otp-verify-heading">
                    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
                        <div>
                            <h3 id="otp-verify-heading" class="h5 fw-bold text-primary mb-1">
                                <i class="bi bi-shield-lock me-1"></i>Xác nhận OTP đổi email mới
                            </h3>
                            <p class="small text-muted mb-0">
                                Mã OTP gồm 6 chữ số đã được gửi tới <strong><c:out value="${pendingEmail}" /></strong> (hiệu lực 5 phút).
                            </p>
                        </div>
                        <span class="badge rounded-pill text-bg-primary">Bước 2/2</span>
                    </div>

                    <form id="profileOtpForm" action="${pageContext.request.contextPath}/profile" method="post" novalidate>
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <input type="hidden" name="action" value="verifyEmailOtp">

                        <div class="field-group mb-3">
                            <label for="otp">Mã OTP xác nhận</label>
                            <input id="otp"
                                   class="input-control ${not empty errors.otp ? 'is-invalid' : ''}"
                                   type="text"
                                   name="otp"
                                   inputmode="numeric"
                                   maxlength="6"
                                   placeholder="Nhập 6 chữ số OTP"
                                   style="letter-spacing: 0.25em; font-weight: 700; font-size: 16px;"
                                   required>
                            <div class="invalid-feedback ${not empty errors.otp ? 'd-block' : ''}"><c:out value="${errors.otp}" /></div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <button class="button button-primary" type="submit">
                                <i class="bi bi-patch-check"></i> Xác nhận đổi email
                            </button>
                        </div>
                    </form>

                    <div class="d-flex flex-wrap gap-2 mt-3 pt-3 border-top">
                        <form action="${pageContext.request.contextPath}/profile" method="post" style="margin:0;">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                            <input type="hidden" name="action" value="resendEmailOtp">
                            <button class="button button-secondary btn-sm" type="submit">
                                <i class="bi bi-arrow-repeat"></i> Gửi lại mã OTP
                            </button>
                        </form>
                        <form action="${pageContext.request.contextPath}/profile" method="post" style="margin:0;">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                            <input type="hidden" name="action" value="cancelEmailOtp">
                            <button class="button button-danger btn-sm" type="submit">
                                <i class="bi bi-x-circle"></i> Hủy đổi email
                            </button>
                        </form>
                    </div>
                </section>
            </c:if>
        </div>
    </div>
</main>

<script>
function previewProfileAvatar(input) {
    if (!input || !input.files || !input.files[0]) return;
    var file = input.files[0];
    if (file.size > 5 * 1024 * 1024) {
        window.ShopValidation.setError(input, 'Kích thước ảnh đại diện tối đa là 5MB.');
        return;
    }
    var reader = new FileReader();
    reader.onload = function (e) {
        var img = document.getElementById('avatarPreviewImg');
        var initials = document.getElementById('avatarInitials');
        if (img) {
            img.src = e.target.result;
            img.style.display = 'block';
        }
        if (initials) {
            initials.style.display = 'none';
        }
    };
    reader.readAsDataURL(file);
}

document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('profileForm', function (form) {
        var valid = true;
        var fullname = form.fullname.value.trim();
        var phone = form.phone.value.trim();
        var email = form.email.value.trim();
        var emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        var phoneDigits = phone.replace(/[\s.-]/g, '');

        if (!fullname) {
            window.ShopValidation.setError(form.fullname, 'Vui lòng nhập họ và tên.');
            valid = false;
        } else if (fullname.length < 2 || fullname.length > 100) {
            window.ShopValidation.setError(form.fullname, 'Họ và tên phải từ 2 đến 100 ký tự.');
            valid = false;
        }

        if (phone && !/^\+?[0-9]{9,15}$/.test(phoneDigits)) {
            window.ShopValidation.setError(form.phone, 'Số điện thoại không hợp lệ (gồm 9-15 chữ số).');
            valid = false;
        }

        if (!email) {
            window.ShopValidation.setError(form.email, 'Vui lòng nhập email.');
            valid = false;
        } else if (email.length > 100 || !emailRegex.test(email)) {
            window.ShopValidation.setError(form.email, 'Địa chỉ email không đúng định dạng.');
            valid = false;
        }

        if (form.images && form.images.files && form.images.files[0]) {
            var file = form.images.files[0];
            var allowed = /\.(jpg|jpeg|png|gif|svg|webp)$/i;
            if (!allowed.test(file.name)) {
                window.ShopValidation.setError(form.images, 'Chỉ hỗ trợ ảnh JPG, PNG, GIF, SVG hoặc WEBP.');
                valid = false;
            } else if (file.size > 5 * 1024 * 1024) {
                window.ShopValidation.setError(form.images, 'Kích thước ảnh đại diện tối đa là 5MB.');
                valid = false;
            }
        }
        return valid;
    });

    window.ShopValidation.attach('profileOtpForm', function (form) {
        var valid = true;
        var otp = form.otp.value.trim();
        if (!otp) {
            window.ShopValidation.setError(form.otp, 'Vui lòng nhập mã OTP gồm 6 chữ số.');
            valid = false;
        } else if (!/^[0-9]{6}$/.test(otp)) {
            window.ShopValidation.setError(form.otp, 'Mã OTP phải gồm đúng 6 chữ số.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
