package lt_web.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt_web.entity.OtpToken;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResendOtpController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendOtpController.class);

    private final OtpService otpService;
    private final UserService userService;

    @Autowired
    public ResendOtpController(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }

    @GetMapping("/resend-otp")
    public String resendGet(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "type", required = false) String type) {
        return processResend(email, type);
    }

    @PostMapping("/resend-otp")
    public String resendPost(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "type", required = false) String type) {
        return processResend(email, type);
    }

    private String processResend(String email, String type) {
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        String normalizedEmail = email.trim();
        String normalizedType = ("forgot".equalsIgnoreCase(type) || OtpToken.TYPE_FORGOT_PASSWORD.equalsIgnoreCase(type))
                ? OtpToken.TYPE_FORGOT_PASSWORD : OtpToken.TYPE_ACTIVATION;

        User user = userService.findByEmail(normalizedEmail);
        String fullName = user != null ? user.getFullName() : null;

        try {
            otpService.resendOtp(normalizedEmail, normalizedType, fullName);
        } catch (Exception e) {
            LOGGER.warn("Lỗi khi gửi lại OTP", e);
        }

        String encodedEmail = URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
        String targetType = OtpToken.TYPE_FORGOT_PASSWORD.equals(normalizedType) ? "forgot" : "activation";
        return "redirect:/verify-otp?email=" + encodedEmail + "&type=" + targetType + "&resent=1";
    }
}
