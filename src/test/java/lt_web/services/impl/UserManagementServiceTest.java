package lt_web.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lt_web.entity.User;
import lt_web.repository.IUserRepository;
import org.junit.jupiter.api.Test;

class UserManagementServiceTest {

    @Test
    void searchFiltersByKeywordAndRoleAndPaginates() {
        MultiUserRepository repo = new MultiUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        for (int i = 1; i <= 7; i++) {
            User user = new User(
                    "member" + i + "@example.com",
                    "member" + i,
                    "Nguyen Van " + i,
                    "secret123",
                    null,
                    i <= 2 ? 1 : (i == 3 ? 2 : 5),
                    "090000000" + i,
                    new Date(System.currentTimeMillis()));
            user.setStatus(i % 2 == 0 ? 0 : 1);
            assertTrue(service.create(user, "secret123"));
        }

        assertEquals(7, service.count());
        assertEquals(2, service.countByRole(1));
        assertEquals(4, service.countByStatus(1));

        List<User> page1 = service.search(null, null, 0, 5);
        List<User> page2 = service.search(null, null, 1, 5);
        assertEquals(5, page1.size());
        assertEquals(2, page2.size());

        List<User> filtered = service.search("member", 5, 0, 5);
        assertEquals(4, service.countSearch("member", 5));
        assertEquals(4, filtered.size());
        assertTrue(filtered.stream().allMatch(u -> u.getRoleid() == 5));
    }

    @Test
    void updatePreservesStatusWhenNullAndUpdatesWhenProvided() {
        MultiUserRepository repo = new MultiUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        User user = new User("admin@example.com", "admin1", "Admin One", "secret123",
                null, 1, "0912345678", new Date(System.currentTimeMillis()));
        user.setStatus(1);
        assertTrue(service.create(user, "secret123"));

        User noStatusPatch = new User();
        noStatusPatch.setId(user.getId());
        noStatusPatch.setFullName("Admin Updated");
        noStatusPatch.setEmail("admin@example.com");
        noStatusPatch.setPhone("0912345678");
        noStatusPatch.setRoleid(1);
        noStatusPatch.setStatus(null);
        assertTrue(service.update(noStatusPatch, ""));
        assertEquals(1, service.get(user.getId()).getStatus());

        User disablePatch = new User();
        disablePatch.setId(user.getId());
        disablePatch.setFullName("Admin Disabled");
        disablePatch.setEmail("admin@example.com");
        disablePatch.setPhone("0912345678");
        disablePatch.setRoleid(2);
        disablePatch.setStatus(0);
        assertTrue(service.update(disablePatch, null));
        assertEquals(0, service.get(user.getId()).getStatus());
        assertEquals(2, service.get(user.getId()).getRoleid());

        service.delete(user.getId());
        assertEquals(0, service.count());
        assertFalse(service.checkExistUsername("admin1"));
    }

    private static final class MultiUserRepository implements IUserRepository {
        private final Map<Integer, User> store = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public List<User> findAll() {
            return store.values().stream()
                    .sorted(Comparator.comparingInt(User::getId).reversed())
                    .toList();
        }

        @Override
        public List<User> findAll(int page, int pageSize) {
            return paginate(findAll(), page, pageSize);
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

        @Override
        public User findById(int id) {
            return store.get(id);
        }

        @Override
        public User findByUsername(String username) {
            return store.values().stream()
                    .filter(u -> username != null && username.equals(u.getUserName()))
                    .findFirst().orElse(null);
        }

        @Override
        public User findByEmail(String email) {
            return store.values().stream()
                    .filter(u -> email != null && email.equalsIgnoreCase(u.getEmail()))
                    .findFirst().orElse(null);
        }

        @Override
        public User get(String username) {
            return findByUsername(username);
        }

        @Override
        public User getById(int id) {
            return findById(id);
        }

        @Override
        public void insert(User user) {
            user.setId(nextId++);
            store.put(user.getId(), copyOf(user));
        }

        @Override
        public void update(User user) {
            store.put(user.getId(), copyOf(user));
        }

        @Override
        public User updateProfile(int userId, String fullName, String phone, String avatar) {
            User existing = store.get(userId);
            if (existing == null) {
                return null;
            }
            existing.setFullName(fullName);
            existing.setPhone(phone);
            existing.setAvatar(avatar);
            return existing;
        }

        @Override
        public User updateEmail(int userId, String newEmail) {
            User existing = store.get(userId);
            if (existing == null) {
                return null;
            }
            existing.setEmail(newEmail);
            return existing;
        }

        @Override
        public void updateStatus(int userId, int status) {
            User existing = store.get(userId);
            if (existing != null) {
                existing.setStatus(status);
            }
        }

        @Override
        public void updatePassword(String username, String passwordHash) {
            User existing = findByUsername(username);
            if (existing != null) {
                existing.setPassWord(passwordHash);
            }
        }

        @Override
        public void updatePasswordByEmail(String email, String passwordHash) {
            User existing = findByEmail(email);
            if (existing != null) {
                existing.setPassWord(passwordHash);
            }
        }

        @Override
        public int migratePlaintextPasswords() {
            return 0;
        }

        @Override
        public void delete(int id) {
            store.remove(id);
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
            return findAll();
        }

        private List<User> filter(String keyword, Integer roleid) {
            String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
            List<User> matched = new ArrayList<>();
            for (User u : findAll()) {
                if (roleid != null && u.getRoleid() != roleid) {
                    continue;
                }
                if (normalized.isEmpty()
                        || contains(u.getUserName(), normalized)
                        || contains(u.getFullName(), normalized)
                        || contains(u.getEmail(), normalized)
                        || contains(u.getPhone(), normalized)) {
                    matched.add(u);
                }
            }
            return matched;
        }

        private List<User> paginate(List<User> items, int page, int pageSize) {
            int safePage = Math.max(0, page);
            int safeSize = Math.max(1, pageSize);
            int fromIndex = safePage * safeSize;
            if (fromIndex >= items.size()) {
                return List.of();
            }
            int toIndex = Math.min(fromIndex + safeSize, items.size());
            return items.subList(fromIndex, toIndex);
        }

        private boolean contains(String field, String needle) {
            return field != null && field.toLowerCase(Locale.ROOT).contains(needle);
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
