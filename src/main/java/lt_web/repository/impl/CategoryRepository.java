package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lt_web.entity.Category;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.ICategoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CategoryRepository implements ICategoryRepository {

    @PersistenceContext
    private EntityManager enma;

    @Override
    public List<Category> findAll() {
        try {
            TypedQuery<Category> query = enma.createNamedQuery("Category.findAll", Category.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục từ MySQL qua Spring Data JPA.", e);
        }
    }

    @Override
    public Category findById(int id) {
        try {
            return enma.find(Category.class, id);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục theo ID.", e);
        }
    }

    @Override
    @Transactional
    public void create(Category category) {
        try {
            enma.persist(category);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể thêm danh mục.", e);
        }
    }

    @Override
    @Transactional
    public void update(Category category) {
        try {
            enma.merge(category);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật danh mục.", e);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        try {
            Category category = enma.find(Category.class, id);
            if (category != null) {
                enma.remove(category);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể xóa danh mục.", e);
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname) {
        try {
            String jpql = "SELECT c FROM Category c WHERE c.catename LIKE :catname ORDER BY c.cateid ASC";
            TypedQuery<Category> query = enma.createQuery(jpql, Category.class);
            query.setParameter("catname", likePattern(catname));
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm danh mục từ MySQL qua Spring Data JPA.", e);
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname, int page, int pagesize) {
        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pagesize);
        try {
            String jpql = "SELECT c FROM Category c WHERE c.catename LIKE :catname ORDER BY c.cateid ASC";
            TypedQuery<Category> query = enma.createQuery(jpql, Category.class);
            query.setParameter("catname", likePattern(catname));
            query.setFirstResult(safePage * safePageSize);
            query.setMaxResults(safePageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm kiếm và phân trang danh mục.", e);
        }
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pagesize);
        try {
            TypedQuery<Category> query = enma.createNamedQuery("Category.findAll", Category.class);
            query.setFirstResult(safePage * safePageSize);
            query.setMaxResults(safePageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể phân trang danh mục.", e);
        }
    }

    @Override
    public int count() {
        try {
            Long result = enma.createQuery("SELECT count(c) FROM Category c", Long.class).getSingleResult();
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng danh mục.", e);
        }
    }

    @Override
    public int countByCategoryname(String catname) {
        try {
            String jpql = "SELECT count(c) FROM Category c WHERE c.catename LIKE :catname";
            TypedQuery<Long> query = enma.createQuery(jpql, Long.class);
            query.setParameter("catname", likePattern(catname));
            Long result = query.getSingleResult();
            return result == null ? 0 : result.intValue();
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
            String jpql = "SELECT c FROM Category c WHERE c.catename = :name";
            TypedQuery<Category> query = enma.createQuery(jpql, Category.class);
            query.setParameter("name", name.trim());
            List<Category> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
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

    private String likePattern(String keyword) {
        return "%" + (keyword == null ? "" : keyword.trim()) + "%";
    }
}
