package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.exceptions.DataAccessException;
import lt_web.services.UserService;
import lt_web.services.VideoService;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoController.class);
    public static final int PAGE_SIZE = 6;

    private final VideoService videoService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public VideoController(VideoService videoService, UserService userService, CloudinaryService cloudinaryService) {
        this.videoService = videoService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping({"/video", "/admin/video/list"})
    public String handleList(
            @RequestParam(value = "keyword", required = false) String rawKeyword,
            @RequestParam(value = "scope", required = false) String rawScope,
            @RequestParam(value = "uid", required = false) String rawUid,
            @RequestParam(value = "page", required = false) String rawPage,
            HttpServletRequest req,
            HttpServletResponse resp,
            Model model) {
        setNoCacheHeaders(resp);
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }

        String keyword = trimOrNull(rawKeyword);
        String scopeParam = trimOrNull(rawScope);
        int uidParam = parsePositiveInt(rawUid);
        int requestedPage = Math.max(parsePositiveInt(rawPage), 1);

        Integer filterUserId = null;
        String effectiveScope = "all";
        boolean showingAllFallback = false;

        try {
            if (uidParam > 0) {
                filterUserId = uidParam;
                effectiveScope = "user";
            } else {
                if ("all".equalsIgnoreCase(scopeParam)) {
                    effectiveScope = "all";
                } else if ("mine".equalsIgnoreCase(scopeParam)) {
                    filterUserId = account.getId();
                    effectiveScope = "mine";
                } else {
                    int myCount = videoService.countByUserId(account.getId());
                    if (myCount > 0) {
                        filterUserId = account.getId();
                        effectiveScope = "mine";
                    } else {
                        effectiveScope = "all";
                        showingAllFallback = true;
                    }
                }
            }

            int totalVideos = videoService.countSearch(keyword, filterUserId);
            int totalPages = Math.max((int) Math.ceil(totalVideos / (double) PAGE_SIZE), 1);
            int currentPage = Math.min(requestedPage, totalPages);
            int pageOffset = (currentPage - 1) * PAGE_SIZE;

            List<Video> videoList = videoService.search(keyword, filterUserId, currentPage, PAGE_SIZE);
            model.addAttribute("videoList", videoList);
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalVideos", totalVideos);
            model.addAttribute("pageOffset", pageOffset);
            model.addAttribute("keyword", keyword);
            model.addAttribute("scope", effectiveScope);
            model.addAttribute("selectedUserId", filterUserId);
            model.addAttribute("showingAllFallback", showingAllFallback);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh sách video", e);
            model.addAttribute("videoList", List.of());
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalVideos", 0);
            model.addAttribute("pageOffset", 0);
            model.addAttribute("keyword", keyword);
            model.addAttribute("scope", effectiveScope);
            model.addAttribute("dbError", "Không thể tải danh sách video. Hãy kiểm tra kết nối MySQL.");
        }

        return Constant.View.VIDEO_LIST;
    }

    @GetMapping("/video/detail")
    public String handleDetail(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "videoId", required = false) String videoIdParam,
            HttpServletRequest req,
            HttpServletResponse resp,
            Model model) {
        setNoCacheHeaders(resp);
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }
        int id = parsePositiveInt(firstNonBlank(idParam, videoIdParam));
        if (id <= 0) {
            return "redirect:/video?videoError=missing";
        }

        try {
            videoService.incrementViews(id);
            Video video = videoService.findById(id);
            if (video == null) {
                return "redirect:/video?videoError=missing";
            }
            List<Video> relatedVideos = videoService.findLatest(6);
            model.addAttribute("video", video);
            model.addAttribute("relatedVideos", relatedVideos);
            model.addAttribute("canManage", canManageVideo(account, video));
            return Constant.View.VIDEO_DETAIL;
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải chi tiết video", e);
            return "redirect:/video?videoError=load";
        }
    }

    @GetMapping({"/video/add", "/admin/video/add"})
    public String handleAddForm(HttpServletRequest req, HttpServletResponse resp, Model model) {
        setNoCacheHeaders(resp);
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }
        Video video = new Video();
        video.setActive(1);
        return showForm(model, video, "add", null);
    }

    @PostMapping({"/video/add", "/admin/video/add"})
    public String handleAddSubmit(
            @RequestParam(value = "title", required = false) String rawTitle,
            @RequestParam(value = "description", required = false) String rawDescription,
            @RequestParam(value = "videoUrl", required = false) String rawVideoUrl,
            @RequestParam(value = "active", required = false) String rawActive,
            @RequestParam(value = "poster", required = false) MultipartFile posterPart,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFilePart,
            @RequestParam(value = "multiPartServlet", required = false) MultipartFile multiPartServlet,
            @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
            HttpServletRequest req,
            Model model) {
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }

        String title = trimOrEmpty(rawTitle);
        String description = trimOrNull(rawDescription);
        String videoUrl = normalizeExternalUrl(rawVideoUrl);
        int active = parseActive(rawActive);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setActive(active);
        video.setUser(account);
        video.setVideoPath(videoUrl);

        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(video));
        if (!errors.containsKey("title") && title.length() < 2) {
            errors.put("title", "Tiêu đề video phải có ít nhất 2 ký tự.");
        }
        if (videoUrl != null && !ValidationUtil.isValidExternalUrl(videoUrl)) {
            errors.put("videoUrl", "Đường dẫn video URL phải hợp lệ và bắt đầu bằng http:// hoặc https://.");
        }

        MultipartFile videoPart = firstNonEmptyFile(videoFilePart, multiPartServlet, uploadFile);

        if (posterPart != null && !posterPart.isEmpty() && posterPart.getSize() > 5L * 1024 * 1024) {
            errors.put("poster", "Ảnh poster tối đa 5MB.");
        }
        if (videoPart != null && !videoPart.isEmpty() && videoPart.getSize() > 100L * 1024 * 1024) {
            errors.put("videoFile", "Tệp video tối đa 100MB.");
        }
        if ((videoPart == null || videoPart.isEmpty()) && videoUrl == null) {
            errors.put("videoFile", "Vui lòng chọn tệp video tải lên hoặc nhập URL Video / YouTube.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showForm(model, video, "add", errors.values().iterator().next());
        }

        String storedPoster = null;
        String storedVideo = null;
        try {
            storedPoster = cloudinaryService.saveImageWithCloudFallback(posterPart, "shopmvc/posters", 255);
            video.setPoster(storedPoster != null ? storedPoster : "default-video-poster.svg");

            storedVideo = cloudinaryService.saveVideoWithCloudFallback(videoPart, "shopmvc/videos", 500);
            if (storedVideo != null) {
                video.setVideoPath(storedVideo);
            } else {
                video.setVideoPath(videoUrl);
            }
        } catch (IllegalStateException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Tệp tải lên vượt quá dung lượng cho phép", e);
            errors.put("videoFile", "Dung lượng tệp tải lên vượt quá giới hạn cho phép (tối đa 100MB).");
            model.addAttribute("errors", errors);
            return showForm(model, video, "add", errors.get("videoFile"));
        } catch (Exception e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Không thể lưu tệp upload cho video", e);
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể tải tệp lên.";
            errors.put(storedPoster == null && posterPart != null && !posterPart.isEmpty() ? "poster" : "videoFile", msg);
            model.addAttribute("errors", errors);
            return showForm(model, video, "add", msg);
        }

        String validationMessage = ValidationUtil.firstMessage(video);
        if (validationMessage != null) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            model.addAttribute("errors", ValidationUtil.fieldErrors(video));
            return showForm(model, video, "add", validationMessage);
        }

        try {
            videoService.create(video);
            return "redirect:/video?saved=1";
        } catch (DataAccessException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Không thể lưu video mới vào cơ sở dữ liệu", e);
            return showForm(model, video, "add", "Không thể lưu video vào cơ sở dữ liệu. Vui lòng thử lại.");
        }
    }

    @GetMapping({"/video/edit", "/admin/video/edit"})
    public String handleEditForm(
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest req,
            HttpServletResponse resp,
            Model model) {
        setNoCacheHeaders(resp);
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }

        int id = parsePositiveInt(idParam);
        if (id <= 0) {
            return "redirect:/video?videoError=missing";
        }

        try {
            Video existing = videoService.findById(id);
            if (existing == null) {
                return "redirect:/video?videoError=missing";
            }
            if (!canManageVideo(account, existing)) {
                return "redirect:/video?videoError=forbidden";
            }
            return showForm(model, existing, "edit", null);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải video để chỉnh sửa", e);
            return "redirect:/video?videoError=load";
        }
    }

    @PostMapping({"/video/edit", "/admin/video/edit"})
    public String handleEditSubmit(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "videoId", required = false) String videoIdParam,
            @RequestParam(value = "title", required = false) String rawTitle,
            @RequestParam(value = "description", required = false) String rawDescription,
            @RequestParam(value = "videoUrl", required = false) String rawVideoUrl,
            @RequestParam(value = "active", required = false) String rawActive,
            @RequestParam(value = "poster", required = false) MultipartFile posterPart,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFilePart,
            @RequestParam(value = "multiPartServlet", required = false) MultipartFile multiPartServlet,
            @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
            HttpServletRequest req,
            Model model) {
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }

        int id = parsePositiveInt(firstNonBlank(idParam, firstNonBlank(videoIdParam, req.getParameter("id"))));
        if (id <= 0) {
            return "redirect:/video?videoError=missing";
        }

        Video existing;
        try {
            existing = videoService.findById(id);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải video cần cập nhật", e);
            return "redirect:/video?videoError=load";
        }

        if (existing == null) {
            return "redirect:/video?videoError=missing";
        }
        if (!canManageVideo(account, existing)) {
            return "redirect:/video?videoError=forbidden";
        }

        String title = trimOrEmpty(rawTitle);
        String description = trimOrNull(rawDescription);
        String videoUrl = normalizeExternalUrl(rawVideoUrl);
        int active = parseActive(rawActive);

        Video draft = new Video();
        draft.setVideoId(existing.getVideoId());
        draft.setTitle(title);
        draft.setDescription(description);
        draft.setActive(active);
        draft.setPoster(existing.getPoster());
        draft.setVideoPath(videoUrl != null ? videoUrl : existing.getVideoPath());
        draft.setUser(existing.getUser());

        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(draft));
        if (!errors.containsKey("title") && title.length() < 2) {
            errors.put("title", "Tiêu đề video phải có ít nhất 2 ký tự.");
        }
        if (videoUrl != null && !ValidationUtil.isValidExternalUrl(videoUrl)) {
            errors.put("videoUrl", "Đường dẫn video URL phải hợp lệ và bắt đầu bằng http:// hoặc https://.");
        }

        MultipartFile videoPart = firstNonEmptyFile(videoFilePart, multiPartServlet, uploadFile);

        if (posterPart != null && !posterPart.isEmpty() && posterPart.getSize() > 5L * 1024 * 1024) {
            errors.put("poster", "Ảnh poster tối đa 5MB.");
        }
        if (videoPart != null && !videoPart.isEmpty() && videoPart.getSize() > 100L * 1024 * 1024) {
            errors.put("videoFile", "Tệp video tối đa 100MB.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showForm(model, draft, "edit", errors.values().iterator().next());
        }

        String storedPoster = null;
        String storedVideo = null;
        try {
            storedPoster = cloudinaryService.saveImageWithCloudFallback(posterPart, "shopmvc/posters", 255);
            storedVideo = cloudinaryService.saveVideoWithCloudFallback(videoPart, "shopmvc/videos", 500);

            Video updated = new Video();
            updated.setVideoId(existing.getVideoId());
            updated.setTitle(title);
            updated.setDescription(description);
            updated.setActive(active);
            updated.setUser(existing.getUser());
            if (storedPoster != null) {
                updated.setPoster(storedPoster);
            }
            if (storedVideo != null) {
                updated.setVideoPath(storedVideo);
                if (storedPoster == null) {
                    updated.setPoster("default-video-poster.svg");
                }
            } else if (videoUrl != null && !videoUrl.equals(existing.getVideoPath())) {
                updated.setVideoPath(videoUrl);
                if (storedPoster == null) {
                    updated.setPoster("default-video-poster.svg");
                }
            }

            String validationMessage = ValidationUtil.firstMessage(updated);
            if (validationMessage != null) {
                cleanupUploadedFiles(storedPoster, storedVideo);
                model.addAttribute("errors", ValidationUtil.fieldErrors(updated));
                return showForm(model, draft, "edit", validationMessage);
            }

            videoService.update(updated);
            return "redirect:/video/detail?id=" + existing.getVideoId() + "&updated=1";
        } catch (IllegalStateException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Tệp tải lên vượt quá dung lượng cho phép", e);
            errors.put("videoFile", "Dung lượng tệp tải lên vượt quá giới hạn cho phép (tối đa 100MB).");
            model.addAttribute("errors", errors);
            return showForm(model, draft, "edit", errors.get("videoFile"));
        } catch (DataAccessException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Không thể cập nhật video", e);
            return showForm(model, draft, "edit", "Không thể cập nhật video vào cơ sở dữ liệu. Vui lòng thử lại.");
        } catch (Exception e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            LOGGER.warn("Lỗi khi xử lý tệp upload chỉnh sửa video", e);
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể xử lý tệp tải lên.";
            errors.put(storedPoster == null && posterPart != null && !posterPart.isEmpty() ? "poster" : "videoFile", msg);
            model.addAttribute("errors", errors);
            return showForm(model, draft, "edit", msg);
        }
    }

    @PostMapping({"/video/delete", "/admin/video/delete"})
    public String handleDelete(
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest req) {
        User account = resolveAccount(req);
        if (account == null) {
            return "redirect:/login?requireAuth=1";
        }

        int id = parsePositiveInt(idParam);
        if (id <= 0) {
            return "redirect:/video?videoError=missing";
        }

        try {
            Video existing = videoService.findById(id);
            if (existing == null) {
                return "redirect:/video?videoError=missing";
            }
            if (!canManageVideo(account, existing)) {
                return "redirect:/video?videoError=forbidden";
            }
            videoService.delete(id);
            return "redirect:/video?deleted=1";
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể xóa video", e);
            return "redirect:/video?videoError=delete";
        }
    }

    private String showForm(Model model, Video video, String mode, String alert) {
        model.addAttribute("video", video);
        model.addAttribute("formMode", mode);
        if (alert != null) {
            model.addAttribute("alert", alert);
        }
        return Constant.View.VIDEO_FORM;
    }

    private void setNoCacheHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }

    private User resolveAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object accountObj = session.getAttribute(Constant.SESSION_ACCOUNT);
        if (accountObj instanceof User user) {
            return user;
        }
        Object usernameObj = session.getAttribute(Constant.SESSION_USERNAME);
        if (usernameObj instanceof String username && !username.isBlank()) {
            User loaded = userService.get(username);
            if (loaded != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, loaded);
                return loaded;
            }
        }
        return null;
    }

    private boolean canManageVideo(User account, Video video) {
        if (account == null || video == null) {
            return false;
        }
        if (account.getRoleid() == 1 || account.getRoleid() == 2) {
            return true;
        }
        return video.getUser() != null && video.getUser().getId() == account.getId();
    }

    private MultipartFile firstNonEmptyFile(MultipartFile... files) {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return file;
            }
        }
        return null;
    }

    private void cleanupUploadedFiles(String posterFile, String videoFile) {
        FileStorage.deleteQuietly(posterFile);
        FileStorage.deleteQuietly(videoFile);
    }

    private String normalizeExternalUrl(String url) {
        String trimmed = trimOrNull(url);
        if (trimmed == null) {
            return null;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("www.youtube.com/")
                || lower.startsWith("youtube.com/")
                || lower.startsWith("m.youtube.com/")
                || lower.startsWith("youtu.be/")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseActive(String value) {
        return "0".equals(trimOrEmpty(value)) ? 0 : 1;
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
