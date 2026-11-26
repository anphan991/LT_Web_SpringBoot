package lt_web.services;

import java.util.List;
import lt_web.entity.User;

public interface UserService {
    User login(String username, String password);
    User get(String username);
    User get(int id);
    User findByEmail(String email);
    boolean register(String username, String password, String email, String fullname, String phone);
    boolean activateUser(String email);
    boolean resetPassword(String email, String rawPassword);
    boolean create(User user, String rawPassword);
    boolean update(User user, String rawPassword);
    User updateProfile(int userId, String fullName, String phone, String avatar);
    User updateEmail(int userId, String newEmail);
    void delete(int id);
    int migrateLegacyPasswords();
    boolean checkExistEmail(String email);
    boolean checkExistEmail(String email, int excludedId);
    boolean checkExistUsername(String username);
    boolean checkExistUsername(String username, int excludedId);
    boolean checkExistPhone(String phone);
    boolean checkExistPhone(String phone, int excludedId);
    List<User> getAll();
}
