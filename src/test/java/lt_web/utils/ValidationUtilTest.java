package lt_web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import lt_web.entity.Category;
import lt_web.entity.User;
import org.junit.jupiter.api.Test;

class ValidationUtilTest {

    @Test
    void rejectsBlankCategoryName() {
        Category category = new Category();
        category.setCatename(" ");

        assertFalse(ValidationUtil.validate(category).isEmpty());
        Map<String, String> errors = ValidationUtil.fieldErrors(category);
        assertTrue(errors.containsKey("catename"));
    }

    @Test
    void acceptsValidCategory() {
        Category category = new Category();
        category.setCatename("Quần áo");
        category.setIcon("ao.jpg");

        assertTrue(ValidationUtil.validate(category).isEmpty());
        assertTrue(ValidationUtil.fieldErrors(category).isEmpty());
    }

    @Test
    void collectsUserFieldErrorsAndValidatesFormHelpers() {
        User invalid = new User();
        invalid.setUserName("");
        invalid.setEmail("bad-email");
        invalid.setPassWord("");

        Map<String, String> errors = ValidationUtil.fieldErrors(invalid);
        assertEquals(3, errors.size());
        assertTrue(errors.containsKey("userName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("passWord"));

        assertTrue(ValidationUtil.isValidUsername("phan_an.24"));
        assertFalse(ValidationUtil.isValidUsername("ab"));
        assertFalse(ValidationUtil.isValidUsername("invalid user!"));

        assertTrue(ValidationUtil.isValidEmail("anphan@example.com"));
        assertFalse(ValidationUtil.isValidEmail("anphan@invalid"));

        assertTrue(ValidationUtil.isValidPhone("0901-234-567"));
        assertFalse(ValidationUtil.isValidPhone("12345"));

        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertFalse(ValidationUtil.isValidPassword("123"));

        assertTrue(ValidationUtil.isValidOtp("012345"));
        assertFalse(ValidationUtil.isValidOtp("12345a"));

        assertTrue(ValidationUtil.isValidExternalUrl("https://www.youtube.com/watch?v=l9AzO1FMgM8"));
        assertFalse(ValidationUtil.isValidExternalUrl("ftp://example.com/video.mp4"));
    }
}
