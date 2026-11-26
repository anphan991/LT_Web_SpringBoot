package lt_web.services;

import java.util.List;
import lt_web.entity.Category;

public interface CategoryService {
    void insert(Category category);
    void edit(Category category);
    void delete(int id);
    Category get(int id);
    Category get(String name);
    List<Category> getAll();
    List<Category> search(String keyword);

    List<Category> findAll();
    Category findById(int id);
    void create(Category category);
    void update(Category category);
    List<Category> findByCategoryname(String catname);
    List<Category> findByCategoryname(String catname, int page, int pagesize);
    List<Category> findAll(int page, int pagesize);
    int count();
    int countByCategoryname(String catname);
}
