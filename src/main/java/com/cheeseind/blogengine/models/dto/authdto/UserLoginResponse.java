package com.cheeseind.blogengine.models.dto.authdto;

import lombok.Data;

@Data
public class UserLoginResponse {

    private String id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private long moderationCount;
    private boolean settings;
}
