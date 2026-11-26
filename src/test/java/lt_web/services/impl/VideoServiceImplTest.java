package lt_web.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.repository.IVideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VideoServiceImplTest {
    private InMemoryVideoRepository videoRepo;
    private VideoServiceImpl videoService;
    private User owner;
    private User otherUser;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        System.setProperty("bt.lt.upload.dir", tempUploadDir.toString());
        videoRepo = new InMemoryVideoRepository();
        videoService = new VideoServiceImpl(videoRepo);
        owner = new User(1, "owner@example.com", "owner", "Owner User", "hash", null, 5, "0901111111",
                new Date(System.currentTimeMillis()));
        otherUser = new User(2, "other@example.com", "other", "Other User", "hash", null, 5, "0902222222",
                new Date(System.currentTimeMillis()));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("bt.lt.upload.dir");
    }

    @Test
    void createLinksVideoToUserAndFindsLatestTenActive() {
        LocalDateTime base = LocalDateTime.now().minusHours(15);
        for (int i = 1; i <= 12; i++) {
            Video v = new Video(0, "Video " + i, null, null, "Desc " + i, 0, 1, base.plusHours(i), owner);
            videoService.create(v);
        }
        Video hidden = new Video(0, "Hidden Latest", null, null, "Hidden", 0, 0, base.plusHours(20), owner);
        videoService.create(hidden);

        List<Video> latest = videoService.findLatest(10);
        assertEquals(10, latest.size());
        assertEquals("Video 12", latest.get(0).getTitle());
        assertEquals("Video 3", latest.get(9).getTitle());
        assertEquals(owner.getId(), latest.get(0).getUser().getId());
    }

    @Test
    void paginateUserVideosSixPerPageAndIncrementViews() {
        LocalDateTime base = LocalDateTime.now().minusHours(20);
        for (int i = 1; i <= 8; i++) {
            videoService.create(new Video(0, "Mine " + i, "default-video-poster.svg", "default-video.mp4",
                    "Description " + i, 0, 1, base.plusHours(i), owner));
        }
        videoService.create(new Video(0, "Other Video", "default-video-poster.svg", "default-video.mp4",
                "Other", 0, 1, base.plusHours(15), otherUser));

        assertEquals(8, videoService.countByUserId(owner.getId()));
        assertEquals(1, videoService.countByUserId(otherUser.getId()));

        List<Video> page1 = videoService.findByUserId(owner.getId(), 1, 6);
        List<Video> page2 = videoService.findByUserId(owner.getId(), 2, 6);
        assertEquals(6, page1.size());
        assertEquals(2, page2.size());
        assertEquals("Mine 8", page1.get(0).getTitle());
        assertEquals("Mine 2", page2.get(0).getTitle());

        int targetId = page1.get(0).getVideoId();
        videoService.incrementViews(targetId);
        videoService.incrementViews(targetId);
        assertEquals(2, videoService.findById(targetId).getViews());
    }

    @Test
    void updateAndDeleteCleanUpCustomFilesWhileKeepingDefaults() throws IOException {
        Path oldPoster = Files.writeString(tempUploadDir.resolve("custom-poster-old.png"), "old-poster");
        Path oldVideo = Files.writeString(tempUploadDir.resolve("custom-video-old.mp4"), "old-video");
        Path newPoster = Files.writeString(tempUploadDir.resolve("custom-poster-new.png"), "new-poster");
        Path defaultPoster = Files.writeString(tempUploadDir.resolve("default-video-poster.svg"), "default-poster");

        Video custom = new Video("Custom Video", "custom-poster-old.png", "custom-video-old.mp4", "Custom desc", owner);
        videoService.create(custom);
        Video withDefaults = new Video("Default Video", "default-video-poster.svg", "default-video.mp4", "Default", owner);
        videoService.create(withDefaults);

        Video updateRequest = new Video();
        updateRequest.setVideoId(custom.getVideoId());
        updateRequest.setTitle("Updated Custom Video");
        updateRequest.setDescription("New desc");
        updateRequest.setActive(1);
        updateRequest.setPoster("custom-poster-new.png");
        videoService.update(updateRequest);

        assertFalse(Files.exists(oldPoster));
        assertTrue(Files.exists(newPoster));
        assertTrue(Files.exists(oldVideo));
        assertEquals("Updated Custom Video", videoService.findById(custom.getVideoId()).getTitle());
        assertEquals("custom-video-old.mp4", videoService.findById(custom.getVideoId()).getVideoPath());

        videoService.delete(custom.getVideoId());
        videoService.delete(withDefaults.getVideoId());

        assertNull(videoService.findById(custom.getVideoId()));
        assertFalse(Files.exists(newPoster));
        assertFalse(Files.exists(oldVideo));
        assertTrue(Files.exists(defaultPoster));
    }

    @Test
    void youtubeAndExternalUrlHelpersWork() {
        Video yt = new Video("YT", "default-video-poster.svg", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "YT", owner);
        assertTrue(yt.isExternalVideo());
        assertTrue(yt.isYoutubeVideo());
        assertEquals("https://www.youtube.com/embed/dQw4w9WgXcQ", yt.getYoutubeEmbedUrl());
        assertEquals("https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg", yt.getYoutubeThumbnailUrl());
        assertFalse(yt.isCustomPoster());

        Video mp4 = new Video("Local", "custom-poster.jpg", "abc.mp4", "Local", owner);
        assertFalse(mp4.isExternalVideo());
        assertFalse(mp4.isYoutubeVideo());
        assertNull(mp4.getYoutubeThumbnailUrl());
        assertTrue(mp4.isCustomPoster());
        assertNotNull(mp4.getFormattedCreatedAt());
    }

    private static final class InMemoryVideoRepository implements IVideoRepository {
        private final Map<Integer, Video> storage = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public List<Video> findAll() {
            return storage.values().stream()
                    .sorted(Comparator.comparing(Video::getCreatedAt).reversed()
                            .thenComparing(Comparator.comparingInt(Video::getVideoId).reversed()))
                    .toList();
        }

        @Override
        public List<Video> findAll(int page, int pagesize) {
            return slice(findAll(), page, pagesize);
        }

        @Override
        public List<Video> findLatest(int limit) {
            int safeLimit = Math.max(limit, 1);
            return findAll().stream()
                    .filter(v -> v.getActive() == 1)
                    .limit(safeLimit)
                    .toList();
        }

        @Override
        public List<Video> findByUserId(int userId) {
            return findAll().stream()
                    .filter(v -> v.getUser() != null && v.getUser().getId() == userId)
                    .toList();
        }

        @Override
        public List<Video> findByUserId(int userId, int page, int pagesize) {
            return slice(findByUserId(userId), page, pagesize);
        }

        @Override
        public int count() {
            return storage.size();
        }

        @Override
        public int countByUserId(int userId) {
            return findByUserId(userId).size();
        }

        @Override
        public List<Video> search(String keyword, Integer userId, int page, int pagesize) {
            return slice(filter(keyword, userId), page, pagesize);
        }

        @Override
        public int countSearch(String keyword, Integer userId) {
            return filter(keyword, userId).size();
        }

        @Override
        public Video findById(int id) {
            return storage.get(id);
        }

        @Override
        public void create(Video video) {
            int id = nextId++;
            Video copy = new Video(id, video.getTitle(), video.getPoster(), video.getVideoPath(),
                    video.getDescription(), video.getViews(), video.getActive(), video.getCreatedAt(), video.getUser());
            video.setVideoId(id);
            storage.put(id, copy);
        }

        @Override
        public void update(Video video) {
            if (storage.containsKey(video.getVideoId())) {
                Video copy = new Video(video.getVideoId(), video.getTitle(), video.getPoster(), video.getVideoPath(),
                        video.getDescription(), video.getViews(), video.getActive(), video.getCreatedAt(), video.getUser());
                storage.put(video.getVideoId(), copy);
            }
        }

        @Override
        public void delete(int id) {
            storage.remove(id);
        }

        @Override
        public void incrementViews(int id) {
            Video v = storage.get(id);
            if (v != null) {
                v.setViews(v.getViews() + 1);
            }
        }

        private List<Video> filter(String keyword, Integer userId) {
            String norm = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
            List<Video> matches = new ArrayList<>();
            for (Video v : findAll()) {
                boolean matchesUser = userId == null || (v.getUser() != null && v.getUser().getId() == userId);
                boolean matchesKey = norm.isEmpty()
                        || (v.getTitle() != null && v.getTitle().toLowerCase(Locale.ROOT).contains(norm));
                if (matchesUser && matchesKey) {
                    matches.add(v);
                }
            }
            return matches;
        }

        private List<Video> slice(List<Video> items, int page, int pagesize) {
            int safePage = Math.max(page, 1);
            int safeSize = Math.max(pagesize, 1);
            int from = (safePage - 1) * safeSize;
            if (from >= items.size()) {
                return List.of();
            }
            return items.subList(from, Math.min(from + safeSize, items.size()));
        }
    }
}
