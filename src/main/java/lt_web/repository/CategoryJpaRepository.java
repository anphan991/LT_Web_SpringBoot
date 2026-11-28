package lt_web.repository;

import java.util.List;
import java.util.Optional;
import lt_web.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByOrderByCateidAsc();

    Page<Category> findAllByOrderByCateidAsc(Pageable pageable);

    List<Category> findByCatenameContainingIgnoreCaseOrderByCateidAsc(String catename);

    Page<Category> findByCatenameContainingIgnoreCaseOrderByCateidAsc(String catename, Pageable pageable);

    long countByCatenameContainingIgnoreCase(String catename);

    Optional<Category> findFirstByCatename(String catename);
}
