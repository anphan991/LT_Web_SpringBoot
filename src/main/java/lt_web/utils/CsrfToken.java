package lt_web.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public final class CsrfToken {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    private static final String REQUEST_PARAMETER = "csrfToken";

    private CsrfToken() {
    }

    public static String ensure(HttpSession session) {
        Object current = session.getAttribute(SESSION_ATTRIBUTE);
        if (current instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = TokenUtil.randomToken();
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expectedValue = session.getAttribute(SESSION_ATTRIBUTE);
        String actual = extractToken(request);
        if (!(expectedValue instanceof String expected) || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private static String extractToken(HttpServletRequest request) {
        String fromQuery = extractFromQueryString(request.getQueryString());
        if (fromQuery != null) {
            return fromQuery;
        }
        String fromParam = request.getParameter(REQUEST_PARAMETER);
        if (fromParam != null && !fromParam.isBlank()) {
            return fromParam.trim();
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
            try {
                Part part = request.getPart(REQUEST_PARAMETER);
                if (part != null) {
                    try (InputStream in = part.getInputStream()) {
                        return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                    }
                }
            } catch (IOException | ServletException | IllegalStateException ignored) {
                return null;
            }
            return null;
        }
        return null;
    }

    private static String extractFromQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return null;
        }
        for (String pair : queryString.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                if (REQUEST_PARAMETER.equals(key)) {
                    return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}
