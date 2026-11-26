package lt_web.controllers.admin;

import lt_web.entity.Category;
import lt_web.exceptions.DataAccessException;
import lt_web.services.CategoryService;
import lt_web.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CategoryDeleteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDeleteController.class);

    private final CategoryService cateService;

    @Autowired
    public CategoryDeleteController(CategoryService cateService) {
        this.cateService = cateService;
    }

    @GetMapping("/admin/category/delete")
    public String showDeleteConfirmation(
            @RequestParam(value = "id", required = false) String idParam,
            Model model) {
        int id = parseId(idParam);
        Category category = id > 0 ? cateService.get(id) : null;
        if (category == null) {
            return "redirect:/admin/category/list?categoryError=missing";
        }
        model.addAttribute("category", category);
        return Constant.View.ADMIN_DELETE_CATEGORY;
    }

    @PostMapping("/admin/category/delete")
    public String handleDelete(
            @RequestParam(value = "id", required = false) String idParam) {
        int id = parseId(idParam);
        try {
            if (id <= 0 || cateService.get(id) == null) {
                return "redirect:/admin/category/list?categoryError=missing";
            }
            cateService.delete(id);
            return "redirect:/admin/category/list?categoryDeleted=1";
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể xóa danh mục", e);
            return "redirect:/admin/category/list?categoryError=delete";
        }
    }

    private int parseId(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
