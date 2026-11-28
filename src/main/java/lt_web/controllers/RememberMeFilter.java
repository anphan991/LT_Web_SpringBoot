package lt_web.controllers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.RememberMeService;
import lt_web.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RememberMeFilter extends OncePerRequestFilter {
    private final RememberMeService rememberMeService;

    @Autowired
    public RememberMeFilter(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        if (!isStaticResource(req)) {
            HttpSession session = req.getSession(false);
            boolean authenticated = session != null
                    && session.getAttribute(Constant.SESSION_ACCOUNT) instanceof User;
            if (!authenticated) {
                User account = rememberMeService.authenticate(getRememberToken(req));
                if (account != null) {
                    session = req.getSession(true);
                    session.setAttribute(Constant.SESSION_ACCOUNT, account);
                    session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                }
            }
        }
        chain.doFilter(req, resp);
    }

    private String getRememberToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (Constant.COOKIE_REMEMBER.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isStaticResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".svg")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".gif") || uri.endsWith(".webp") || uri.endsWith(".ico");
    }
}
