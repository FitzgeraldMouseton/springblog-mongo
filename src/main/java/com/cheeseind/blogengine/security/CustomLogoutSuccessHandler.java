package com.cheeseind.blogengine.security;

import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @SneakyThrows
//    @Override
//    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        response.setStatus(HttpServletResponse.SC_OK);
//        SimpleResponseDto simpleResponseDto = new SimpleResponseDto(true);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        response.getWriter().println(objectMapper.writeValueAsString(simpleResponseDto));
//    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        SimpleResponseDto simpleResponseDto = new SimpleResponseDto(true);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().println(objectMapper.writeValueAsString(simpleResponseDto));
    }
}
