<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xóa danh mục - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261119-v3">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />
    <div class="admin-workspace">

        <main class="admin-form-container">
            <section class="admin-form-card">
                <div class="admin-form-header">
                    <h1>Xóa danh mục</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}/admin/category/list">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <p>Bạn có chắc muốn xóa danh mục <strong><c:out value="${category.name}" /></strong>?</p>
                    <p class="admin-field-hint">Thao tác này không thể hoàn tác.</p>
                    <form action="${pageContext.request.contextPath}/admin/category/delete" method="post">
                        <input type="hidden" name="id" value="<c:out value='${category.cateid}' />">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-danger" type="submit">Xác nhận xóa</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/category/list">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
