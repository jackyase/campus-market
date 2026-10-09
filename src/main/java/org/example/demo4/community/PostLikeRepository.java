package org.example.demo4.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    List<PostLike> findByPostIdInAndUserId(Collection<Long> postIds, Long userId);

    @Query("select l.postId, count(l) from PostLike l where l.postId in :postIds group by l.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") Collection<Long> postIds);

    long countByPostId(Long postId);

    void deleteByPostId(Long postId);

    void deleteByPostIdIn(Collection<Long> postIds);

    void deleteByUserId(Long userId);
}
