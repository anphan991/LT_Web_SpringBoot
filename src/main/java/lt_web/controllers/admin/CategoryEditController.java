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
public class CategoryEditController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryEditController.class);

    private final CategoryService cateService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public CategoryEditController(CategoryService cateService, CloudinaryService cloudinaryService) {
        this.cateService = cateService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/admin/category/edit")
    public String showEditForm(
            @RequestParam(value = "id", required = false) String id,
            Model model) {
        int categoryId = parseId(id);
        if (categoryId <= 0) {
            return "redirect:/admin/category/list";
        }
        try {
            Category category = cateService.get(categoryId);
            if (category == null) {
                return "redirect:/admin/category/list?categoryError=missing";
            }
            model.addAttribute("category", category);
            return Constant.View.ADMIN_EDIT_CATEGORY;
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh mục cần sửa", e);
            return "redirect:/admin/category/list?categoryError=load";
        }
    }

    @PostMapping("/admin/category/edit")
    public String handleEdit(
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "cateid", required = false) String cateidParam,
            @RequestParam(value = "catename", required = false) String catename,
            @RequestParam(value = "icon", required = false) MultipartFile iconPart,
            Model model) {
        String rawId = (idParam != null && !idParam.isBlank()) ? idParam : cateidParam;
        int cateid = parseId(rawId);
        if (cateid <= 0) {
            return "redirect:/admin/category/list";
        }

        Category category = new Category();
        category.setCateId(cateid);
        category.setCateName(catename == null ? "" : catename.trim());

        Category cateold;
        try {
            cateold = cateService.get(cateid);
        } catch (DataAccessException e) {
            LOGGER.warn("Không thể tải danh mục cần cập nhật", e);
            return "redirect:/admin/category/list?categoryError=load";
        }
        if (cateold == null) {
            return "redirect:/admin/category/list?categoryError=missing";
        }

        Category draft = new Category(cateold.getCateid(), category.getCatename(), cateold.getIcon());
        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(category));
        if (!errors.containsKey("catename") && category.getCatename().length() < 2) {
            errors.put("catename", "Tên danh mục phải có ít nhất 2 ký tự.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return showFormError(model, draft, errors.values().iterator().next());
        }

        String fileold = cateold.getIcon() != null ? cateold.getIcon() : "default-category.svg";
        String storedName = null;
        try {
            if (iconPart != null && !iconPart.isEmpty() && iconPart.getSize() > 5L * 1024 * 1024) {
                errors.put("icon", "Kích thước ảnh tối đa là 5MB.");
                model.addAttribute("errors", errors);
                return showFormError(model, draft, errors.get("icon"));
            }
            storedName = cloudinaryService.saveImageWithCloudFallback(iconPart, "shopmvc/categories", 255);
            category.setIcon(storedName == null ? fileold : storedName);
        } catch (IllegalStateException e) {
            LOGGER.warn("Ảnh danh mục vượt dung lượng cho phép", e);
            errors.put("icon", "Kích thước ảnh vượt quá giới hạn tối đa (5MB).");
            model.addAttribute("errors", errors);
            return showFormError(model, draft, errors.get("icon"));
        } catch (Exception e) {
            LOGGER.warn("Không thể lưu ảnh danh mục", e);
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.";
            errors.put("icon", msg);
            model.addAttribute("errors", errors);
            return showFormError(model, draft, msg);
        }

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            model.addAttribute("errors", ValidationUtil.fieldErrors(category));
            return showFormError(model, draft, validationMessage);
        }

        try {
            cateService.edit(category);
            return "redirect:/admin/category/list";
        } catch (DataAccessException | IllegalStateException e) {
            FileStorage.deleteQuietly(storedName);
            LOGGER.warn("Không thể cập nhật danh mục", e);
            return showFormError(model, draft, "Không thể cập nhật danh mục. Vui lòng thử lại.");
        }
    }

    private String showFormError(Model model, Category cateold, String alert) {
        model.addAttribute("category", cateold);
        model.addAttribute("alert", alert);
        return Constant.View.ADMIN_EDIT_CATEGORY;
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
