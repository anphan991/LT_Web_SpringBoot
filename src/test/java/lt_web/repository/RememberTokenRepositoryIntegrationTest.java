package lt_web.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lt_web.entity.User;
import lt_web.utils.PasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "BT_LT_RUN_DB_TESTS", matches = "true")
class RememberTokenRepositoryIntegrationTest {
    @Autowired
    private IUserRepository userRepo;

    @Autowired
    private IRememberTokenRepository tokenRepo;

    private final String username = "test_remember_" + System.nanoTime();
    private int userId;

    @Test
    void tokenIsStoredAndCanLoadUserFromMySql() {
        User user = new User("integration@example.com", username, "Integration Test",
                PasswordHasher.hash("secret"), null, 5, null,
                new java.sql.Date(System.currentTimeMillis()));
        userRepo.insert(user);
        userId = userRepo.get(username).getId();

        String tokenHash = "integration-token-hash-" + System.nanoTime();
        tokenRepo.save(tokenHash, userId,
                Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES)));

        User loaded = tokenRepo.findValidUser(tokenHash);
        assertNotNull(loaded);
        assertEquals(username, loaded.getUserName());

        tokenRepo.delete(tokenHash);
        assertNull(tokenRepo.findValidUser(tokenHash));
    }

    @AfterEach
    void cleanUp() {
        if (userId > 0) {
            userRepo.delete(userId);
        }
    }
}
