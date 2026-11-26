package lt_web.controllers;

import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lt_web.utils.FileStorage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadImageController {

    @GetMapping("/image")
    public void downloadImage(
            @RequestParam(value = "fname", required = false) String fileName,
            HttpServletResponse resp) throws IOException {
        if (fileName != null && !fileName.isBlank()) {
            Path file = FileStorage.resolve(fileName);
            if (file != null && Files.isRegularFile(file)) {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".png")) {
                    resp.setContentType("image/png");
                } else if (lower.endsWith(".svg")) {
                    resp.setContentType("image/svg+xml");
                } else if (lower.endsWith(".gif")) {
                    resp.setContentType("image/gif");
                } else if (lower.endsWith(".webp")) {
                    resp.setContentType("image/webp");
                } else {
                    resp.setContentType("image/jpeg");
                }
                try (FileInputStream in = new FileInputStream(file.toFile());
                     OutputStream out = resp.getOutputStream()) {
                    in.transferTo(out);
                }
                return;
            }
        }

        resp.setContentType("image/svg+xml;charset=UTF-8");
        if (fileName != null && fileName.startsWith("default-video")) {
            String videoPosterSvg = """
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 225" width="400" height="225">
                      <defs>
                        <linearGradient id="vg" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" stop-color="#172033"/>
                          <stop offset="100%" stop-color="#2358c4"/>
                        </linearGradient>
                      </defs>
                      <rect width="400" height="225" fill="url(#vg)"/>
                      <circle cx="200" cy="105" r="34" fill="rgba(255,255,255,0.18)" stroke="#ffffff" stroke-width="2.5"/>
                      <polygon points="190,89 190,121 218,105" fill="#ffffff"/>
                      <text x="200" y="168" font-family="Arial, sans-serif" font-size="14" fill="#e2e8f0" text-anchor="middle" font-weight="bold">ShopSpringBoot Video</text>
                    </svg>
                    """;
            resp.getOutputStream().write(videoPosterSvg.getBytes(StandardCharsets.UTF_8));
            return;
        }
        String color = "#1976d2";
        String label = "Danh mục";
        if ("default-women.svg".equalsIgnoreCase(fileName)) {
            color = "#ff5722";
            label = "Nữ";
        } else if ("default-men.svg".equalsIgnoreCase(fileName)) {
            color = "#1565c0";
            label = "Nam";
        }
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 150" width="200" height="150">
                  <rect width="200" height="150" fill="#ffffff"/>
                  <path d="M65,25 L85,25 C90,35 110,35 115,25 L135,25 L160,50 L142,68 L130,55 L130,130 L70,130 L70,55 L58,68 L40,50 Z" fill="%s"/>
                  <text x="100" y="95" font-family="Arial, sans-serif" font-size="13" fill="#ffffff" text-anchor="middle" font-weight="bold">%s</text>
                </svg>
                """.formatted(color, label);
        resp.getOutputStream().write(svg.getBytes(StandardCharsets.UTF_8));
    }
}
