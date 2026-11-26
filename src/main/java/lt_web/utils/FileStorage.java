package lt_web.utils;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public final class FileStorage {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "svg", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "ogg", "mov", "mkv");

    private FileStorage() {
    }

    public static String saveImage(Part part) throws IOException {
        return savePart(part, IMAGE_EXTENSIONS, "Định dạng ảnh không được hỗ trợ.");
    }

    public static String saveVideo(Part part) throws IOException {
        return savePart(part, VIDEO_EXTENSIONS, "Định dạng video không được hỗ trợ (hỗ trợ MP4, WEBM, OGG, MOV, MKV).");
    }

    public static String saveImage(MultipartFile file) throws IOException {
        return saveMultipart(file, IMAGE_EXTENSIONS, "Định dạng ảnh không được hỗ trợ.");
    }

    public static String saveVideo(MultipartFile file) throws IOException {
        return saveMultipart(file, VIDEO_EXTENSIONS, "Định dạng video không được hỗ trợ (hỗ trợ MP4, WEBM, OGG, MOV, MKV).");
    }

    public static boolean isAllowedImage(String fileName) {
        return fileName != null && IMAGE_EXTENSIONS.contains(extensionOf(fileName));
    }

    public static boolean isAllowedVideo(String fileName) {
        return fileName != null && VIDEO_EXTENSIONS.contains(extensionOf(fileName));
    }

    public static String getFileName(Part part) {
        if (part == null) {
            return null;
        }
        String submitted = part.getSubmittedFileName();
        if (submitted != null && !submitted.isBlank()) {
            return Path.of(submitted).getFileName().toString();
        }
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String content : contentDisposition.split(";")) {
                String trimmed = content.trim();
                if (trimmed.startsWith("filename")) {
                    int eq = trimmed.indexOf('=');
                    if (eq >= 0) {
                        String name = trimmed.substring(eq + 1).trim().replace("\"", "");
                        if (!name.isBlank()) {
                            return Path.of(name).getFileName().toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getFileName(MultipartFile file) {
        if (file == null) {
            return null;
        }
        String original = file.getOriginalFilename();
        if (original != null && !original.isBlank()) {
            return Path.of(original).getFileName().toString();
        }
        return null;
    }

    private static String savePart(Part part, Set<String> allowedExtensions, String invalidTypeMessage)
            throws IOException {
        String originalName = getFileName(part);
        if (part == null || part.getSize() == 0 || originalName == null || originalName.isBlank()) {
            return null;
        }
        try (InputStream input = part.getInputStream()) {
            return storeStream(input, originalName, allowedExtensions, invalidTypeMessage);
        }
    }

    private static String saveMultipart(MultipartFile file, Set<String> allowedExtensions, String invalidTypeMessage)
            throws IOException {
        String originalName = getFileName(file);
        if (file == null || file.isEmpty() || originalName == null || originalName.isBlank()) {
            return null;
        }
        try (InputStream input = file.getInputStream()) {
            return storeStream(input, originalName, allowedExtensions, invalidTypeMessage);
        }
    }

    private static String storeStream(InputStream input, String originalName,
                                      Set<String> allowedExtensions, String invalidTypeMessage)
            throws IOException {
        String extension = extensionOf(originalName);
        if (!allowedExtensions.contains(extension)) {
            throw new IOException(invalidTypeMessage);
        }

        Path directory = Constant.uploadDirectory();
        Files.createDirectories(directory);
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(directory)) {
            throw new IOException("Đường dẫn lưu tệp không hợp lệ.");
        }
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    public static Path resolve(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        Path directory = Constant.uploadDirectory();
        Path resolved = directory.resolve(fileName).normalize();
        return resolved.startsWith(directory) ? resolved : null;
    }

    public static void delete(String fileName) throws IOException {
        Path file = resolve(fileName);
        if (file != null) {
            Files.deleteIfExists(file);
        }
    }

    public static void deleteQuietly(String fileName) {
        if (fileName == null || fileName.isBlank()
                || fileName.startsWith("default-")
                || fileName.startsWith("http://")
                || fileName.startsWith("https://")) {
            return;
        }
        try {
            delete(fileName);
        } catch (IOException ignored) {
        }
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
