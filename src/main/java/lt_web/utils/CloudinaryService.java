package lt_web.utils;

import jakarta.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tích hợp upload ảnh và video lên Cloudinary thông qua API multipart/form-data (Spring Bean).
 */
@Service
public class CloudinaryService {
    private static final String DEFAULT_FOLDER = "shopmvc/avatars";
    private static final Pattern SECURE_URL_PATTERN = Pattern.compile("\"secure_url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PUBLIC_ID_PATTERN = Pattern.compile("\"public_id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String uploadPreset;
    private final String folder;
    private volatile HttpClient httpClient;

    public CloudinaryService() {
        Properties props = loadProperties();
        String urlConfig = resolve(props, "cloudinary.url", "CLOUDINARY_URL", "");
        String[] parsedUrl = parseCloudinaryUrl(urlConfig);

        this.cloudName = firstNonBlank(resolve(props, "cloudinary.cloud_name", "CLOUDINARY_CLOUD_NAME", ""), parsedUrl[0]);
        this.apiKey = firstNonBlank(resolve(props, "cloudinary.api_key", "CLOUDINARY_API_KEY", ""), parsedUrl[1]);
        this.apiSecret = firstNonBlank(resolve(props, "cloudinary.api_secret", "CLOUDINARY_API_SECRET", ""), parsedUrl[2]);
        this.uploadPreset = resolve(props, "cloudinary.upload_preset", "CLOUDINARY_UPLOAD_PRESET", "");
        String configuredFolder = resolve(props, "cloudinary.folder", "CLOUDINARY_FOLDER", DEFAULT_FOLDER);
        this.folder = configuredFolder.isBlank() ? DEFAULT_FOLDER : configuredFolder;
    }

    @Autowired
    public CloudinaryService(
            @Value("${cloudinary.url:}") String cloudinaryUrl,
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret,
            @Value("${cloudinary.upload_preset:}") String uploadPreset,
            @Value("${cloudinary.folder:shopmvc/avatars}") String folder) {
        Properties props = loadProperties();
        String effectiveUrl = firstNonBlank(cloudinaryUrl, resolve(props, "cloudinary.url", "CLOUDINARY_URL", ""));
        String[] parsedUrl = parseCloudinaryUrl(effectiveUrl);

        this.cloudName = firstNonBlank(
                firstNonBlank(cloudName, resolve(props, "cloudinary.cloud_name", "CLOUDINARY_CLOUD_NAME", "")),
                parsedUrl[0]);
        this.apiKey = firstNonBlank(
                firstNonBlank(apiKey, resolve(props, "cloudinary.api_key", "CLOUDINARY_API_KEY", "")),
                parsedUrl[1]);
        this.apiSecret = firstNonBlank(
                firstNonBlank(apiSecret, resolve(props, "cloudinary.api_secret", "CLOUDINARY_API_SECRET", "")),
                parsedUrl[2]);
        this.uploadPreset = firstNonBlank(
                uploadPreset,
                resolve(props, "cloudinary.upload_preset", "CLOUDINARY_UPLOAD_PRESET", ""));
        String configuredFolder = firstNonBlank(
                folder,
                resolve(props, "cloudinary.folder", "CLOUDINARY_FOLDER", DEFAULT_FOLDER));
        this.folder = configuredFolder.isBlank() ? DEFAULT_FOLDER : configuredFolder;
    }

    public CloudinaryService(String cloudName, String apiKey, String apiSecret, String uploadPreset, String folder) {
        this.cloudName = safeTrim(cloudName);
        this.apiKey = safeTrim(apiKey);
        this.apiSecret = safeTrim(apiSecret);
        this.uploadPreset = safeTrim(uploadPreset);
        String cleanFolder = safeTrim(folder);
        this.folder = cleanFolder.isEmpty() ? DEFAULT_FOLDER : cleanFolder;
    }

    private HttpClient getHttpClient() {
        HttpClient client = this.httpClient;
        if (client == null) {
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            this.httpClient = client;
        }
        return client;
    }

    public boolean isConfigured() {
        return !cloudName.isEmpty() && (isSignedConfigured() || !uploadPreset.isEmpty());
    }

    public boolean isSignedConfigured() {
        return !cloudName.isEmpty() && !apiKey.isEmpty() && !apiSecret.isEmpty();
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getFolder() {
        return folder;
    }

    public UploadResult uploadImage(Part filePart) throws IOException {
        return uploadImage(filePart, null);
    }

    public UploadResult uploadImage(Part filePart, String targetFolder) throws IOException {
        return uploadMediaPart(filePart, "image", targetFolder, "avatar.png", "Không có tệp ảnh nào được chọn.", 30);
    }

    public UploadResult uploadVideo(Part filePart, String targetFolder) throws IOException {
        return uploadMediaPart(filePart, "video", targetFolder, "video.mp4", "Không có tệp video nào được chọn.", 120);
    }

    public UploadResult uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    public UploadResult uploadImage(MultipartFile file, String targetFolder) throws IOException {
        return uploadMultipartFile(file, "image", targetFolder, "avatar.png", "Không có tệp ảnh nào được chọn.", 30);
    }

    public UploadResult uploadVideo(MultipartFile file, String targetFolder) throws IOException {
        return uploadMultipartFile(file, "video", targetFolder, "video.mp4", "Không có tệp video nào được chọn.", 120);
    }

    public String saveImageWithCloudFallback(Part part, String targetFolder, int maxLength) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String fileName = FileStorage.getFileName(part);
        if (!FileStorage.isAllowedImage(fileName)) {
            throw new IOException("Định dạng ảnh không được hỗ trợ.");
        }
        if (!isConfigured()) {
            return FileStorage.saveImage(part);
        }
        UploadResult result = uploadImage(part, targetFolder);
        if (!result.isSuccess()) {
            throw new IOException(result.getErrorMessage());
        }
        if (result.getSecureUrl().length() > maxLength) {
            throw new IOException("URL ảnh Cloudinary vượt quá " + maxLength + " ký tự.");
        }
        return result.getSecureUrl();
    }

    public String saveImageWithCloudFallback(MultipartFile file, String targetFolder, int maxLength) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String fileName = FileStorage.getFileName(file);
        if (!FileStorage.isAllowedImage(fileName)) {
            throw new IOException("Định dạng ảnh không được hỗ trợ.");
        }
        if (!isConfigured()) {
            return FileStorage.saveImage(file);
        }
        UploadResult result = uploadImage(file, targetFolder);
        if (!result.isSuccess()) {
            throw new IOException(result.getErrorMessage());
        }
        if (result.getSecureUrl().length() > maxLength) {
            throw new IOException("URL ảnh Cloudinary vượt quá " + maxLength + " ký tự.");
        }
        return result.getSecureUrl();
    }

