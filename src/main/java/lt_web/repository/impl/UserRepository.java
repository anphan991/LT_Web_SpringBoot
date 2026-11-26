package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IUserRepository;
import lt_web.utils.PasswordHasher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class UserRepository implements IUserRepository {

    @PersistenceContext
    private EntityManager enma;

    @Override
    public List<User> findAll() {
        try {
            TypedQuery<User> query = enma.createNamedQuery("User.findAll", User.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách tài khoản.", e);
        }
    }

    @Override
    public User findById(int id) {
        try {
            return enma.find(User.class, id);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải tài khoản theo ID.", e);
        }
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            TypedQuery<User> query = enma.createQuery(
                    "SELECT u FROM User u WHERE u.userName = :username", User.class);
            query.setParameter("username", username.trim());
            List<User> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải tài khoản theo tên đăng nhập.", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            TypedQuery<User> query = enma.createQuery(
                    "SELECT u FROM User u WHERE LOWER(TRIM(u.email)) = :email", User.class);
            query.setParameter("email", email.trim().toLowerCase(Locale.ROOT));
            List<User> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải tài khoản theo email.", e);
        }
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
    @Transactional
    public void insert(User user) {
        try {
            if (user.getRoleid() <= 0) {
                user.setRoleid(5);
            }
            enma.persist(user);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể thêm tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public void update(User user) {
        try {
            enma.merge(user);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public User updateProfile(int userId, String fullName, String phone, String avatar) {
        try {
            User existing = enma.find(User.class, userId);
            if (existing == null) {
                return null;
            }
            existing.setFullName(fullName);
            existing.setPhone(phone);
            if (avatar != null && !avatar.isBlank()) {
                existing.setAvatar(avatar.trim());
            }
            enma.flush();
            return existing;
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật hồ sơ cá nhân.", e);
        }
    }

    @Override
    @Transactional
    public User updateEmail(int userId, String newEmail) {
        try {
            User existing = enma.find(User.class, userId);
            if (existing == null) {
                return null;
            }
            existing.setEmail(newEmail);
            enma.flush();
            return existing;
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật email mới.", e);
        }
    }

    @Override
    @Transactional
    public void updatePassword(String username, String passwordHash) {
        try {
            enma.createQuery("UPDATE User u SET u.passWord = :password WHERE u.userName = :username")
                    .setParameter("password", passwordHash)
                    .setParameter("username", username)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Không thể nâng cấp mật khẩu tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public void updateStatus(int userId, int status) {
        try {
            User user = enma.find(User.class, userId);
            if (user != null) {
                user.setStatus(status);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật trạng thái kích hoạt tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public void updatePasswordByEmail(String email, String passwordHash) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            enma.createQuery("UPDATE User u SET u.passWord = :password WHERE LOWER(TRIM(u.email)) = :email")
                    .setParameter("password", passwordHash)
                    .setParameter("email", email.trim().toLowerCase(Locale.ROOT))
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật mật khẩu qua email.", e);
        }
    }

    @Override
    @Transactional
    public int migratePlaintextPasswords() {
        try {
            List<User> users = enma.createQuery(
                    "SELECT u FROM User u WHERE u.passWord IS NOT NULL AND u.passWord <> ''", User.class)
                    .getResultList();
            int migrated = 0;
            for (User user : users) {
                String stored = user.getPassWord();
                if (!PasswordHasher.isHash(stored)) {
                    enma.createQuery("UPDATE User u SET u.passWord = :password WHERE u.id = :id")
                            .setParameter("password", PasswordHasher.hash(stored))
                            .setParameter("id", user.getId())
                            .executeUpdate();
                    migrated++;
                }
            }
            return migrated;
        } catch (Exception e) {
            throw new DataAccessException("Không thể mã hóa các mật khẩu cũ.", e);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        try {
            User user = enma.find(User.class, id);
            if (user != null) {
                enma.remove(user);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể xóa tài khoản.", e);
        }
    }

    @Override
    public boolean checkExistEmail(String email) {
        return checkExistEmail(email, 0);
    }

    @Override
    public boolean checkExistEmail(String email, int excludedId) {
        return exists("email", email, excludedId);
    }

    @Override
    public boolean checkExistUsername(String username) {
        return checkExistUsername(username, 0);
    }

    @Override
    public boolean checkExistUsername(String username, int excludedId) {
        return exists("userName", username, excludedId);
    }

    @Override
    public boolean checkExistPhone(String phone) {
        return checkExistPhone(phone, 0);
    }

    @Override
    public boolean checkExistPhone(String phone, int excludedId) {
        return exists("phone", phone, excludedId);
    }

    private boolean exists(String fieldName, String value, int excludedId) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            String jpql = "SELECT COUNT(u) FROM User u WHERE u." + fieldName + " = :value";
            if (excludedId > 0) {
                jpql += " AND u.id <> :excludedId";
            }
            TypedQuery<Long> query = enma.createQuery(jpql, Long.class);
            query.setParameter("value", value.trim());
            if (excludedId > 0) {
                query.setParameter("excludedId", excludedId);
            }
            Long count = query.getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DataAccessException("Không thể kiểm tra dữ liệu tài khoản.", e);
        }
    }

    @Override
    public List<User> getAll() {
        return findAll();
    }
}
