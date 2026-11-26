package lt_web.repository;

import java.util.List;
import lt_web.entity.Category;

public interface ICategoryRepository {
    List<Category> findAll();
    Category findById(int id);
    void create(Category category);
    void update(Category category);
    void delete(int id);
    List<Category> findByCategoryname(String catname);
    List<Category> findByCategoryname(String catname, int page, int pagesize);
    List<Category> findAll(int page, int pagesize);
    int count();
    int countByCategoryname(String catname);

    void insert(Category category);
    void edit(Category category);
    Category get(int id);
    Category get(String name);
    List<Category> getAll();
    List<Category> search(String keyword);
}
