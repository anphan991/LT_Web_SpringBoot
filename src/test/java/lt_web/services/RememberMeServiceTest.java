package lt_web.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import lt_web.entity.User;
import lt_web.repository.IRememberTokenRepository;
import org.junit.jupiter.api.Test;

class RememberMeServiceTest {
    @Test
    void issuedTokenCanBeAuthenticatedAndRevoked() {
        InMemoryTokenRepository tokenRepo = new InMemoryTokenRepository();
        RememberMeService service = new RememberMeService(tokenRepo);
        User user = new User();
        user.setId(42);

        String token = service.issue(user);

        assertNotNull(token);
        assertTrue(tokenRepo.hash != null && tokenRepo.hash.length() == 64);
        assertNotNull(service.authenticate(token));

        service.revoke(token);
        assertNull(service.authenticate(token));
    }

    private static final class InMemoryTokenRepository implements IRememberTokenRepository {
        private String hash;
        private User user;

        @Override
        public void save(String tokenHash, int userId, Timestamp expiresAt) {
            hash = tokenHash;
            user = new User();
            user.setId(userId);
        }

        @Override
        public User findValidUser(String tokenHash) {
            return hash != null && hash.equals(tokenHash) ? user : null;
        }

        @Override
        public void delete(String tokenHash) {
            if (hash != null && hash.equals(tokenHash)) {
                hash = null;
                user = null;
            }
        }

        @Override
        public void deleteExpired() {
        }
    }
}
