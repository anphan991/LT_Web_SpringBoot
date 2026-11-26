package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.List;
import lt_web.entity.RememberToken;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IRememberTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class RememberTokenRepository implements IRememberTokenRepository {

    @PersistenceContext
    private EntityManager enma;

    @Override
    @Transactional
    public void save(String tokenHash, int userId, Timestamp expiresAt) {
        try {
            RememberToken token = new RememberToken(
                    tokenHash,
                    userId,
                    expiresAt,
                    new Timestamp(System.currentTimeMillis()));
            enma.merge(token);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể lưu phiên đăng nhập ghi nhớ.", e);
        }
    }

    @Override
    public User findValidUser(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return null;
        }
        try {
            String jpql = "SELECT u FROM RememberToken t JOIN User u ON u.id = t.userId "
                    + "WHERE t.tokenHash = :tokenHash AND t.expiresAt > :now";
            TypedQuery<User> query = enma.createQuery(jpql, User.class);
            query.setParameter("tokenHash", tokenHash);
            query.setParameter("now", new Timestamp(System.currentTimeMillis()));
            List<User> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể xác thực Cookie ghi nhớ.", e);
        }
    }

    @Override
    @Transactional
    public void delete(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return;
        }
        try {
            RememberToken token = enma.find(RememberToken.class, tokenHash);
            if (token != null) {
                enma.remove(token);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể thu hồi Cookie ghi nhớ.", e);
        }
    }

    @Override
    @Transactional
    public void deleteExpired() {
        try {
            enma.createQuery("DELETE FROM RememberToken t WHERE t.expiresAt <= :now")
                    .setParameter("now", new Timestamp(System.currentTimeMillis()))
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Không thể dọn Cookie ghi nhớ hết hạn.", e);
        }
    }
}
