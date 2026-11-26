package lt_web.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void hashIsDifferentFromOriginalAndMatchesOnlyCorrectPassword() {
        String hash = PasswordHasher.hash("123456");

        assertNotEquals("123456", hash);
        assertTrue(PasswordHasher.isHash(hash));
        assertTrue(PasswordHasher.matches("123456", hash));
        assertFalse(PasswordHasher.matches("654321", hash));
    }

    @Test
    void samePasswordGetsDifferentSaltedHashes() {
        assertNotEquals(PasswordHasher.hash("secret"), PasswordHasher.hash("secret"));
    }
}
