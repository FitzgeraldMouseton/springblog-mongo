package com.cheeseind.blogengine.repositories;

import com.cheeseind.blogengine.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findAllByPostId(String postId);
}
