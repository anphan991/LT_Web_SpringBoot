package lt_web.controllers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Autowired
    public AuthorizationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User account = (session != null) ? (User) session.getAttribute(Constant.SESSION_ACCOUNT) : null;

        if (account == null) {
            String username = (session != null) ? (String) session.getAttribute(Constant.SESSION_USERNAME) : null;
            if (username != null && !username.isBlank()) {
                account = userService.get(username);
                if (account != null) {
                    session = req.getSession(true);
                    session.setAttribute(Constant.SESSION_ACCOUNT, account);
                    session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                }
            }
        }

        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        String path = resolvePath(req);
        boolean isAdminRoute = path.startsWith("/admin") || path.startsWith("/manager");

        if (isAdminRoute && account.getRoleid() != 1 && account.getRoleid() != 2) {
            resp.sendRedirect(req.getContextPath() + "/home?forbidden=1");
            return;
        }

        if (path.startsWith("/admin/user") && account.getRoleid() != 1) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?forbidden=1#users-section");
            return;
        }

        chain.doFilter(req, resp);
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            String stripped = uri.substring(ctx.length());
            return stripped.isEmpty() ? "/" : stripped;
        }
        return uri;
    }
}
