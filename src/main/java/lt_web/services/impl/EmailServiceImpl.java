package lt_web.services.impl;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import lt_web.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());

    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String fromName;
    private final boolean authEnabled;
    private final boolean starttlsEnabled;

    public EmailServiceImpl() {
        Properties config = loadConfiguration();
        this.host = resolve(config, "mail.smtp.host", "MAIL_HOST", "smtp.gmail.com");
        this.port = resolve(config, "mail.smtp.port", "MAIL_PORT", "587");
        this.username = resolve(config, "mail.smtp.username", "MAIL_USERNAME", "");
        this.password = resolve(config, "mail.smtp.password", "MAIL_PASSWORD", "");
        this.fromName = resolve(config, "mail.from.name", "MAIL_FROM_NAME", "ShopSpringBoot");
        this.authEnabled = Boolean.parseBoolean(resolve(config, "mail.smtp.auth", "MAIL_AUTH", "true"));
        this.starttlsEnabled = Boolean.parseBoolean(resolve(config, "mail.smtp.starttls.enable", "MAIL_STARTTLS", "true"));
    }

    @Autowired
    public EmailServiceImpl(
            @Value("${mail.smtp.host:smtp.gmail.com}") String host,
            @Value("${mail.smtp.port:587}") String port,
            @Value("${mail.smtp.username:}") String username,
            @Value("${mail.smtp.password:}") String password,
            @Value("${mail.from.name:ShopSpringBoot}") String fromName,
            @Value("${mail.smtp.auth:true}") boolean authEnabled,
            @Value("${mail.smtp.starttls.enable:true}") boolean starttlsEnabled) {
        Properties config = loadConfiguration();
        this.host = firstNonBlank(host, resolve(config, "mail.smtp.host", "MAIL_HOST", "smtp.gmail.com"));
        this.port = firstNonBlank(port, resolve(config, "mail.smtp.port", "MAIL_PORT", "587"));
        this.username = firstNonBlank(username, resolve(config, "mail.smtp.username", "MAIL_USERNAME", ""));
        this.password = firstNonBlank(password, resolve(config, "mail.smtp.password", "MAIL_PASSWORD", ""));
        this.fromName = firstNonBlank(fromName, resolve(config, "mail.from.name", "MAIL_FROM_NAME", "ShopSpringBoot"));
        this.authEnabled = authEnabled;
        this.starttlsEnabled = starttlsEnabled;
    }

    public EmailServiceImpl(String host, String port, String username, String password, String fromName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromName = fromName;
        this.authEnabled = username != null && !username.isBlank();
        this.starttlsEnabled = true;
    }

    @Override
    public boolean sendActivationOtp(String toEmail, String fullName, String otpCode) {
        String greeting = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        String subject = "[ShopSpringBoot] Mã OTP kích hoạt tài khoản";
        String title = "Kích hoạt tài khoản ShopSpringBoot";
        String instruction = "Xin chào <strong>" + escape(greeting) + "</strong>,<br>"
                + "Cảm ơn bạn đã đăng ký tài khoản tại ShopSpringBoot. Vui lòng sử dụng mã OTP dưới đây để hoàn tất kích hoạt tài khoản:";
        return sendOtp(toEmail, subject, title, otpCode, instruction);
    }

    @Override
    public boolean sendForgotPasswordOtp(String toEmail, String fullName, String otpCode) {
        String greeting = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        String subject = "[ShopSpringBoot] Mã OTP đặt lại mật khẩu";
        String title = "Đặt lại mật khẩu ShopSpringBoot";
        String instruction = "Xin chào <strong>" + escape(greeting) + "</strong>,<br>"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng sử dụng mã OTP dưới đây để xác nhận đặt lại mật khẩu:";
        return sendOtp(toEmail, subject, title, otpCode, instruction);
    }

    @Override
    public boolean sendEmailChangeOtp(String toEmail, String fullName, String otpCode) {
        String greeting = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        String subject = "[ShopSpringBoot] Mã OTP xác nhận đổi email mới";
        String title = "Xác nhận email mới ShopSpringBoot";
        String instruction = "Xin chào <strong>" + escape(greeting) + "</strong>,<br>"
                + "Bạn vừa yêu cầu cập nhật sang địa chỉ email này tại ShopSpringBoot. Vui lòng nhập mã OTP dưới đây để hoàn tất thay đổi email:";
        return sendOtp(toEmail, subject, title, otpCode, instruction);
    }

    @Override
    public boolean sendOtp(String toEmail, String subject, String title, String otpCode, String instruction) {
        if (toEmail == null || toEmail.isBlank() || otpCode == null) {
            return false;
        }

        logOtpToConsole(toEmail, subject, otpCode);

        if (!hasSmtpCredentials()) {
            LOGGER.info("[EmailService] Chưa cấu hình mail.smtp.username hoặc mail.smtp.password. Chạy ở chế độ dev.");
            return true;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", String.valueOf(authEnabled));
            if ("465".equals(port)) {
                props.put("mail.smtp.ssl.enable", "true");
            } else {
                props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));
            }
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.connectiontimeout", "7000");
            props.put("mail.smtp.timeout", "7000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
            message.setSubject(subject, "UTF-8");

            String htmlBody = buildHtmlTemplate(title, instruction, otpCode);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            LOGGER.info("[EmailService] Đã gửi mail thành công tới: " + toEmail);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[EmailService] Không thể gửi email qua SMTP: " + e.getMessage()
                    + " (Mã OTP vẫn hợp lệ và đã được ghi nhận vào hệ thống)");
            return false;
        }
    }

    public boolean hasSmtpCredentials() {
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    private void logOtpToConsole(String toEmail, String subject, String otpCode) {
        String border = "======================================================================";
        System.out.println(border);
        System.out.println("[ShopSpringBoot EMAIL OTP] Đã tạo mã xác thực gửi tới: " + toEmail);
        System.out.println("[ShopSpringBoot EMAIL OTP] Tiêu đề : " + subject);
        System.out.println("[ShopSpringBoot EMAIL OTP] Mã OTP  : [ " + otpCode + " ] (Hiệu lực: 5 phút)");
        System.out.println(border);
    }

    private String buildHtmlTemplate(String title, String instruction, String otpCode) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:24px;background:#f5f7fa;font-family:Segoe UI,Helvetica,Arial,sans-serif;color:#172033;'>"
                + "<div style='max-width:520px;margin:0 auto;background:#ffffff;border:1px solid #dde3ec;border-radius:8px;padding:32px;box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                + "<div style='border-bottom:2px solid #2358c4;padding-bottom:12px;margin-bottom:20px;'>"
                + "<span style='font-size:22px;font-weight:700;color:#2358c4;'>ShopSpringBoot</span>"
                + "</div>"
                + "<h2 style='margin:0 0 16px;font-size:20px;color:#172033;'>" + escape(title) + "</h2>"
                + "<p style='margin:0 0 20px;font-size:14.5px;line-height:1.55;color:#475467;'>" + instruction + "</p>"
                + "<div style='text-align:center;margin:28px 0;'>"
                + "<span style='display:inline-block;padding:12px 28px;background:#f0f4ff;border:2px dashed #2358c4;border-radius:8px;font-size:32px;font-weight:700;letter-spacing:8px;color:#19449b;font-family:monospace;'>"
                + escape(otpCode)
                + "</span>"
                + "</div>"
                + "<p style='margin:0 0 12px;font-size:13px;color:#697386;'>* Mã OTP này có hiệu lực trong vòng <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                + "<div style='margin-top:28px;padding-top:16px;border-top:1px solid #edf0f4;font-size:12px;color:#98a2b3;text-align:center;'>"
                + "Đây là email tự động từ hệ thống ShopSpringBoot. Vui lòng không trả lời thư này."
                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private Properties loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception ignored) {
        }
        return properties;
    }

    private String resolve(Properties properties, String key, String envKey, String fallback) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        if (envKey != null) {
            value = System.getenv(envKey);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        value = properties.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return fallback;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }
}
