package lt_web.repository;

import java.time.LocalDateTime;
import lt_web.entity.OtpToken;

public interface IOtpRepository {
    void create(OtpToken token);
    OtpToken findLatestValid(String email, String otpType, LocalDateTime now);
    void markUsed(int id);
    void invalidatePrevious(String email, String otpType);
}
