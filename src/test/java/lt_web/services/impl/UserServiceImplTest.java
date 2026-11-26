package lt_web.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import lt_web.entity.User;
import lt_web.repository.IUserRepository;
import lt_web.utils.PasswordHasher;
import org.junit.jupiter.api.Test;

class UserServiceImplTest {
    @Test
    void registerHashesPasswordAndLoginValidatesIt() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        assertTrue(service.register("member", "secret", "member@example.com", "Member", "0900000000"));
        assertTrue(PasswordHasher.isHash(repo.user.getPassWord()));
        assertNotNull(service.login("member", "secret"));
        assertNull(service.login("member", "wrong"));
    }

    @Test
    void duplicateUsernameIsRejected() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        assertTrue(service.register("member", "secret", null, "Member", null));
        assertFalse(service.register("member", "another", null, "Member 2", null));
    }

    @Test
    void activateUserAndResetPassword() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        service.register("user_otp", "oldpass", "user@test.com", "Test OTP", "0911222333");
        assertNotNull(service.findByEmail("user@test.com"));
        assertFalse(repo.user.isActive());

        assertTrue(service.activateUser("user@test.com"));
        assertTrue(repo.user.isActive());

        assertTrue(service.resetPassword("user@test.com", "newpass123"));
        assertTrue(PasswordHasher.matches("newpass123", repo.user.getPassWord()));
        assertNotNull(service.login("user_otp", "newpass123"));
    }

    @Test
    void updateProfileAndEmailPreserveCredentials() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserServiceImpl service = new UserServiceImpl(repo);

        service.register("profile_user", "secret123", "old@example.com", "Old Name", "0901111222");
        User updated = service.updateProfile(1, "New Name", "0988777666", "https://res.cloudinary.com/demo/image/upload/v1/avatar.png");

        assertNotNull(updated);
        assertTrue("New Name".equals(updated.getFullName()));
        assertTrue("0988777666".equals(updated.getPhone()));
        assertTrue("https://res.cloudinary.com/demo/image/upload/v1/avatar.png".equals(updated.getAvatar()));
        assertTrue("old@example.com".equals(updated.getEmail()));

        User afterEmail = service.updateEmail(1, "  NEW@Example.com ");
        assertNotNull(afterEmail);
        assertTrue("new@example.com".equals(afterEmail.getEmail()));
        assertNotNull(service.login("profile_user", "secret123"));
    }

    private static final class InMemoryUserRepository implements IUserRepository {
        private User user;

        @Override
        public List<User> findAll() {
            return user == null ? List.of() : new ArrayList<>(List.of(user));
        }

        @Override
        public User findById(int id) {
            return user != null && user.getId() == id ? user : null;
        }

        @Override
        public User findByUsername(String username) {
            return user != null && user.getUserName().equals(username) ? user : null;
        }

        @Override
        public User findByEmail(String email) {
            return user != null && email != null && email.equalsIgnoreCase(user.getEmail()) ? user : null;
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
        public void insert(User value) {
            value.setId(1);
            user = value;
        }

        @Override
        public void update(User value) {
            user = value;
        }

        @Override
        public User updateProfile(int userId, String fullName, String phone, String avatar) {
            if (user == null || user.getId() != userId) {
                return null;
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            if (avatar != null && !avatar.isBlank()) {
                user.setAvatar(avatar);
            }
            return user;
        }

        @Override
        public User updateEmail(int userId, String newEmail) {
            if (user == null || user.getId() != userId) {
                return null;
            }
            user.setEmail(newEmail);
            return user;
        }

        @Override
        public void updateStatus(int userId, int status) {
            if (user != null && user.getId() == userId) {
                user.setStatus(status);
            }
        }

        @Override
        public void updatePassword(String username, String passwordHash) {
            if (user != null && user.getUserName().equals(username)) {
                user.setPassWord(passwordHash);
            }
        }

        @Override
        public void updatePasswordByEmail(String email, String passwordHash) {
            if (user != null && email != null && email.equalsIgnoreCase(user.getEmail())) {
                user.setPassWord(passwordHash);
            }
        }

        @Override
        public int migratePlaintextPasswords() {
            return 0;
        }

        @Override
        public void delete(int id) {
            user = null;
        }

        @Override
        public boolean checkExistEmail(String email) {
            return checkExistEmail(email, 0);
        }

        @Override
        public boolean checkExistEmail(String email, int excludedId) {
            return user != null && email != null && email.equals(user.getEmail()) && user.getId() != excludedId;
        }

        @Override
        public boolean checkExistUsername(String username) {
            return checkExistUsername(username, 0);
        }

        @Override
        public boolean checkExistUsername(String username, int excludedId) {
            return user != null && username != null && username.equals(user.getUserName()) && user.getId() != excludedId;
        }

        @Override
        public boolean checkExistPhone(String phone) {
            return checkExistPhone(phone, 0);
        }

        @Override
        public boolean checkExistPhone(String phone, int excludedId) {
            return user != null && phone != null && phone.equals(user.getPhone()) && user.getId() != excludedId;
        }

        @Override
        public List<User> getAll() {
            return findAll();
        }
    }
}
