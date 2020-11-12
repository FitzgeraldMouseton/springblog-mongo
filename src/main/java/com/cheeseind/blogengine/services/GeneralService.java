package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.exceptions.NotEnoughPrivilegesException;
import com.cheeseind.blogengine.exceptions.UnauthenticatedUserException;
import com.cheeseind.blogengine.models.*;
import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.blogdto.CalendarDto;
import com.cheeseind.blogengine.models.dto.blogdto.ModerationRequest;
import com.cheeseind.blogengine.models.dto.blogdto.StatisticsDto;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentRequest;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentResponse;
import com.cheeseind.blogengine.models.dto.userdto.EditProfileRequest;
import com.cheeseind.blogengine.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralService {

    private final PostService postService;
    private final UserService userService;
    private final SettingService settingService;
    private final CommentService commentService;
    private final PasswordEncoder encoder;
    private final MongoTemplate mongoTemplate;

    private final PostRepository postRepository;
//    private final BCryptPasswordEncoder encoder;

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final int FOLDER_NAME_LENGTH = 4;
    private static final int NUMBER_OF_FOLDERS_IN_IMAGE_PATH = 3;

    @Value("${image.avatar_width}")
    private int avatarWidth;
    @Value("${image.avatar_height}")
    private int avatarHeight;
    @Value("${image.max_image_width}")
    private int maxImageWidth;
    @Value("${image.max_image_height}")
    private int maxImageHeight;
    @Value("${password.min_length}")
    private int passwordMinLength;
    @Value("${location.images}")
    private String imagesLocation;
    @Value("${location.avatars}")
    private String avatarsLocation;

    public void moderation(final ModerationRequest request) {
        User moderator = userService.getCurrentUser();
        Post post = postService.findPostById(request.getPostId());
        if ("decline".equals(request.getDecision())) {
            post.setModerationStatus(ModerationStatus.DECLINED);
        } else if ("accept".equals(request.getDecision())) {
            post.setModerationStatus(ModerationStatus.ACCEPTED);
        }
        post.setModerator(moderator);
        postService.save(post);
    }

    public StatisticsDto getCurrentUserStatistics() {
        String userId = userService.getCurrentUser().getId();
        long postsCount = postService.countUserAcceptedPosts(userId);
        Post firstPost = postService.findFirstPostOfUser(userId);
        long firstPostDate = firstPost.getTime().toEpochSecond();
        long likesCount = postService.countLikesOfUserPosts(userId);
        long dislikesCount = postService.countDislikesOfUserPosts(userId);
        long viewsCount = postService.countUserPostsViews(userId);
        return new StatisticsDto(postsCount, likesCount, dislikesCount, viewsCount, firstPostDate);
    }

    public StatisticsDto getBlogStatistics() {
        long postsCount = postService.countVisiblePosts();
        Post firstPost = postService.findFirstPost();
        long firstPostDate = firstPost.getTime().toEpochSecond();
        long likesCount = postService.countAllLikes();
        long dislikesCount = postService.countAllDislikes();
        long viewsCount = postService.countAllPostsViews();
        return new StatisticsDto(postsCount, likesCount, dislikesCount, viewsCount, firstPostDate);
    }

    //TODO получение из БД через distinct()
    public CalendarDto calendar(final int year) {
        dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        final List<Integer> allYears = postRepository.findAllYears().stream().distinct().collect(Collectors.toList());
        final Integer[] years = allYears.toArray(Integer[]::new);

        final List<Date> allDaysWithPostsInYear = postRepository.findAllDatesWithPostsInYear(year);
        final Map<String, Long> postsPerDate = allDaysWithPostsInYear.stream()
                .map(date -> date.toInstant().atZone(ZoneOffset.UTC))
                .collect(Collectors.groupingBy(date -> dateFormat.format(date), Collectors.counting()));

        CalendarDto calendarDto = new CalendarDto();
        calendarDto.setPostsPerDate(postsPerDate);
        calendarDto.setYears(years);
        return calendarDto;
    }



    @Transactional
    public CommentResponse addComment(final CommentRequest request) {
        User user = userService.getCurrentUser();

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUser(user);
        comment.setTime(ZonedDateTime.now());
        comment.setPostId(request.getPostId());
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            comment.setParentId(request.getParentId());
        }
        commentService.save(comment);
        Post post = postService.findPostById(request.getPostId());
        incrementCommentsCountForPost(post);
        postService.save(post);
        return new CommentResponse(comment.getId());
    }

    public Map<String, Boolean> getSettings() {
        Map<String, Boolean> response = new HashMap<>();
        List<GlobalSetting> settings = settingService.getSettings();
        if (settings.isEmpty()) {
            settingService.fillSettings();
        }
        settings.forEach(setting -> response.put(setting.getCode(), setting.getValue()));
        return response;
    }

    public void changeSettings(final Map<String, Boolean> request) {
        User user = userService.getCurrentUser();
        if (user.isModerator()) {
            request.keySet().forEach(k -> {
                GlobalSetting setting = settingService.getSettingByCode(k);
                if (setting == null) {
                    setting = settingService.setSetting(k, request.get(k));
                } else {
                    setting.setValue(request.get(k));
                }
                settingService.save(setting);
            });
        } else {
            throw new NotEnoughPrivilegesException("Только модератор может редактировать настройки");
        }
    }

    @Transactional
    public SimpleResponseDto editProfileWithPhoto(final MultipartFile file,
                                                  final EditProfileRequest request) throws IOException {
        User user = getEditedUser(request);
        String photo = uploadUserAvatar(file);
        log.info("Photo - " + photo);
        user.setPhoto(photo);
        userService.save(user);
        return new SimpleResponseDto(true);
    }

    @Transactional
    public SimpleResponseDto editProfileWithoutPhoto(final EditProfileRequest request) {
        userService.save(getEditedUser(request));
        return new SimpleResponseDto(true);
    }

    public String uploadImage(final MultipartFile image) throws IOException {
        String uploadPath = getPathForUpload(imagesLocation, image.getOriginalFilename());
        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
        File newFile = new File(uploadPath);
        float height = bufferedImage.getHeight();
        float width = bufferedImage.getWidth();
        if (height > maxImageHeight || width > maxImageWidth) {

            float i;
            if (height > width) {
                i = width / height;
                bufferedImage = getCroppedImage(bufferedImage, (int) (maxImageWidth * i), maxImageHeight);
            } else {
                i = height / width;
                bufferedImage = getCroppedImage(bufferedImage, maxImageWidth, (int) (maxImageHeight * i));
            }
        }
        ImageIO.write(bufferedImage, "jpg", newFile);
        return "/" + uploadPath;
//        image.transferTo(Path.of(uploadPath));
    }

    // ================================== Additional methods =========================================

    private void incrementCommentsCountForPost(Post post) {
        post.setCommentsCount(post.getCommentsCount() + 1);
    }

    private String getPathForUpload(final String location, final String fileName) throws IOException {
        StringBuilder builder = new StringBuilder(location);
        for (int i = 0; i < NUMBER_OF_FOLDERS_IN_IMAGE_PATH; i++) {
            String rand = RandomStringUtils.randomAlphabetic(FOLDER_NAME_LENGTH).toLowerCase();
            builder.append(rand).append("/");
        }
        builder.append(fileName);
        Path path = Path.of(builder.toString());
        Files.createDirectories(path);
        return builder.toString();
    }

    private String uploadUserAvatar(final MultipartFile image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
        String path = getPathForUpload(avatarsLocation, image.getOriginalFilename());
        File newFile = new File(path);
        if (bufferedImage.getHeight() > avatarHeight || bufferedImage.getWidth() > maxImageWidth) {
            bufferedImage = getCroppedImage(bufferedImage, avatarWidth, avatarHeight);
        }
        ImageIO.write(bufferedImage, "jpg", newFile);
        return "/" + path;
    }

    private BufferedImage getCroppedImage(BufferedImage bufferedImage, int width, int height) {

        BufferedImage preliminaryResizedImage = resizeImage(bufferedImage, width * 2,
                                height * 2, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        bufferedImage = resizeImage(preliminaryResizedImage,
                                        width, height, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return bufferedImage;
    }

    private void removeUserAvatar(final User user) {

        String pathToPhoto = user.getPhoto();
        int startIndex = pathToPhoto.indexOf(avatarsLocation);
        int endIndex = pathToPhoto.indexOf("/", avatarsLocation.length() + 1);
        Path path = Path.of(pathToPhoto.substring(startIndex, endIndex));
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setPhoto(null);
        userService.save(user);
    }

    private User getEditedUser(final EditProfileRequest request) {
        User user = userService.getCurrentUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        String password = request.getPassword();
        if (request.getRemovePhoto() == 1 && user.getPhoto() != null) {
            removeUserAvatar(user);
        }
        if (password != null && password.length() >= passwordMinLength) {
            user.setPassword(encoder.encode(request.getPassword()));
        }
        return user;
    }

    private BufferedImage resizeImage(final BufferedImage sourceImage, final int width,
                                      final int height, final Object renderHint) {
        BufferedImage resizedImage = new BufferedImage(width, height, sourceImage.getType());
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        graphics2D.drawImage(sourceImage, 0, 0, width, height, null);
        graphics2D.dispose();
        return resizedImage;
    }
}

