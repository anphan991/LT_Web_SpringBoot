package lt_web.services.impl;

import java.sql.Date;
import java.util.List;
import java.util.Locale;
import lt_web.entity.User;
import lt_web.repository.IUserRepository;
import lt_web.repository.impl.UserRepository;
import lt_web.services.UserService;
import lt_web.utils.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final IUserRepository userRepo;

    public UserServiceImpl() {
        this(new UserRepository());
    }

    @Autowired
    public UserServiceImpl(IUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User login(String username, String password) {
        User user = get(username);
        if (user == null || password == null) {
            return null;
        }

        if (PasswordHasher.matches(password, user.getPassWord())) {
            return user;
        }

        if (!PasswordHasher.isHash(user.getPassWord()) && password.equals(user.getPassWord())) {
            userRepo.updatePassword(user.getUserName(), PasswordHasher.hash(password));
            return user;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public User get(String username) {
        return username == null || username.isBlank() ? null : userRepo.get(username.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public User get(int id) {
        return userRepo.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return email == null || email.isBlank() ? null : userRepo.findByEmail(email.trim());
    }

    @Override
    public boolean register(String username, String password, String email, String fullname, String phone) {
        if (checkExistUsername(username)) {
            return false;
        }
        User user = new User(email, username, fullname, password, null, 5, phone, new Date(System.currentTimeMillis()));
        user.setStatus(0);
        return create(user, password);
    }

    @Override
    public boolean activateUser(String email) {
        User user = findByEmail(email);
        if (user == null) {
            return false;
        }
        user.setStatus(1);
        userRepo.updateStatus(user.getId(), 1);
        return true;
    }

    @Override
    public boolean resetPassword(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        User user = findByEmail(email);
        if (user == null) {
            return false;
        }
        String passwordHash = PasswordHasher.hash(rawPassword);
        userRepo.updatePasswordByEmail(email, passwordHash);
        return true;
    }

    @Override
    public boolean create(User user, String rawPassword) {
        if (user == null || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        user.setPassWord(PasswordHasher.hash(rawPassword));
        if (user.getRoleid() <= 0) {
            user.setRoleid(5);
        }
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(new Date(System.currentTimeMillis()));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userRepo.insert(user);
        return true;
    }

    @Override
    public boolean update(User user, String rawPassword) {
        if (user == null || user.getId() <= 0) {
            return false;
        }
        User existing = userRepo.getById(user.getId());
        if (existing == null) {
            return false;
        }
        user.setUserName(existing.getUserName());
        user.setAvatar(existing.getAvatar());
        user.setCreatedDate(existing.getCreatedDate());
        user.setPassWord(rawPassword == null || rawPassword.isBlank()
                ? existing.getPassWord() : PasswordHasher.hash(rawPassword));
        if (user.getRoleid() <= 0) {
            user.setRoleid(existing.getRoleid());
        }
        userRepo.update(user);
        return true;
    }

    @Override
    public User updateProfile(int userId, String fullName, String phone, String avatar) {
        if (userId <= 0) {
            return null;
        }
        String cleanFullName = fullName == null ? null : fullName.trim();
        String cleanPhone = phone == null || phone.isBlank() ? null : phone.trim();
        String cleanAvatar = avatar == null || avatar.isBlank() ? null : avatar.trim();
        return userRepo.updateProfile(userId, cleanFullName, cleanPhone, cleanAvatar);
    }

    @Override
    public User updateEmail(int userId, String newEmail) {
        if (userId <= 0 || newEmail == null || newEmail.isBlank()) {
            return null;
        }
        return userRepo.updateEmail(userId, newEmail.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public void delete(int id) {
        userRepo.delete(id);
    }

    @Override
    public int migrateLegacyPasswords() {
        return userRepo.migratePlaintextPasswords();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistEmail(String email) {
        return userRepo.checkExistEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistEmail(String email, int excludedId) {
        return userRepo.checkExistEmail(email, excludedId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistUsername(String username) {
        return userRepo.checkExistUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistUsername(String username, int excludedId) {
        return userRepo.checkExistUsername(username, excludedId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistPhone(String phone) {
        return userRepo.checkExistPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistPhone(String phone, int excludedId) {
        return userRepo.checkExistPhone(phone, excludedId);
    }

    @Override
    public List<User> getAll() {
        try {
            migrateLegacyPasswords();
        } catch (Exception ignored) {
        }
        return userRepo.findAll();
    }
}
