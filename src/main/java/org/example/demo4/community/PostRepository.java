package org.example.demo4.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
