package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.exceptions.DataAccessException;
import lt_web.services.CategoryService;
import lt_web.services.UserService;
import lt_web.services.VideoService;
import lt_web.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServlet.class);

    private final CategoryService cateService;
    private final VideoService videoService;
    private final UserService userService;

    @Autowired
    public HomeServlet(CategoryService cateService, VideoService videoService, UserService userService) {
        this.cateService = cateService;
        this.videoService = videoService;
        this.userService = userService;
    }

    @GetMapping({"/", "/index", "/home", "/admin", "/admin/home", "/manager/home"})
    public String home(
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String path = resolvePath(request);
        if ("/admin".equals(path) || "/admin/home".equals(path) || "/manager/home".equals(path)) {
            return "redirect:/admin/category/list";
        }

        boolean authenticated = isAuthenticated(request);
        if (!authenticated && !"/home".equals(path)) {
            model.addAttribute("latestVideos", List.of());
            model.addAttribute("cateList", List.of());
            return Constant.View.INDEX;
        }

        List<Video> latestVideos;
        try {
            latestVideos = videoService.findLatest(10);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh sách video mới nhất", e);
            latestVideos = List.of();
            model.addAttribute("videoDbError", "Không thể tải danh sách video mới nhất. Hãy kiểm tra kết nối MySQL.");
        }
        model.addAttribute("latestVideos", latestVideos);

        List<Category> cateList;
        try {
            if (keyword != null && !keyword.isBlank()) {
                cateList = cateService.search(keyword);
                model.addAttribute("keyword", keyword);
            } else {
                cateList = cateService.getAll();
            }
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh mục cho trang public", e);
            cateList = List.of();
            model.addAttribute("dbError", "Không thể tải danh mục. Hãy kiểm tra MySQL đang chạy và cơ sở dữ liệu bt_lt_web đã tồn tại.");
        }
        model.addAttribute("cateList", cateList);

        if ("/home".equals(path)) {
            return Constant.View.HOME;
        }
        return Constant.View.INDEX;
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            String stripped = uri.substring(ctx.length());
            return stripped.isEmpty() ? "/" : stripped;
        }
        return uri;
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        if (session.getAttribute(Constant.SESSION_ACCOUNT) instanceof User) {
            return true;
        }
        Object usernameObj = session.getAttribute(Constant.SESSION_USERNAME);
        if (usernameObj instanceof String username && !username.isBlank()) {
            User account = userService.get(username);
            if (account != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, account);
                session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                return true;
            }
        }
        return false;
    }
}
