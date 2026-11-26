package lt_web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenUtilTest {
    @Test
    void randomTokenIsUrlSafeAndUnique() {
        String first = TokenUtil.randomToken();
        String second = TokenUtil.randomToken();

        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
        assertTrue(first.length() >= 40);
    }

    @Test
    void sha256IsDeterministicAndHasExpectedLength() {
        assertEquals(TokenUtil.sha256("token"), TokenUtil.sha256("token"));
        assertEquals(64, TokenUtil.sha256("token").length());
        assertNotEquals(TokenUtil.sha256("token"), TokenUtil.sha256("other"));
    }
}
