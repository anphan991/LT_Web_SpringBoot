<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isIndexPage" value="true" scope="request" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ShopSpringBoot - Danh mục sản phẩm</title>
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
<body class="${empty sessionScope.account ? 'landing-page' : ''}">

<c:if test="${empty sessionScope.account}">
    <section class="home-hero">
        <div class="home-hero-inner">
            <div class="hero-layout">
                <div>
                    <span class="eyebrow"><i class="bi bi-layers-fill"></i> ShopSpringBoot</span>
                    <h1>Bài tập lập trình Web.</h1>
                    <p>Quản lý danh mục, thư viện video và hồ sơ tài khoản tập trung trên một nền tảng.</p>

                    <div class="hero-actions">
                        <a class="button button-primary" href="<c:url value='/login'/>"><i class="bi bi-box-arrow-in-right"></i> Đăng nhập</a>
                        <a class="button button-secondary" href="<c:url value='/register'/>"><i class="bi bi-person-plus"></i> Tạo tài khoản</a>
                    </div>
                </div>

                <div class="hero-visual-art" aria-hidden="true">
                    <div class="hero-visual-glow"></div>
                    <svg class="hero-visual-svg" viewBox="0 0 460 340" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <linearGradient id="glassCard" x1="0" y1="0" x2="460" y2="340" gradientUnits="userSpaceOnUse">
                                <stop offset="0%" stop-color="#1E293B" stop-opacity="0.88"/>
                                <stop offset="100%" stop-color="#0F172A" stop-opacity="0.94"/>
                            </linearGradient>
                            <linearGradient id="accentGrad" x1="0" y1="0" x2="1" y2="1">
                                <stop offset="0%" stop-color="#38BDF8"/>
                                <stop offset="100%" stop-color="#6366F1"/>
                            </linearGradient>
                            <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stop-color="#60A5FA"/>
                                <stop offset="100%" stop-color="#2563EB" stop-opacity="0.25"/>
                            </linearGradient>
                        </defs>

                        <rect x="34" y="28" width="368" height="260" rx="22" fill="url(#glassCard)" stroke="rgba(148,163,184,0.24)" stroke-width="1.5"/>

                        <rect x="58" y="54" width="84" height="10" rx="5" fill="#3B82F6" fill-opacity="0.65"/>
                        <rect x="308" y="52" width="66" height="14" rx="7" fill="rgba(255,255,255,0.1)"/>

                        <rect x="58" y="84" width="150" height="86" rx="14" fill="rgba(255,255,255,0.04)" stroke="rgba(148,163,184,0.16)"/>
                        <circle cx="88" cy="114" r="14" fill="url(#accentGrad)" fill-opacity="0.85"/>
                        <rect x="74" y="138" width="96" height="8" rx="4" fill="rgba(255,255,255,0.25)"/>
                        <rect x="74" y="152" width="62" height="6" rx="3" fill="rgba(255,255,255,0.12)"/>

                        <rect x="224" y="84" width="150" height="86" rx="14" fill="rgba(255,255,255,0.04)" stroke="rgba(148,163,184,0.16)"/>
                        <rect x="244" y="136" width="14" height="22" rx="4" fill="url(#barGrad)"/>
                        <rect x="266" y="122" width="14" height="36" rx="4" fill="url(#barGrad)"/>
                        <rect x="288" y="106" width="14" height="52" rx="4" fill="url(#accentGrad)"/>
                        <rect x="310" y="118" width="14" height="40" rx="4" fill="url(#barGrad)"/>
                        <rect x="332" y="98" width="14" height="60" rx="4" fill="url(#accentGrad)"/>

                        <rect x="58" y="186" width="316" height="76" rx="14" fill="rgba(255,255,255,0.03)" stroke="rgba(148,163,184,0.14)"/>
                        <path d="M74 238 C116 202, 162 246, 214 214 C258 188, 304 228, 356 200" stroke="url(#accentGrad)" stroke-width="3.5" stroke-linecap="round"/>
                        <circle cx="214" cy="214" r="5" fill="#38BDF8"/>
                        <circle cx="356" cy="200" r="6" fill="#818CF8"/>

                        <rect x="316" y="226" width="114" height="86" rx="18" fill="#172554" stroke="#60A5FA" stroke-opacity="0.45" stroke-width="1.5"/>
                        <circle cx="373" cy="260" r="18" fill="url(#accentGrad)"/>
                        <path d="M369 253 L381 260 L369 267 Z" fill="#FFFFFF"/>
                        <rect x="342" y="288" width="62" height="7" rx="3.5" fill="rgba(255,255,255,0.35)"/>
                    </svg>
                </div>
            </div>
        </div>
    </section>
