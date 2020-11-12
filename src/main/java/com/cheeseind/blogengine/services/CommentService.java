package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.models.Comment;
import com.cheeseind.blogengine.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment findById(String commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    public List<Comment> findAllCommentOfPost(String postId) {
        return commentRepository.findAllByPostId(postId);
    }
}
