package lt_web.controllers.admin;

import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.Category;
import lt_web.exceptions.DataAccessException;
import lt_web.services.CategoryService;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CategoryAddController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryAddController.class);

    private final CategoryService cateService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public CategoryAddController(CategoryService cateService, CloudinaryService cloudinaryService) {
        this.cateService = cateService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/admin/category/add")
    public String showAddForm() {
        return Constant.View.ADMIN_ADD_CATEGORY;
    }

    @PostMapping("/admin/category/add")
    public String handleAdd(
            @RequestParam(value = "catename", required = false) String categoryname,
            @RequestParam(value = "icon", required = false) MultipartFile iconFile,
            @RequestParam(value = "images", required = false) MultipartFile imagesFile,
            Model model) {
        Category category = new Category();
        String trimmedName = categoryname == null ? "" : categoryname.trim();
        category.setCateName(trimmedName);

        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(category));
        if (!errors.containsKey("catename") && trimmedName.length() < 2) {
            errors.put("catename", "Tên danh mục phải có ít nhất 2 ký tự.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showFormError(model, trimmedName, errors.values().iterator().next());
        }

        String storedName = null;
        try {
            MultipartFile part = (iconFile != null && !iconFile.isEmpty()) ? iconFile
                    : ((imagesFile != null && !imagesFile.isEmpty()) ? imagesFile : null);
            if (part != null && part.getSize() > 5L * 1024 * 1024) {
                errors.put("icon", "Kích thước ảnh tối đa là 5MB.");
                model.addAttribute("errors", errors);
                return showFormError(model, trimmedName, errors.get("icon"));
            }
            storedName = cloudinaryService.saveImageWithCloudFallback(part, "shopmvc/categories", 255);
            category.setIcon(storedName == null ? "default-category.svg" : storedName);
        } catch (IllegalStateException e) {
            LOGGER.warn("Ảnh danh mục vượt dung lượng cho phép", e);
            errors.put("icon", "Kích thước ảnh vượt quá giới hạn tối đa (5MB).");
            model.addAttribute("errors", errors);
            return showFormError(model, trimmedName, errors.get("icon"));
        } catch (Exception e) {
            LOGGER.warn("Không thể lưu ảnh danh mục", e);
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.";
            errors.put("icon", msg);
            model.addAttribute("errors", errors);
            return showFormError(model, trimmedName, msg);
        }

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            model.addAttribute("errors", ValidationUtil.fieldErrors(category));
            return showFormError(model, trimmedName, validationMessage);
        }

        try {
            cateService.insert(category);
            return "redirect:/admin/category/list?categoryAdded=1";
        } catch (DataAccessException e) {
            FileStorage.deleteQuietly(storedName);
            LOGGER.warn("Không thể thêm danh mục", e);
            return showFormError(model, category.getCatename(), "Không thể lưu danh mục. Vui lòng thử lại.");
        }
    }

    private String showFormError(Model model, String catename, String alert) {
        model.addAttribute("catename", catename);
        model.addAttribute("alert", alert);
        return Constant.View.ADMIN_ADD_CATEGORY;
    }
}
