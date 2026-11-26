package lt_web.services;

import lt_web.entity.OtpToken;

public interface OtpService {
    String generateAndSendActivationOtp(String email, String fullName);
    String generateAndSendForgotPasswordOtp(String email, String fullName);
    String generateAndSendEmailChangeOtp(String newEmail, String fullName);
    boolean verifyActivationOtp(String email, String otpCode);
    boolean verifyForgotPasswordOtp(String email, String otpCode);
    boolean verifyEmailChangeOtp(String newEmail, String otpCode);
    boolean resendOtp(String email, String otpType, String fullName);
    OtpToken getLatestValidOtp(String email, String otpType);
}
