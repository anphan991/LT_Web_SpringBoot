<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${video.title}" /> - ShopSpringBoot Video</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
    <style>
        .video-detail-layout {
            display: grid;
            grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
            gap: 24px;
            align-items: start;
        }
        .video-player-card,
        .video-sidebar-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(15, 23, 42, .04);
        }
        .video-player-wrap {
            width: 100%;
            aspect-ratio: 16 / 9;
            background: #0f172a;
            border-radius: 6px;
            margin-bottom: 20px;
            overflow: hidden;
        }
        .video-player-wrap video,
        .video-player-wrap iframe {
            width: 100%;
            height: 100%;
            border: 0;
            display: block;
        }
        .video-detail-title {
            margin: 0 0 12px;
            font-size: 24px;
            font-weight: 700;
            line-height: 1.35;
            color: #0f172a;
        }
        .video-detail-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 8px 18px;
            padding-bottom: 16px;
            margin-bottom: 18px;
            border-bottom: 1px solid #f1f5f9;
            color: #64748b;
            font-size: 13.5px;
        }
        .video-detail-desc {
            white-space: pre-line;
            color: #334155;
            font-size: 14.5px;
            line-height: 1.65;
            margin: 0 0 20px;
        }
        .video-related-list {
            display: flex;
            flex-direction: column;
            gap: 14px;
        }
        .video-related-item {
            display: grid;
            grid-template-columns: 120px minmax(0, 1fr);
            gap: 12px;
            text-decoration: none;
            color: #0f172a;
            padding-bottom: 12px;
            border-bottom: 1px solid #f1f5f9;
            transition: opacity .15s ease;
        }
        .video-related-item:last-child {
            border-bottom: 0;
            padding-bottom: 0;
        }
        .video-related-item:hover strong {
            color: #1e40af;
        }
        .video-related-item img,
        .video-related-item video {
            width: 120px;
            height: 68px;
            object-fit: cover;
            border-radius: 4px;
            background: #172033;
        }
        .video-related-item strong {
            display: block;
            font-size: 13.5px;
            line-height: 1.4;
            margin-bottom: 4px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .video-related-item span {
            font-size: 12px;
            color: #64748b;
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
        @media (max-width: 900px) {
            .video-detail-layout {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>

<main class="page-container member-main">
    <div style="margin-bottom: 16px;">
        <a class="video-btn-sm" href="${pageContext.request.contextPath}/video">&larr; Quay lại danh sách video</a>
    </div>

    <c:if test="${param.updated == '1'}">
        <div class="alert alert-success">Đã cập nhật thông tin video thành công.</div>
    </c:if>

    <div class="video-detail-layout">
        <section class="video-player-card" aria-labelledby="detail-video-title">
            <c:url value="/image" var="posterUrl">
                <c:param name="fname" value="${not empty video.poster ? video.poster : 'default-video-poster.svg'}" />
            </c:url>
            <c:set var="resolvedPosterUrl" value="${video.externalPoster ? video.poster : posterUrl}" />

            <div class="video-player-wrap">
                <c:choose>
                    <c:when test="${video.youtubeVideo}">
                        <iframe src="${video.youtubeEmbedUrl}?autoplay=1"
                                title="<c:out value='${video.title}' />"></iframe>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${video.externalVideo}">
                                <c:set var="videoSrc" value="${video.videoPath}" />
                            </c:when>
                            <c:otherwise>
                                <c:url value="/video-file" var="videoSrc">
                                    <c:param name="fname" value="${video.videoPath}" />
                                </c:url>
                            </c:otherwise>
                        </c:choose>
                        <video id="videoPlayer"
                               controls
                               autoplay
                               preload="metadata"
                               poster="${resolvedPosterUrl}"
                               data-sample="${video.sampleVideo}"
                               data-title="<c:out value='${video.title}' />">
                            <c:if test="${!video.sampleVideo}">
                                <source src="${videoSrc}" type="video/mp4">
                            </c:if>
                        </video>
                    </c:otherwise>
                </c:choose>
            </div>

            <h1 id="detail-video-title" class="video-detail-title"><c:out value="${video.title}" /></h1>

            <div class="video-detail-meta">
                <span><strong>${video.views}</strong> lượt xem</span>
                <span>Đăng bởi: <strong><c:out value="${video.user != null ? video.user.fullName : 'Thành viên'}" /></strong></span>
                <span>Ngày đăng: <c:out value="${video.formattedCreatedAt}" /></span>
                <c:if test="${video.active == 0}">
                    <span style="color: var(--danger); font-weight: 600;">Trạng thái: Đang ẩn</span>
                </c:if>
            </div>

            <p class="video-detail-desc"><c:out value="${not empty video.description ? video.description : 'Chưa có mô tả chi tiết cho video này.'}" /></p>

            <c:if test="${canManage}">
                <c:url value="/video/edit" var="editUrl">
                    <c:param name="id" value="${video.videoId}" />
                </c:url>
                <div style="display: flex; gap: 10px; padding-top: 14px; border-top: 1px solid #edf0f4;">
                    <a class="video-btn-sm" href="${editUrl}">Sửa / Đổi video</a>
                    <form class="video-inline-form" action="${pageContext.request.contextPath}/video/delete" method="post" onsubmit="return confirm('Bạn có chắc muốn xóa video này không?');">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <input type="hidden" name="id" value="${video.videoId}">
                        <button class="video-btn-sm danger" type="submit">Xóa video</button>
                    </form>
                </div>
            </c:if>
        </section>

        <aside class="video-sidebar-card" aria-labelledby="related-videos-heading">
            <h2 id="related-videos-heading" style="margin: 0 0 16px; font-size: 18px;">Video mới nhất</h2>
            <div class="video-related-list">
                <c:forEach items="${relatedVideos}" var="rv">
                    <c:if test="${rv.videoId != video.videoId}">
                        <c:url value="/video/detail" var="relDetailUrl">
                            <c:param name="id" value="${rv.videoId}" />
                        </c:url>
                        <a class="video-related-item" href="${relDetailUrl}">
                            <c:choose>
                                <c:when test="${rv.customPoster}">
                                    <c:url value="/image" var="relPosterUrl">
                                        <c:param name="fname" value="${rv.poster}" />
                                    </c:url>
                                    <img src="${rv.externalPoster ? rv.poster : relPosterUrl}" alt="<c:out value='${rv.title}' />">
                                </c:when>
                                <c:when test="${rv.youtubeVideo}">
                                    <img src="${rv.youtubeThumbnailUrl}" alt="<c:out value='${rv.title}' />">
                                </c:when>
                                <c:when test="${!rv.sampleVideo}">
                                    <c:url value="/video-file" var="relThumbVideoUrl">
                                        <c:param name="fname" value="${rv.videoPath}" />
                                    </c:url>
                                    <video src="${rv.externalVideo ? rv.videoPath : relThumbVideoUrl}#t=0.5" preload="metadata" muted playsinline></video>
                                </c:when>
                                <c:otherwise>
                                    <c:url value="/image" var="relPosterUrl">
                                        <c:param name="fname" value="${not empty rv.poster ? rv.poster : 'default-video-poster.svg'}" />
                                    </c:url>
                                    <img src="${relPosterUrl}" alt="<c:out value='${rv.title}' />">
                                </c:otherwise>
                            </c:choose>
                            <span style="display:block;">
                                <strong><c:out value="${rv.title}" /></strong>
                                <span><c:out value="${rv.user != null ? rv.user.fullName : 'Thành viên'}" /> - ${rv.views} lượt xem</span>
                            </span>
                        </a>
                    </c:if>
                </c:forEach>
            </div>
        </aside>
    </div>
</main>

<script>
(function () {
    const player = document.getElementById('videoPlayer');
    if (!player) return;
    player.playsInline = true;

    function startSampleCanvasStream() {
        if (!HTMLCanvasElement.prototype.captureStream) return;
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 360;
        const ctx = canvas.getContext('2d');
        const title = player.getAttribute('data-title') || 'ShopSpringBoot Demo Video';
        let start = performance.now();

        function render(now) {
            const sec = ((now - start) / 1000).toFixed(1);
            const grad = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
            grad.addColorStop(0, '#0f172a');
            grad.addColorStop(1, '#2358c4');
            ctx.fillStyle = grad;
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            const x = (Math.sin(now / 700) * 140) + canvas.width / 2;
            ctx.fillStyle = 'rgba(255, 255, 255, 0.14)';
            ctx.beginPath();
            ctx.arc(x, 180, 64, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = '#ffffff';
            ctx.textAlign = 'center';
            ctx.font = 'bold 20px sans-serif';
            ctx.fillText(title.slice(0, 44), canvas.width / 2, 165);
            ctx.font = '14px monospace';
            ctx.fillStyle = '#cbd5e1';
            ctx.fillText('ShopSpringBoot Video Preview - ' + sec + 's', canvas.width / 2, 200);
            requestAnimationFrame(render);
        }

        requestAnimationFrame(render);
        player.srcObject = canvas.captureStream(24);
        player.muted = true;
        player.play().catch(function () {});
    }

    if (player.getAttribute('data-sample') === 'true') {
        startSampleCanvasStream();
    } else {
        player.addEventListener('error', startSampleCanvasStream);
    }
})();
</script>
</body>
</html>
