package lt_web.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "Category")
@NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.cateid ASC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cate_id")
    private int cateid;

    @NotBlank(message = "Tên danh mục không được để trống.")
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự.")
    @Column(name = "cate_name", nullable = false, length = 255)
    private String catename;

    @Size(max = 255, message = "Tên ảnh không được vượt quá 255 ký tự.")
    @Column(name = "icons", length = 255)
    private String icon;

    public int getId() {
        return cateid;
    }

    public void setId(int id) {
        this.cateid = id;
    }

    public int getCateid() {
        return cateid;
    }

    public void setCateid(int cateid) {
        this.cateid = cateid;
    }

    public int getCateId() {
        return cateid;
    }

    public void setCateId(int cateId) {
        this.cateid = cateId;
    }

    public String getName() {
        return catename;
    }

    public void setName(String name) {
        this.catename = name;
    }

    public String getCatename() {
        return catename;
    }

    public void setCatename(String catename) {
        this.catename = catename;
    }

    public boolean isExternalIcon() {
        return icon != null && (icon.startsWith("http://") || icon.startsWith("https://"));
    }

    public String getCateName() {
        return catename;
    }

    public void setCateName(String cateName) {
        this.catename = cateName;
    }
}
