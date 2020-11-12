package com.cheeseind.blogengine.mappers;

import com.cheeseind.blogengine.models.Comment;
import com.cheeseind.blogengine.models.ModerationStatus;
import com.cheeseind.blogengine.models.Post;
import com.cheeseind.blogengine.models.User;
import com.cheeseind.blogengine.models.dto.blogdto.ModerationResponse;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentDTO;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.PostDto;
import com.cheeseind.blogengine.models.postconstants.PostConstraints;
import com.cheeseind.blogengine.services.CommentService;
import com.cheeseind.blogengine.services.PostService;
import com.cheeseind.blogengine.services.SettingService;
import com.cheeseind.blogengine.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
//@RequiredArgsConstructor
public class PostDtoMapper {

    private final PostService postService;
    private final UserDtoMapper userDtoMapper;
    private final UserService userService;
    private final SettingService settingService;
    private final CommentService commentService;
    private final CommentDtoMapper commentDtoMapper;

    public PostDtoMapper(@Lazy PostService postService, UserDtoMapper userDtoMapper,
                         UserService userService, CommentService commentService,
                         SettingService settingService, CommentDtoMapper commentDtoMapper) {
        this.postService = postService;
        this.userDtoMapper = userDtoMapper;
        this.userService = userService;
        this.commentService = commentService;
        this.settingService = settingService;
        this.commentDtoMapper = commentDtoMapper;
    }

    public PostDto postToPostDto(final Post post) {
        PostDto postDto = new PostDto();
        if (post != null) {
            postDto.setId(post.getId());
            postDto.setTimestamp(post.getTime().toEpochSecond());
            postDto.setTitle(post.getTitle());
            postDto.setAnnounce(getAnnounce(post));
            postDto.setLikeCount(post.getUsersLikedPost().size());
            postDto.setDislikeCount(post.getUsersDislikedPost().size());
            postDto.setCommentCount(post.getCommentsCount());
            postDto.setViewCount(post.getViewCount());
            postDto.setUser(userDtoMapper.userToUserDto(post.getUser()));
        }
        return postDto;
    }

    public PostDto singlePostToPostDto(final Post post) {
        if (post != null) {
            PostDto postDTO = postToPostDto(post);
            postDTO.setText(post.getText());
            postDTO.setActive(post.isActive());
            List<Comment> comments = commentService.findAllCommentOfPost(post.getId());
            List<CommentDTO> commentDTOS = comments.stream()
                    .map(commentDtoMapper::commentToCommentDto)
                    .collect(Collectors.toList());
            postDTO.setComments(commentDTOS);
            postDTO.setTags(post.getTags().toArray(String[]::new));
            return postDTO;
        }
        return null;
    }

    public Post addPostRequestToPost(final AddPostRequest request) {
        Post post = new Post();
        return postDtoToPost(post, request);
    }

    public Post editPostRequestToPost(final String id, final AddPostRequest request) {
        Post post = postService.findPostById(id);
        return postDtoToPost(post, request);
    }

    public Post postDtoToPost(final Post post, final AddPostRequest request) {
        User user = userService.getCurrentUser();
        post.setUser(user);
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        setTimeToPost(post, request);
        post.setActive(request.isActive());
        if (!settingService.isPremoderationEnabled() || user.isModerator()) {
            post.setModerationStatus(ModerationStatus.ACCEPTED);
        } else {
            post.setModerationStatus(ModerationStatus.NEW);
        }
        request.getTagNames().forEach(tag -> post.getTags().add(tag.toUpperCase()));
        return post;
    }

    public ModerationResponse postToModerationResponse(final Post post) {
        ModerationResponse response = new ModerationResponse();
        response.setId(post.getId());
        response.setTimestamp(post.getTime().toEpochSecond());
        response.setUser(userDtoMapper.userToUserDto(post.getUser()));
        response.setTitle(post.getTitle());
        response.setAnnounce(getAnnounce(post));
        return response;
    }

    private String getAnnounce(final Post post) {
        String announce = Jsoup.parse(post.getText()).text();
        announce = (announce.length() > PostConstraints.ANNOUNCE_LENGTH)
                ? announce.substring(0, PostConstraints.ANNOUNCE_LENGTH) : announce;
        announce = (announce.matches(".*[.,?!]")) ? announce + ".." : announce + "...";
        return announce;
    }

    private void setTimeToPost(Post post, AddPostRequest request) {
        ZonedDateTime requestTime
                = Instant.ofEpochSecond(request.getTimestamp()).atZone(ZoneOffset.UTC);
        ZonedDateTime postTime = requestTime
                .isBefore(ZonedDateTime.now(ZoneOffset.UTC)) ? ZonedDateTime.now(ZoneOffset.UTC) : requestTime;
        post.setTime(postTime);
    }
}
