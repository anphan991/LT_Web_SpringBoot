package lt_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "Videos")
@NamedQuery(
        name = "Video.findAll",
        query = "SELECT v FROM Video v LEFT JOIN FETCH v.user ORDER BY v.createdAt DESC, v.videoId DESC"
)
@Getter
@Setter
@NoArgsConstructor
public class Video implements Serializable {
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/|live/)|youtu\\.be/)([A-Za-z0-9_-]{11})"
    );
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private int videoId;

    @NotBlank(message = "Tiêu đề video không được để trống.")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự.")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 255, message = "Đường dẫn poster không được vượt quá 255 ký tự.")
    @Column(name = "poster", length = 255)
    private String poster;

    @Size(max = 500, message = "Đường dẫn video không được vượt quá 500 ký tự.")
    @Column(name = "video_path", length = 500)
    private String videoPath;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự.")
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "views", nullable = false)
    private int views = 0;

    @Column(name = "active", nullable = false)
    private int active = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Video(String title, String poster, String videoPath, String description, User user) {
        this.title = title;
        this.poster = poster;
        this.videoPath = videoPath;
        this.description = description;
        this.user = user;
        this.views = 0;
        this.active = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Video(int videoId, String title, String poster, String videoPath,
                 String description, int views, int active, LocalDateTime createdAt, User user) {
        this.videoId = videoId;
        this.title = title;
        this.poster = poster;
        this.videoPath = videoPath;
        this.description = description;
        this.views = views;
        this.active = active;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.user = user;
    }

    public int getId() {
        return videoId;
    }

    public void setId(int id) {
        this.videoId = id;
    }

    public boolean isExternalVideo() {
        return videoPath != null && (videoPath.startsWith("http://") || videoPath.startsWith("https://"));
    }

    public boolean isYoutubeVideo() {
        return getYoutubeEmbedUrl() != null;
    }

    public String getYoutubeEmbedUrl() {
        if (videoPath == null || videoPath.isBlank()) {
            return null;
        }
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(videoPath.trim());
        if (matcher.find()) {
            return "https://www.youtube.com/embed/" + matcher.group(1);
        }
        return null;
    }

    public boolean isSampleVideo() {
        return videoPath == null || videoPath.isBlank() || "default-video.mp4".equals(videoPath);
    }

    public boolean isCustomPoster() {
        return poster != null && !poster.isBlank() && !"default-video-poster.svg".equals(poster);
    }

    public boolean isExternalPoster() {
        return poster != null && (poster.startsWith("http://") || poster.startsWith("https://"));
    }

    public String getYoutubeThumbnailUrl() {
        if (videoPath == null || videoPath.isBlank()) {
            return null;
        }
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(videoPath.trim());
        if (matcher.find()) {
            return "https://img.youtube.com/vi/" + matcher.group(1) + "/hqdefault.jpg";
        }
        return null;
    }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_DATE_FORMAT);
    }
}
