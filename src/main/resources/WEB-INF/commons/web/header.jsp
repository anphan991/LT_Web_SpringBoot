<%@ page pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/commons/taglib.jsp" %>
<c:set var="origServletPath" value="${requestScope['jakarta.servlet.forward.servlet_path']}" />
<c:set var="reqPath" value="${not empty origServletPath ? origServletPath : pageContext.request.servletPath}" />
<c:set var="isHomePage" value="${requestScope.isIndexPage == true || reqPath == '/' || reqPath == '' || reqPath == '/index' || reqPath == '/index.jsp' || reqPath == '/WEB-INF/views/index.jsp'}" />
<nav class="navbar navbar-expand-lg navbar-light sitemesh-navbar sticky-top">
    <div class="container-xl">
        <a class="navbar-brand d-flex align-items-center gap-2 fw-bold text-dark" href="${pageContext.request.contextPath}/">
            <span class="sitemesh-brand-mark" aria-hidden="true"><i class="bi bi-layers-fill"></i></span>
            <span style="letter-spacing: -0.02em;">ShopSpringBoot</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#shopNavbar"
                aria-controls="shopNavbar" aria-expanded="false" aria-label="Mở menu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="shopNavbar">
            <%@ include file="/WEB-INF/commons/web/navbar.jsp" %>
            <%@ include file="/WEB-INF/commons/web/topbar.jsp" %>
        </div>
    </div>
</nav>
