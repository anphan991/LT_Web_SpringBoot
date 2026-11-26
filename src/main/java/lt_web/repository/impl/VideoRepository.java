package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IVideoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class VideoRepository implements IVideoRepository {

    @PersistenceContext
    private EntityManager enma;

    @Override
    public List<Video> findAll() {
        try {
            return enma.createNamedQuery("Video.findAll", Video.class).getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách video.", e);
        }
    }

    @Override
    public List<Video> findAll(int page, int pagesize) {
        return search(null, null, page, pagesize);
    }

    @Override
    public List<Video> findLatest(int limit) {
        int safeLimit = Math.max(limit, 1);
        try {
            TypedQuery<Video> query = enma.createQuery(
                    "SELECT v FROM Video v LEFT JOIN FETCH v.user WHERE v.active = 1 ORDER BY v.createdAt DESC, v.videoId DESC",
                    Video.class);
            query.setMaxResults(safeLimit);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách video mới nhất.", e);
        }
    }

    @Override
    public List<Video> findByUserId(int userId) {
        try {
            TypedQuery<Video> query = enma.createQuery(
                    "SELECT v FROM Video v LEFT JOIN FETCH v.user WHERE v.user.id = :userId ORDER BY v.createdAt DESC, v.videoId DESC",
                    Video.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách video của tài khoản.", e);
        }
    }

    @Override
    public List<Video> findByUserId(int userId, int page, int pagesize) {
        return search(null, userId, page, pagesize);
    }

    @Override
    public int count() {
        return countSearch(null, null);
    }

    @Override
    public int countByUserId(int userId) {
        return countSearch(null, userId);
    }

    @Override
    public List<Video> search(String keyword, Integer userId, int page, int pagesize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pagesize, 1);
        String normalizedKeyword = normalizeKeyword(keyword);
        try {
            StringBuilder jpql = new StringBuilder("SELECT v FROM Video v LEFT JOIN FETCH v.user WHERE 1=1");
            if (userId != null) {
                jpql.append(" AND v.user.id = :userId");
            }
            if (normalizedKeyword != null) {
                jpql.append(" AND LOWER(v.title) LIKE :pattern");
            }
            jpql.append(" ORDER BY v.createdAt DESC, v.videoId DESC");

            TypedQuery<Video> query = enma.createQuery(jpql.toString(), Video.class);
            bindFilterParams(query, userId, normalizedKeyword);
            query.setFirstResult((safePage - 1) * safeSize);
            query.setMaxResults(safeSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm kiếm hoặc phân trang video.", e);
        }
    }

    @Override
    public int countSearch(String keyword, Integer userId) {
        String normalizedKeyword = normalizeKeyword(keyword);
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Video v WHERE 1=1");
            if (userId != null) {
                jpql.append(" AND v.user.id = :userId");
            }
            if (normalizedKeyword != null) {
                jpql.append(" AND LOWER(v.title) LIKE :pattern");
            }

            TypedQuery<Long> query = enma.createQuery(jpql.toString(), Long.class);
            bindFilterParams(query, userId, normalizedKeyword);
            Long result = query.getSingleResult();
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng video.", e);
        }
    }

    @Override
    public Video findById(int id) {
        try {
            TypedQuery<Video> query = enma.createQuery(
                    "SELECT v FROM Video v LEFT JOIN FETCH v.user WHERE v.videoId = :id",
                    Video.class);
            query.setParameter("id", id);
            List<Video> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải chi tiết video.", e);
        }
    }

    @Override
    @Transactional
    public void create(Video video) {
        try {
            if (video.getUser() != null && video.getUser().getId() > 0) {
                User managedUser = enma.getReference(User.class, video.getUser().getId());
                video.setUser(managedUser);
            }
            enma.persist(video);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể thêm video mới.", e);
        }
    }

    @Override
    @Transactional
    public void update(Video video) {
        try {
            enma.merge(video);
            enma.flush();
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật video.", e);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        try {
            Video video = enma.find(Video.class, id);
            if (video != null) {
                enma.remove(video);
                enma.flush();
            }
        } catch (Exception e) {
            throw new DataAccessException("Không thể xóa video.", e);
        }
    }

    @Override
    @Transactional
    public void incrementViews(int id) {
        try {
            enma.createQuery("UPDATE Video v SET v.views = v.views + 1 WHERE v.videoId = :id")
                    .setParameter("id", id)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Không thể cập nhật lượt xem video.", e);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void bindFilterParams(TypedQuery<?> query, Integer userId, String normalizedKeyword) {
        if (userId != null) {
            query.setParameter("userId", userId);
        }
        if (normalizedKeyword != null) {
            query.setParameter("pattern", "%" + normalizedKeyword + "%");
        }
    }
}
