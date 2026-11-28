package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IUserRepository;
import lt_web.repository.UserJpaRepository;
import lt_web.utils.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class UserRepository implements IUserRepository {

    private final UserJpaRepository userJpaRepo;

    @PersistenceContext
    private EntityManager enma;

    @Autowired
    public UserRepository(UserJpaRepository userJpaRepo) {
        this.userJpaRepo = userJpaRepo;
    }

    @Override
    public List<User> findAll() {
        try {
            return userJpaRepo.findAllByOrderByIdAsc();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách tài khoản.", e);
        }
    }

    @Override
    public List<User> findAll(int page, int pageSize) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, pageSize);
        try {
            return userJpaRepo.findAllByOrderByIdAsc(PageRequest.of(safePage, safeSize)).getContent();
        } catch (Exception e) {
            throw new DataAccessException("Không thể phân trang danh sách tài khoản.", e);
        }
    }

    @Override
    public List<User> search(String keyword, Integer roleid, int page, int pageSize) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, pageSize);
        String cleanKeyword = normalizeKeyword(keyword);
        try {
            PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
            return userJpaRepo.searchUsers(cleanKeyword, roleid, pageable).getContent();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm kiếm và phân trang tài khoản.", e);
        }
    }

    @Override
    public int count() {
        try {
            return (int) userJpaRepo.count();
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng tài khoản.", e);
        }
    }

    @Override
    public int countSearch(String keyword, Integer roleid) {
        String cleanKeyword = normalizeKeyword(keyword);
        try {
            return (int) userJpaRepo.countSearchUsers(cleanKeyword, roleid);
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng tài khoản theo điều kiện tìm kiếm.", e);
        }
    }

    @Override
    public int countByRole(int roleid) {
        try {
            return (int) userJpaRepo.countByRoleid(roleid);
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng tài khoản theo vai trò.", e);
        }
    }

    @Override
    public int countByStatus(int status) {
        try {
            return (int) userJpaRepo.countByStatus(status);
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng tài khoản theo trạng thái.", e);
        }
    }

    @Override
    public User findById(int id) {
        try {
            return userJpaRepo.findById(id).orElse(null);
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
            return userJpaRepo.findFirstByUserName(username.trim()).orElse(null);
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
            List<User> results = userJpaRepo.findByNormalizedEmail(email.trim());
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
            userJpaRepo.saveAndFlush(user);
        } catch (Exception e) {
            throw new DataAccessException("Không thể thêm tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public void update(User user) {
        try {
            userJpaRepo.saveAndFlush(user);
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật tài khoản.", e);
        }
    }

    @Override
    @Transactional
    public User updateProfile(int userId, String fullName, String phone, String avatar) {
        try {
            User existing = userJpaRepo.findById(userId).orElse(null);
            if (existing == null) {
                return null;
            }
            existing.setFullName(fullName);
            existing.setPhone(phone);
            if (avatar != null && !avatar.isBlank()) {
                existing.setAvatar(avatar.trim());
            }
            return userJpaRepo.saveAndFlush(existing);
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật hồ sơ cá nhân.", e);
        }
    }

    @Override
    @Transactional
    public User updateEmail(int userId, String newEmail) {
        try {
            User existing = userJpaRepo.findById(userId).orElse(null);
            if (existing == null) {
                return null;
            }
            existing.setEmail(newEmail);
            return userJpaRepo.saveAndFlush(existing);
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
            User user = userJpaRepo.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(status);
                userJpaRepo.saveAndFlush(user);
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
            if (!userJpaRepo.existsById(id)) {
                return;
            }
            enma.createQuery("DELETE FROM RememberToken t WHERE t.userId = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            enma.createQuery("DELETE FROM Video v WHERE v.user.id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            userJpaRepo.deleteById(id);
            userJpaRepo.flush();
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
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            String jpql = "SELECT COUNT(u) FROM User u WHERE LOWER(TRIM(u.email)) = :value";
            if (excludedId > 0) {
                jpql += " AND u.id <> :excludedId";
            }
            TypedQuery<Long> query = enma.createQuery(jpql, Long.class);
            query.setParameter("value", email.trim().toLowerCase(Locale.ROOT));
            if (excludedId > 0) {
                query.setParameter("excludedId", excludedId);
            }
            Long count = query.getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DataAccessException("Không thể kiểm tra email tài khoản.", e);
        }
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
