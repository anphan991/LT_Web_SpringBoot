package lt_web.utils;

import java.nio.file.Paths;

public class Constant {
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ACCOUNT = "account";
    public static final String SESSION_PENDING_PROFILE_EMAIL = "profilePendingEmail";
    public static final String COOKIE_REMEMBER = "remember_token";
    public static final String LEGACY_COOKIE_REMEMBER = "username";

    private Constant() {
    }

    public static java.nio.file.Path uploadDirectory() {
        String configured = firstNonBlank(
                System.getProperty("bt.lt.upload.dir"),
                System.getenv("BT_LT_UPLOAD_DIR"));
        if (configured == null) {
            configured = Paths.get(System.getProperty("user.home"), "BT_SpringBoot", "uploads").toString();
        }
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }

    public static class View {
        public static final String INDEX = "index";
        public static final String HOME = "home";
        public static final String LOGIN = "login";
        public static final String REGISTER = "register";
        public static final String VERIFY_OTP = "verify-otp";
        public static final String FORGOT_PASSWORD = "forgot-password";
        public static final String RESET_PASSWORD = "reset-password";
        public static final String PROFILE = "profile";
        public static final String VIDEO_LIST = "video-list";
        public static final String VIDEO_DETAIL = "video-detail";
        public static final String VIDEO_FORM = "video-form";
        public static final String ERROR = "error";
        public static final String ADMIN_LIST_CATEGORY = "admin/list-category";
        public static final String ADMIN_ADD_CATEGORY = "admin/add-category";
        public static final String ADMIN_EDIT_CATEGORY = "admin/edit-category";
        public static final String ADMIN_DELETE_CATEGORY = "admin/delete-category";
        public static final String ADMIN_USER_FORM = "admin/user-form";
    }

    public static class Path {
        public static final String LOGIN = "/WEB-INF/views/login.jsp";
        public static final String REGISTER = "/WEB-INF/views/register.jsp";
        public static final String VERIFY_OTP = "/WEB-INF/views/verify-otp.jsp";
        public static final String FORGOT_PASSWORD = "/WEB-INF/views/forgot-password.jsp";
        public static final String RESET_PASSWORD = "/WEB-INF/views/reset-password.jsp";
        public static final String HOME = "/WEB-INF/views/home.jsp";
        public static final String PROFILE = "/WEB-INF/views/profile.jsp";
        public static final String VIDEO_LIST = "/WEB-INF/views/video-list.jsp";
        public static final String VIDEO_DETAIL = "/WEB-INF/views/video-detail.jsp";
        public static final String VIDEO_FORM = "/WEB-INF/views/video-form.jsp";
        public static final String ADMIN_LIST_CATEGORY = "/WEB-INF/views/admin/list-category.jsp";
        public static final String ADMIN_ADD_CATEGORY = "/WEB-INF/views/admin/add-category.jsp";
        public static final String ADMIN_EDIT_CATEGORY = "/WEB-INF/views/admin/edit-category.jsp";
        public static final String ADMIN_DELETE_CATEGORY = "/WEB-INF/views/admin/delete-category.jsp";
        public static final String ADMIN_USER_FORM = "/WEB-INF/views/admin/user-form.jsp";
    }
}
