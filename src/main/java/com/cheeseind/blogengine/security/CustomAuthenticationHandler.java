package com.cheeseind.blogengine.security;

import com.cheeseind.blogengine.mappers.UserDtoMapper;
import com.cheeseind.blogengine.models.User;
import com.cheeseind.blogengine.models.dto.authdto.AuthenticationResponse;
import com.cheeseind.blogengine.models.dto.authdto.UserLoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@AllArgsConstructor
public class CustomAuthenticationHandler implements AuthenticationSuccessHandler {

    private final UserDtoMapper userDtoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        response.setStatus(HttpStatus.OK.value());
        User user = (User) authentication.getPrincipal();
        UserLoginResponse loginDto = userDtoMapper.userToLoginResponse(user);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(true, loginDto);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().println(objectMapper.writeValueAsString(authenticationResponse));
    }
}
