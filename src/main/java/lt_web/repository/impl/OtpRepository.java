package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lt_web.entity.OtpToken;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IOtpRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class OtpRepository implements IOtpRepository {

    @PersistenceContext
    private EntityManager enma;

    @Override
    @Transactional
    public void create(OtpToken token) {
        if (token == null) {
            return;
        }
        try {
            enma.persist(token);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể lưu mã OTP.", e);
        }
    }

    @Override
    public OtpToken findLatestValid(String email, String otpType, LocalDateTime now) {
        if (email == null || email.isBlank() || otpType == null) {
            return null;
        }
        try {
            TypedQuery<OtpToken> query = enma.createNamedQuery("OtpToken.findLatestValid", OtpToken.class);
            query.setParameter("email", email.trim().toLowerCase(Locale.ROOT));
            query.setParameter("otpType", otpType);
            query.setParameter("now", now != null ? now : LocalDateTime.now());
            List<OtpToken> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm mã OTP hợp lệ.", e);
        }
    }

    @Override
    @Transactional
    public void markUsed(int id) {
        try {
            OtpToken token = enma.find(OtpToken.class, id);
            if (token != null) {
                token.setUsed(true);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể đánh dấu mã OTP đã sử dụng.", e);
        }
    }

    @Override
    @Transactional
    public void invalidatePrevious(String email, String otpType) {
        if (email == null || email.isBlank() || otpType == null) {
            return;
        }
        try {
            enma.createQuery("UPDATE OtpToken o SET o.used = true WHERE LOWER(TRIM(o.email)) = :email AND o.otpType = :otpType AND o.used = false")
                    .setParameter("email", email.trim().toLowerCase(Locale.ROOT))
                    .setParameter("otpType", otpType)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Không thể vô hiệu hóa mã OTP cũ.", e);
        }
    }
}
