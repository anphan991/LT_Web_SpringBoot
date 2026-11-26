package lt_web.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.User;
import lt_web.services.RememberMeService;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginServlet {
    private final UserService userService;
    private final RememberMeService rememberMeService;

    @Autowired
    public LoginServlet(UserService userService, RememberMeService rememberMeService) {
        this.userService = userService;
        this.rememberMeService = rememberMeService;
    }

    @GetMapping("/login")
    public String showLogin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_ACCOUNT) != null) {
            return "redirect:/waiting";
        }

        User rememberedUser = authenticateRememberedUser(req, resp);
        if (rememberedUser != null) {
            establishSession(req, rememberedUser);
            return "redirect:/waiting";
        }

        return Constant.View.LOGIN;
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "remember", required = false) String remember,
            HttpServletRequest req,
            HttpServletResponse resp,
            Model model) {
        boolean isRememberMe = "on".equals(remember);
        String trimmedUsername = username == null ? "" : username.trim();
        model.addAttribute("rememberedUsername", trimmedUsername);
        model.addAttribute("remembered", isRememberMe);

        Map<String, String> errors = new LinkedHashMap<>();
        if (trimmedUsername.isEmpty()) {
            errors.put("username", "Vui lòng nhập tên đăng nhập.");
        } else if (trimmedUsername.length() > 50) {
            errors.put("username", "Tên đăng nhập không được vượt quá 50 ký tự.");
        }
        if (password == null || password.isBlank()) {
            errors.put("password", "Vui lòng nhập mật khẩu.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("alert", errors.values().iterator().next());
            return Constant.View.LOGIN;
        }

        User user = userService.login(trimmedUsername, password);
        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                model.addAttribute("alert", "Tài khoản chưa được kích hoạt. Vui lòng kích hoạt qua mã OTP đã gửi tới email.");
                model.addAttribute("unactivatedEmail", user.getEmail());
                return Constant.View.LOGIN;
            }
            establishSession(req, user);
            if (isRememberMe) {
                saveRememberMe(req, resp, user);
            } else {
                revokeRememberMe(req, resp);
            }
            return "redirect:/waiting";
        } else {
            model.addAttribute("alert", "Tài khoản hoặc mật khẩu không đúng");
            return Constant.View.LOGIN;
        }
    }

    private void saveRememberMe(HttpServletRequest request, HttpServletResponse response, User user) {
        String rawToken = rememberMeService.issue(user);
        Cookie cookie = new Cookie(Constant.COOKIE_REMEMBER, rawToken);
        cookie.setMaxAge(RememberMeService.COOKIE_MAX_AGE_SECONDS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void revokeRememberMe(HttpServletRequest request, HttpServletResponse response) {
        rememberMeService.revoke(getCookieValue(request, Constant.COOKIE_REMEMBER));
        clearCookie(response, Constant.COOKIE_REMEMBER);
        clearCookie(response, Constant.LEGACY_COOKIE_REMEMBER);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private User authenticateRememberedUser(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = getCookieValue(request, Constant.COOKIE_REMEMBER);
        if (rawToken == null) {
            return null;
        }
        User user = rememberMeService.authenticate(rawToken);
        if (user == null) {
            clearCookie(response, Constant.COOKIE_REMEMBER);
        }
        return user;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())
                    && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void establishSession(HttpServletRequest request, User user) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(Constant.SESSION_ACCOUNT, user);
        session.setAttribute(Constant.SESSION_USERNAME, user.getUserName());
    }
}
