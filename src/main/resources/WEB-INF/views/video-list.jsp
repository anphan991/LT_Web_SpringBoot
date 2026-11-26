<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách Video - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
    <style>
        .video-toolbar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            margin-bottom: 24px;
            padding: 12px 16px;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
        }
        .video-tabs {
            display: inline-flex;
            gap: 6px;
            background: #e2e8f0;
            padding: 4px;
            border-radius: 8px;
        }
        .video-tab {
            padding: 8px 18px;
            font-size: 13.5px;
            font-weight: 600;
            color: #475569;
            text-decoration: none;
            border-radius: 6px;
            transition: all .15s ease;
        }
        .video-tab:hover {
            color: #0f172a;
        }
        .video-tab.active {
            background: #ffffff;
            color: #1e40af;
            box-shadow: 0 1px 3px rgba(15, 23, 42, .12);
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
        .video-badge-status {
            position: absolute;
            left: 10px;
            top: 10px;
            padding: 4px 9px;
            font-size: 11.5px;
            font-weight: 600;
            color: #ffffff;
            background: rgba(220, 38, 38, .88);
            border-radius: 4px;
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
            flex-wrap: wrap;
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
        .site-pagination {
            margin-top: 32px;
            padding-top: 20px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 14px;
        }
        .site-pagination-info {
            color: #64748b;
            font-size: 13.5px;
            font-weight: 500;
        }
        .site-pagination-links {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }
        .site-page-link {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 38px;
            height: 38px;
            padding: 0 12px;
            font-size: 13.5px;
            font-weight: 600;
            color: #334155;
            background: #ffffff;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            text-decoration: none;
            transition: all .15s ease;
        }
        .site-page-link:hover {
            border-color: #1e40af;
            color: #1e40af;
            background: #f8fafc;
        }
        .site-page-link.active {
            background: #1e40af;
            border-color: #1e40af;
            color: #ffffff;
        }
    </style>
</head>
<body>

<main class="page-container member-main">
    <div class="section-heading">
        <div>
            <h1>Thư viện Video</h1>
            <p>Hiển thị 6 video / trang - bấm vào tiêu đề để xem chi tiết và phát video</p>
        </div>
        <a class="button button-primary" href="${pageContext.request.contextPath}/video/add">+ Thêm video mới</a>
    </div>

    <c:if test="${param.saved == '1'}">
        <div class="alert alert-success">Đã thêm video mới thành công.</div>
    </c:if>
    <c:if test="${param.updated == '1'}">
        <div class="alert alert-success">Đã cập nhật thông tin video.</div>
    </c:if>
    <c:if test="${param.deleted == '1'}">
        <div class="alert alert-success">Đã xóa video thành công.</div>
    </c:if>
    <c:if test="${param.videoError == 'missing'}">
        <div class="alert alert-danger">Video không tồn tại hoặc đã bị xóa.</div>
    </c:if>
    <c:if test="${param.videoError == 'forbidden'}">
        <div class="alert alert-danger">Bạn không có quyền chỉnh sửa hoặc xóa video của người khác.</div>
    </c:if>
    <c:if test="${param.videoError == 'load' || param.videoError == 'delete'}">
        <div class="alert alert-danger">Không thể thực hiện thao tác với cơ sở dữ liệu lúc này.</div>
    </c:if>
    <c:if test="${not empty dbError}">
        <div class="alert alert-danger"><c:out value="${dbError}" /></div>
    </c:if>
    <c:if test="${showingAllFallback}">
        <div class="alert alert-warning">Tài khoản của bạn chưa đăng video nào.</div>
    </c:if>

    <div class="video-toolbar">
        <div class="video-tabs" role="tablist" aria-label="Bộ lọc video">
            <c:if test="${not empty sessionScope.account}">
                <c:url value="/video" var="mineUrl">
                    <c:param name="scope" value="mine" />
                    <c:if test="${not empty keyword}">
                        <c:param name="keyword" value="${keyword}" />
                    </c:if>
                </c:url>
                <a class="video-tab ${scope == 'mine' ? 'active' : ''}" href="${mineUrl}">Video của tôi</a>
            </c:if>
            <c:url value="/video" var="allUrl">
                <c:param name="scope" value="all" />
                <c:if test="${not empty keyword}">
                    <c:param name="keyword" value="${keyword}" />
                </c:if>
            </c:url>
            <a class="video-tab ${scope == 'all' || scope == 'user' ? 'active' : ''}" href="${allUrl}">Tất cả video</a>
        </div>

        <form class="search-form" action="${pageContext.request.contextPath}/video" method="get">
            <c:if test="${not empty scope}">
                <input type="hidden" name="scope" value="<c:out value='${scope}' />">
            </c:if>
            <c:if test="${not empty selectedUserId && scope == 'user'}">
                <input type="hidden" name="uid" value="<c:out value='${selectedUserId}' />">
            </c:if>
            <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm tiêu đề video..." aria-label="Tìm tiêu đề video">
            <button class="button button-primary" type="submit">Tìm kiếm</button>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty videoList}">
            <div class="empty-state">
                <p>Chưa có video nào phù hợp.</p>
                <c:if test="${not empty sessionScope.account}">
                    <a class="button button-primary" style="margin-top: 12px;" href="${pageContext.request.contextPath}/video/add">Tải lên video đầu tiên</a>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="video-grid">
                <c:forEach items="${videoList}" var="v">
                    <c:url value="/video/detail" var="detailUrl">
                        <c:param name="id" value="${v.videoId}" />
                    </c:url>
                    <c:url value="/video/edit" var="editUrl">
                        <c:param name="id" value="${v.videoId}" />
                    </c:url>
                    <article class="video-card">
                        <a class="video-thumb" href="${detailUrl}">
                            <c:choose>
                                <c:when test="${v.customPoster}">
                                    <c:url value="/image" var="posterUrl">
                                        <c:param name="fname" value="${v.poster}" />
                                    </c:url>
                                    <img src="${v.externalPoster ? v.poster : posterUrl}" alt="<c:out value='${v.title}' />">
                                </c:when>
                                <c:when test="${v.youtubeVideo}">
                                    <img src="${v.youtubeThumbnailUrl}" alt="<c:out value='${v.title}' />">
                                </c:when>
                                <c:when test="${!v.sampleVideo}">
                                    <c:url value="/video-file" var="thumbVideoUrl">
                                        <c:param name="fname" value="${v.videoPath}" />
                                    </c:url>
                                    <video src="${v.externalVideo ? v.videoPath : thumbVideoUrl}#t=0.5" preload="metadata" muted playsinline></video>
                                </c:when>
                                <c:otherwise>
                                    <c:url value="/image" var="posterUrl">
                                        <c:param name="fname" value="${not empty v.poster ? v.poster : 'default-video-poster.svg'}" />
                                    </c:url>
                                    <img src="${posterUrl}" alt="<c:out value='${v.title}' />">
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${v.active == 0}">
                                <span class="video-badge-status">Đang ẩn</span>
                            </c:if>
                            <span class="video-badge-views">${v.views} lượt xem</span>
                        </a>
                        <div class="video-card-body">
                            <a class="video-title-link" href="${detailUrl}"><c:out value="${v.title}" /></a>
                            <div class="video-meta">
                                <span>Đăng bởi: <strong><c:out value="${v.user != null ? v.user.fullName : 'Thành viên'}" /></strong></span>
                                <span><c:out value="${v.formattedCreatedAt}" /></span>
                            </div>
                            <c:if test="${not empty v.description}">
                                <p class="video-desc"><c:out value="${v.description}" /></p>
                            </c:if>
                            <div class="video-card-actions">
                                <a class="video-btn-sm primary" href="${detailUrl}">Phát video</a>
                                <c:if test="${not empty sessionScope.account && (sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2 || (v.user != null && v.user.id == sessionScope.account.id))}">
                                    <a class="video-btn-sm" href="${editUrl}">Sửa / Đổi video</a>
                                    <form class="video-inline-form" action="${pageContext.request.contextPath}/video/delete" method="post" onsubmit="return confirm('Bạn có chắc muốn xóa video này không?');">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                        <input type="hidden" name="id" value="${v.videoId}">
                                        <button class="video-btn-sm danger" type="submit">Xóa</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${totalPages > 1}">
        <nav class="site-pagination" aria-label="Phân trang video">
            <span class="site-pagination-info">Trang ${currentPage} / ${totalPages} (${totalVideos} video)</span>
            <div class="site-pagination-links">
                <c:if test="${currentPage > 1}">
                    <c:url value="/video" var="prevUrl">
                        <c:param name="page" value="${currentPage - 1}" />
                        <c:if test="${not empty scope}">
                            <c:param name="scope" value="${scope}" />
                        </c:if>
                        <c:if test="${not empty selectedUserId && scope == 'user'}">
                            <c:param name="uid" value="${selectedUserId}" />
                        </c:if>
                        <c:if test="${not empty keyword}">
                            <c:param name="keyword" value="${keyword}" />
                        </c:if>
                    </c:url>
                    <a class="site-page-link" href="${prevUrl}">Trước</a>
                </c:if>

                <c:forEach begin="1" end="${totalPages}" var="p">
                    <c:url value="/video" var="pageUrl">
                        <c:param name="page" value="${p}" />
                        <c:if test="${not empty scope}">
                            <c:param name="scope" value="${scope}" />
                        </c:if>
                        <c:if test="${not empty selectedUserId && scope == 'user'}">
                            <c:param name="uid" value="${selectedUserId}" />
                        </c:if>
                        <c:if test="${not empty keyword}">
                            <c:param name="keyword" value="${keyword}" />
                        </c:if>
                    </c:url>
                    <a class="site-page-link ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                </c:forEach>

                <c:if test="${currentPage < totalPages}">
                    <c:url value="/video" var="nextUrl">
                        <c:param name="page" value="${currentPage + 1}" />
                        <c:if test="${not empty scope}">
                            <c:param name="scope" value="${scope}" />
                        </c:if>
                        <c:if test="${not empty selectedUserId && scope == 'user'}">
                            <c:param name="uid" value="${selectedUserId}" />
                        </c:if>
                        <c:if test="${not empty keyword}">
                            <c:param name="keyword" value="${keyword}" />
                        </c:if>
                    </c:url>
                    <a class="site-page-link" href="${nextUrl}">Sau</a>
                </c:if>
            </div>
        </nav>
    </c:if>
</main>
</body>
</html>
