package lt_web.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
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
public class UserAddController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAddController.class);

    private final UserService userService;

    @Autowired
    public UserAddController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/user/add")
    public String showAddUser(
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list?forbidden=1#users-section";
        }
        User user = new User();
        user.setRoleid(5);
        user.setStatus(1);
        return showForm(model, user, null, null, returnTo);
    }

    @PostMapping("/admin/user/add")
    public String handleAddUser(
            @RequestParam(value = "username", required = false) String rawUsername,
            @RequestParam(value = "fullname", required = false) String rawFullname,
            @RequestParam(value = "email", required = false) String rawEmail,
            @RequestParam(value = "phone", required = false) String rawPhone,
            @RequestParam(value = "roleid", required = false) String rawRoleId,
            @RequestParam(value = "status", required = false) String rawStatus,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest req,
            Model model) {
        if (!AdminAccess.isAdmin(req)) {
            return "redirect:/admin/category/list?forbidden=1#users-section";
        }

        String username = trim(rawUsername);
        String fullname = trim(rawFullname);
        User user = new User();
        user.setUserName(username);
        user.setFullName(fullname);
        user.setEmail(trimOrNull(rawEmail));
        user.setPhone(trimOrNull(rawPhone));
        user.setRoleid(parseRole(rawRoleId));
        user.setStatus(parseStatus(rawStatus));

        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isBlank()) {
            errors.put("username", "Tên đăng nhập không được để trống.");
        } else if (!ValidationUtil.isValidUsername(username)) {
            errors.put("username", "Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, '.', '_' hoặc '-'.");
        }
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
        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu phải từ 6 đến 100 ký tự.");
        }

        if (!errors.containsKey("username") && userService.checkExistUsername(username)) {
            errors.put("username", "Tên đăng nhập đã tồn tại.");
        }
        if (!errors.containsKey("email") && user.getEmail() != null && userService.checkExistEmail(user.getEmail())) {
            errors.put("email", "Email đã tồn tại.");
        }
        if (!errors.containsKey("phone") && user.getPhone() != null && userService.checkExistPhone(user.getPhone())) {
            errors.put("phone", "Số điện thoại đã tồn tại.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showForm(model, user, fullname, errors.values().iterator().next(), returnTo);
        }

        try {
            userService.create(user, password);
            if ("users".equalsIgnoreCase(returnTo)) {
                return "redirect:/admin/user/list?userAdded=1";
            }
            return "redirect:/admin/category/list?userAdded=1#users-section";
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể thêm tài khoản", e);
            return showForm(model, user, fullname, "Không thể lưu tài khoản. Vui lòng thử lại.", returnTo);
        }
    }

    private String showForm(Model model, User user, String draftFullname, String alert, String returnTo) {
        model.addAttribute("user", user);
        model.addAttribute("formMode", "add");
        if (draftFullname != null) {
            model.addAttribute("draftFullname", draftFullname);
        }
        if (returnTo != null && !returnTo.isBlank()) {
            model.addAttribute("returnTo", returnTo.trim());
        }
        if (alert != null) {
            model.addAttribute("alert", alert);
        }
        return Constant.View.ADMIN_USER_FORM;
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

    private int parseStatus(String value) {
        return "0".equals(trim(value)) ? 0 : 1;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimOrNull(String value) {
        String trimmed = trim(value);
        return trimmed.isBlank() ? null : trimmed;
    }
}
