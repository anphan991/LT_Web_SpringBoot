package lt_web.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lt_web.entity.User;

public final class AdminAccess {
    private AdminAccess() {
    }

    public static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        User account = (User) session.getAttribute(Constant.SESSION_ACCOUNT);
        return account != null && account.getRoleid() == 1;
    }
}
