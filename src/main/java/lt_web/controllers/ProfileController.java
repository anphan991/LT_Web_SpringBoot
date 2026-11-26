package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProfileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);
    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "svg", "webp");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final UserService userService;
    private final OtpService otpService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public ProfileController(UserService userService, OtpService otpService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.otpService = otpService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpServletRequest req, Model model) {
        User currentUser = resolveCurrentUser(req);
        if (currentUser == null) {
            return "redirect:/login?requireAuth=1";
        }
        return renderProfile(model, currentUser, null, null, null);
    }

    @PostMapping("/profile")
    public String handlePost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "fullname", required = false) String rawFullname,
            @RequestParam(value = "phone", required = false) String rawPhone,
            @RequestParam(value = "email", required = false) String rawEmail,
            @RequestParam(value = "otp", required = false) String rawOtp,
            @RequestParam(value = "images", required = false) MultipartFile imagesFile,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            HttpServletRequest req,
            Model model) {
        User currentUser = resolveCurrentUser(req);
        if (currentUser == null) {
            return "redirect:/login?requireAuth=1";
        }

        if ("verifyEmailOtp".equals(action)) {
            return handleVerifyEmailOtp(req, model, currentUser, rawOtp);
        }
        if ("resendEmailOtp".equals(action)) {
            return handleResendEmailOtp(req, model, currentUser);
        }
        if ("cancelEmailOtp".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            }
            return renderProfile(model, currentUser, null, null, "Đã hủy yêu cầu đổi địa chỉ email.");
        }

        MultipartFile imagePart = (imagesFile != null && !imagesFile.isEmpty()) ? imagesFile
                : ((avatarFile != null && !avatarFile.isEmpty()) ? avatarFile : null);
        return handleUpdateProfile(req, model, currentUser, rawFullname, rawPhone, rawEmail, imagePart);
    }

    private String handleUpdateProfile(
            HttpServletRequest req,
            Model model,
            User currentUser,
            String rawFullname,
            String rawPhone,
            String rawEmail,
            MultipartFile imagePart) {
        String fullname = trimOrEmpty(rawFullname);
        String phone = trimOrEmpty(rawPhone);
        String email = trimOrEmpty(rawEmail).toLowerCase(Locale.ROOT);
        String currentEmail = trimOrEmpty(currentUser.getEmail()).toLowerCase(Locale.ROOT);

        Map<String, String> errors = new LinkedHashMap<>();

        if (fullname.isEmpty()) {
            errors.put("fullname", "Vui lòng nhập họ và tên.");
        } else if (fullname.length() < 2 || fullname.length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Số điện thoại không hợp lệ (gồm 9-15 chữ số).");
        }

        if (email.isEmpty()) {
            errors.put("email", "Vui lòng nhập email.");
        } else if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }

        if (imagePart != null && !imagePart.isEmpty()) {
            if (imagePart.getSize() > MAX_IMAGE_BYTES) {
                errors.put("images", "Kích thước ảnh đại diện tối đa là 5MB.");
            } else {
                String ext = extensionOf(FileStorage.getFileName(imagePart));
                if (!ALLOWED_IMAGE_EXT.contains(ext)) {
                    errors.put("images", "Chỉ hỗ trợ định dạng ảnh JPG, PNG, GIF, SVG hoặc WEBP.");
                }
            }
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("draftFullname", fullname);
            model.addAttribute("draftPhone", phone);
            model.addAttribute("draftEmail", email);
            return renderProfile(model, currentUser, errors.values().iterator().next(), null, null);
        }

        try {
            if (!phone.isEmpty() && userService.checkExistPhone(phone, currentUser.getId())) {
                errors.put("phone", "Số điện thoại này đã được sử dụng bởi tài khoản khác.");
            }
            if (!email.equals(currentEmail) && userService.checkExistEmail(email, currentUser.getId())) {
                errors.put("email", "Email này đã được sử dụng bởi tài khoản khác.");
            }
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                model.addAttribute("draftFullname", fullname);
                model.addAttribute("draftPhone", phone);
                model.addAttribute("draftEmail", email);
                return renderProfile(model, currentUser, errors.values().iterator().next(), null, null);
            }

            String newAvatar = null;
            boolean uploadedToCloudinary = false;
            boolean usedLocalFallback = false;

            if (imagePart != null && !imagePart.isEmpty()) {
                if (cloudinaryService.isConfigured()) {
                    CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(imagePart);
                    if (!uploadResult.isSuccess()) {
                        errors.put("images", uploadResult.getErrorMessage());
                        model.addAttribute("errors", errors);
                        model.addAttribute("draftFullname", fullname);
                        model.addAttribute("draftPhone", phone);
                        model.addAttribute("draftEmail", email);
                        return renderProfile(model, currentUser, uploadResult.getErrorMessage(), null, null);
                    }
                    if (uploadResult.getSecureUrl().length() > 255) {
                        String msg = "URL ảnh Cloudinary vượt quá 255 ký tự.";
                        errors.put("images", msg);
                        model.addAttribute("errors", errors);
                        return renderProfile(model, currentUser, msg, null, null);
                    }
                    newAvatar = uploadResult.getSecureUrl();
                    uploadedToCloudinary = true;
                } else {
                    newAvatar = FileStorage.saveImage(imagePart);
                    usedLocalFallback = true;
                }
            }

            String oldAvatar = currentUser.getAvatar();
            User updatedUser = userService.updateProfile(currentUser.getId(), fullname, phone, newAvatar);
            if (updatedUser == null) {
                return renderProfile(model, currentUser, "Không tìm thấy tài khoản trong cơ sở dữ liệu MySQL. Vui lòng đăng nhập lại.", null, null);
            }
            if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
                FileStorage.deleteQuietly(oldAvatar);
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(Constant.SESSION_ACCOUNT, updatedUser);
            currentUser = updatedUser;

            String avatarNote = "";
            if (uploadedToCloudinary) {
                avatarNote = " Ảnh đại diện đã được tải lên Cloudinary.";
            } else if (usedLocalFallback) {
                avatarNote = " Ảnh đại diện đã lưu cục bộ (chưa cấu hình Cloudinary trong application.properties / db.properties).";
            }

            if (!email.equals(currentEmail)) {
                session.setAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL, email);
                otpService.generateAndSendEmailChangeOtp(email, fullname);
                String notice = "Đã cập nhật họ tên / số điện thoại"
                        + (avatarNote.isEmpty() ? "." : "." + avatarNote)
                        + " Hệ thống đã gửi mã OTP 6 chữ số đến email mới (" + email
                        + "). Vui lòng nhập mã OTP bên dưới để xác nhận thay đổi email.";
                return renderProfile(model, currentUser, null, null, notice);
            }

            session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            return renderProfile(model, currentUser, null, "Cập nhật thông tin hồ sơ thành công!" + avatarNote, null);
        } catch (DataAccessException e) {
            LOGGER.warn("Lỗi cơ sở dữ liệu khi cập nhật hồ sơ cá nhân", e);
            return renderProfile(model, currentUser, "Không thể lưu thông tin vào cơ sở dữ liệu. Vui lòng thử lại.", null, null);
        } catch (IllegalStateException e) {
            LOGGER.warn("Ảnh đại diện vượt dung lượng tối đa", e);
            errors.put("images", "Ảnh đại diện vượt quá dung lượng tối đa 5MB.");
            model.addAttribute("errors", errors);
            return renderProfile(model, currentUser, errors.get("images"), null, null);
        } catch (Exception e) {
            LOGGER.warn("Không thể cập nhật hồ sơ cá nhân", e);
            return renderProfile(model, currentUser, "Không thể xử lý yêu cầu cập nhật hồ sơ. Vui lòng thử lại.", null, null);
        }
    }

    private String handleVerifyEmailOtp(HttpServletRequest req, Model model, User currentUser, String rawOtp) {
        HttpSession session = req.getSession(false);
        String pendingEmail = session != null
                ? trimOrEmpty((String) session.getAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL))
                : "";
        if (pendingEmail.isEmpty()) {
            return renderProfile(model, currentUser, "Không có yêu cầu đổi email nào đang chờ xác nhận.", null, null);
        }

        String otp = trimOrEmpty(rawOtp);
        Map<String, String> errors = new LinkedHashMap<>();
        if (otp.isEmpty()) {
            errors.put("otp", "Vui lòng nhập mã OTP gồm 6 chữ số.");
        } else if (!ValidationUtil.isValidOtp(otp)) {
            errors.put("otp", "Mã OTP phải gồm đúng 6 chữ số.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return renderProfile(model, currentUser, errors.get("otp"), null, null);
        }

        try {
            if (userService.checkExistEmail(pendingEmail, currentUser.getId())) {
                session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
                return renderProfile(model, currentUser, "Email mới đã được tài khoản khác sử dụng. Vui lòng nhập email khác.", null, null);
            }

            if (!otpService.verifyEmailChangeOtp(pendingEmail, otp)) {
                errors.put("otp", "Mã OTP không chính xác hoặc đã hết hạn (5 phút).");
                model.addAttribute("errors", errors);
                return renderProfile(model, currentUser, errors.get("otp"), null, null);
            }

            User updated = userService.updateEmail(currentUser.getId(), pendingEmail);
            if (updated != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, updated);
                currentUser = updated;
            }
            session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            return renderProfile(model, currentUser, null,
                    "Xác nhận OTP thành công! Email mới (" + pendingEmail + ") đã được cập nhật.", null);
        } catch (DataAccessException e) {
            LOGGER.warn("Lỗi cơ sở dữ liệu khi xác nhận OTP đổi email", e);
            return renderProfile(model, currentUser, "Không thể hoàn tất đổi email. Vui lòng thử lại.", null, null);
        }
    }

    private String handleResendEmailOtp(HttpServletRequest req, Model model, User currentUser) {
        HttpSession session = req.getSession(false);
        String pendingEmail = session != null
                ? trimOrEmpty((String) session.getAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL))
                : "";
        if (pendingEmail.isEmpty()) {
            return renderProfile(model, currentUser, "Không có email mới đang chờ xác nhận.", null, null);
        }
        try {
            otpService.generateAndSendEmailChangeOtp(pendingEmail, currentUser.getFullName());
            return renderProfile(model, currentUser, null, null,
                    "Đã gửi lại mã OTP 6 chữ số đến " + pendingEmail + ".");
        } catch (DataAccessException e) {
            LOGGER.warn("Lỗi khi gửi lại OTP đổi email", e);
            return renderProfile(model, currentUser, "Không thể gửi lại mã OTP lúc này. Vui lòng thử lại.", null, null);
        }
    }

    private User resolveCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        User sessionUser = session != null ? (User) session.getAttribute(Constant.SESSION_ACCOUNT) : null;
        if (sessionUser == null) {
            return null;
        }
        User fresh = userService.get(sessionUser.getId());
        if (fresh == null && sessionUser.getUserName() != null && !sessionUser.getUserName().isBlank()) {
            fresh = userService.get(sessionUser.getUserName());
        }
        if (fresh != null) {
            session.setAttribute(Constant.SESSION_ACCOUNT, fresh);
            session.setAttribute(Constant.SESSION_USERNAME, fresh.getUserName());
            return fresh;
        }
        session.invalidate();
        return null;
    }

    private String renderProfile(Model model, User currentUser, String alert, String successMsg, String infoMsg) {
        model.addAttribute("profileUser", currentUser);
        model.addAttribute("cloudinaryConfigured", cloudinaryService.isConfigured());
        model.addAttribute("cloudinaryCloudName", cloudinaryService.getCloudName());
        if (alert != null) {
            model.addAttribute("alert", alert);
        }
        if (successMsg != null) {
            model.addAttribute("successMsg", successMsg);
        }
        if (infoMsg != null) {
            model.addAttribute("infoMsg", infoMsg);
        }
        return Constant.View.PROFILE;
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
