package lt_web.services.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import lt_web.entity.OtpToken;
import lt_web.repository.IOtpRepository;
import lt_web.repository.impl.OtpRepository;
import lt_web.services.EmailService;
import lt_web.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {
    public static final int EXPIRY_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IOtpRepository otpRepo;
    private final EmailService emailService;

    public OtpServiceImpl() {
        this(new OtpRepository(), new EmailServiceImpl());
    }

    @Autowired
    public OtpServiceImpl(IOtpRepository otpRepo, EmailService emailService) {
        this.otpRepo = otpRepo;
        this.emailService = emailService;
    }

    @Override
    public String generateAndSendActivationOtp(String email, String fullName) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String code = generateNumericOtp(6);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        otpRepo.invalidatePrevious(normalizedEmail, OtpToken.TYPE_ACTIVATION);
        OtpToken token = new OtpToken(normalizedEmail, code, OtpToken.TYPE_ACTIVATION, expiresAt);
        otpRepo.create(token);

        emailService.sendActivationOtp(normalizedEmail, fullName, code);
        return code;
    }

    @Override
    public String generateAndSendForgotPasswordOtp(String email, String fullName) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String code = generateNumericOtp(6);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        otpRepo.invalidatePrevious(normalizedEmail, OtpToken.TYPE_FORGOT_PASSWORD);
        OtpToken token = new OtpToken(normalizedEmail, code, OtpToken.TYPE_FORGOT_PASSWORD, expiresAt);
        otpRepo.create(token);

        emailService.sendForgotPasswordOtp(normalizedEmail, fullName, code);
        return code;
    }

    @Override
    public String generateAndSendEmailChangeOtp(String newEmail, String fullName) {
        if (newEmail == null || newEmail.isBlank()) {
            return null;
        }
        String normalizedEmail = newEmail.trim().toLowerCase(Locale.ROOT);
        String code = generateNumericOtp(6);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        otpRepo.invalidatePrevious(normalizedEmail, OtpToken.TYPE_EMAIL_CHANGE);
        OtpToken token = new OtpToken(normalizedEmail, code, OtpToken.TYPE_EMAIL_CHANGE, expiresAt);
        otpRepo.create(token);

        emailService.sendEmailChangeOtp(normalizedEmail, fullName, code);
        return code;
    }

    @Override
    public boolean verifyActivationOtp(String email, String otpCode) {
        return verify(email, OtpToken.TYPE_ACTIVATION, otpCode);
    }

    @Override
    public boolean verifyForgotPasswordOtp(String email, String otpCode) {
        return verify(email, OtpToken.TYPE_FORGOT_PASSWORD, otpCode);
    }

    @Override
    public boolean verifyEmailChangeOtp(String newEmail, String otpCode) {
        return verify(newEmail, OtpToken.TYPE_EMAIL_CHANGE, otpCode);
    }

    @Override
    public boolean resendOtp(String email, String otpType, String fullName) {
        if (email == null || email.isBlank() || otpType == null) {
            return false;
        }
        if (OtpToken.TYPE_ACTIVATION.equalsIgnoreCase(otpType)) {
            generateAndSendActivationOtp(email, fullName);
            return true;
        } else if (OtpToken.TYPE_FORGOT_PASSWORD.equalsIgnoreCase(otpType)) {
            generateAndSendForgotPasswordOtp(email, fullName);
            return true;
        } else if (OtpToken.TYPE_EMAIL_CHANGE.equalsIgnoreCase(otpType)) {
            generateAndSendEmailChangeOtp(email, fullName);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public OtpToken getLatestValidOtp(String email, String otpType) {
        if (email == null || email.isBlank() || otpType == null) {
            return null;
        }
        return otpRepo.findLatestValid(email.trim().toLowerCase(Locale.ROOT), otpType, LocalDateTime.now());
    }

    private boolean verify(String email, String otpType, String otpCode) {
        if (email == null || email.isBlank() || otpCode == null || otpCode.isBlank()) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        OtpToken token = otpRepo.findLatestValid(normalizedEmail, otpType, LocalDateTime.now());
        if (token == null || token.isUsed() || token.isExpired()) {
            return false;
        }
        if (token.getOtpCode().equals(otpCode.trim())) {
            otpRepo.markUsed(token.getId());
            return true;
        }
        return false;
    }

    private String generateNumericOtp(int digits) {
        int bound = (int) Math.pow(10, digits);
        int number = RANDOM.nextInt(bound);
        return String.format("%0" + digits + "d", number);
    }
}
