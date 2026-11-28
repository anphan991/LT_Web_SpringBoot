package lt_web.controllers.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.services.CategoryService;
import lt_web.services.UserService;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class AdminCrudControllersTest {
    private StubCategoryService categoryService;
    private StubUserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        categoryService = new StubCategoryService();
        userService = new StubUserService();
        CloudinaryService cloudinaryService = new CloudinaryService("", "", "", "", "test") {
            @Override
            public String saveImageWithCloudFallback(MultipartFile file, String targetFolder, int maxLength) {
                return (file == null || file.isEmpty()) ? null : "uploaded-" + file.getOriginalFilename();
            }
        };

        for (int i = 1; i <= 7; i++) {
            Category category = new Category();
            category.setCateName(i == 7 ? "Laptop Gaming" : "Category " + i);
            category.setIcon("icon-" + i + ".png");
            categoryService.insert(category);
        }

        User admin = makeUser("admin", "Quản trị viên", "admin@shop.local", "0900000001", 1, 1);
        userService.create(admin, "admin123");
        User manager = makeUser("manager", "Quản lý kho", "manager@shop.local", "0900000002", 2, 1);
        userService.create(manager, "manager123");
        for (int i = 1; i <= 5; i++) {
            User member = makeUser(
                    "khach" + i,
                    "Khách Hàng " + i,
                    "khach" + i + "@shop.local",
                    "091111111" + i,
                    5,
                    i % 2 == 0 ? 0 : 1);
            userService.create(member, "secret123");
        }

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver("/WEB-INF/views/", ".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CategoryListController(categoryService, userService),
                        new CategoryAddController(categoryService, cloudinaryService),
                        new CategoryEditController(categoryService, cloudinaryService),
                        new CategoryDeleteController(categoryService),
                        new UserListController(userService),
                        new UserAddController(userService),
                        new UserEditController(userService),
                        new UserDeleteController(userService))
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void categoryListSupportsSearchAndPaginationAlongWithUserFilters() throws Exception {
        mockMvc.perform(get("/admin/category/list").param("page", "2").param("userRole", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_LIST_CATEGORY))
                .andExpect(model().attribute("totalCategories", 7))
                .andExpect(model().attribute("filteredCategories", 7))
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(model().attribute("cateList", hasSize(2)))
                .andExpect(model().attribute("totalUsers", 7))
                .andExpect(model().attribute("filteredUsers", 5))
                .andExpect(model().attribute("userList", hasSize(5)));

        mockMvc.perform(get("/admin/category/list").param("keyword", "Gaming"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filteredCategories", 1))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("cateList", hasSize(1)));
    }

    @Test
    void categoryCrudLifecycleWorksEndToEnd() throws Exception {
        mockMvc.perform(get("/admin/category/add"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_ADD_CATEGORY));

        mockMvc.perform(multipart("/admin/category/add").param("catename", "A"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_ADD_CATEGORY))
                .andExpect(model().attributeExists("errors", "alert"));

        mockMvc.perform(multipart("/admin/category/add").param("catename", "Phụ kiện mới"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?categoryAdded=1"));
        assertEquals(8, categoryService.count());

        Category created = categoryService.get("Phụ kiện mới");
        assertNotNull(created);

        mockMvc.perform(get("/admin/category/edit").param("id", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?categoryError=missing"));

        mockMvc.perform(multipart("/admin/category/edit")
                        .param("id", String.valueOf(created.getCateid()))
                        .param("catename", "Phụ kiện cao cấp"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?categoryUpdated=1"));
        assertEquals("Phụ kiện cao cấp", categoryService.get(created.getCateid()).getCatename());

        mockMvc.perform(multipart("/admin/category/edit?id=" + created.getCateid())
                        .param("id", String.valueOf(created.getCateid()))
                        .param("catename", "Phụ kiện trùng ID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?categoryUpdated=1"));
        assertEquals("Phụ kiện trùng ID", categoryService.get(created.getCateid()).getCatename());

        mockMvc.perform(get("/admin/category/delete").param("id", String.valueOf(created.getCateid())))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_DELETE_CATEGORY))
                .andExpect(model().attributeExists("category"));

        mockMvc.perform(post("/admin/category/delete").param("id", String.valueOf(created.getCateid())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?categoryDeleted=1"));
        assertNull(categoryService.get(created.getCateid()));
    }

    @Test
    void userEndpointsRequireAdminRoleAndSupportSearchAndPagination() throws Exception {
        MockHttpSession managerSession = sessionFor(userService.get("manager"));
        mockMvc.perform(get("/admin/user/list").session(managerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category/list?forbidden=1#users-section"));

        MockHttpSession adminSession = sessionFor(userService.get("admin"));
        mockMvc.perform(get("/admin/user/list").session(adminSession).param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_LIST_USER))
                .andExpect(model().attribute("totalUsers", 7))
                .andExpect(model().attribute("filteredUsers", 7))
                .andExpect(model().attribute("activeUsers", 5))
                .andExpect(model().attribute("adminUsers", 1))
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(model().attribute("userList", hasSize(2)));

        mockMvc.perform(get("/admin/user/list").session(adminSession)
                        .param("keyword", "khach3")
                        .param("roleid", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filteredUsers", 1))
                .andExpect(model().attribute("userList", hasSize(1)));
    }

    @Test
    void userCrudPreventsDuplicatesAndSelfLockout() throws Exception {
        User admin = userService.get("admin");
        MockHttpSession adminSession = sessionFor(admin);

        mockMvc.perform(post("/admin/user/add").session(adminSession)
                        .param("username", "admin")
                        .param("fullname", "Trùng tên")
                        .param("email", "dup@shop.local")
                        .param("password", "secret123")
                        .param("returnTo", "users"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_USER_FORM))
                .andExpect(model().attributeExists("errors", "alert"));

        mockMvc.perform(post("/admin/user/add").session(adminSession)
                        .param("username", "nhanvien_moi")
                        .param("fullname", "Nhân Viên Mới")
                        .param("email", "nvmoi@shop.local")
                        .param("phone", "0988777666")
                        .param("roleid", "2")
                        .param("status", "1")
                        .param("password", "secret123")
                        .param("returnTo", "users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user/list?userAdded=1"));

        User created = userService.get("nhanvien_moi");
        assertNotNull(created);
        assertEquals(2, created.getRoleid());

        mockMvc.perform(post("/admin/user/edit").session(adminSession)
                        .param("id", String.valueOf(admin.getId()))
                        .param("fullname", "Admin Chính")
                        .param("email", "admin@shop.local")
                        .param("phone", "0900000001")
                        .param("roleid", "1")
                        .param("status", "0")
                        .param("returnTo", "users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user/list?userUpdated=1"));
        assertEquals(1, userService.get(admin.getId()).getStatus());

        mockMvc.perform(post("/admin/user/edit").session(adminSession)
                        .param("id", String.valueOf(created.getId()))
                        .param("fullname", "Nhân Viên Tạm Khóa")
                        .param("email", "nvmoi@shop.local")
                        .param("phone", "0988777666")
                        .param("roleid", "5")
                        .param("status", "0")
                        .param("returnTo", "users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user/list?userUpdated=1"));
        User edited = userService.get(created.getId());
        assertEquals(0, edited.getStatus());
        assertEquals(5, edited.getRoleid());

        mockMvc.perform(post("/admin/user/delete").session(adminSession)
                        .param("id", String.valueOf(admin.getId()))
                        .param("returnTo", "users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user/list?userError=self"));

        mockMvc.perform(get("/admin/user/delete").session(adminSession)
                        .param("id", String.valueOf(created.getId()))
                        .param("returnTo", "users"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.View.ADMIN_DELETE_USER))
                .andExpect(model().attribute("returnTo", "users"));

        mockMvc.perform(post("/admin/user/delete").session(adminSession)
                        .param("id", String.valueOf(created.getId()))
                        .param("returnTo", "users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user/list?userDeleted=1"));
        assertNull(userService.get(created.getId()));
    }

    private MockHttpSession sessionFor(User account) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constant.SESSION_ACCOUNT, account);
        return session;
    }

    private User makeUser(String username, String fullname, String email, String phone, int roleid, int status) {
        User user = new User(email, username, fullname, "", null, roleid, phone,
                new Date(System.currentTimeMillis()));
        user.setStatus(status);
        return user;
    }

    private static final class StubCategoryService implements CategoryService {
        private final Map<Integer, Category> store = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public void insert(Category category) {
            category.setCateId(nextId++);
            store.put(category.getCateid(), new Category(category.getCateid(), category.getCatename(), category.getIcon()));
        }

        @Override
        public void edit(Category category) {
            store.put(category.getCateid(), new Category(category.getCateid(), category.getCatename(), category.getIcon()));
        }

        @Override
        public void delete(int id) {
            store.remove(id);
        }

        @Override
        public Category get(int id) {
            return store.get(id);
        }

        @Override
        public Category get(String name) {
            return store.values().stream()
                    .filter(c -> name != null && name.equals(c.getCatename()))
                    .findFirst().orElse(null);
        }

        @Override
        public List<Category> getAll() {
            return findAll();
        }

        @Override
        public List<Category> search(String keyword) {
            return findByCategoryname(keyword);
        }

        @Override
        public List<Category> findAll() {
            return store.values().stream()
                    .sorted(Comparator.comparingInt(Category::getCateid))
                    .toList();
        }

        @Override
        public Category findById(int id) {
            return get(id);
        }

        @Override
        public void create(Category category) {
            insert(category);
        }

        @Override
        public void update(Category category) {
            edit(category);
        }

        @Override
        public List<Category> findByCategoryname(String catname) {
            String normalized = catname == null ? "" : catname.trim().toLowerCase(Locale.ROOT);
            return findAll().stream()
                    .filter(c -> c.getCatename() != null && c.getCatename().toLowerCase(Locale.ROOT).contains(normalized))
                    .toList();
        }

        @Override
        public List<Category> findByCategoryname(String catname, int page, int pagesize) {
            return paginate(findByCategoryname(catname), page, pagesize);
        }

        @Override
        public List<Category> findAll(int page, int pagesize) {
            return paginate(findAll(), page, pagesize);
        }

        @Override
        public int count() {
            return store.size();
        }

        @Override
        public int countByCategoryname(String catname) {
            return findByCategoryname(catname).size();
        }

        private List<Category> paginate(List<Category> items, int page, int pagesize) {
            int fromIndex = Math.max(0, page) * Math.max(1, pagesize);
            if (fromIndex >= items.size()) {
                return List.of();
            }
            return items.subList(fromIndex, Math.min(fromIndex + pagesize, items.size()));
        }
    }

    private static final class StubUserService implements UserService {
        private final Map<Integer, User> store = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public User login(String username, String password) {
            User found = get(username);
            return found != null && PasswordHasher.matches(password, found.getPassWord()) ? found : null;
        }

        @Override
        public User get(String username) {
            return store.values().stream()
                    .filter(u -> username != null && username.equals(u.getUserName()))
                    .findFirst().orElse(null);
        }

        @Override
        public User get(int id) {
            return store.get(id);
        }

        @Override
        public User findByEmail(String email) {
            return store.values().stream()
                    .filter(u -> email != null && email.equalsIgnoreCase(u.getEmail()))
                    .findFirst().orElse(null);
        }

        @Override
        public boolean register(String username, String password, String email, String fullname, String phone) {
            return false;
        }

        @Override
        public boolean activateUser(String email) {
            return false;
        }

        @Override
        public boolean resetPassword(String email, String rawPassword) {
            return false;
        }

        @Override
        public boolean create(User user, String rawPassword) {
            user.setId(nextId++);
            user.setPassWord(PasswordHasher.hash(rawPassword));
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            store.put(user.getId(), copyOf(user));
            return true;
        }

        @Override
        public boolean update(User user, String rawPassword) {
            User existing = store.get(user.getId());
            if (existing == null) {
                return false;
            }
            user.setUserName(existing.getUserName());
            user.setPassWord(rawPassword == null || rawPassword.isBlank()
                    ? existing.getPassWord() : PasswordHasher.hash(rawPassword));
            if (user.getStatus() == null) {
                user.setStatus(existing.getStatus());
            }
            store.put(user.getId(), copyOf(user));
            return true;
        }

        @Override
        public User updateProfile(int userId, String fullName, String phone, String avatar) {
            return null;
        }

        @Override
        public User updateEmail(int userId, String newEmail) {
            return null;
        }

        @Override
        public void delete(int id) {
            store.remove(id);
        }

        @Override
        public int migrateLegacyPasswords() {
            return 0;
        }

        @Override
        public boolean checkExistEmail(String email) {
            return checkExistEmail(email, 0);
        }

        @Override
        public boolean checkExistEmail(String email, int excludedId) {
            return email != null && store.values().stream()
                    .anyMatch(u -> u.getId() != excludedId && email.equalsIgnoreCase(u.getEmail()));
        }

        @Override
        public boolean checkExistUsername(String username) {
            return checkExistUsername(username, 0);
        }

        @Override
        public boolean checkExistUsername(String username, int excludedId) {
            return username != null && store.values().stream()
                    .anyMatch(u -> u.getId() != excludedId && username.equalsIgnoreCase(u.getUserName()));
        }

        @Override
        public boolean checkExistPhone(String phone) {
            return checkExistPhone(phone, 0);
        }

        @Override
        public boolean checkExistPhone(String phone, int excludedId) {
            return phone != null && store.values().stream()
                    .anyMatch(u -> u.getId() != excludedId && phone.equals(u.getPhone()));
        }

        @Override
        public List<User> getAll() {
            return store.values().stream()
                    .sorted(Comparator.comparingInt(User::getId).reversed())
                    .toList();
        }

        @Override
        public List<User> findAll(int page, int pageSize) {
            return paginate(getAll(), page, pageSize);
        }

        @Override
        public List<User> search(String keyword, Integer roleid, int page, int pageSize) {
            return paginate(filter(keyword, roleid), page, pageSize);
        }

        @Override
        public int count() {
            return store.size();
        }

        @Override
        public int countSearch(String keyword, Integer roleid) {
            return filter(keyword, roleid).size();
        }

        @Override
        public int countByRole(int roleid) {
            return (int) store.values().stream().filter(u -> u.getRoleid() == roleid).count();
        }

        @Override
        public int countByStatus(int status) {
            return (int) store.values().stream()
                    .filter(u -> Integer.valueOf(status).equals(u.getStatus()))
                    .count();
        }

        private List<User> filter(String keyword, Integer roleid) {
            String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
            List<User> result = new ArrayList<>();
            for (User u : getAll()) {
                if (roleid != null && u.getRoleid() != roleid) {
                    continue;
                }
                if (normalized.isEmpty()
                        || contains(u.getUserName(), normalized)
                        || contains(u.getFullName(), normalized)
                        || contains(u.getEmail(), normalized)
                        || contains(u.getPhone(), normalized)) {
                    result.add(u);
                }
            }
            return result;
        }

        private List<User> paginate(List<User> items, int page, int pageSize) {
            int fromIndex = Math.max(0, page) * Math.max(1, pageSize);
            if (fromIndex >= items.size()) {
                return List.of();
            }
            return items.subList(fromIndex, Math.min(fromIndex + pageSize, items.size()));
        }

        private boolean contains(String value, String needle) {
            return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
        }

        private User copyOf(User src) {
            User copy = new User(src.getEmail(), src.getUserName(), src.getFullName(), src.getPassWord(),
                    src.getAvatar(), src.getRoleid(), src.getPhone(), src.getCreatedDate());
            copy.setId(src.getId());
            copy.setStatus(src.getStatus());
            return copy;
        }
    }
}
