package lt_web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CloudinaryServiceTest {

    @Test
    void parsesCloudinaryUrlIntoCredentials() {
        String[] parsed = CloudinaryService.parseCloudinaryUrl("cloudinary://123456789:my_secret_key@mycloud");
        assertEquals("mycloud", parsed[0]);
        assertEquals("123456789", parsed[1]);
        assertEquals("my_secret_key", parsed[2]);
    }

    @Test
    void generatesSignedUploadFieldsAndDeterministicSha1Signature() {
        CloudinaryService service = new CloudinaryService("mycloud", "12345", "abcd", "", "shopmvc/avatars");
        assertTrue(service.isConfigured());
        assertTrue(service.isSignedConfigured());

        Map<String, String> fields = service.buildUploadFields(1700000000L);
        assertEquals("shopmvc/avatars", fields.get("folder"));
        assertEquals("1700000000", fields.get("timestamp"));
        assertEquals("12345", fields.get("api_key"));
        assertTrue(fields.get("signature").matches("^[a-f0-9]{40}$"));

        Map<String, String> categoryFields = service.buildUploadFields(1700000000L, "shopmvc/categories");
        assertEquals("shopmvc/categories", categoryFields.get("folder"));
        assertTrue(categoryFields.get("signature").matches("^[a-f0-9]{40}$"));
    }

    @Test
    void validatesAllowedImageAndVideoExtensions() {
        assertTrue(FileStorage.isAllowedImage("photo.png"));
        assertTrue(FileStorage.isAllowedImage("banner.WEBP"));
        assertFalse(FileStorage.isAllowedImage("script.exe"));
        assertTrue(FileStorage.isAllowedVideo("clip.mp4"));
        assertTrue(FileStorage.isAllowedVideo("demo.MOV"));
        assertFalse(FileStorage.isAllowedVideo("archive.zip"));
    }

    @Test
    void buildsValidMultipartBodyWithFileAndFields() {
        byte[] body = CloudinaryService.buildMultipartBody(
                "BoundaryTest123",
                Map.of("folder", "shopmvc/avatars", "upload_preset", "unsigned_demo"),
                "avatar.png",
                "image/png",
                "PNGDATA".getBytes(StandardCharsets.UTF_8)
        );
        String text = new String(body, StandardCharsets.UTF_8);
        assertTrue(text.contains("--BoundaryTest123"));
        assertTrue(text.contains("name=\"folder\""));
        assertTrue(text.contains("shopmvc/avatars"));
        assertTrue(text.contains("name=\"file\"; filename=\"avatar.png\""));
        assertTrue(text.contains("Content-Type: image/png"));
        assertTrue(text.contains("PNGDATA"));
        assertTrue(text.contains("--BoundaryTest123--"));
    }

    @Test
    void unconfiguredServiceReturnsClearFailure() {
        CloudinaryService service = new CloudinaryService("", "", "", "", "");
        assertFalse(service.isConfigured());
        CloudinaryService.UploadResult result = service.uploadImage(new byte[]{1, 2, 3}, "a.png", "image/png");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Cloudinary"));
    }

    @Test
    void extractsSecureUrlFromCloudinaryJson() {
        String json = "{\"public_id\":\"shopmvc/avatars/u1\",\"secure_url\":\"https:\\/\\/res.cloudinary.com\\/demo\\/image\\/upload\\/v1\\/avatar.png\"}";
        String url = CloudinaryService.extractJsonString(Pattern.compile("\"secure_url\"\\s*:\\s*\"([^\"]+)\""), json);
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/avatar.png", url);
    }
}
