package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.exceptions.NotEnoughPrivilegesException;
import com.cheeseind.blogengine.exceptions.PageNotFoundException;
import com.cheeseind.blogengine.mappers.PostDtoMapper;
import com.cheeseind.blogengine.models.ModerationStatus;
import com.cheeseind.blogengine.models.Post;
import com.cheeseind.blogengine.models.User;
import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.blogdto.ModerationResponse;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.PostDto;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.PostsInfoResponse;
import com.cheeseind.blogengine.models.dto.blogdto.votedto.VoteRequest;
import com.cheeseind.blogengine.repositories.PostRepository;
import com.cheeseind.blogengine.util.MongoTemplateQueryCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostDtoMapper postDtoMapper;
    private final PostRepository postRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final SettingService settingService;
    private final MongoTemplateQueryCreator queryCreator;

    //================================= Methods for working with repository =======================

    public Post findPostById(final String id) {
        return postRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Не найден пост с id " + id));
    }

    public List<Post> findAllDatesInYear(int year) {
        return postRepository.findAllByYear(year);
    }

    public long countVisiblePosts() {
        return postRepository.countVisiblePosts();
    }

    public void save(final Post post) {
        postRepository.save(post);
    }

    long countUserAcceptedPosts(final String userId) {
        return postRepository.countActivePostsOfUser(userId, ModerationStatus.ACCEPTED);
    }

    Post findFirstPost() {
        return postRepository.findFirstPost().orElse(null);
    }

    Post findFirstPostOfUser(String userId) {
        return postRepository.findFirstPostOfUser(userId).findFirst().orElse(null);
    }

    public long countLikesOfUserPosts(String userId) {
        return postRepository.countLikesOfAllUserPosts(userId);
    }

    public long countDislikesOfUserPosts(String userId) {
        return postRepository.countDislikesOfAllUserPosts(userId);
    }

    public long countUserPostsViews(String userId){
        return postRepository.countUserPostsViews(userId);
    }

    public long countAllLikes() {
        return postRepository.countAllLikes();
    }

    public long countAllDislikes() {
        return postRepository.countAllDislikes();
    }

    public long countAllPostsViews() {
        return postRepository.countAllPostsViews();
    }

    public long countPostsForModeration() {
        return postRepository.countPostsForModeration();
    }

    //================================= Main logic methods ==========================================

    public PostsInfoResponse<PostDto> findPosts(final int offset, final int limit, final String mode) {
        List<Post> posts;
        long postsCount = countVisiblePosts();
        Pageable pageable = PageRequest.of(offset/limit, limit);
        posts = switch (mode) {
            case "recent" -> postRepository.getRecentPosts(pageable);
            case "early" -> postRepository.getEarlyPosts(pageable);
            case "popular" -> postRepository.getPopularPosts(pageable);
            case "best" -> postRepository.getBestPosts(pageable);
            default -> throw new IllegalArgumentException("Wrong argument 'mode': " + mode);
        };
        List<PostDto> postDtos = getPostDTOs(posts);
        return new PostsInfoResponse<>(postsCount, postDtos);
    }

    public PostsInfoResponse<PostDto> findCurrentUserPosts(final int offset, final int limit, final String status) {
        String userId = userService.getCurrentUser().getId();
        List<Post> posts;
        long postsCount;
        Pageable pageable = PageRequest.of(offset/limit, limit);
        switch (status) {
            case "inactive" -> {
                postsCount = postRepository.countInactivePostsOfUser(userId);
                posts = postRepository.getCurrentUserInactivePosts(userId, pageable);
            }
            case "pending" -> {
                postsCount = postRepository.countActivePostsOfUser(userId, ModerationStatus.NEW);
                posts = postRepository.getCurrentUserActivePosts(userId, ModerationStatus.NEW, pageable);
            }
            case "declined" -> {
                postsCount = postRepository.countActivePostsOfUser(userId, ModerationStatus.DECLINED);
                posts = postRepository.getCurrentUserActivePosts(userId, ModerationStatus.DECLINED, pageable);
            }
            case "published" -> {
                postsCount = postRepository.countActivePostsOfUser(userId, ModerationStatus.ACCEPTED);
                posts = postRepository.getCurrentUserActivePosts(userId, ModerationStatus.ACCEPTED, pageable);
            }
            default -> throw new IllegalArgumentException("Wrong argument 'status': " + status);
        }

        List<PostDto> postDtos = getPostDTOs(posts);
        return new PostsInfoResponse<>(postsCount, postDtos);
    }

    public PostsInfoResponse<ModerationResponse> findPostsForModeration(final int offset, final int limit, final String status) {
        User user = userService.getCurrentUser();
        if(user.isModerator()) {
            long count;
            List<Post> posts;
            Pageable pageable = PageRequest.of(offset/limit, limit);
            switch (status) {
                case "new" -> {
                    count = postRepository.countAllByModerationStatusAndActiveTrue(ModerationStatus.NEW);
                    posts = postRepository.getPostsForModeration(ModerationStatus.NEW, pageable);
                }
                case "declined" -> {
                    count = postRepository.countAllByModerationStatusAndActiveTrue(ModerationStatus.DECLINED);
                    posts = postRepository.getPostsForModeration(ModerationStatus.DECLINED, pageable);
                }
                case "accepted" -> {
                    count = postRepository.countAllByModerationStatusAndActiveTrue(ModerationStatus.ACCEPTED);
                    posts = postRepository.getPostsForModeration(ModerationStatus.ACCEPTED, pageable);
                }
                default -> throw new IllegalArgumentException("Wrong argument 'status': " + status);
            }
            List<ModerationResponse> postDtos = getModerationPostDTOs(posts);
            return new PostsInfoResponse<>(count, postDtos);
        }
        return null;
    }

    public PostsInfoResponse<PostDto> findAllByQuery(final int offset, final int limit, final String query) {
        Pageable pageable = PageRequest.of(offset/limit, limit);
        List<Post> posts = query == null ? postRepository.getRecentPosts(pageable)
                : postRepository.findPostsByQuery(query, pageable);

        List<PostDto> postDtos = getPostDTOs(posts);
        return new PostsInfoResponse<>(posts.size(), postDtos);
    }

    //TODO рефактор + разрешить смотреть незалогиненным
    public PostDto getPost(final String id) {
        log.info(id);
        User user = userService.getCurrentUser();
        Post post = postRepository.findById(id).orElseThrow(PageNotFoundException::new);
        PostDto postDto = null;
        if (user != null && (user.equals(post.getUser()) || user.isModerator())) {
            postDto = postDtoMapper.singlePostToPostDto(post);
        } else if (post.isActive() && post.getModerationStatus() == ModerationStatus.ACCEPTED
                && post.getTime().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            incrementPostViewsCount(post);
            postRepository.save(post);
            postDto = postDtoMapper.singlePostToPostDto(post);
        }
        return postDto;
    }

    public PostsInfoResponse<PostDto> findPostsByDate(final int offset, final int limit, final ZonedDateTime date) {
        Pageable pageable = PageRequest.of(offset/limit, limit);
        List<Post> posts = postRepository.findPostsByDate(date, date.plusDays(1), pageable);
        return new PostsInfoResponse<>(posts.size(), getPostDTOs(posts));
    }

    public PostsInfoResponse<PostDto> findPostsByTag(final int offset, final int limit, final String tag) {
        Pageable pageable = PageRequest.of(offset/limit, limit);
        List<Post> posts = mongoTemplate.find(queryCreator.getPostsByTag(tag, pageable), Post.class);
        return new PostsInfoResponse<>(posts.size(), getPostDTOs(posts));
    }

    @Transactional
    public SimpleResponseDto addPost(final AddPostRequest request) {
        User user = userService.getCurrentUser();
        if (!settingService.isMultiUserEnabled() && !user.isModerator()) {
            throw new NotEnoughPrivilegesException("Публиковать посты может только модератор");
        }
        Post post = postDtoMapper.addPostRequestToPost(request);
        postRepository.save(post);
        return new SimpleResponseDto(true);
    }

    @Transactional
    public SimpleResponseDto editPost(final String id, final AddPostRequest request) {
        Post post = postDtoMapper.editPostRequestToPost(id, request);
        save(post);
        return new SimpleResponseDto(true);
    }

    public SimpleResponseDto likePost(final VoteRequest request) {
        String userId = userService.getCurrentUserId();
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PageNotFoundException::new);
        removeDislikeIfExists(userId, post);
        if (post.getUsersLikedPost().contains(userId)) {
            return new SimpleResponseDto(false);
        }
        post.getUsersLikedPost().add(userId);
        postRepository.save(post);
        return new SimpleResponseDto(true);
    }

    public SimpleResponseDto dislikePost(final VoteRequest request) {
        String userId = userService.getCurrentUserId();
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PageNotFoundException::new);
        removeLikeIfExists(userId, post);
        if (post.getUsersDislikedPost().contains(userId)) {
            return new SimpleResponseDto(false);
        }
        post.getUsersDislikedPost().add(userId);
        postRepository.save(post);
        return new SimpleResponseDto(true);
    }

