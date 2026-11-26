package lt_web.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "remember_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RememberToken implements Serializable {
    @Id
    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
}
