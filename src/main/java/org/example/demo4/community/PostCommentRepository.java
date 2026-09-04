package org.example.demo4.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    @Query("select c.postId, count(c) from PostComment c where c.postId in :postIds group by c.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") Collection<Long> postIds);
}
