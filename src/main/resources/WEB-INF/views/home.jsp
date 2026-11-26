<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh mục - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v5">
    <style>
        .video-section {
            margin-top: 48px;
            padding-bottom: 40px;
            border-top: 1px solid #e2e8f0;
            padding-top: 36px;
        }
        .video-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 22px;
        }
        .video-card {
            display: flex;
            flex-direction: column;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(15, 23, 42, .05);
            transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
        }
        .video-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 22px rgba(15, 23, 42, .09);
            border-color: #cbd5e1;
        }
        .video-thumb {
            position: relative;
            display: block;
            aspect-ratio: 16 / 9;
            width: 100%;
            overflow: hidden;
            background: #0f172a;
        }
        .video-thumb img,
        .video-thumb video {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform .3s ease;
        }
        .video-card:hover .video-thumb img,
        .video-card:hover .video-thumb video {
            transform: scale(1.04);
        }
        .video-badge-views {
            position: absolute;
            right: 10px;
            bottom: 10px;
            padding: 4px 9px;
            font-size: 12px;
            font-weight: 600;
            color: #ffffff;
            background: rgba(15, 23, 42, .78);
            border-radius: 4px;
            backdrop-filter: blur(4px);
            pointer-events: none;
        }
        .video-card-body {
            flex: 1;
            display: flex;
            flex-direction: column;
            padding: 16px 18px 18px;
        }
        .video-title-link {
            font-size: 16px;
            font-weight: 700;
            line-height: 1.4;
            color: #0f172a;
            text-decoration: none;
            margin-bottom: 8px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            transition: color .15s ease;
        }
        .video-title-link:hover {
            color: #1e40af;
            text-decoration: underline;
        }
        .video-meta {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            gap: 6px;
            font-size: 12.5px;
            color: #64748b;
            margin-bottom: 10px;
        }
        .video-desc {
            font-size: 13.5px;
            line-height: 1.5;
            color: #475569;
            margin: 0 0 14px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .video-card-actions {
            margin-top: auto;
            padding-top: 12px;
            border-top: 1px solid #f1f5f9;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .video-btn-sm {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 32px;
            padding: 0 12px;
            font-size: 13px;
            font-weight: 600;
            border-radius: 6px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            color: #334155;
            text-decoration: none;
            cursor: pointer;
            transition: all .15s ease;
        }
        .video-btn-sm:hover {
            background: #f8fafc;
            border-color: #94a3b8;
        }
        .video-btn-sm.primary {
            background: #1e40af;
            border-color: #1e40af;
            color: #ffffff;
        }
        .video-btn-sm.primary:hover {
            background: #1d4ed8;
        }
        .video-btn-sm.danger {
            color: #dc2626;
            border-color: #fecaca;
            background: #fef2f2;
        }
        .video-btn-sm.danger:hover {
            background: #fee2e2;
        }
        .video-inline-form {
            display: inline;
            margin: 0;
        }
    </style>
</head>
<body>

<main class="page-container member-main">
    <c:if test="${param.forbidden == '1'}">
        <div class="member-alert">Tài khoản thành viên không có quyền truy cập khu vực quản trị.</div>
    </c:if>

    <section class="member-intro" aria-labelledby="member-title">
        <div class="member-intro-content">
            <div class="avatar member-avatar">
                <c:choose>
                    <c:when test="${not empty sessionScope.account.avatar}">
                        <c:choose>
                            <c:when test="${sessionScope.account.avatar.startsWith('http://') || sessionScope.account.avatar.startsWith('https://')}">
                                <img src="<c:out value='${sessionScope.account.avatar}' />" alt="Avatar">
                            </c:when>
                            <c:otherwise>
                                <c:url value="/image" var="memberAvatarUrl">
                                    <c:param name="fname" value="${sessionScope.account.avatar}" />
                                </c:url>
                                <img src="${memberAvatarUrl}" alt="Avatar">
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${not empty sessionScope.account ? sessionScope.account.userName.substring(0, 1) : 'U'}" />
                    </c:otherwise>
                </c:choose>
            </div>
            <div>
                <h1 id="member-title">Xin chào, <c:out value="${not empty sessionScope.account ? sessionScope.account.fullName : sessionScope.username}" /></h1>
                <div class="member-meta">
                    <span><strong>Tài khoản:</strong> <c:out value="${sessionScope.account.userName}" /></span>
                    <span>
                        <strong>Vai trò:</strong>
                        <c:choose>
                            <c:when test="${sessionScope.account.roleid == 1}">Quản trị viên</c:when>
                            <c:when test="${sessionScope.account.roleid == 2}">Quản lý</c:when>
                            <c:otherwise>Thành viên</c:otherwise>
                        </c:choose>
                    </span>
                    <c:if test="${not empty sessionScope.account.email}">
                        <span><strong>Email:</strong> <c:out value="${sessionScope.account.email}" /></span>
                    </c:if>
                </div>
            </div>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <a class="button button-secondary" href="${pageContext.request.contextPath}/profile">
                <i class="bi bi-person-gear"></i> Cập nhật hồ sơ
            </a>
            <c:if test="${sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2}">
                <a class="button button-primary" href="${pageContext.request.contextPath}/admin/category/list">
                    <i class="bi bi-speedometer2"></i> Mở trang quản trị
                </a>
            </c:if>
        </div>
    </section>

    <section aria-labelledby="catalog-title">
        <div class="section-heading member-catalog-heading">
            <div>
                <h2 id="catalog-title">Danh mục sản phẩm</h2>
                <p><c:out value="${cateList.size()}" /> danh mục đang được hiển thị</p>
            </div>
            <form class="search-form" action="${pageContext.request.contextPath}/home" method="get">
                <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm danh mục" aria-label="Tìm danh mục">
                <button class="button button-primary" type="submit">Tìm kiếm</button>
            </form>
        </div>

        <c:if test="${not empty dbError}">
            <div class="member-alert"><c:out value="${dbError}" /></div>
        </c:if>

        <c:choose>
            <c:when test="${empty cateList}">
                <div class="empty-state">Chưa có danh mục phù hợp với tìm kiếm của bạn.</div>
            </c:when>
            <c:otherwise>
                <div class="catalog-grid">
                    <c:forEach items="${cateList}" var="cate">
                        <c:url value="/image" var="imgUrl">
                            <c:param name="fname" value="${cate.icon}" />
                        </c:url>
                        <article class="catalog-card">
                            <div class="catalog-image">
                                <img src="${cate.externalIcon ? cate.icon : imgUrl}" alt="<c:out value='${cate.catename}' />">
                            </div>
                            <div class="catalog-card-body">
                                <h3><c:out value="${cate.catename}" /></h3>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
