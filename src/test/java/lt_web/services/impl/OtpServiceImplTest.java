package lt_web.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lt_web.entity.OtpToken;
import lt_web.repository.IOtpRepository;
import lt_web.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OtpServiceImplTest {
    private InMemoryOtpRepository otpRepo;
    private MockEmailService emailService;
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpRepo = new InMemoryOtpRepository();
        emailService = new MockEmailService();
        otpService = new OtpServiceImpl(otpRepo, emailService);
    }

    @Test
    void generateAndVerifyActivationOtpSuccessfully() {
        String code = otpService.generateAndSendActivationOtp("test@example.com", "Nguyen Van A");

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(emailService.sent);
        assertEquals("test@example.com", emailService.lastToEmail);

        // First verification succeeds
        assertTrue(otpService.verifyActivationOtp("test@example.com", code));

        // Reusing the same OTP fails
        assertFalse(otpService.verifyActivationOtp("test@example.com", code));
    }

    @Test
    void wrongCodeIsRejected() {
        otpService.generateAndSendActivationOtp("test@example.com", "Nguyen Van A");

        assertFalse(otpService.verifyActivationOtp("test@example.com", "000000"));
        assertFalse(otpService.verifyActivationOtp("test@example.com", "123"));
        assertFalse(otpService.verifyActivationOtp("test@example.com", null));
    }

    @Test
    void resendOtpInvalidatesPreviousCode() {
        String firstCode = otpService.generateAndSendActivationOtp("test@example.com", "Nguyen Van A");
        assertTrue(otpService.resendOtp("test@example.com", OtpToken.TYPE_ACTIVATION, "Nguyen Van A"));

        OtpToken latest = otpService.getLatestValidOtp("test@example.com", OtpToken.TYPE_ACTIVATION);
        assertNotNull(latest);
        String secondCode = latest.getOtpCode();

        // Old code is now invalid
        assertFalse(otpService.verifyActivationOtp("test@example.com", firstCode));

        // New code works
        assertTrue(otpService.verifyActivationOtp("test@example.com", secondCode));
    }

    @Test
    void forgotPasswordOtpFlow() {
        String code = otpService.generateAndSendForgotPasswordOtp("reset@example.com", "User Reset");

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(otpService.verifyForgotPasswordOtp("reset@example.com", code));
        assertFalse(otpService.verifyForgotPasswordOtp("reset@example.com", code));
    }

    @Test
    void emailChangeOtpFlow() {
        String code = otpService.generateAndSendEmailChangeOtp("newmail@example.com", "User Profile");

        assertNotNull(code);
        assertEquals(6, code.length());
        assertEquals("newmail@example.com", emailService.lastToEmail);
        assertTrue(otpService.verifyEmailChangeOtp("newmail@example.com", code));
        assertFalse(otpService.verifyEmailChangeOtp("newmail@example.com", code));
    }

    @Test
    void expiredOtpIsRejected() {
        OtpToken expiredToken = new OtpToken("expired@example.com", "999999", OtpToken.TYPE_ACTIVATION,
                LocalDateTime.now().minusMinutes(1));
        otpRepo.create(expiredToken);

        assertFalse(otpService.verifyActivationOtp("expired@example.com", "999999"));
    }

    @Test
    void blankEmailReturnsNullOrFalse() {
        assertNull(otpService.generateAndSendActivationOtp("   ", "Name"));
        assertFalse(otpService.verifyActivationOtp(null, "123456"));
    }

    private static final class InMemoryOtpRepository implements IOtpRepository {
        private final List<OtpToken> tokens = new ArrayList<>();
        private int nextId = 1;

        @Override
        public void create(OtpToken token) {
            token.setId(nextId++);
            tokens.add(token);
        }

        @Override
        public OtpToken findLatestValid(String email, String otpType, LocalDateTime now) {
            if (email == null || otpType == null) {
                return null;
            }
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            for (int i = tokens.size() - 1; i >= 0; i--) {
                OtpToken t = tokens.get(i);
                if (t.getEmail().equalsIgnoreCase(normalizedEmail)
                        && t.getOtpType().equalsIgnoreCase(otpType)
                        && !t.isUsed()
                        && (now == null || t.getExpiresAt().isAfter(now))) {
                    return t;
                }
            }
            return null;
        }

        @Override
        public void markUsed(int id) {
            for (OtpToken t : tokens) {
                if (t.getId() == id) {
                    t.setUsed(true);
                    return;
                }
            }
        }

        @Override
        public void invalidatePrevious(String email, String otpType) {
            if (email == null || otpType == null) {
                return;
            }
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            for (OtpToken t : tokens) {
                if (t.getEmail().equalsIgnoreCase(normalizedEmail)
                        && t.getOtpType().equalsIgnoreCase(otpType)) {
                    t.setUsed(true);
                }
            }
        }
    }

    private static final class MockEmailService implements EmailService {
        private boolean sent = false;
        private String lastToEmail;

        @Override
        public boolean sendActivationOtp(String toEmail, String fullName, String otpCode) {
            this.sent = true;
            this.lastToEmail = toEmail;
            return true;
        }

        @Override
        public boolean sendForgotPasswordOtp(String toEmail, String fullName, String otpCode) {
            this.sent = true;
            this.lastToEmail = toEmail;
            return true;
        }

        @Override
        public boolean sendEmailChangeOtp(String toEmail, String fullName, String otpCode) {
            this.sent = true;
            this.lastToEmail = toEmail;
            return true;
        }

        @Override
        public boolean sendOtp(String toEmail, String subject, String title, String otpCode, String instruction) {
            this.sent = true;
            this.lastToEmail = toEmail;
            return true;
        }
    }
}
