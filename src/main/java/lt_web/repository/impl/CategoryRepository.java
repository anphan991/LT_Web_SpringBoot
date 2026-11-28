package lt_web.repository.impl;

import java.util.List;
import lt_web.entity.Category;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.CategoryJpaRepository;
import lt_web.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CategoryRepository implements ICategoryRepository {

    private final CategoryJpaRepository categoryJpaRepo;

    @Autowired
    public CategoryRepository(CategoryJpaRepository categoryJpaRepo) {
        this.categoryJpaRepo = categoryJpaRepo;
    }

    @Override
    public List<Category> findAll() {
        try {
            return categoryJpaRepo.findAllByOrderByCateidAsc();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục từ MySQL qua Spring Data JPA.", e);
        }
    }

    @Override
    public Category findById(int id) {
        try {
            return categoryJpaRepo.findById(id).orElse(null);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục theo ID.", e);
        }
    }

    @Override
    @Transactional
    public void create(Category category) {
        try {
            categoryJpaRepo.saveAndFlush(category);
        } catch (Exception e) {
            throw new DataAccessException("Không thể thêm danh mục.", e);
        }
    }

    @Override
    @Transactional
    public void update(Category category) {
        try {
            categoryJpaRepo.saveAndFlush(category);
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật danh mục.", e);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        try {
            if (categoryJpaRepo.existsById(id)) {
                categoryJpaRepo.deleteById(id);
                categoryJpaRepo.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể xóa danh mục.", e);
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname) {
        try {
            String normalized = normalizeKeyword(catname);
            if (normalized.isEmpty()) {
                return categoryJpaRepo.findAllByOrderByCateidAsc();
            }
            return categoryJpaRepo.findByCatenameContainingIgnoreCaseOrderByCateidAsc(normalized);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm danh mục từ MySQL qua Spring Data JPA.", e);
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname, int page, int pagesize) {
        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pagesize);
        try {
            String normalized = normalizeKeyword(catname);
            PageRequest pageable = PageRequest.of(safePage, safePageSize);
            if (normalized.isEmpty()) {
                return categoryJpaRepo.findAllByOrderByCateidAsc(pageable).getContent();
            }
            return categoryJpaRepo.findByCatenameContainingIgnoreCaseOrderByCateidAsc(normalized, pageable).getContent();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm kiếm và phân trang danh mục.", e);
        }
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pagesize);
        try {
            return categoryJpaRepo.findAllByOrderByCateidAsc(PageRequest.of(safePage, safePageSize)).getContent();
        } catch (Exception e) {
            throw new DataAccessException("Không thể phân trang danh mục.", e);
        }
    }

    @Override
    public int count() {
        try {
            return (int) categoryJpaRepo.count();
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng danh mục.", e);
        }
    }

    @Override
    public int countByCategoryname(String catname) {
        try {
            String normalized = normalizeKeyword(catname);
            if (normalized.isEmpty()) {
                return (int) categoryJpaRepo.count();
            }
            return (int) categoryJpaRepo.countByCatenameContainingIgnoreCase(normalized);
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng danh mục theo từ khóa.", e);
        }
    }

    @Override
    @Transactional
    public void insert(Category category) {
        create(category);
    }

    @Override
    @Transactional
    public void edit(Category category) {
        update(category);
    }

    @Override
    public Category get(int id) {
        return findById(id);
    }

    @Override
    public Category get(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return categoryJpaRepo.findFirstByCatename(name.trim()).orElse(null);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục theo tên.", e);
        }
    }

    @Override
    public List<Category> getAll() {
        return findAll();
    }

    @Override
    public List<Category> search(String keyword) {
        return findByCategoryname(keyword);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
