package lt_web.repository;

import java.util.List;
import lt_web.entity.Video;

public interface IVideoRepository {
    List<Video> findAll();
    List<Video> findAll(int page, int pagesize);
    List<Video> findLatest(int limit);
    List<Video> findByUserId(int userId);
    List<Video> findByUserId(int userId, int page, int pagesize);
    int count();
    int countByUserId(int userId);
    List<Video> search(String keyword, Integer userId, int page, int pagesize);
    int countSearch(String keyword, Integer userId);
    Video findById(int id);
    void create(Video video);
    void update(Video video);
    void delete(int id);
    void incrementViews(int id);
}
