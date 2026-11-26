package lt_web.services;

public interface EmailService {
    boolean sendActivationOtp(String toEmail, String fullName, String otpCode);
    boolean sendForgotPasswordOtp(String toEmail, String fullName, String otpCode);
    boolean sendEmailChangeOtp(String toEmail, String fullName, String otpCode);
    boolean sendOtp(String toEmail, String subject, String title, String otpCode, String instruction);
}
