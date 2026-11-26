package lt_web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadVideoController {
    private static final int BUFFER_SIZE = 8192;
    private final UserService userService;

    @Autowired
    public DownloadVideoController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/video-file")
    public void downloadVideo(
            @RequestParam(value = "fname", required = false) String fileName,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        if (fileName == null || fileName.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path file = FileStorage.resolve(fileName);
        if (file == null || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = Files.size(file);
        resp.setContentType(resolveContentType(fileName));
        resp.setHeader("Accept-Ranges", "bytes");

        String rangeHeader = req.getHeader("Range");
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Length", String.valueOf(fileLength));
            streamRange(file, 0, fileLength - 1, resp.getOutputStream());
            return;
        }

        String rangeSpec = rangeHeader.substring("bytes=".length()).split(",")[0].trim();
        String[] parts = rangeSpec.split("-", -1);
        long start;
        long end;
        try {
            if (parts[0].isEmpty()) {
                long suffixLength = Long.parseLong(parts[1]);
                start = Math.max(fileLength - suffixLength, 0);
                end = fileLength - 1;
            } else {
                start = Long.parseLong(parts[0]);
                end = (parts.length > 1 && !parts[1].isEmpty())
                        ? Math.min(Long.parseLong(parts[1]), fileLength - 1)
                        : fileLength - 1;
            }
        } catch (NumberFormatException e) {
            resp.setHeader("Content-Range", "bytes */" + fileLength);
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        if (start < 0 || start > end || start >= fileLength) {
            resp.setHeader("Content-Range", "bytes */" + fileLength);
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        long contentLength = end - start + 1;
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        resp.setHeader("Content-Length", String.valueOf(contentLength));
        streamRange(file, start, end, resp.getOutputStream());
    }

    private void streamRange(Path file, long start, long end, OutputStream out) throws IOException {
        if (end < start) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(start);
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = end - start + 1;
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int read = raf.read(buffer, 0, toRead);
                if (read == -1) {
                    break;
                }
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private String resolveContentType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        if (lower.endsWith(".ogg")) {
            return "video/ogg";
        }
        if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        return "video/mp4";
    }

    private boolean isAuthenticated(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        if (session.getAttribute(Constant.SESSION_ACCOUNT) instanceof User) {
            return true;
        }
        Object usernameObj = session.getAttribute(Constant.SESSION_USERNAME);
        if (usernameObj instanceof String username && !username.isBlank()) {
            User loaded = userService.get(username);
            if (loaded != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, loaded);
                session.setAttribute(Constant.SESSION_USERNAME, loaded.getUserName());
                return true;
            }
        }
        return false;
    }
}
