<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="backListUrl" value="${returnTo == 'users' ? '/admin/user/list' : '/admin/category/list#users-section'}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xóa tài khoản - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261128-v1">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />
    <div class="admin-workspace">

        <main class="admin-form-container">
            <section class="admin-form-card">
                <div class="admin-form-header">
                    <h1>Xóa tài khoản</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}${backListUrl}">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <p>
                        Bạn có chắc muốn xóa tài khoản
                        <strong><c:out value="${user.userName}" /></strong>
                        (<c:out value="${user.fullName}" />)?
                    </p>
                    <c:if test="${not empty user.email}">
                        <p class="admin-field-hint">Email: <c:out value="${user.email}" /></p>
                    </c:if>
                    <p class="admin-field-hint">Thao tác này không thể hoàn tác và sẽ xóa các video / token liên quan của tài khoản.</p>
                    <form action="${pageContext.request.contextPath}/admin/user/delete" method="post">
                        <input type="hidden" name="id" value="<c:out value='${user.id}' />">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <c:if test="${not empty returnTo}">
                            <input type="hidden" name="returnTo" value="<c:out value='${returnTo}' />">
                        </c:if>
                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-danger" type="submit">Xác nhận xóa</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}${backListUrl}">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
