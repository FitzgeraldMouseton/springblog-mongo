package com.cheeseind.blogengine.rest;

import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.blogdto.BlogInfo;
import com.cheeseind.blogengine.models.dto.blogdto.CalendarDto;
import com.cheeseind.blogengine.models.dto.blogdto.ModerationRequest;
import com.cheeseind.blogengine.models.dto.blogdto.StatisticsDto;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentRequest;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentResponse;
import com.cheeseind.blogengine.models.dto.blogdto.tagdto.TagsResponse;
import com.cheeseind.blogengine.models.dto.userdto.EditProfileRequest;
import com.cheeseind.blogengine.services.GeneralService;
import com.cheeseind.blogengine.services.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GeneralController {

    private final GeneralService generalService;
    private final TagService tagService;

    @Value("${blog_info.title}")
    private String title;
    @Value("${blog_info.subtitle}")
    private String subtitle;
    @Value("${blog_info.phone}")
    private String phone;
    @Value("${blog_info.email}")
    private String email;
    @Value("${blog_info.copyright}")
    private String copyright;
    @Value("${blog_info.copyright_form}")
    private String copyrightForm;

    @GetMapping("/init")
    public BlogInfo getBlogInfo() {
        return BlogInfo.builder().title(title).subtitle(subtitle).phone(phone)
                .email(email).copyright(copyright).copyrightForm(copyrightForm).build();
    }

    @PostMapping("comment")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody final CommentRequest commentRequest) {
        return ResponseEntity.ok().body(generalService.addComment(commentRequest));
    }

    @GetMapping("tag")
    public TagsResponse getTags(final String query) {
        return tagService.findTagsByName();
    }

    @GetMapping("calendar")
    public CalendarDto calendar(@RequestParam final int year) {
        return generalService.calendar(year);
    }

    @GetMapping("statistics/my")
    public StatisticsDto getUserStatistics() {
        return generalService.getCurrentUserStatistics();
    }

    @GetMapping("statistics/all")
    public StatisticsDto getGeneralStatistics(){
        return generalService.getBlogStatistics();
    }

    @GetMapping("settings")
    public Map<String, Boolean> getSettings() {
        return generalService.getSettings();
    }

    @PutMapping("settings")
    public void changeSettings(@RequestBody Map<String, Boolean> request) {
        generalService.changeSettings(request);
    }

    @PostMapping("moderation")
    public void moderation(@RequestBody final ModerationRequest request) {
        generalService.moderation(request);
    }


    @PostMapping(value = "image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(@RequestParam(name = "image") final MultipartFile file) throws IOException {
        return generalService.uploadImage(file);
    }

    @PostMapping(value = "profile/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SimpleResponseDto editProfileWithPhoto(@RequestParam final MultipartFile photo,
                                                  @ModelAttribute @Valid final EditProfileRequest request) throws IOException {

        return generalService.editProfileWithPhoto(photo, request);
    }

    @PostMapping(value = "profile/my", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SimpleResponseDto editProfile(@Valid @RequestBody final EditProfileRequest request) {
        return generalService.editProfileWithoutPhoto(request);
    }
}
