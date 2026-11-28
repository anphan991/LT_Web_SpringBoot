package lt_web.controllers.admin;

import java.util.List;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.services.CategoryService;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CategoryListController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryListController.class);
    private static final int PAGE_SIZE = 5;
    private static final int USER_PAGE_SIZE = 5;

    private final CategoryService cateService;
    private final UserService userService;

    @Autowired
    public CategoryListController(CategoryService cateService, UserService userService) {
        this.cateService = cateService;
        this.userService = userService;
    }

    @GetMapping("/admin/category/list")
    public String listCategories(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) String pageParam,
            @RequestParam(value = "userKeyword", required = false) String userKeyword,
            @RequestParam(value = "userRole", required = false) String userRoleParam,
            @RequestParam(value = "userPage", required = false) String userPageParam,
            Model model) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        int requestedPage = parsePage(pageParam);

        List<Category> cateList;
        int totalCategories = 0;
        int overallCategories = 0;
        int currentPage = 1;
        int totalPages = 1;
        try {
            if (hasKeyword) {
                String trimmed = keyword.trim();
                totalCategories = cateService.countByCategoryname(trimmed);
                overallCategories = cateService.count();
                totalPages = Math.max(1, (int) Math.ceil((double) totalCategories / PAGE_SIZE));
                currentPage = Math.min(requestedPage, totalPages);
                cateList = cateService.findByCategoryname(trimmed, currentPage - 1, PAGE_SIZE);
                model.addAttribute("keyword", trimmed);
            } else {
                totalCategories = cateService.count();
                overallCategories = totalCategories;
                totalPages = Math.max(1, (int) Math.ceil((double) totalCategories / PAGE_SIZE));
                currentPage = Math.min(requestedPage, totalPages);
                cateList = cateService.findAll(currentPage - 1, PAGE_SIZE);
            }
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh mục trong trang quản trị", e);
            cateList = List.of();
            model.addAttribute("dbError", "Không thể tải danh mục. Hãy kiểm tra MySQL đang chạy và cơ sở dữ liệu bt_lt_web đã tồn tại.");
        }

        String trimmedUserKeyword = (userKeyword != null && !userKeyword.isBlank()) ? userKeyword.trim() : null;
        Integer parsedUserRole = parseRoleFilter(userRoleParam);
        int requestedUserPage = parsePage(userPageParam);

        List<User> userList;
        int totalUsers = 0;
        int filteredUsers = 0;
        int userCurrentPage = 1;
        int userTotalPages = 1;
        try {
            totalUsers = userService.count();
            filteredUsers = userService.countSearch(trimmedUserKeyword, parsedUserRole);
            userTotalPages = Math.max(1, (int) Math.ceil((double) filteredUsers / USER_PAGE_SIZE));
            userCurrentPage = Math.min(requestedUserPage, userTotalPages);
            userList = userService.search(trimmedUserKeyword, parsedUserRole, userCurrentPage - 1, USER_PAGE_SIZE);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải tài khoản trong trang quản trị", e);
            userList = List.of();
            model.addAttribute("userError", "Không thể tải danh sách tài khoản. Hãy kiểm tra kết nối MySQL.");
        }

        model.addAttribute("cateList", cateList);
        model.addAttribute("totalCategories", overallCategories);
        model.addAttribute("filteredCategories", totalCategories);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageOffset", (currentPage - 1) * PAGE_SIZE);

        model.addAttribute("userList", userList);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("filteredUsers", filteredUsers);
        model.addAttribute("userCurrentPage", userCurrentPage);
        model.addAttribute("userTotalPages", userTotalPages);
        model.addAttribute("userPageOffset", (userCurrentPage - 1) * USER_PAGE_SIZE);
        model.addAttribute("userKeyword", trimmedUserKeyword);
        model.addAttribute("userRole", parsedUserRole);
        return Constant.View.ADMIN_LIST_CATEGORY;
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
