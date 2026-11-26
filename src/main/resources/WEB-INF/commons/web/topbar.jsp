<%@ page pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/commons/taglib.jsp" %>
<div class="d-flex align-items-center gap-2">
    <c:choose>
        <c:when test="${sessionScope.account == null}">
            <a class="btn btn-outline-secondary btn-sm fw-semibold px-3 rounded-3" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            <a class="btn btn-primary btn-sm fw-semibold px-3 rounded-3 shadow-sm" href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </c:when>
        <c:otherwise>
            <a class="sitemesh-user-pill ${reqPath == '/profile' || reqPath == '/WEB-INF/views/profile.jsp' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/profile" title="Xem và cập nhật hồ sơ cá nhân">
                <span class="sitemesh-avatar">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account.avatar}">
                            <c:choose>
                                <c:when test="${sessionScope.account.avatar.startsWith('http://') || sessionScope.account.avatar.startsWith('https://')}">
                                    <img src="<c:out value='${sessionScope.account.avatar}' />" alt="Avatar">
                                </c:when>
                                <c:otherwise>
                                    <c:url value="/image" var="navAvatarUrl">
                                        <c:param name="fname" value="${sessionScope.account.avatar}" />
                                    </c:url>
                                    <img src="${navAvatarUrl}" alt="Avatar">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${sessionScope.account.userName.substring(0, 1)}" />
                        </c:otherwise>
                    </c:choose>
                </span>
                <span class="fw-semibold text-dark small"><c:out value="${not empty sessionScope.account.fullName ? sessionScope.account.fullName : sessionScope.account.userName}" /></span>
                <span class="badge rounded-pill bg-white text-secondary border">
                    <c:choose>
                        <c:when test="${sessionScope.account.roleid == 1}">Admin</c:when>
                        <c:when test="${sessionScope.account.roleid == 2}">Manager</c:when>
                        <c:otherwise>Member</c:otherwise>
                    </c:choose>
                </span>
            </a>
            <a class="btn btn-outline-danger btn-sm fw-semibold rounded-3" href="${pageContext.request.contextPath}/logout">
                <i class="bi bi-box-arrow-right"></i> Đăng xuất
            </a>
        </c:otherwise>
    </c:choose>
</div>
