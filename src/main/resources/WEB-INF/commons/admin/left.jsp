<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="origServletPath" value="${requestScope['jakarta.servlet.forward.servlet_path']}" />
<c:set var="reqPath" value="${not empty origServletPath ? origServletPath : pageContext.request.servletPath}" />

<aside class="admin-sidebar">
    <a class="admin-brand" href="${pageContext.request.contextPath}/admin/category/list"><span>ShopSpringBoot Admin</span></a>

    <div class="admin-profile d-flex align-items-center gap-3">
        <span class="sitemesh-avatar" style="width: 38px; height: 38px; flex: 0 0 38px;">
            <c:choose>
                <c:when test="${not empty sessionScope.account.avatar}">
                    <c:choose>
                        <c:when test="${sessionScope.account.avatar.startsWith('http://') || sessionScope.account.avatar.startsWith('https://')}">
                            <img src="<c:out value='${sessionScope.account.avatar}' />" alt="Avatar">
                        </c:when>
                        <c:otherwise>
                            <c:url value="/image" var="sideAvatarUrl">
                                <c:param name="fname" value="${sessionScope.account.avatar}" />
                            </c:url>
                            <img src="${sideAvatarUrl}" alt="Avatar">
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <c:out value="${not empty sessionScope.account ? sessionScope.account.userName.substring(0, 1) : 'A'}" />
                </c:otherwise>
            </c:choose>
        </span>
        <div class="min-w-0">
            <div class="admin-profile-name"><c:out value="${not empty sessionScope.account ? sessionScope.account.fullName : 'Admin'}" /></div>
            <div class="admin-profile-role">
                <c:choose>
                    <c:when test="${sessionScope.account.roleid == 1}">Quản trị viên</c:when>
                    <c:otherwise>Quản lý</c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <div class="admin-nav-label">Danh mục</div>
    <a class="admin-link ${reqPath == '/admin/category/list' || reqPath == '/admin/category/edit' || reqPath == '/admin/category/delete' || reqPath == '/WEB-INF/views/admin/list-category.jsp' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/category/list">
        <i class="bi bi-grid-1x2 me-2"></i>Danh mục sản phẩm
    </a>
    <a class="admin-link ${reqPath == '/admin/category/add' || reqPath == '/WEB-INF/views/admin/add-category.jsp' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/category/add">
        <i class="bi bi-plus-square me-2"></i>Thêm danh mục
    </a>

    <div class="admin-nav-label admin-nav-spacer">Tài khoản &amp; Video</div>
    <c:choose>
        <c:when test="${sessionScope.account.roleid == 1}">
            <a class="admin-link ${reqPath == '/admin/user/list' || reqPath == '/admin/user/edit' || reqPath == '/admin/user/delete' || reqPath == '/WEB-INF/views/admin/list-user.jsp' || reqPath == '/WEB-INF/views/admin/delete-user.jsp' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/user/list">
                <i class="bi bi-people me-2"></i>Quản lý Tài khoản
            </a>
            <a class="admin-link ${reqPath == '/admin/user/add' || (reqPath == '/WEB-INF/views/admin/user-form.jsp' && formMode == 'add') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/user/add?returnTo=users">
                <i class="bi bi-person-plus me-2"></i>Thêm tài khoản
            </a>
        </c:when>
        <c:otherwise>
            <a class="admin-link" href="${pageContext.request.contextPath}/admin/category/list#users-section">
                <i class="bi bi-people me-2"></i>Tài khoản
            </a>
        </c:otherwise>
    </c:choose>
    <a class="admin-link ${reqPath == '/admin/video/list' || reqPath == '/video' || reqPath == '/WEB-INF/views/video-list.jsp' ? 'active' : ''}" href="${pageContext.request.contextPath}/video">
        <i class="bi bi-collection-play me-2"></i>Quản lý Video
    </a>
    <a class="admin-link ${reqPath == '/admin/video/add' || reqPath == '/video/add' || reqPath == '/WEB-INF/views/video-form.jsp' ? 'active' : ''}" href="${pageContext.request.contextPath}/video/add">
        <i class="bi bi-camera-video me-2"></i>Thêm Video
    </a>

    <div class="admin-nav-label admin-nav-spacer">Tài khoản &amp; Nhanh</div>
    <a class="admin-link" href="${pageContext.request.contextPath}/profile">
        <i class="bi bi-person-badge me-2"></i>Hồ sơ cá nhân
    </a>
    <a class="admin-link" href="${pageContext.request.contextPath}/home">
        <i class="bi bi-box-seam me-2"></i>Danh mục
    </a>
    <a class="admin-link" href="${pageContext.request.contextPath}/">
        <i class="bi bi-house-door me-2"></i>Trang chủ
    </a>
</aside>
