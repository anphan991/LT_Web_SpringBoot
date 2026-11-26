package lt_web.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lt_web.entity.Category;
import lt_web.repository.ICategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CategoryServiceImplTest {
    private InMemoryCategoryRepository categoryRepo;
    private CategoryServiceImpl categoryService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        System.setProperty("bt.lt.upload.dir", tempUploadDir.toString());
        categoryRepo = new InMemoryCategoryRepository();
        categoryService = new CategoryServiceImpl(categoryRepo);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("bt.lt.upload.dir");
    }

    @Test
    void createAndReadMethodsSupportJpaAndLegacyApis() {
        categoryService.create(new Category(0, "Áo Thun", "default-shirt.svg"));
        categoryService.insert(new Category(0, "Giày Chạy Bộ", "default-shoes.svg"));

        assertEquals(2, categoryService.count());
        assertEquals(2, categoryService.findAll().size());
        assertEquals(2, categoryService.getAll().size());

        Category first = categoryService.findById(1);
        assertNotNull(first);
        assertEquals("Áo Thun", first.getCatename());
        assertEquals("Áo Thun", categoryService.get("Áo Thun").getName());
        assertEquals(first.getCateid(), categoryService.get(1).getCateid());
    }

    @Test
    void editPreservesExistingIconWhenNewIconIsBlank() {
        categoryService.create(new Category(0, "Quần Jeans", "custom-old.png"));

        categoryService.edit(new Category(1, "Quần Jeans Slim", "   "));

        Category updated = categoryService.findById(1);
        assertEquals("Quần Jeans Slim", updated.getCatename());
        assertEquals("custom-old.png", updated.getIcon());
    }

    @Test
    void updateReplacesAndDeletesPreviousCustomIconOnly() throws IOException {
        Path oldCustom = Files.writeString(tempUploadDir.resolve("custom-old.png"), "old");
        Path defaultIcon = Files.writeString(tempUploadDir.resolve("default-shirt.svg"), "default");

        categoryService.create(new Category(0, "Áo Khoác", "custom-old.png"));
        categoryService.create(new Category(0, "Mũ Lưỡi Trai", "default-shirt.svg"));

        categoryService.update(new Category(1, "Áo Khoác Gió", "custom-new.png"));
        categoryService.update(new Category(2, "Mũ Len", "custom-hat.png"));

        assertFalse(Files.exists(oldCustom));
        assertTrue(Files.exists(defaultIcon));
        assertEquals("custom-new.png", categoryService.findById(1).getIcon());
        assertEquals("custom-hat.png", categoryService.findById(2).getIcon());
    }

    @Test
    void deleteRemovesCategoryAndCustomUploadedFile() throws IOException {
        Path customFile = Files.writeString(tempUploadDir.resolve("upload-123.png"), "img");
        Path defaultFile = Files.writeString(tempUploadDir.resolve("default-shoes.svg"), "svg");

        categoryService.create(new Category(0, "Túi Xách", "upload-123.png"));
        categoryService.create(new Category(0, "Giày Thể Thao", "default-shoes.svg"));

        categoryService.delete(1);
        categoryService.delete(2);

        assertNull(categoryService.findById(1));
        assertNull(categoryService.findById(2));
        assertEquals(0, categoryService.count());
        assertFalse(Files.exists(customFile));
        assertTrue(Files.exists(defaultFile));
    }

    @Test
    void searchAndPaginationReturnExpectedSlicesAndCounts() {
        categoryService.create(new Category(0, "Áo Nam", "default-shirt.svg"));
        categoryService.create(new Category(0, "Áo Nữ", "default-dress.svg"));
        categoryService.create(new Category(0, "Áo Khoác", "default-shirt.svg"));
        categoryService.create(new Category(0, "Giày Nam", "default-shoes.svg"));
        categoryService.create(new Category(0, "Áo Thể Thao", "default-shirt.svg"));
        categoryService.create(new Category(0, "Áo Sơ Mi", "default-shirt.svg"));

        assertEquals(6, categoryService.count());
        assertEquals(5, categoryService.countByCategoryname("áo"));
        assertEquals(5, categoryService.search("ÁO").size());
        assertEquals(5, categoryService.findByCategoryname("áo").size());

        List<Category> page1 = categoryService.findAll(1, 5);
        List<Category> page2 = categoryService.findAll(2, 5);
        assertEquals(5, page1.size());
        assertEquals(1, page2.size());
        assertEquals("Áo Sơ Mi", page2.get(0).getCatename());

        List<Category> filteredPage1 = categoryService.findByCategoryname("áo", 1, 2);
        List<Category> filteredPage3 = categoryService.findByCategoryname("áo", 3, 2);
        assertEquals(2, filteredPage1.size());
        assertEquals(1, filteredPage3.size());
        assertEquals("Áo Sơ Mi", filteredPage3.get(0).getCatename());
    }

    private static final class InMemoryCategoryRepository implements ICategoryRepository {
        private final Map<Integer, Category> storage = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public List<Category> findAll() {
            return storage.values().stream()
                    .sorted(Comparator.comparingInt(Category::getCateid))
                    .toList();
        }

        @Override
        public Category findById(int id) {
            return storage.get(id);
        }

        @Override
        public void create(Category category) {
            int id = nextId++;
            Category copy = new Category(id, category.getCatename(), category.getIcon());
            category.setCateid(id);
            storage.put(id, copy);
        }

        @Override
        public void update(Category category) {
            if (storage.containsKey(category.getCateid())) {
                storage.put(category.getCateid(), new Category(category.getCateid(), category.getCatename(), category.getIcon()));
            }
        }

        @Override
        public void delete(int id) {
            storage.remove(id);
        }

        @Override
        public List<Category> findByCategoryname(String catname) {
            String normalized = catname == null ? "" : catname.trim().toLowerCase(Locale.ROOT);
            List<Category> matches = new ArrayList<>();
            for (Category category : findAll()) {
                if (category.getCatename() != null
                        && category.getCatename().toLowerCase(Locale.ROOT).contains(normalized)) {
                    matches.add(category);
                }
            }
            return matches;
        }

        @Override
        public List<Category> findByCategoryname(String catname, int page, int pagesize) {
            return slice(findByCategoryname(catname), page, pagesize);
        }

        @Override
        public List<Category> findAll(int page, int pagesize) {
            return slice(findAll(), page, pagesize);
        }

        @Override
        public int count() {
            return storage.size();
        }

        @Override
        public int countByCategoryname(String catname) {
            return findByCategoryname(catname).size();
        }

        @Override
        public void insert(Category category) {
            create(category);
        }

        @Override
        public void edit(Category category) {
            update(category);
        }

        @Override
        public Category get(int id) {
            return findById(id);
        }

        @Override
        public Category get(String name) {
            if (name == null) {
                return null;
            }
            return storage.values().stream()
                    .filter(c -> name.equals(c.getCatename()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Category> getAll() {
            return findAll();
        }

        @Override
        public List<Category> search(String keyword) {
            return findByCategoryname(keyword);
        }

        private List<Category> slice(List<Category> items, int page, int pagesize) {
            int safePage = Math.max(page, 1);
            int safePageSize = Math.max(pagesize, 1);
            int from = (safePage - 1) * safePageSize;
            if (from >= items.size()) {
                return List.of();
            }
            int to = Math.min(from + safePageSize, items.size());
            return items.subList(from, to);
        }
    }
}
