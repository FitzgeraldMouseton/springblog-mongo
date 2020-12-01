package com.cheeseind.blogengine.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

public class Authority implements GrantedAuthority {

    @Override
    public String getAuthority() {
        return null;
    }
}