    public String saveVideoWithCloudFallback(Part part, String targetFolder, int maxLength) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String fileName = FileStorage.getFileName(part);
        if (!FileStorage.isAllowedVideo(fileName)) {
            throw new IOException("Định dạng video không được hỗ trợ (hỗ trợ MP4, WEBM, OGG, MOV, MKV).");
        }
        if (!isConfigured()) {
            return FileStorage.saveVideo(part);
        }
        UploadResult result = uploadVideo(part, targetFolder);
        if (!result.isSuccess()) {
            throw new IOException(result.getErrorMessage());
        }
        if (result.getSecureUrl().length() > maxLength) {
            throw new IOException("URL video Cloudinary vượt quá " + maxLength + " ký tự.");
        }
        return result.getSecureUrl();
    }

    public String saveVideoWithCloudFallback(MultipartFile file, String targetFolder, int maxLength) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String fileName = FileStorage.getFileName(file);
        if (!FileStorage.isAllowedVideo(fileName)) {
            throw new IOException("Định dạng video không được hỗ trợ (hỗ trợ MP4, WEBM, OGG, MOV, MKV).");
        }
        if (!isConfigured()) {
            return FileStorage.saveVideo(file);
        }
        UploadResult result = uploadVideo(file, targetFolder);
        if (!result.isSuccess()) {
            throw new IOException(result.getErrorMessage());
        }
        if (result.getSecureUrl().length() > maxLength) {
            throw new IOException("URL video Cloudinary vượt quá " + maxLength + " ký tự.");
        }
        return result.getSecureUrl();
    }

    private UploadResult uploadMediaPart(Part filePart, String resourceType, String targetFolder,
                                         String fallbackName, String emptyMessage, int timeoutSeconds)
            throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return UploadResult.failure(emptyMessage);
        }
        String fileName = FileStorage.getFileName(filePart);
        if (fileName == null || fileName.isBlank()) {
            fileName = fallbackName;
        }
        String contentType = filePart.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        try (InputStream in = filePart.getInputStream()) {
            byte[] bytes = in.readAllBytes();
            return uploadMedia(bytes, fileName, contentType, resourceType, targetFolder, timeoutSeconds);
        }
    }

    private UploadResult uploadMultipartFile(MultipartFile file, String resourceType, String targetFolder,
                                             String fallbackName, String emptyMessage, int timeoutSeconds)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return UploadResult.failure(emptyMessage);
        }
        String fileName = FileStorage.getFileName(file);
        if (fileName == null || fileName.isBlank()) {
            fileName = fallbackName;
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        return uploadMedia(file.getBytes(), fileName, contentType, resourceType, targetFolder, timeoutSeconds);
    }

    public UploadResult uploadImage(byte[] fileBytes, String fileName, String contentType) {
        return uploadMedia(fileBytes, fileName, contentType, "image", null, 30);
    }

    public UploadResult uploadMedia(byte[] fileBytes, String fileName, String contentType,
                                    String resourceType, String targetFolder, int timeoutSeconds) {
        if (!isConfigured()) {
            return UploadResult.failure("Chưa cấu hình Cloudinary (cloudinary.cloud_name / api_key / api_secret).");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            return UploadResult.failure("Dữ liệu tệp tải lên trống.");
        }

        String safeResource = "video".equalsIgnoreCase(resourceType) ? "video" : "image";
        String safeFileName = (fileName == null || fileName.isBlank())
                ? ("video".equals(safeResource) ? "video.mp4" : "image.png") : fileName;
        String safeContentType = (contentType == null || contentType.isBlank())
                ? ("video".equals(safeResource) ? "video/mp4" : "image/png") : contentType;
        String boundary = "----ShopMvcCloudinary" + UUID.randomUUID().toString().replace("-", "");

        Map<String, String> fields = buildUploadFields(Instant.now().getEpochSecond(), targetFolder);
        byte[] body = buildMultipartBody(boundary, fields, safeFileName, safeContentType, fileBytes);

        URI endpoint = URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/" + safeResource + "/upload");
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(Math.max(15, timeoutSeconds)))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        try {
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String respBody = response.body() == null ? "" : response.body();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String secureUrl = extractJsonString(SECURE_URL_PATTERN, respBody);
                if (secureUrl == null) {
                    secureUrl = extractJsonString(URL_PATTERN, respBody);
                }
                String publicId = extractJsonString(PUBLIC_ID_PATTERN, respBody);
                if (secureUrl != null && !secureUrl.isBlank()) {
                    return UploadResult.success(secureUrl, publicId);
                }
                return UploadResult.failure("Cloudinary không trả về đường dẫn tệp.");
            }
            String remoteError = extractJsonString(ERROR_MESSAGE_PATTERN, respBody);
            return UploadResult.failure(remoteError != null
                    ? "Cloudinary lỗi (" + response.statusCode() + "): " + remoteError
                    : "Upload Cloudinary thất bại (HTTP " + response.statusCode() + ").");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return UploadResult.failure("Quá trình tải lên Cloudinary bị gián đoạn.");
        } catch (IOException e) {
            return UploadResult.failure("Không thể kết nối tới Cloudinary: " + e.getMessage());
        }
    }

    Map<String, String> buildUploadFields(long timestampSeconds) {
        return buildUploadFields(timestampSeconds, null);
    }

    Map<String, String> buildUploadFields(long timestampSeconds, String targetFolder) {
        Map<String, String> fields = new TreeMap<>();
        String effectiveFolder = (targetFolder != null && !targetFolder.isBlank()) ? targetFolder.trim() : folder;
        if (!effectiveFolder.isEmpty()) {
            fields.put("folder", effectiveFolder);
        }
        if (isSignedConfigured()) {
            fields.put("timestamp", String.valueOf(timestampSeconds));
            String signature = generateSignature(fields, apiSecret);
            fields.put("api_key", apiKey);
            fields.put("signature", signature);
        } else if (!uploadPreset.isEmpty()) {
            fields.put("upload_preset", uploadPreset);
        }
        return fields;
    }

    static String generateSignature(Map<String, String> paramsToSign, String secret) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : paramsToSign.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null || value.isBlank()) {
                continue;
            }
            if ("file".equals(key) || "api_key".equals(key) || "signature".equals(key) || "resource_type".equals(key)) {
                continue;
            }
            sorted.put(key, value);
        }

        StringBuilder payload = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (payload.length() > 0) {
                payload.append('&');
            }
            payload.append(entry.getKey()).append('=').append(entry.getValue());
        }
        payload.append(secret == null ? "" : secret);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format(Locale.ROOT, "%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm unavailable", e);
        }
    }

    static byte[] buildMultipartBody(String boundary, Map<String, String> fields,
                                     String fileName, String contentType, byte[] fileBytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String lineBreak = "\r\n";
        try {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                out.write(("--" + boundary + lineBreak).getBytes(StandardCharsets.UTF_8));
                out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineBreak)
                        .getBytes(StandardCharsets.UTF_8));
                out.write(lineBreak.getBytes(StandardCharsets.UTF_8));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.write(lineBreak.getBytes(StandardCharsets.UTF_8));
            }

            String cleanFileName = fileName.replace("\"", "").replace("\r", "").replace("\n", "");
            out.write(("--" + boundary + lineBreak).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + cleanFileName + "\"" + lineBreak)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: " + contentType + lineBreak).getBytes(StandardCharsets.UTF_8));
            out.write(lineBreak.getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
            out.write(lineBreak.getBytes(StandardCharsets.UTF_8));
            out.write(("--" + boundary + "--" + lineBreak).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build multipart body", e);
        }
        return out.toByteArray();
    }

    static String[] parseCloudinaryUrl(String rawUrl) {
        String[] result = {"", "", ""};
        if (rawUrl == null || rawUrl.isBlank()) {
            return result;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            if (!"cloudinary".equalsIgnoreCase(uri.getScheme())) {
                return result;
            }
            String cloud = uri.getHost() == null ? "" : uri.getHost().trim();
            String userInfo = uri.getUserInfo() == null ? "" : uri.getUserInfo();
            int colon = userInfo.indexOf(':');
            if (colon > 0) {
                result[0] = cloud;
                result[1] = userInfo.substring(0, colon).trim();
                result[2] = userInfo.substring(colon + 1).trim();
            }
        } catch (IllegalArgumentException ignored) {
            return new String[]{"", "", ""};
        }
        return result;
    }

    static String extractJsonString(Pattern pattern, String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\/", "/");
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        loadFromClasspath(props, "db.properties");
        loadFromClasspath(props, "cloudinary.properties");
        return props;
    }

    private void loadFromClasspath(Properties target, String resourceName) {
        try (InputStream in = CloudinaryService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in != null) {
                target.load(in);
            }
        } catch (IOException ignored) {
        }
    }

    private String resolve(Properties props, String propKey, String envKey, String defaultValue) {
        String sys = System.getProperty(propKey);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String fromFile = props.getProperty(propKey);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }
        return defaultValue;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class UploadResult {
        private final boolean success;
        private final String secureUrl;
        private final String publicId;
        private final String errorMessage;

        private UploadResult(boolean success, String secureUrl, String publicId, String errorMessage) {
            this.success = success;
            this.secureUrl = secureUrl;
            this.publicId = publicId;
            this.errorMessage = errorMessage;
        }

        public static UploadResult success(String secureUrl, String publicId) {
            return new UploadResult(true, secureUrl, publicId, null);
        }

        public static UploadResult failure(String errorMessage) {
            return new UploadResult(false, null, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSecureUrl() {
            return secureUrl;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
