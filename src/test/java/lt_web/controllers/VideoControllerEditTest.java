package lt_web.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.services.UserService;
import lt_web.services.VideoService;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class VideoControllerEditTest {
    private StubVideoService videoService;
    private MockMvc mockMvc;
    private MockHttpSession adminSession;

    @BeforeEach
    void setUp() {
        User admin = new User("admin@shop.local", "admin", "Admin", "hash", null, 1, "0900000001",
                new Date(System.currentTimeMillis()));
        admin.setId(1);
        admin.setStatus(1);

        videoService = new StubVideoService();
        Video sample = new Video(
                10,
                "MySQL Basics in 100 Seconds",
                "default-video-poster.svg",
                "https://www.youtube.com/watch?v=zsjvFFKOm3c",
                "Thuc hanh MySQL co ban.",
                297,
                1,
                LocalDateTime.now(),
                admin);
        videoService.store.put(10, sample);

        CloudinaryService cloudinaryService = new CloudinaryService("", "", "", "", "test") {
            @Override
            public String saveImageWithCloudFallback(MultipartFile file, String targetFolder, int maxLength) {
                return null;
            }

            @Override
            public String saveVideoWithCloudFallback(MultipartFile file, String targetFolder, int maxLength) {
                return null;
            }
        };

        UserService userService = new EmptyUserService();
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver("/WEB-INF/views/", ".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(new VideoController(videoService, userService, cloudinaryService))
                .setViewResolvers(viewResolver)
                .build();

        adminSession = new MockHttpSession();
        adminSession.setAttribute(Constant.SESSION_ACCOUNT, admin);
    }

    @Test
    void editTextOnlyUpdatesVideoAndRedirectsToDetail() throws Exception {
        mockMvc.perform(multipart("/video/edit")
                        .session(adminSession)
                        .param("id", "10")
                        .param("title", "MySQL Co Ban Da Sua")
                        .param("description", "Mo ta da cap nhat moi.")
                        .param("videoUrl", "https://www.youtube.com/watch?v=zsjvFFKOm3c")
                        .param("active", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/video/detail?id=10&updated=1"));

        Video updated = videoService.findById(10);
        assertEquals("MySQL Co Ban Da Sua", updated.getTitle());
        assertEquals("Mo ta da cap nhat moi.", updated.getDescription());
        assertEquals("https://www.youtube.com/watch?v=zsjvFFKOm3c", updated.getVideoPath());
    }

    @Test
    void editToleratesIdenticalDuplicateIdAndRejectsConflictingIds() throws Exception {
        mockMvc.perform(multipart("/video/edit?id=10")
                        .session(adminSession)
                        .param("id", "10")
                        .param("title", "MySQL Trung ID Hop Le")
                        .param("description", "Mo ta hop le.")
                        .param("videoUrl", "https://www.youtube.com/watch?v=zsjvFFKOm3c")
                        .param("active", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/video/detail?id=10&updated=1"));

        assertEquals("MySQL Trung ID Hop Le", videoService.findById(10).getTitle());

        mockMvc.perform(multipart("/video/edit?id=10")
                        .session(adminSession)
                        .param("id", "99")
                        .param("title", "Khong duoc phep")
                        .param("active", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/video?videoError=missing"));

        assertEquals(-1, ValidationUtil.parsePositiveInt("10,99"));
        assertEquals(10, ValidationUtil.parsePositiveInt("10, 10"));
    }

    private static final class StubVideoService implements VideoService {
        private final Map<Integer, Video> store = new LinkedHashMap<>();

        @Override
        public List<Video> findAll() {
            return List.copyOf(store.values());
        }

        @Override
        public List<Video> findAll(int page, int pagesize) {
            return findAll();
        }

        @Override
        public List<Video> findLatest(int limit) {
            return findAll();
        }

        @Override
        public List<Video> findByUserId(int userId) {
            return findAll();
        }

        @Override
        public List<Video> findByUserId(int userId, int page, int pagesize) {
            return findAll();
        }

        @Override
        public int count() {
            return store.size();
        }

        @Override
        public int countByUserId(int userId) {
            return store.size();
        }

        @Override
        public List<Video> search(String keyword, Integer userId, int page, int pagesize) {
            return findAll();
        }

        @Override
        public int countSearch(String keyword, Integer userId) {
            return store.size();
        }

        @Override
        public Video findById(int id) {
            return store.get(id);
        }

        @Override
        public void create(Video video) {
            store.put(video.getVideoId(), video);
        }

        @Override
        public void update(Video video) {
            Video old = store.get(video.getVideoId());
            if (old == null) {
                return;
            }
            old.setTitle(video.getTitle());
            old.setDescription(video.getDescription());
            old.setActive(video.getActive());
            if (video.getPoster() != null && !video.getPoster().isBlank()) {
                old.setPoster(video.getPoster());
            }
            if (video.getVideoPath() != null && !video.getVideoPath().isBlank()) {
                old.setVideoPath(video.getVideoPath());
            }
        }

        @Override
        public void delete(int id) {
            store.remove(id);
        }

        @Override
        public void incrementViews(int id) {
        }
    }

    private static final class EmptyUserService implements UserService {
        @Override public User login(String username, String password) { return null; }
        @Override public User get(String username) { return null; }
        @Override public User get(int id) { return null; }
        @Override public User findByEmail(String email) { return null; }
        @Override public boolean register(String username, String password, String email, String fullname, String phone) { return false; }
        @Override public boolean activateUser(String email) { return false; }
        @Override public boolean resetPassword(String email, String rawPassword) { return false; }
        @Override public boolean create(User user, String rawPassword) { return false; }
        @Override public boolean update(User user, String rawPassword) { return false; }
        @Override public User updateProfile(int userId, String fullName, String phone, String avatar) { return null; }
        @Override public User updateEmail(int userId, String newEmail) { return null; }
        @Override public void delete(int id) {}
        @Override public int migrateLegacyPasswords() { return 0; }
        @Override public boolean checkExistEmail(String email) { return false; }
        @Override public boolean checkExistEmail(String email, int excludedId) { return false; }
        @Override public boolean checkExistUsername(String username) { return false; }
        @Override public boolean checkExistUsername(String username, int excludedId) { return false; }
        @Override public boolean checkExistPhone(String phone) { return false; }
        @Override public boolean checkExistPhone(String phone, int excludedId) { return false; }
        @Override public List<User> getAll() { return List.of(); }
        @Override public List<User> findAll(int page, int pageSize) { return List.of(); }
        @Override public List<User> search(String keyword, Integer roleid, int page, int pageSize) { return List.of(); }
        @Override public int count() { return 0; }
        @Override public int countSearch(String keyword, Integer roleid) { return 0; }
        @Override public int countByRole(int roleid) { return 0; }
        @Override public int countByStatus(int status) { return 0; }
    }
}
