package com.cheeseind.blogengine.models.dto.blogdto.commentdto;

import com.cheeseind.blogengine.models.dto.userdto.UserDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentDTO {

    public CommentDTO(String text) {
        this.text = text;
    }

    private String id;
    private long timestamp;
    private String text;
    private UserDto user;
}
