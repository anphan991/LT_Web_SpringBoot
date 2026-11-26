<%@ page pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/commons/taglib.jsp" %>
<div class="list-group list-group-flush">
    <a class="list-group-item list-group-item-action" href="${pageContext.request.contextPath}/">Trang chủ</a>
    <c:if test="${sessionScope.account != null}">
        <a class="list-group-item list-group-item-action" href="${pageContext.request.contextPath}/video">Video</a>
        <a class="list-group-item list-group-item-action" href="${pageContext.request.contextPath}/home">Danh mục</a>
    </c:if>
</div>
