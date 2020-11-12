package com.cheeseind.blogengine;

import com.cheeseind.blogengine.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class BootRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        User user = new User();
        user.setName("J");
        mongoTemplate.save(user);
    }
}
