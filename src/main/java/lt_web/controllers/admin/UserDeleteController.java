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

    @PostMapping("/admin/user/delete")
    public String handleDeleteUser(
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest req) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list";
        }
        int id = parseId(idParam);
        HttpSession session = req.getSession(false);
        User current = session == null ? null : (User) session.getAttribute(Constant.SESSION_ACCOUNT);
        if (current != null && current.getId() == id) {
            return "redirect:/admin/category/list?userError=self#users-section";
        }

        try {
            if (id > 0) {
                userService.delete(id);
            }
            return "redirect:/admin/category/list?userDeleted=1#users-section";
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể xóa tài khoản", e);
            return "redirect:/admin/category/list?userError=delete#users-section";
        }
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
