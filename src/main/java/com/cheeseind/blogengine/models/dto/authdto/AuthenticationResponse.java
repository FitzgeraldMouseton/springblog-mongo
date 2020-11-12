package com.cheeseind.blogengine.models.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private boolean result;
    private UserLoginResponse user;
}
