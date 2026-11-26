package lt_web.entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "users")
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u ORDER BY u.id ASC")
@Getter
@Setter
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    @Column(name = "email", length = 100)
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự.")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String userName;

    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
    @Column(name = "fullname", length = 100)
    private String fullName;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(max = 255, message = "Mật khẩu không được vượt quá 255 ký tự.")
    @Column(name = "password", nullable = false, length = 255)
    private String passWord;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "roleid")
    private Integer roleid = 5;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "createdDate")
    private Date createdDate;

    @Column(name = "status")
    private Integer status = 1;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Video> videos;

    public User(String email, String userName, String fullName, String passWord,
                String avatar, int roleid, String phone, Date createdDate) {
        this.email = email;
        this.userName = userName;
        this.fullName = fullName;
        this.passWord = passWord;
        this.avatar = avatar;
        this.roleid = roleid;
        this.phone = phone;
        this.createdDate = createdDate;
    }

    public User(int id, String email, String userName, String fullName, String passWord,
                String avatar, int roleid, String phone, Date createdDate) {
        this.id = id;
        this.email = email;
        this.userName = userName;
        this.fullName = fullName;
        this.passWord = passWord;
        this.avatar = avatar;
        this.roleid = roleid;
        this.phone = phone;
        this.createdDate = createdDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getFullName() {
        return (fullName != null && !fullName.isBlank()) ? fullName : userName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullname() {
        return getFullName();
    }

    public void setFullname(String fullname) {
        this.fullName = fullname;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getPassword() {
        return passWord;
    }

    public void setPassword(String password) {
        this.passWord = password;
    }

    public int getRoleid() {
        return roleid == null ? 5 : roleid;
    }

    public void setRoleid(int roleid) {
        this.roleid = roleid;
    }

    public Integer getStatus() {
        return status == null ? 1 : status;
    }

    public boolean isActive() {
        return getStatus() == 1;
    }
}
