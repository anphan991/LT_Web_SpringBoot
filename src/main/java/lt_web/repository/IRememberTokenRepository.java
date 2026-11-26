package lt_web.repository;

import java.sql.Timestamp;
import lt_web.entity.User;

public interface IRememberTokenRepository {
    void save(String tokenHash, int userId, Timestamp expiresAt);
    User findValidUser(String tokenHash);
    void delete(String tokenHash);
    void deleteExpired();
}
