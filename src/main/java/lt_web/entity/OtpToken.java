package lt_web.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "otp_tokens")
@NamedQuery(
        name = "OtpToken.findLatestValid",
        query = "SELECT o FROM OtpToken o WHERE LOWER(TRIM(o.email)) = :email AND o.otpType = :otpType AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC"
)
@Getter
@Setter
@NoArgsConstructor
public class OtpToken implements Serializable {
    public static final String TYPE_ACTIVATION = "ACTIVATION";
    public static final String TYPE_FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String TYPE_EMAIL_CHANGE = "EMAIL_CHANGE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "otp_type", nullable = false, length = 20)
    private String otpType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OtpToken(String email, String otpCode, String otpType, LocalDateTime expiresAt) {
        this.email = email;
        this.otpCode = otpCode;
        this.otpType = otpType;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }
}
