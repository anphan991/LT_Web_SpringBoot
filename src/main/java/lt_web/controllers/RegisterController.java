package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
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
public class RegisterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    private final UserService userService;
    private final OtpService otpService;

    @Autowired
    public RegisterController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/register")
    public String showRegister(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_USERNAME) != null) {
            return "redirect:/waiting";
        }
        return Constant.View.REGISTER;
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam(value = "username", required = false) String rawUsername,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "repassword", required = false) String repassword,
            @RequestParam(value = "email", required = false) String rawEmail,
            @RequestParam(value = "fullname", required = false) String rawFullname,
            @RequestParam(value = "phone", required = false) String rawPhone,
            HttpServletRequest req,
            Model model) {
        String username = trim(rawUsername);
        String email = trim(rawEmail);
        String fullname = trim(rawFullname);
        String phone = trim(rawPhone);

        preserveFormFields(model, username, fullname, email, phone);

        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isEmpty()) {
            errors.put("username", "Tên đăng nhập không được để trống.");
        } else if (!ValidationUtil.isValidUsername(username)) {
            errors.put("username", "Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, '.', '_' hoặc '-'.");
        }

        if (fullname.isEmpty()) {
            errors.put("fullname", "Họ và tên không được để trống.");
        } else if (fullname.length() < 2 || fullname.length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }

        if (email.isEmpty()) {
            errors.put("email", "Email không được để trống để nhận mã kích hoạt OTP.");
        } else if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }

        if (phone.isEmpty()) {
            errors.put("phone", "Số điện thoại không được để trống.");
        } else if (!ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Số điện thoại phải gồm 9-15 chữ số hợp lệ.");
        }

        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu phải từ 6 đến 100 ký tự.");
        }

        if (repassword == null || repassword.isBlank()) {
            errors.put("repassword", "Vui lòng nhập lại mật khẩu.");
        } else if (password != null && !password.equals(repassword)) {
            errors.put("repassword", "Mật khẩu nhập lại không khớp.");
        }

        User candidate = new User(email, username, fullname, password == null ? "" : password,
                null, 5, phone, new Date(System.currentTimeMillis()));
        for (Map.Entry<String, String> entry : ValidationUtil.fieldErrors(candidate).entrySet()) {
            String key = switch (entry.getKey()) {
                case "userName" -> "username";
                case "fullName" -> "fullname";
                case "passWord" -> "password";
                default -> entry.getKey();
            };
            errors.putIfAbsent(key, entry.getValue());
        }

        if (!errors.containsKey("email") && userService.checkExistEmail(email)) {
            errors.put("email", "Email đã tồn tại trong hệ thống.");
        }
        if (!errors.containsKey("username") && userService.checkExistUsername(username)) {
            errors.put("username", "Tên đăng nhập đã tồn tại.");
        }
        if (!errors.containsKey("phone") && userService.checkExistPhone(phone)) {
            errors.put("phone", "Số điện thoại đã tồn tại.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("alert", errors.values().iterator().next());
            return Constant.View.REGISTER;
        }

        boolean isSuccess = userService.register(username, password, email, fullname, phone);
        if (isSuccess) {
            try {
                otpService.generateAndSendActivationOtp(email, fullname);
            } catch (Exception e) {
                LOGGER.warn("Không thể gửi email OTP kích hoạt", e);
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("activationEmail", email);
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return "redirect:/verify-otp?email=" + encodedEmail + "&type=activation";
        } else {
            model.addAttribute("alert", "Lỗi hệ thống khi tạo tài khoản. Vui lòng thử lại!");
            return Constant.View.REGISTER;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void preserveFormFields(Model model, String username, String fullname, String email, String phone) {
        model.addAttribute("username", username);
        model.addAttribute("fullname", fullname);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
    }
}
