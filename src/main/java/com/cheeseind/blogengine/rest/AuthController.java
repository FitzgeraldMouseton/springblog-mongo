package com.cheeseind.blogengine.rest;

import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.authdto.*;
import com.cheeseind.blogengine.services.AuthService;
import com.cheeseind.blogengine.services.CaptchaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @PostMapping("register")
    public ResponseEntity<SimpleResponseDto> register(@Valid @RequestBody final RegisterRequest registerRequest) {
        return ResponseEntity.ok().body(authService.register(registerRequest));
    }

    @GetMapping("check")
    public ResponseEntity<Object> check() {
        AuthenticationResponse response = authService.check();
        if (response == null) {
            return ResponseEntity.ok().body(new SimpleResponseDto(false));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("captcha")
    public CaptchaDto getCaptcha() {
        return captchaService.generateCaptcha();
    }

    @PostMapping("restore")
    public SimpleResponseDto restorePassword(@RequestBody final RestorePasswordRequest request) {
        return authService.sendRestorePasswordMessage(request.getEmail());
    }

    @PostMapping("password")
    public ResponseEntity<SimpleResponseDto> setNewPassword(@Valid @RequestBody final SetPassRequest setPassDto) {
        return ResponseEntity.ok().body(authService.setNewPassword(setPassDto));
    }
}
