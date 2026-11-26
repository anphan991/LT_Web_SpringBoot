package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResetPasswordController {
    private final UserService userService;
    private final OtpService otpService;

    @Autowired
    public ResetPasswordController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/reset-password")
    public String showResetPassword(HttpServletRequest req, Model model) {
        String email = resolveEmail(req);
        if (email == null || email.isBlank()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        model.addAttribute("isOtpVerified", isOtpPreVerified(req, email));
        return Constant.View.RESET_PASSWORD;
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam(value = "otp", required = false) String otp,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "repassword", required = false) String repassword,
            HttpServletRequest req,
            Model model) {
        String email = resolveEmail(req);
        if (email == null || email.isBlank()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        boolean preVerified = isOtpPreVerified(req, email);
        model.addAttribute("isOtpVerified", preVerified);

        Map<String, String> errors = new LinkedHashMap<>();
        if (!preVerified) {
            if (!ValidationUtil.isValidOtp(otp)) {
                errors.put("otp", "Vui lòng nhập mã OTP gồm đúng 6 chữ số.");
            }
        }
        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu mới không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu mới phải từ 6 đến 100 ký tự.");
        }
        if (repassword == null || repassword.isBlank()) {
            errors.put("repassword", "Vui lòng nhập lại mật khẩu mới.");
        } else if (password != null && !password.equals(repassword)) {
            errors.put("repassword", "Mật khẩu xác nhận không khớp.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("alert", errors.values().iterator().next());
            return Constant.View.RESET_PASSWORD;
        }

        if (!preVerified) {
            boolean verified = otpService.verifyForgotPasswordOtp(email, otp.trim());
            if (!verified) {
                errors.put("otp", "Mã OTP không chính xác hoặc đã hết hạn.");
                model.addAttribute("errors", errors);
                model.addAttribute("alert", "Mã OTP không chính xác hoặc đã hết hạn.");
                return Constant.View.RESET_PASSWORD;
            }
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("alert", "Không tìm thấy tài khoản người dùng.");
            return Constant.View.RESET_PASSWORD;
        }

        boolean success = userService.resetPassword(email, password);
        if (success) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute("verifiedResetEmail");
                session.removeAttribute("forgotEmail");
            }
            return "redirect:/login?resetSuccess=1";
        } else {
            model.addAttribute("alert", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            return Constant.View.RESET_PASSWORD;
        }
    }

    private boolean isOtpPreVerified(HttpServletRequest req, String email) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object verified = session.getAttribute("verifiedResetEmail");
            return verified instanceof String s && s.equalsIgnoreCase(email);
        }
        return false;
    }

    private String resolveEmail(HttpServletRequest req) {
        String email = req.getParameter("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionEmail = session.getAttribute("verifiedResetEmail");
            if (sessionEmail == null) {
                sessionEmail = session.getAttribute("forgotEmail");
            }
            if (sessionEmail instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }
}
