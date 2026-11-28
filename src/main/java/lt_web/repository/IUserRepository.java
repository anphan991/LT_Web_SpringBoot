package lt_web.repository;

import java.util.List;
import lt_web.entity.User;

public interface IUserRepository {
    List<User> findAll();
    List<User> findAll(int page, int pageSize);
    List<User> search(String keyword, Integer roleid, int page, int pageSize);
    int count();
    int countSearch(String keyword, Integer roleid);
    int countByRole(int roleid);
    int countByStatus(int status);

    User findById(int id);
    User findByUsername(String username);
    User findByEmail(String email);

    User get(String username);
    User getById(int id);
    void insert(User user);
    void update(User user);
    User updateProfile(int userId, String fullName, String phone, String avatar);
    User updateEmail(int userId, String newEmail);
    void updateStatus(int userId, int status);
    void updatePassword(String username, String passwordHash);
    void updatePasswordByEmail(String email, String passwordHash);
    int migratePlaintextPasswords();
    void delete(int id);
    boolean checkExistEmail(String email);
    boolean checkExistEmail(String email, int excludedId);
    boolean checkExistUsername(String username);
    boolean checkExistUsername(String username, int excludedId);
    boolean checkExistPhone(String phone);
    boolean checkExistPhone(String phone, int excludedId);
    List<User> getAll();
}
