package lt_web.controllers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.utils.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        if (isStaticResource(req)) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null && needsSession(req)) {
            session = req.getSession(true);
        }
        if (session != null) {
            CsrfToken.ensure(session);
        }

        if (isUnsafeMethod(req.getMethod()) && !CsrfToken.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ.");
            return;
        }
        chain.doFilter(req, resp);
    }

    private boolean needsSession(HttpServletRequest request) {
        String path = resolvePath(request);
        return !path.startsWith("/image") && !path.startsWith("/video-file");
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private boolean isStaticResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".svg")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".gif") || uri.endsWith(".webp") || uri.endsWith(".ico");
    }

    private boolean isUnsafeMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }
}
