<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/commons/taglib.jsp" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title">ShopSpringBoot Admin</sitemesh:write></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet" crossorigin="anonymous">
    <sitemesh:write property="head" />
    <%@ include file="/WEB-INF/commons/info.jsp" %>
</head>
<body class="<sitemesh:write property='body.class' /> d-flex flex-column min-vh-100">
<%@ include file="/WEB-INF/commons/admin/header.jsp" %>

<div class="sitemesh-main-wrap">
    <sitemesh:write property="body" />
</div>

<%@ include file="/WEB-INF/commons/admin/footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
</body>
</html>
