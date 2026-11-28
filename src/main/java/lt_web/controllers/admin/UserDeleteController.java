package lt_web.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserDeleteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDeleteController.class);

    private final UserService userService;

    @Autowired
    public UserDeleteController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/user/delete")
    public String showDeleteConfirmation(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list?forbidden=1#users-section";
        }
        int id = parseId(idParam);
        User user = id > 0 ? userService.get(id) : null;
        if (user == null) {
            return redirectWithParam(returnTo, "userError=missing");
        }
        HttpSession session = req.getSession(false);
        User current = session == null ? null : (User) session.getAttribute(Constant.SESSION_ACCOUNT);
        if (current != null && current.getId() == id) {
            return redirectWithParam(returnTo, "userError=self");
        }
        model.addAttribute("user", user);
        if (returnTo != null && !returnTo.isBlank()) {
            model.addAttribute("returnTo", returnTo.trim());
        }
        return Constant.View.ADMIN_DELETE_USER;
    }

    @PostMapping("/admin/user/delete")
    public String handleDeleteUser(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest req) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list?forbidden=1#users-section";
        }
        int id = parseId(idParam);
        HttpSession session = req.getSession(false);
        User current = session == null ? null : (User) session.getAttribute(Constant.SESSION_ACCOUNT);
        if (current != null && current.getId() == id) {
            return redirectWithParam(returnTo, "userError=self");
        }

        try {
            if (id <= 0 || userService.get(id) == null) {
                return redirectWithParam(returnTo, "userError=missing");
            }
            userService.delete(id);
            return redirectWithParam(returnTo, "userDeleted=1");
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể xóa tài khoản", e);
            return redirectWithParam(returnTo, "userError=delete");
        }
    }

    private String redirectWithParam(String returnTo, String queryParam) {
        if ("users".equalsIgnoreCase(returnTo)) {
            return "redirect:/admin/user/list?" + queryParam;
        }
        return "redirect:/admin/category/list?" + queryParam + "#users-section";
    }

    private int parseId(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
