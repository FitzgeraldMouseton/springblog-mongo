package com.cheeseind.blogengine.models;

import com.cheeseind.blogengine.models.postconstants.PostConstraints;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {

    public Comment(@Size(min = PostConstraints.MIN_COMMENT_SIZE) String text) {
        this.text = text;
    }

    @Id
    private String id;

    private String postId;

    private String parentId;

    @DBRef
    private User user;

    private ZonedDateTime time;

    private String text;
}
