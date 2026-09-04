package org.example.demo4.community;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.common.PageResult;
import org.example.demo4.community.dto.CommentResponse;
import org.example.demo4.community.dto.FollowUserResponse;
import org.example.demo4.community.dto.PostCreateRequest;
import org.example.demo4.community.dto.PostCreateRequest.PostMediaRequest;
import org.example.demo4.community.dto.PostMediaResponse;
import org.example.demo4.community.dto.PostResponse;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
public class CommunityService {

    private final PostRepository postRepository;
    private final PostMediaRepository mediaRepository;
    private final PostCommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public CommunityService(PostRepository postRepository,
                            PostMediaRepository mediaRepository,
                            PostCommentRepository commentRepository,
                            PostLikeRepository likeRepository,
                            FollowRepository followRepository,
                            UserRepository userRepository,
                            CurrentUserService currentUserService) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    // ============ 帖子 ============

    public PageResult<PostResponse> listPosts(String topic, int page, int size) {
        Long userId = currentUserService.optionalUserId();
        Specification<Post> spec = (root, query, cb) -> {
            if (topic == null || topic.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(root.get("topics"), "%" + topic.trim() + "%");
        };
        Page<Post> result = postRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 30),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<PostResponse> list = buildPostResponses(result.getContent(), userId);
        return new PageResult<>(list, result.getTotalElements(), page, size);
    }

    public List<PostResponse> listUserPosts(Long userId) {
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return buildPostResponses(posts, currentUserService.optionalUserId());
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest req) {
        User user = currentUserService.requireUser();
        Post post = new Post();
        post.setUserId(user.getId());
        post.setContent(req.getContent());
        post.setTopics(req.getTopics() == null || req.getTopics().isEmpty()
                ? null : String.join(",", req.getTopics()));
        postRepository.save(post);

        List<PostMediaRequest> media = req.getMedia() == null ? List.of() : req.getMedia();
        for (int i = 0; i < media.size(); i++) {
            PostMediaRequest m = media.get(i);
            if (m == null || m.getUrl() == null || m.getUrl().isBlank()) {
                continue;
            }
            PostMedia pm = new PostMedia();
            pm.setPostId(post.getId());
            pm.setUrl(m.getUrl());
            pm.setType(m.getType() == null ? "IMAGE" : m.getType());
            pm.setSortOrder(i);
            mediaRepository.save(pm);
        }
        return buildPostResponses(List.of(post), user.getId()).get(0);
    }

    @Transactional
    public void deletePost(Long postId) {
        User user = currentUserService.requireUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!post.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "无权删除该帖子");
        }
        mediaRepository.deleteAll(mediaRepository.findByPostIdOrderBySortOrderAsc(postId));
        commentRepository.deleteAll(commentRepository.findByPostIdOrderByCreatedAtAsc(postId));
        likeRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId) {
        Long userId = currentUserService.requireUser().getId();
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        Optional<PostLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);
        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);
            liked = true;
        }
        return Map.of("liked", liked, "likeCount", likeRepository.countByPostId(postId));
    }

    // ============ 评论 ============

    public List<CommentResponse> listComments(Long postId) {
        Long userId = currentUserService.optionalUserId();
        List<PostComment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        if (comments.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return comments.stream()
                .map(c -> toCommentResponse(c, users.get(c.getUserId()), userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long postId, String content) {
        User user = currentUserService.requireUser();
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(content);
        commentRepository.save(comment);
        return toCommentResponse(comment, user, user.getId());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User user = currentUserService.requireUser();
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        if (!comment.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "无权删除该评论");
        }
        commentRepository.delete(comment);
    }

    // ============ 关注 ============

    @Transactional
    public Map<String, Object> toggleFollow(Long followingId) {
        Long followerId = currentUserService.requireUser().getId();
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }
        if (!userRepository.existsById(followingId)) {
            throw new BusinessException(404, "用户不存在");
        }
        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        boolean following;
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            following = false;
        } else {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            followRepository.save(follow);
            following = true;
        }
        return Map.of(
                "following", following,
                "followerCount", followRepository.countByFollowingId(followingId),
                "followingCount", followRepository.countByFollowerId(followingId));
    }

    public List<FollowUserResponse> listFollowers(Long userId) {
        List<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId);
        return follows.stream()
                .map(f -> toFollowUser(userRepository.findById(f.getFollowerId()).orElse(null)))
                .collect(Collectors.toList());
    }

    public List<FollowUserResponse> listFollowing(Long userId) {
        List<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId);
        return follows.stream()
                .map(f -> toFollowUser(userRepository.findById(f.getFollowingId()).orElse(null)))
                .collect(Collectors.toList());
    }

    // ============ 内部方法 ============

    private List<PostResponse> buildPostResponses(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        Set<Long> authorIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, User> authors = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, List<PostMedia>> mediaMap = mediaRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(PostMedia::getPostId));
        Map<Long, Long> likeCounts = likeRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Map<Long, Long> commentCounts = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Set<Long> likedIds = currentUserId == null ? Set.of()
                : likeRepository.findByPostIdInAndUserId(postIds, currentUserId).stream()
                        .map(PostLike::getPostId).collect(Collectors.toSet());

        return posts.stream().map(p -> {
            User author = authors.get(p.getUserId());
            PostResponse r = new PostResponse();
            r.setId(p.getId());
            r.setUserId(p.getUserId());
            r.setAuthorName(author == null ? "匿名用户" : displayName(author));
            r.setAuthorAvatar(author == null ? null : author.getAvatar());
            r.setContent(p.getContent());
            r.setTopics(p.getTopics() == null || p.getTopics().isBlank()
                    ? List.of() : Arrays.asList(p.getTopics().split(",")));
            r.setMedia(mediaMap.getOrDefault(p.getId(), List.of()).stream()
                    .sorted(comparing(PostMedia::getSortOrder))
                    .map(m -> new PostMediaResponse(m.getUrl(), m.getType()))
                    .collect(Collectors.toList()));
            r.setLikeCount(likeCounts.getOrDefault(p.getId(), 0L));
            r.setCommentCount(commentCounts.getOrDefault(p.getId(), 0L));
            r.setLiked(likedIds.contains(p.getId()));
            r.setMine(currentUserId != null && currentUserId.equals(p.getUserId()));
            r.setCreatedAt(p.getCreatedAt());
            return r;
        }).collect(Collectors.toList());
    }

    private CommentResponse toCommentResponse(PostComment c, User u, Long currentUserId) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setPostId(c.getPostId());
        r.setUserId(c.getUserId());
        r.setUserName(u == null ? "匿名用户" : displayName(u));
        r.setUserAvatar(u == null ? null : u.getAvatar());
        r.setContent(c.getContent());
        r.setMine(currentUserId != null && currentUserId.equals(c.getUserId()));
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }

    private FollowUserResponse toFollowUser(User u) {
        FollowUserResponse r = new FollowUserResponse();
        if (u == null) {
            return r;
        }
        r.setId(u.getId());
        r.setNickname(displayName(u));
        r.setAvatar(u.getAvatar());
        return r;
    }

    private String displayName(User u) {
        return u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname();
    }
}
