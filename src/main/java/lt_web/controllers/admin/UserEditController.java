package lt_web.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.services.UserService;
import lt_web.utils.AdminAccess;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserEditController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEditController.class);

    private final UserService userService;

    @Autowired
    public UserEditController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/user/edit")
    public String showEditUser(
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list";
        }
        User user = userService.get(parseId(idParam));
        if (user == null) {
            return "redirect:/admin/category/list?userError=missing#users-section";
        }
        return showForm(model, user, null);
    }

    @PostMapping("/admin/user/edit")
    public String handleEditUser(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "fullname", required = false) String rawFullname,
            @RequestParam(value = "email", required = false) String rawEmail,
            @RequestParam(value = "phone", required = false) String rawPhone,
            @RequestParam(value = "roleid", required = false) String rawRoleId,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list";
        }

        int id = parseId(idParam);
        User existing = userService.get(id);
        if (existing == null) {
            return "redirect:/admin/category/list?userError=missing#users-section";
        }

        String fullname = trim(rawFullname);
        User user = new User();
        user.setId(existing.getId());
        user.setUserName(existing.getUserName());
        user.setFullName(fullname);
        user.setEmail(trimOrNull(rawEmail));
        user.setPhone(trimOrNull(rawPhone));
        user.setRoleid(parseRole(rawRoleId));

        Map<String, String> errors = new LinkedHashMap<>();
        if (fullname.isEmpty()) {
            errors.put("fullname", "Họ và tên không được để trống.");
        } else if (fullname.length() < 2 || fullname.length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }
        if (user.getEmail() != null && !ValidationUtil.isValidEmail(user.getEmail())) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }
        if (user.getPhone() != null && !ValidationUtil.isValidPhone(user.getPhone())) {
            errors.put("phone", "Số điện thoại phải gồm 9-15 chữ số hợp lệ.");
        }
        if (password != null && !password.isBlank() && !ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu mới phải từ 6 đến 100 ký tự.");
        }
        if (!errors.containsKey("email") && user.getEmail() != null && userService.checkExistEmail(user.getEmail(), id)) {
            errors.put("email", "Email đã tồn tại.");
        }
        if (!errors.containsKey("phone") && user.getPhone() != null && userService.checkExistPhone(user.getPhone(), id)) {
            errors.put("phone", "Số điện thoại đã tồn tại.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showForm(model, user, errors.values().iterator().next());
        }

        try {
            userService.update(user, password);
            HttpSession session = req.getSession(false);
            User current = session == null ? null : (User) session.getAttribute(Constant.SESSION_ACCOUNT);
            if (current != null && current.getId() == id) {
                session.setAttribute(Constant.SESSION_ACCOUNT, userService.get(id));
            }
            return "redirect:/admin/category/list#users-section";
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể cập nhật tài khoản", e);
            return showForm(model, user, "Không thể lưu thay đổi. Vui lòng thử lại.");
        }
    }

    private String showForm(Model model, User user, String alert) {
        model.addAttribute("user", user);
        model.addAttribute("formMode", "edit");
        if (alert != null) {
            model.addAttribute("alert", alert);
        }
        return Constant.View.ADMIN_USER_FORM;
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

    private int parseRole(String value) {
        if (value == null || value.isBlank()) {
            return 5;
        }
        try {
            int role = Integer.parseInt(value.trim());
            return role == 1 || role == 2 || role == 5 ? role : 5;
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimOrNull(String value) {
        String trimmed = trim(value);
        return trimmed.isBlank() ? null : trimmed;
    }
}
