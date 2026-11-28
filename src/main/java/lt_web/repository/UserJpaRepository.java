package lt_web.repository;

import java.util.List;
import java.util.Optional;
import lt_web.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<User, Integer> {
    List<User> findAllByOrderByIdAsc();

    Page<User> findAllByOrderByIdAsc(Pageable pageable);

    Optional<User> findFirstByUserName(String userName);

    @Query("SELECT u FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) ORDER BY u.id ASC")
    List<User> findByNormalizedEmail(@Param("email") String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleid IS NULL OR u.roleid = :roleid)
            """)
    Page<User> searchUsers(@Param("keyword") String keyword, @Param("roleid") Integer roleid, Pageable pageable);

    @Query("""
            SELECT COUNT(u) FROM User u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleid IS NULL OR u.roleid = :roleid)
            """)
    long countSearchUsers(@Param("keyword") String keyword, @Param("roleid") Integer roleid);

    long countByRoleid(Integer roleid);

    long countByStatus(Integer status);
}
