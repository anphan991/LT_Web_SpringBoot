package lt_web.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import lt_web.entity.Video;
import lt_web.repository.IVideoRepository;
import lt_web.repository.impl.VideoRepository;
import lt_web.services.VideoService;
import lt_web.utils.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VideoServiceImpl implements VideoService {
    private final IVideoRepository videoRepo;

    public VideoServiceImpl() {
        this(new VideoRepository());
    }

    @Autowired
    public VideoServiceImpl(IVideoRepository videoRepo) {
        this.videoRepo = videoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findAll() {
        return videoRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findAll(int page, int pagesize) {
        return videoRepo.findAll(page, pagesize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findLatest(int limit) {
        return videoRepo.findLatest(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findByUserId(int userId) {
        return videoRepo.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findByUserId(int userId, int page, int pagesize) {
        return videoRepo.findByUserId(userId, page, pagesize);
    }

    @Override
    @Transactional(readOnly = true)
    public int count() {
        return videoRepo.count();
    }

    @Override
    @Transactional(readOnly = true)
    public int countByUserId(int userId) {
        return videoRepo.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> search(String keyword, Integer userId, int page, int pagesize) {
        return videoRepo.search(keyword, userId, page, pagesize);
    }

    @Override
    @Transactional(readOnly = true)
    public int countSearch(String keyword, Integer userId) {
        return videoRepo.countSearch(keyword, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Video findById(int id) {
        return videoRepo.findById(id);
    }

    @Override
    public void create(Video video) {
        if (video == null) {
            return;
        }
        if (video.getPoster() == null || video.getPoster().isBlank()) {
            video.setPoster("default-video-poster.svg");
        }
        if (video.getVideoPath() == null || video.getVideoPath().isBlank()) {
            video.setVideoPath("default-video.mp4");
        }
        if (video.getCreatedAt() == null) {
            video.setCreatedAt(LocalDateTime.now());
        }
        videoRepo.create(video);
    }

    @Override
    public void update(Video newVideo) {
        if (newVideo == null) {
            return;
        }
        Video oldVideo = videoRepo.findById(newVideo.getVideoId());
        if (oldVideo == null) {
            return;
        }
        String previousPoster = oldVideo.getPoster();
        String previousVideoPath = oldVideo.getVideoPath();

        oldVideo.setTitle(newVideo.getTitle());
        oldVideo.setDescription(newVideo.getDescription());
        oldVideo.setActive(newVideo.getActive());

        boolean replacingPoster = hasValue(newVideo.getPoster())
                && !newVideo.getPoster().equals(previousPoster);
        if (hasValue(newVideo.getPoster())) {
            oldVideo.setPoster(newVideo.getPoster());
        }

        boolean replacingVideo = hasValue(newVideo.getVideoPath())
                && !newVideo.getVideoPath().equals(previousVideoPath);
        if (hasValue(newVideo.getVideoPath())) {
            oldVideo.setVideoPath(newVideo.getVideoPath());
        }

        videoRepo.update(oldVideo);

        if (replacingPoster) {
            FileStorage.deleteQuietly(previousPoster);
        }
        if (replacingVideo) {
            FileStorage.deleteQuietly(previousVideoPath);
        }
    }

    @Override
    public void delete(int id) {
        Video existing = videoRepo.findById(id);
        videoRepo.delete(id);
        if (existing != null) {
            FileStorage.deleteQuietly(existing.getPoster());
            FileStorage.deleteQuietly(existing.getVideoPath());
        }
    }

    @Override
    public void incrementViews(int id) {
        if (id > 0) {
            videoRepo.incrementViews(id);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
