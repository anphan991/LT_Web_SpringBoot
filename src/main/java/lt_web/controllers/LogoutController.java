package lt_web.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lt_web.services.RememberMeService;
import lt_web.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {
    private final RememberMeService rememberMeService;

    @Autowired
    public LogoutController(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req, HttpServletResponse resp) {
        rememberMeService.revoke(getCookieValue(req, Constant.COOKIE_REMEMBER));
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        clearCookie(resp, Constant.COOKIE_REMEMBER);
        clearCookie(resp, Constant.LEGACY_COOKIE_REMEMBER);
        return "redirect:/login?signedOut=1";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest req, HttpServletResponse resp) {
        return logout(req, resp);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
