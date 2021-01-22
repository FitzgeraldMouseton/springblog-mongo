package com.cheeseind.blogengine.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    //    @Field(targetType = FieldType.INT32)
    private boolean active;

    private ModerationStatus moderationStatus;

    @DBRef
    private User user;

    @DBRef(lazy = true)
    private User moderator;

    @Field
    private ZonedDateTime time;

    @TextIndexed
    private String title;

    @TextIndexed
    private String text;

    private int viewCount;

    private int commentsCount;

    private List<String> tags = new ArrayList<>();

    private List<String> usersLikedPost = new ArrayList<>();

    private List<String> usersDislikedPost = new ArrayList<>();
}
