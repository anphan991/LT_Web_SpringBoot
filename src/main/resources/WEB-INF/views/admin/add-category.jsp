<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thêm danh mục - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261119-v3">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />

    <div class="admin-workspace">

        <main class="admin-form-container">
            <section class="admin-form-card">
                <div class="admin-form-header">
                    <h1>Thêm danh mục</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}/admin/category/list">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <c:if test="${not empty alert}">
                <div class="admin-alert"><c:out value="${alert}" /></div>
                    </c:if>
                    <form id="addCategoryForm" action="${pageContext.request.contextPath}/admin/category/add?csrfToken=${sessionScope.csrfToken}" method="post" enctype="multipart/form-data" novalidate>
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <div class="admin-field">
                            <label for="catename">Tên danh mục</label>
                            <input id="catename" class="${not empty errors.catename ? 'is-invalid' : ''}" type="text" name="catename" value="<c:out value='${catename}' />" placeholder="Ví dụ: Phụ kiện thời trang" required maxlength="255">
                            <div class="invalid-feedback ${not empty errors.catename ? 'd-block' : ''}"><c:out value="${errors.catename}" /></div>
                        </div>

                        <div class="admin-field">
                            <label for="icon">Ảnh đại diện</label>
                            <div class="admin-upload">
                                <input id="icon" class="${not empty errors.icon ? 'is-invalid' : ''}" type="file" name="icon" accept="image/*" onchange="previewFile(this)">
                                <p class="admin-upload-hint">JPG, PNG, SVG hoặc WEBP (tối đa 5MB)</p>
                                <div class="invalid-feedback ${not empty errors.icon ? 'd-block' : ''}"><c:out value="${errors.icon}" /></div>
                                <img id="preview" class="admin-preview" alt="Xem trước ảnh">
                            </div>
                        </div>

                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-primary" type="submit">Lưu danh mục</button>
                            <button class="admin-button admin-button-secondary" type="reset" onclick="document.getElementById('preview').style.display='none'">Nhập lại</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/category/list">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>

<script>
function previewFile(input) {
    const preview = document.getElementById('preview');
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (event) {
            preview.src = event.target.result;
            preview.style.display = 'inline-block';
        };
        reader.readAsDataURL(input.files[0]);
    }
}
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('addCategoryForm', function (form) {
        var valid = true;
        var name = form.catename.value.trim();
        if (!name) {
            window.ShopValidation.setError(form.catename, 'Tên danh mục không được để trống.');
            valid = false;
        } else if (name.length < 2 || name.length > 255) {
            window.ShopValidation.setError(form.catename, 'Tên danh mục phải từ 2 đến 255 ký tự.');
            valid = false;
        }
        if (form.icon.files && form.icon.files[0]) {
            var file = form.icon.files[0];
            if (!/\.(jpg|jpeg|png|gif|svg|webp)$/i.test(file.name)) {
                window.ShopValidation.setError(form.icon, 'Chỉ hỗ trợ ảnh JPG, PNG, SVG, GIF hoặc WEBP.');
                valid = false;
            } else if (file.size > 5 * 1024 * 1024) {
                window.ShopValidation.setError(form.icon, 'Kích thước ảnh tối đa là 5MB.');
                valid = false;
            }
        }
        return valid;
    });
});
</script>
</body>
</html>
