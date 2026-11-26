package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WaitingController {
    private final UserService userService;

    @Autowired
    public WaitingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/waiting")
    public String waiting(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User u = (User) session.getAttribute(Constant.SESSION_ACCOUNT);
            if (u == null && session.getAttribute(Constant.SESSION_USERNAME) != null) {
                u = userService.get(String.valueOf(session.getAttribute(Constant.SESSION_USERNAME)));
                if (u != null) {
                    session.setAttribute(Constant.SESSION_ACCOUNT, u);
                }
            }
            if (u != null) {
                req.setAttribute("username", u.getUserName());
                if (u.getRoleid() == 1) {
                    return "redirect:/admin/home";
                } else if (u.getRoleid() == 2) {
                    return "redirect:/manager/home";
                } else {
                    return "redirect:/home";
                }
            }
        }
        return "redirect:/login";
    }
}
