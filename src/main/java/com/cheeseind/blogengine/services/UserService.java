package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.exceptions.UnauthenticatedUserException;
import com.cheeseind.blogengine.models.User;
import com.cheeseind.blogengine.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findByCode(String code) {
        return userRepository.findByCode(code).orElse(null);
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof User)) {
            throw new UnauthenticatedUserException();
        }
        else {
            return userRepository.findByEmail(authentication.getName()).orElseThrow(UnauthenticatedUserException::new);
        }
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
