package org.example.demo4.community;

import jakarta.validation.Valid;
import org.example.demo4.common.PageResult;
import org.example.demo4.common.R;
import org.example.demo4.community.dto.CommentRequest;
import org.example.demo4.community.dto.CommentResponse;
import org.example.demo4.community.dto.FollowUserResponse;
import org.example.demo4.community.dto.PostCreateRequest;
import org.example.demo4.community.dto.PostResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public R<PageResult<PostResponse>> list(
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(communityService.listPosts(topic, page, size));
    }

    @PostMapping("/posts")
    public R<PostResponse> create(@Valid @RequestBody PostCreateRequest req) {
        return R.ok(communityService.createPost(req));
    }

    @DeleteMapping("/posts/{id}")
    public R<Void> delete(@PathVariable Long id) {
        communityService.deletePost(id);
        return R.ok();
    }

    @PostMapping("/posts/{id}/like")
    public R<Map<String, Object>> like(@PathVariable Long id) {
        return R.ok(communityService.toggleLike(id));
    }

    @GetMapping("/posts/{id}/comments")
    public R<List<CommentResponse>> comments(@PathVariable Long id) {
        return R.ok(communityService.listComments(id));
    }

    @PostMapping("/posts/{id}/comments")
    public R<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest req) {
        return R.ok(communityService.addComment(id, req.getContent()));
    }

    @DeleteMapping("/comments/{id}")
    public R<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return R.ok();
    }

    @PostMapping("/users/{id}/follow")
    public R<Map<String, Object>> follow(@PathVariable Long id) {
        return R.ok(communityService.toggleFollow(id));
    }

    @GetMapping("/users/{id}/followers")
    public R<List<FollowUserResponse>> followers(@PathVariable Long id) {
        return R.ok(communityService.listFollowers(id));
    }

    @GetMapping("/users/{id}/following")
    public R<List<FollowUserResponse>> following(@PathVariable Long id) {
        return R.ok(communityService.listFollowing(id));
    }

    @GetMapping("/users/{id}/posts")
    public R<List<PostResponse>> userPosts(@PathVariable Long id) {
        return R.ok(communityService.listUserPosts(id));
    }
}
