package com.cheeseind.blogengine.mappers;

import com.cheeseind.blogengine.models.Comment;
import com.cheeseind.blogengine.models.dto.blogdto.commentdto.CommentDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
public class CommentDtoMapper {

    private final UserDtoMapper userDtoMapper;

    public CommentDTO commentToCommentDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setTimestamp(comment.getTime().toEpochSecond());
        commentDTO.setUser(userDtoMapper.userToUserDto(comment.getUser()));
        commentDTO.setText(comment.getText());
        return commentDTO;
    }
}
