package lt_web.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.services.UserService;
import lt_web.utils.AdminAccess;
import lt_web.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserListController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserListController.class);
    public static final int PAGE_SIZE = 5;

    private final UserService userService;

    @Autowired
    public UserListController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/user/list")
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String rawKeyword,
            @RequestParam(value = "roleid", required = false) String rawRole,
            @RequestParam(value = "page", required = false) String pageParam,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list?forbidden=1#users-section";
        }

        String keyword = (rawKeyword != null && !rawKeyword.isBlank()) ? rawKeyword.trim() : null;
        Integer roleFilter = parseRoleFilter(rawRole);
        int requestedPage = parsePage(pageParam);

        List<User> userList;
        int totalUsers = 0;
        int filteredUsers = 0;
        int activeUsers = 0;
        int adminUsers = 0;
        int currentPage = 1;
        int totalPages = 1;

        try {
            totalUsers = userService.count();
            activeUsers = userService.countByStatus(1);
            adminUsers = userService.countByRole(1);
            filteredUsers = userService.countSearch(keyword, roleFilter);
            totalPages = Math.max(1, (int) Math.ceil((double) filteredUsers / PAGE_SIZE));
            currentPage = Math.min(requestedPage, totalPages);
            userList = userService.search(keyword, roleFilter, currentPage - 1, PAGE_SIZE);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh sách tài khoản trong trang quản trị", e);
            userList = List.of();
            model.addAttribute("userError", "Không thể tải danh sách tài khoản. Hãy kiểm tra kết nối MySQL.");
        }

        model.addAttribute("userList", userList);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("filteredUsers", filteredUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageOffset", (currentPage - 1) * PAGE_SIZE);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", roleFilter);
        return Constant.View.ADMIN_LIST_USER;
    }

    private int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(value.trim());
            return Math.max(1, page);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Integer parseRoleFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int role = Integer.parseInt(value.trim());
            return (role == 1 || role == 2 || role == 5) ? role : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