//    //================================= Additional methods ==========================================

    private List<PostDto> getPostDTOs(final Iterable<Post> posts) {
        List<PostDto> postDtos = new ArrayList<>();
        posts.forEach(post -> {
            PostDto postDTO = postDtoMapper.postToPostDto(post);
            postDtos.add(postDTO);
        });
        return postDtos;
    }

    private List<ModerationResponse> getModerationPostDTOs(final Iterable<Post> posts) {
        List<ModerationResponse> postDtos = new ArrayList<>();
        posts.forEach(post -> {
            ModerationResponse postDTO = postDtoMapper.postToModerationResponse(post);
            postDtos.add(postDTO);
        });
        return postDtos;
    }

    private void removeDislikeIfExists(String userId, Post post) {
        ListIterator<String> iterator = post.getUsersDislikedPost().listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(userId)) {
                iterator.remove();
                break;
            }
        }
    }

    private void removeLikeIfExists(String userId, Post post) {
        ListIterator<String> iterator = post.getUsersLikedPost().listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(userId)) {
                iterator.remove();
                break;
            }
        }
    }

    private void incrementPostViewsCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
    }
}





//    public class EmployeeService {
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    private Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);
//
//    public void lookupOperation(){
//    LookupOperation lookupOperation = LookupOperation.newLookup()
//                        .from("Department")
//                        .localField("dept_id")
//                        .foreignField("_id")
//                        .as("departments");
//
//    Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("_id").is("1")) , lookupOperation);
//        List<EmpDeptResult> results = mongoTemplate.aggregate(aggregation, "Employee", EmpDeptResult.class).getMappedResults();
//        LOGGER.info("Obj Size " +results.size());
//    }
//}