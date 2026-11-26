<%@ page pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/commons/taglib.jsp" %>
<ul class="navbar-nav me-auto mb-2 mb-lg-0 gap-1 ms-lg-3">
    <c:if test="${not isHomePage}">
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link"
               href="${pageContext.request.contextPath}/">
                <i class="bi bi-house-door"></i>
                <span>Trang chủ</span>
            </a>
        </li>
    </c:if>
    <c:if test="${sessionScope.account != null}">
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath.startsWith('/video') || reqPath.startsWith('/WEB-INF/views/video') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/video">
                <i class="bi bi-collection-play"></i>
                <span>Video</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath == '/home' || reqPath == '/WEB-INF/views/home.jsp' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/home">
                <i class="bi bi-grid"></i>
                <span>Danh mục</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath == '/profile' || reqPath == '/WEB-INF/views/profile.jsp' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/profile">
                <i class="bi bi-person-circle"></i>
                <span>Hồ sơ</span>
            </a>
        </li>
    </c:if>
    <c:if test="${sessionScope.account != null && (sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2)}">
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath.startsWith('/admin') || reqPath.startsWith('/WEB-INF/views/admin') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/admin/category/list">
                <i class="bi bi-shield-check"></i>
                <span>Quản trị</span>
            </a>
        </li>
    </c:if>
</ul>
