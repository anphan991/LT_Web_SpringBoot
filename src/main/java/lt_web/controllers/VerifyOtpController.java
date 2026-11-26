package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.OtpToken;
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
public class VerifyOtpController {
    private final OtpService otpService;
    private final UserService userService;

    @Autowired
    public VerifyOtpController(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtp(HttpServletRequest req, Model model) {
        String email = resolveEmail(req);
        String type = resolveType(req);

        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        model.addAttribute("email", email);
        model.addAttribute("type", type);
        return Constant.View.VERIFY_OTP;
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @RequestParam(value = "otp", required = false) String otpCode,
            HttpServletRequest req,
            Model model) {
        String email = resolveEmail(req);
        String type = resolveType(req);

        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        model.addAttribute("email", email);
        model.addAttribute("type", type);

        Map<String, String> errors = new LinkedHashMap<>();
        if (!ValidationUtil.isValidOtp(otpCode)) {
            errors.put("otp", "Mã OTP phải gồm đúng 6 chữ số.");
            model.addAttribute("errors", errors);
            model.addAttribute("alert", "Mã OTP phải gồm 6 chữ số.");
            return Constant.View.VERIFY_OTP;
        }

        String trimmedOtp = otpCode.trim();
        boolean isActivation = OtpToken.TYPE_ACTIVATION.equalsIgnoreCase(type);

        boolean verified = isActivation
                ? otpService.verifyActivationOtp(email, trimmedOtp)
                : otpService.verifyForgotPasswordOtp(email, trimmedOtp);

        if (verified) {
            HttpSession session = req.getSession(true);
            if (isActivation) {
                userService.activateUser(email);
                session.removeAttribute("activationEmail");
                return "redirect:/login?activated=1";
            } else {
                session.setAttribute("verifiedResetEmail", email.trim());
                String encodedEmail = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
                return "redirect:/reset-password?email=" + encodedEmail;
            }
        } else {
            model.addAttribute("alert", "Mã OTP không đúng hoặc đã hết hạn (hiệu lực 5 phút). Vui lòng thử lại hoặc nhấn Gửi lại mã.");
            return Constant.View.VERIFY_OTP;
        }
    }

    private String resolveEmail(HttpServletRequest req) {
        String email = req.getParameter("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionEmail = session.getAttribute("activationEmail");
            if (sessionEmail == null) {
                sessionEmail = session.getAttribute("forgotEmail");
            }
            if (sessionEmail instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }

    private String resolveType(HttpServletRequest req) {
        String type = req.getParameter("type");
        if (type != null && !type.isBlank()) {
            if ("forgot".equalsIgnoreCase(type) || OtpToken.TYPE_FORGOT_PASSWORD.equalsIgnoreCase(type)) {
                return OtpToken.TYPE_FORGOT_PASSWORD;
            }
            return OtpToken.TYPE_ACTIVATION;
        }
        return OtpToken.TYPE_ACTIVATION;
    }
}
