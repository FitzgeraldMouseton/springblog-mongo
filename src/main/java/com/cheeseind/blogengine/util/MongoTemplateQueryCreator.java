package com.cheeseind.blogengine.util;

import com.cheeseind.blogengine.models.Post;
import com.mongodb.internal.operation.AggregateOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class MongoTemplateQueryCreator {

    public Query getPostsByDateQuery(LocalDate date, Pageable pageable) {
        return Query.query(getOnlyActivePostsCriteria()
                .andOperator(Criteria.where("time").gte(date),
                            Criteria.where("time").lt(date.plusDays(1))
                            ))
                .with(pageable);
    }

    public Query getVisiblePosts() {
        return Query.query(getOnlyActivePostsCriteria());
    }

    public Query getVisiblePostsFetchOnlyTags() {
        Query query = getVisiblePosts();
        query.fields().include("tags");
        return query;
    }

    public Query getPostsByTag(String tag, Pageable pageable) {
        return Query.query(Criteria.where("tags").in(tag)
                .andOperator(getOnlyActivePostsCriteria()))
                .with(pageable);
    }

    public TypedAggregation<Post> getLikesOfAllPostsOfUser(String userId) {
        MatchOperation match = Aggregation.match(new Criteria()
                .andOperator(Criteria.where("userId").is(userId), getOnlyActivePostsCriteria()));
        GroupOperation group = Aggregation.group("id").sum("usersLikedPost").as("total");
        ProjectionOperation total = Aggregation.project("total");

        return Aggregation.newAggregation(Post.class, match, group);
    }

    private Criteria getOnlyActivePostsCriteria() {
        return new Criteria().andOperator(
                Criteria.where("active").is(true),
                Criteria.where("time").lt(LocalDateTime.now()),
                Criteria.where("moderationStatus").is("ACCEPTED"));
    }
}


//    Query query = new Query().with(Sort.by(Sort.Direction.ASC, "title"))
//            .with(PageRequest.of(offset/limit, limit));
//                posts = mongoTemplate.find(query, Post.class);