</c:if>

<c:if test="${not empty sessionScope.account}">
    <main class="page-container dashboard-shell">
        <section class="dashboard-hero-card">
            <div class="dashboard-hero-user">
                <div class="dashboard-hero-avatar">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account.avatar}">
                            <c:choose>
                                <c:when test="${sessionScope.account.avatar.startsWith('http://') || sessionScope.account.avatar.startsWith('https://')}">
                                    <img src="<c:out value='${sessionScope.account.avatar}' />" alt="Avatar">
                                </c:when>
                                <c:otherwise>
                                    <c:url value="/image" var="dashAvatarUrl">
                                        <c:param name="fname" value="${sessionScope.account.avatar}" />
                                    </c:url>
                                    <img src="${dashAvatarUrl}" alt="Avatar">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${sessionScope.account.userName.substring(0, 1)}" />
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <h1>Xin chào, <c:out value="${not empty sessionScope.account.fullName ? sessionScope.account.fullName : sessionScope.account.userName}" /></h1>
                    <p>Bảng điều khiển tổng quan - Truy cập nhanh mọi khu vực trong hệ thống.</p>
                </div>
            </div>

            <div class="d-flex flex-wrap gap-2">
                <a class="button button-secondary" href="<c:url value='/profile'/>"><i class="bi bi-person-gear"></i> Hồ sơ cá nhân</a>
                <a class="button button-primary" href="<c:url value='/video/add'/>"><i class="bi bi-plus-lg"></i> Đăng video mới</a>
                <c:if test="${sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2}">
                    <a class="button button-secondary" href="<c:url value='/admin/category/list'/>"><i class="bi bi-speedometer2"></i> Quản trị</a>
                </c:if>
            </div>
        </section>

        <section class="dashboard-kpi-grid" aria-label="Thống kê nhanh">
            <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/home">
                <div class="dashboard-kpi-top">
                    <span class="dashboard-kpi-label">Danh mục sản phẩm</span>
                    <span class="dashboard-kpi-icon"><i class="bi bi-grid-1x2-fill"></i></span>
                </div>
                <div class="dashboard-kpi-value"><c:out value="${cateList.size()}" /></div>
                <span class="dashboard-kpi-link">Mở trang Danh mục <i class="bi bi-arrow-right"></i></span>
            </a>

            <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/video">
                <div class="dashboard-kpi-top">
                    <span class="dashboard-kpi-label">Video mới cập nhật</span>
                    <span class="dashboard-kpi-icon" style="background:#eef2ff;color:#4f46e5;"><i class="bi bi-collection-play-fill"></i></span>
                </div>
                <div class="dashboard-kpi-value"><c:out value="${latestVideos.size()}" /></div>
                <span class="dashboard-kpi-link">Xem toàn bộ thư viện <i class="bi bi-arrow-right"></i></span>
            </a>

            <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/profile">
                <div class="dashboard-kpi-top">
                    <span class="dashboard-kpi-label">Tài khoản &amp; Bảo mật</span>
                    <span class="dashboard-kpi-icon" style="background:#ecfdf5;color:#059669;"><i class="bi bi-shield-check"></i></span>
                </div>
                <div class="dashboard-kpi-value" style="font-size:17px; word-break:break-word;"><c:out value="${not empty sessionScope.account.email ? sessionScope.account.email : sessionScope.account.userName}" /></div>
                <span class="dashboard-kpi-link">Cập nhật hồ sơ &amp; ảnh <i class="bi bi-arrow-right"></i></span>
            </a>
        </section>

        <section aria-labelledby="index-latest-videos-title">
            <div class="section-heading">
                <div>
                    <h2 id="index-latest-videos-title">Video nổi bật gần đây</h2>
                    <p>Chọn một video để phát ngay hoặc bấm xem tất cả</p>
                </div>
                <a class="button button-secondary" href="${pageContext.request.contextPath}/video">Xem tất cả video <i class="bi bi-arrow-right"></i></a>
            </div>

            <c:if test="${not empty videoDbError}">
                <div class="catalog-alert"><c:out value="${videoDbError}" /></div>
            </c:if>

            <c:choose>
                <c:when test="${empty latestVideos}">
                    <div class="empty-state">Chưa có video nào được đăng tải.</div>
                </c:when>
                <c:otherwise>
                    <div class="video-grid">
                        <c:forEach items="${latestVideos}" var="v" end="5">
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
                                        <c:if test="${sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2 || (v.user != null && v.user.id == sessionScope.account.id)}">
                                            <a class="video-btn-sm" href="${editUrl}">Sửa / Đổi video</a>
                                        </c:if>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</c:if>
</body>
</html>
