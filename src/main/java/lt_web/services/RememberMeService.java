package lt_web.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lt_web.entity.User;
import lt_web.repository.IRememberTokenRepository;
import lt_web.repository.impl.RememberTokenRepository;
import lt_web.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RememberMeService {
    public static final int COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;
    private final IRememberTokenRepository tokenRepo;

    public RememberMeService() {
        this(new RememberTokenRepository());
    }

    @Autowired
    public RememberMeService(IRememberTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public String issue(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        }
        tokenRepo.deleteExpired();
        String rawToken = TokenUtil.randomToken();
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
        tokenRepo.save(TokenUtil.sha256(rawToken), user.getId(), expiresAt);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public User authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return tokenRepo.findValidUser(TokenUtil.sha256(rawToken));
    }

    public void revoke(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            tokenRepo.delete(TokenUtil.sha256(rawToken));
        }
    }
}
