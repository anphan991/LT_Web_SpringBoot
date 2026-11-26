package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
public class ForgotPasswordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordController.class);

    private final UserService userService;
    private final OtpService otpService;

    @Autowired
    public ForgotPasswordController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_ACCOUNT) != null) {
            return "redirect:/waiting";
        }
        return Constant.View.FORGOT_PASSWORD;
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam(value = "email", required = false) String email,
            HttpServletRequest req,
            Model model) {
        String normalizedEmail = email == null ? "" : email.trim();
        model.addAttribute("email", normalizedEmail);

        Map<String, String> errors = new LinkedHashMap<>();
        if (normalizedEmail.isEmpty()) {
            errors.put("email", "Vui lòng nhập email đã đăng ký.");
        } else if (!ValidationUtil.isValidEmail(normalizedEmail)) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("alert", errors.values().iterator().next());
            return Constant.View.FORGOT_PASSWORD;
        }

        User user = userService.findByEmail(normalizedEmail);
        if (user == null) {
            errors.put("email", "Email không tồn tại trong hệ thống.");
            model.addAttribute("errors", errors);
            model.addAttribute("alert", "Email không tồn tại trong hệ thống.");
            return Constant.View.FORGOT_PASSWORD;
        }

        try {
            otpService.generateAndSendForgotPasswordOtp(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            LOGGER.warn("Lỗi khi gửi email OTP quên mật khẩu", e);
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("forgotEmail", user.getEmail());
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        return "redirect:/verify-otp?email=" + encodedEmail + "&type=forgot";
    }
}
