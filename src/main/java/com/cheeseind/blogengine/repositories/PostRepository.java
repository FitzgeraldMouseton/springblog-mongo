package com.cheeseind.blogengine.repositories;

import com.cheeseind.blogengine.models.ModerationStatus;
import com.cheeseind.blogengine.models.Post;
import com.cheeseind.blogengine.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    String MAKE_POST_VISIBLE_CONDITION = "'active' : true, 'moderationStatus' : 'ACCEPTED', 'time' : {$lt : new Date()}";

    // ======================== Recent posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : -1}")
    List<Post> getRecentPosts(Pageable pageable);

    // ========================= Early posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : 1}")
    List<Post> getEarlyPosts(Pageable pageable);

    // ========================= Popular posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'comments' : -1}")
    List<Post> getPopularPosts(Pageable pageable);

    // ========================= Best posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'usersLikedPost' : -1}")
    List<Post> getBestPosts(Pageable pageable);

    // ========================= Find posts by query
    @Query("{" + MAKE_POST_VISIBLE_CONDITION + "'text' : {$regex : ?0, $options: 'i'}, 'title' : {$regex : ?0, $options: 'i'}}" )
    List<Post> findPostsByQuery(String query, Pageable pageable);

//    List<Post> findAllByTextRegex("")

    // ========================= Find active accepted post by id
    Optional<Post> findByIdAndModerationStatusAndTimeBeforeAndActiveTrue(String id, ModerationStatus moderationStatus, ZonedDateTime date);

    // ========================= Find post by id for current user
    Optional<Post> findByIdAndUser(String id, User user);

    // ========================= Find posts by date
    @Query("{'active' : true, 'moderationStatus' : ACCEPTED, 'time' : {$gte : ?0, $lt : ?1}}")
    List<Post> findPostsByDate(ZonedDateTime startOfDay, ZonedDateTime startOfNextDay, Pageable pageable);

    // ========================= Find all dates of active posts by year
    @Query(value = "{'active' : true}, {'moderationStatus' : 'ACCEPTED'}, {{$year(time)} : ?0}}",
            sort = "{'time' : -1}")
    List<Post> findAllByYear(int year);

    // ======================== Count user's inactive posts count
    @Query(value = "{'user.id' : ?0, 'active' : false}", count = true)
    long countInactivePostsOfUser(String userId);

    // ======================== Count user's active posts count
    @Query(value = "{'user.id' : ?0, 'moderationStatus' : ?1, 'active' : true}", count = true)
    long countActivePostsOfUser(String userId, ModerationStatus moderationStatus);

    // ======================== Inactive posts of user
    @Query(value = "{'user.id' : ?0, 'active' : false}")
    List<Post> getCurrentUserInactivePosts(String userId, Pageable pageable);

    // ========================= Active posts of user
    @Query(value = "{'user.id' : ?0, 'moderationStatus' : ?1,'active' : true}")
    List<Post> getCurrentUserActivePosts(String userId, ModerationStatus moderationStatus, Pageable pageable);

    // ======================== Posts for moderation
    List<Post> findAllByActiveTrueAndModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    default List<Post> getPostsForModeration(ModerationStatus moderationStatus, Pageable pageable) {
        return findAllByActiveTrueAndModerationStatus(moderationStatus, pageable);
    }

    // ======================== Count posts for moderation
    long countAllByModerationStatusAndActiveTrue(ModerationStatus moderationStatus);

    @Query(value = "{ 'active' : true, moderationStatus : { $ne : ACCEPTED}, time : {$lt : new Date()} }", count = true)
    long countPostsForModeration();

    // ======================== Find first post
    Optional<Post> findFirstByModerationStatusAndActiveTrueOrderByTime(ModerationStatus moderationStatus);

    default Optional<Post> findFirstPost() {
        return findFirstByModerationStatusAndActiveTrueOrderByTime(ModerationStatus.ACCEPTED);
    }

    // ======================== Count visible posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", count = true)
    long countVisiblePosts();

    // ======================== Find all tags in visible posts
//    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    @Aggregation(pipeline = "{ '$project': { 'id' : '$tags' } }")
    List<List<String>> findAllTags();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    @Aggregation("{ '$project': { 'id' : {$year : '$time' } } }")
    List<Integer> findAllYears();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    @Aggregation("{ '$project': { 'id' : '$time' } }")
    List<Date> findAllDatesWithPostsInYear(int year);


    // ========================
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { '_id' : null, 'total' : { $sum : 'viewCount' } } }"
    })
    long countAllPostsViews();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : 1}")
    Stream<Post> findFirstPostInBlog();

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { 'id' : null, 'total' : { $sum : {$size : '$usersLikedPost'} } } }"
    })
    long countAllLikes();

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { 'id' : null, 'total' : { $sum : {$size : '$usersDislikedPost'} } } }"
    })
    long countAllDislikes();

    // ========================
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { '_id' : ?0, 'total' : { $sum : 'viewCount' } } }"
    })
    long countUserPostsViews(String userId);

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { 'id' : ?0, 'total' : { $sum : {$size : '$usersLikedPost'} } } }"})
    long countLikesOfAllUserPosts(String userId);

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { 'id' : ?0, 'total' : { $sum : {$size : '$usersDislikedPost'} } } }"})
    long countDislikesOfAllUserPosts(String userId);

    // ======================== Find first post of user
    // При использовании Stream мы сможем получить только первое значение, использовав limit в сервисе без вытаскивания
    // всего листа
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0}", sort = "{'time' : 1}")
    Stream<Post> findFirstPostOfUser(String userId);
}
