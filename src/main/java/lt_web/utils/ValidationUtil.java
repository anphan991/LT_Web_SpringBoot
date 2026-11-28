package lt_web.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");
    private static final Pattern OTP_PATTERN = Pattern.compile("^[0-9]{6}$");

    private ValidationUtil() {
    }

    public static <T> Set<ConstraintViolation<T>> validate(T value) {
        return VALIDATOR.validate(value);
    }

    public static <T> String firstMessage(T value) {
        return validate(value).stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(null);
    }

    public static <T> Map<String, String> fieldErrors(T value) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<T> violation : validate(value)) {
            String field = violation.getPropertyPath().toString();
            errors.putIfAbsent(field, violation.getMessage());
        }
        return errors;
    }

    public static boolean isValidUsername(String value) {
        return value != null && USERNAME_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.length() <= 100 && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPhone(String value) {
        if (value == null) {
            return false;
        }
        String compact = value.trim().replaceAll("[\\s.-]", "");
        return compact.length() <= 20 && PHONE_PATTERN.matcher(compact).matches();
    }

    public static boolean isValidPassword(String value) {
        return value != null && value.length() >= 6 && value.length() <= 100;
    }

    public static boolean isValidOtp(String value) {
        return value != null && OTP_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidExternalUrl(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.length() > 500
                || (!trimmed.startsWith("http://") && !trimmed.startsWith("https://"))) {
            return false;
        }
        try {
            URI.create(trimmed).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses a positive integer ID and tolerates identical comma-joined duplicates
     * produced when a parameter appears in both query string and form body.
     */
    public static int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        String candidate = value.trim();
        if (candidate.contains(",")) {
            String unique = null;
            for (String token : candidate.split(",")) {
                String clean = token.trim();
                if (clean.isEmpty()) {
                    continue;
                }
                if (unique == null) {
                    unique = clean;
                } else if (!unique.equals(clean)) {
                    return -1;
                }
            }
            candidate = unique == null ? "" : unique;
        }
        if (candidate.isEmpty()) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(candidate);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void close() {
        FACTORY.close();
    }
}
