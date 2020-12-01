package com.cheeseind.blogengine.controllers;

import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.PostDto;
import com.cheeseind.blogengine.models.dto.blogdto.postdto.PostsInfoResponse;
import com.cheeseind.blogengine.models.dto.blogdto.votedto.VoteRequest;
import com.cheeseind.blogengine.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/post")
public class PostController {

    private final PostService postService;

    @GetMapping
    public PostsInfoResponse<PostDto> getPosts(@RequestParam final int offset,
                                               @RequestParam final int limit,
                                               @RequestParam final String mode) {
        return postService.findPosts(offset, limit, mode);
    }

    @GetMapping("/search")
    public PostsInfoResponse<PostDto> searchPost(@RequestParam final int offset,
                                                 @RequestParam final int limit,
                                                 @RequestParam final String query) {
        return postService.findAllByQuery(offset, limit, query);
    }

    @GetMapping("{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable String id) {
        final PostDto postDto = postService.getPost(id);
        if (postDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("byDate")
    public PostsInfoResponse<PostDto> searchByDate(@RequestParam final int offset,
                                                   @RequestParam final int limit,
                                                   @RequestParam final String date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateQuery = LocalDate.parse(date, dateFormat);
        ZonedDateTime zonedDateTime = dateQuery.atStartOfDay(ZoneOffset.UTC);
        return postService.findPostsByDate(offset, limit, zonedDateTime);
    }

    @GetMapping("byTag")
    public PostsInfoResponse<PostDto> searchByTag(@RequestParam final int offset,
                                                  @RequestParam final int limit,
                                                  @RequestParam final String tag) {
        return postService.findPostsByTag(offset, limit, tag);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleResponseDto addPost(@Valid @RequestBody final AddPostRequest request) {
        return postService.addPost(request);
    }

    @PutMapping("{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "Потому что для методов PUT " +
            "и DELETE так принято")
    public SimpleResponseDto editPost(@PathVariable final String id, @Valid @RequestBody final AddPostRequest request) {
        return postService.editPost(id, request);
    }

    @PostMapping("like")
    public SimpleResponseDto addLike(@RequestBody final VoteRequest request) {
        return postService.likePost(request);
    }

    @PostMapping("dislike")
    public SimpleResponseDto dislikePost(@RequestBody final VoteRequest request) {
        return postService.dislikePost(request);
    }

    @GetMapping("/my")
    public PostsInfoResponse<PostDto> getCurrentUserPosts(@RequestParam final int offset,
                                                          @RequestParam final int limit,
                                                          @RequestParam final String status) {
        return postService.findCurrentUserPosts(offset, limit, status);
    }

    @GetMapping("moderation")
    public PostsInfoResponse getPostsForModeration(@RequestParam final int offset,
                                                   @RequestParam final int limit,
                                                   @RequestParam final String status) {
        return postService.findPostsForModeration(offset, limit, status);
    }
}
