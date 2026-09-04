package org.example.demo4.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    List<PostMedia> findByPostIdOrderBySortOrderAsc(Long postId);

    List<PostMedia> findByPostIdIn(Collection<Long> postIds);
}
