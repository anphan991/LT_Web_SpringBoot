<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${formMode == 'edit' ? 'Cập nhật Video' : 'Thêm Video mới'} - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css?v=20261119-v3">
    <style>
        .video-form-card {
            max-width: 760px;
            margin: 0 auto;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 32px;
            box-shadow: 0 2px 10px rgba(15, 23, 42, .05);
        }
        .textarea-control {
            width: 100%;
            min-height: 110px;
            padding: 10px 14px;
            color: #0f172a;
            background: #ffffff;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font: inherit;
            resize: vertical;
        }
        .textarea-control:focus {
            outline: none;
            border-color: #1e40af;
            box-shadow: 0 0 0 3px rgba(30, 64, 175, .15);
        }
        .video-btn-sm {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 34px;
            padding: 0 14px;
            font-size: 13.5px;
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
    </style>
</head>
<body>

<main class="page-container member-main">
    <section class="video-form-card" aria-labelledby="video-form-title">
        <div class="section-heading" style="margin-bottom: 20px;">
            <div>
                <h1 id="video-form-title" style="margin: 0; font-size: 24px;">
                    ${formMode == 'edit' ? 'Cập nhật Video' : 'Thêm Video mới'}
                </h1>
                <p>Tải lên ảnh poster và tệp video bằng Spring Boot MultipartFile</p>
            </div>
            <a class="video-btn-sm" href="${pageContext.request.contextPath}/video">Quay lại danh sách</a>
        </div>

        <c:if test="${not empty alert}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <c:url value="/video/${formMode == 'edit' ? 'edit' : 'add'}" var="formActionUrl">
            <c:param name="csrfToken" value="${sessionScope.csrfToken}" />
            <c:if test="${formMode == 'edit'}">
                <c:param name="id" value="${video.videoId}" />
            </c:if>
        </c:url>
        <form id="videoForm" data-mode="${formMode}" action="${formActionUrl}"
              method="post"
              enctype="multipart/form-data" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <c:if test="${formMode == 'edit'}">
                <input type="hidden" name="id" value="${video.videoId}">
            </c:if>

            <div class="field-group">
                <label for="title">Tiêu đề video *</label>
                <input id="title" class="input-control ${not empty errors.title ? 'is-invalid' : ''}" type="text" name="title"
                       value="<c:out value='${video.title}' />"
                       placeholder="Ví dụ: Hướng dẫn phối đồ công sở" required maxlength="255">
                <div class="invalid-feedback ${not empty errors.title ? 'd-block' : ''}"><c:out value="${errors.title}" /></div>
            </div>

            <div class="field-group">
                <label for="description">Mô tả chi tiết</label>
                <textarea id="description" class="textarea-control ${not empty errors.description ? 'is-invalid' : ''}" name="description"
                          maxlength="2000" placeholder="Nhập mô tả nội dung video..."><c:out value="${video.description}" /></textarea>
                <div class="invalid-feedback ${not empty errors.description ? 'd-block' : ''}"><c:out value="${errors.description}" /></div>
            </div>

            <div class="form-grid">
                <div class="field-group">
                    <label for="poster">Ảnh poster (JPG, PNG, SVG, WEBP)</label>
                    <input id="poster" class="input-control ${not empty errors.poster ? 'is-invalid' : ''}" type="file" name="poster" accept="image/*" onchange="previewPosterFile(this)">
                    <div class="invalid-feedback ${not empty errors.poster ? 'd-block' : ''}"><c:out value="${errors.poster}" /></div>
                    <c:choose>
                        <c:when test="${formMode == 'edit' && video.youtubeVideo && !video.customPoster}">
                            <div style="margin-top: 8px;">
                                <img id="posterPreview" src="${video.youtubeThumbnailUrl}" alt="Xem trước poster" style="width: 160px; height: 90px; object-fit: cover; border: 1px solid #cbd5e1; border-radius: 6px; display: block;">
                            </div>
                        </c:when>
                        <c:when test="${formMode == 'edit' && not empty video.poster}">
                            <c:url value="/image" var="currentPosterUrl">
                                <c:param name="fname" value="${video.poster}" />
                            </c:url>
                            <div style="margin-top: 8px;">
                                <img id="posterPreview" src="${video.externalPoster ? video.poster : currentPosterUrl}" alt="Xem trước poster" style="width: 160px; height: 90px; object-fit: cover; border: 1px solid #cbd5e1; border-radius: 6px; display: block;">
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="margin-top: 8px;">
                                <img id="posterPreview" alt="Xem trước poster" style="width: 160px; height: 90px; object-fit: cover; border: 1px solid #cbd5e1; border-radius: 6px; display: none;">
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="field-group">
                    <label for="videoFile">Tệp Video (MP4, WEBM, OGG, MOV - tối đa 100MB)</label>
                    <input id="videoFile" class="input-control ${not empty errors.videoFile ? 'is-invalid' : ''}" type="file" name="videoFile" accept="video/mp4,video/webm,video/ogg,video/quicktime,video/*" onchange="previewVideoFile(this)">
                    <div class="invalid-feedback ${not empty errors.videoFile ? 'd-block' : ''}"><c:out value="${errors.videoFile}" /></div>
                    <c:if test="${formMode == 'edit' && not empty video.videoPath}">
                        <p class="demo-note" style="margin-top: 6px;">Video hiện tại: <strong><c:out value="${video.videoPath}" /></strong> (chọn tệp mới hoặc dán URL mới ở dưới để thay thế)</p>
                    </c:if>
                    <div id="videoPreviewWrap" style="margin-top: 8px; display: ${formMode == 'edit' && !video.sampleVideo && !video.youtubeVideo ? 'block' : 'none'};">
                        <c:choose>
                            <c:when test="${formMode == 'edit' && !video.sampleVideo && !video.youtubeVideo}">
                                <c:url value="/video-file" var="currentVideoSrc">
                                    <c:param name="fname" value="${video.videoPath}" />
                                </c:url>
                                <video id="videoPreview" controls preload="metadata"
                                       src="${video.externalVideo ? video.videoPath : currentVideoSrc}"
                                       style="width: 100%; max-width: 280px; aspect-ratio: 16 / 9; background: #0f172a; border-radius: 6px; display: block;"></video>
                            </c:when>
                            <c:otherwise>
                                <video id="videoPreview" controls preload="metadata"
                                       style="width: 100%; max-width: 280px; aspect-ratio: 16 / 9; background: #0f172a; border-radius: 6px; display: block;"></video>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="field-group">
                <label for="videoUrl">Hoặc URL Video / YouTube (tùy chọn nếu không tải tệp từ máy)</label>
                <input id="videoUrl" class="input-control ${not empty errors.videoUrl ? 'is-invalid' : ''}" type="text" name="videoUrl"
                       value="<c:out value='${video.externalVideo ? video.videoPath : ""}' />"
                       placeholder="https://www.youtube.com/watch?v=... hoặc https://.../video.mp4"
                       maxlength="500"
                       oninput="previewYoutubeUrl(this)">
                <div class="invalid-feedback ${not empty errors.videoUrl ? 'd-block' : ''}"><c:out value="${errors.videoUrl}" /></div>
            </div>

            <div class="field-group">
                <label for="active">Trạng thái hiển thị</label>
                <select id="active" class="input-control" name="active">
                    <option value="1" ${video.active != 0 ? 'selected' : ''}>Hiển thị công khai</option>
                    <option value="0" ${video.active == 0 ? 'selected' : ''}>Ẩn video</option>
                </select>
            </div>

            <div style="display: flex; gap: 10px; margin-top: 20px;">
                <button class="button button-primary" type="submit">
                    ${formMode == 'edit' ? 'Lưu thay đổi' : 'Đăng video'}
                </button>
                <a class="button button-quiet" href="${pageContext.request.contextPath}/video">Hủy</a>
            </div>
        </form>
    </section>
</main>

<script>
let currentVideoBlobUrl = null;
function previewPosterFile(input) {
    const img = document.getElementById('posterPreview');
    if (!img) return;
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            img.src = e.target.result;
            img.style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
    }
}
function previewVideoFile(input) {
    const wrap = document.getElementById('videoPreviewWrap');
    const video = document.getElementById('videoPreview');
    if (!wrap || !video) return;
    if (currentVideoBlobUrl) {
        URL.revokeObjectURL(currentVideoBlobUrl);
        currentVideoBlobUrl = null;
    }
    if (input.files && input.files[0]) {
        currentVideoBlobUrl = URL.createObjectURL(input.files[0]);
        video.src = currentVideoBlobUrl;
        wrap.style.display = 'block';
        const urlInput = document.getElementById('videoUrl');
        if (urlInput) {
            urlInput.value = '';
        }
    }
}
function previewYoutubeUrl(input) {
    const posterInput = document.getElementById('poster');
    const img = document.getElementById('posterPreview');
    if (!img || (posterInput && posterInput.files && posterInput.files.length > 0)) {
        return;
    }
    const raw = (input.value || '').trim();
    const match = raw.match(/(?:youtube\.com\/(?:watch\?(?:.*&)?v=|embed\/|shorts\/|live\/)|youtu\.be\/)([A-Za-z0-9_-]{11})/);
    if (match && match[1]) {
        img.src = 'https://img.youtube.com/vi/' + match[1] + '/hqdefault.jpg';
        img.style.display = 'block';
    }
}
document.addEventListener('DOMContentLoaded', function () {
    window.ShopValidation.attach('videoForm', function (form) {
        var valid = true;
        var mode = form.getAttribute('data-mode');
        var title = form.title.value.trim();
        var desc = form.description.value;
        var url = form.videoUrl.value.trim();
        var hasPoster = form.poster.files && form.poster.files.length > 0;
        var hasVideo = form.videoFile.files && form.videoFile.files.length > 0;

        if (!title) {
            window.ShopValidation.setError(form.title, 'Tiêu đề video không được để trống.');
            valid = false;
        } else if (title.length < 2 || title.length > 255) {
            window.ShopValidation.setError(form.title, 'Tiêu đề video phải từ 2 đến 255 ký tự.');
            valid = false;
        }
        if (desc.length > 2000) {
            window.ShopValidation.setError(form.description, 'Mô tả không được vượt quá 2000 ký tự.');
            valid = false;
        }
        if (hasPoster) {
            var posterFile = form.poster.files[0];
            if (!/\.(jpg|jpeg|png|gif|svg|webp)$/i.test(posterFile.name)) {
                window.ShopValidation.setError(form.poster, 'Ảnh poster phải có định dạng JPG, PNG, SVG, GIF hoặc WEBP.');
                valid = false;
            } else if (posterFile.size > 5 * 1024 * 1024) {
                window.ShopValidation.setError(form.poster, 'Ảnh poster tối đa 5MB.');
                valid = false;
            }
        }
        if (hasVideo) {
            var videoFile = form.videoFile.files[0];
            if (!/\.(mp4|webm|ogg|mov|mkv)$/i.test(videoFile.name)) {
                window.ShopValidation.setError(form.videoFile, 'Video chỉ hỗ trợ MP4, WEBM, OGG, MOV hoặc MKV.');
                valid = false;
            } else if (videoFile.size > 100 * 1024 * 1024) {
                window.ShopValidation.setError(form.videoFile, 'Tệp video tối đa 100MB.');
                valid = false;
            }
        }
        if (url) {
            var normalized = /^(www\.|m\.|youtube\.com\/|youtu\.be\/)/i.test(url) ? ('https://' + url) : url;
            if (!/^https?:\/\/.+/i.test(normalized) || normalized.length > 500) {
                window.ShopValidation.setError(form.videoUrl, 'Đường dẫn video URL phải bắt đầu bằng http:// hoặc https://.');
                valid = false;
            }
        }
        if (mode !== 'edit' && !hasVideo && !url) {
            window.ShopValidation.setError(form.videoFile, 'Vui lòng chọn tệp video tải lên hoặc nhập URL Video / YouTube.');
            valid = false;
        }
        return valid;
    });
});
</script>
</body>
</html>
