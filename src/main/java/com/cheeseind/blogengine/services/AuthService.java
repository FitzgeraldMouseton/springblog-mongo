package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.exceptions.PageNotFoundException;
import com.cheeseind.blogengine.exceptions.authexceptions.InvalidCaptchaCodeException;
import com.cheeseind.blogengine.mappers.UserDtoMapper;
import com.cheeseind.blogengine.models.CaptchaCode;
import com.cheeseind.blogengine.models.User;
import com.cheeseind.blogengine.models.dto.SimpleResponseDto;
import com.cheeseind.blogengine.models.dto.authdto.AuthenticationResponse;
import com.cheeseind.blogengine.models.dto.authdto.RegisterRequest;
import com.cheeseind.blogengine.models.dto.authdto.SetPassRequest;
import com.cheeseind.blogengine.models.dto.authdto.UserLoginResponse;
import com.cheeseind.blogengine.util.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final CaptchaService captchaService;
    private final SettingService settingService;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    @Value("${restore_code.code_length}")
    private int restoreCodeLength;
    @Value("${restore_code.path}")
    private String restoreCodePath;

    public SimpleResponseDto register(final RegisterRequest request) {
        if (!settingService.isMultiUserEnabled()) {
            throw new PageNotFoundException();
        }
        checkCaptchaCode(request.getCaptchaSecret(), request.getCaptcha());
        User user = userDtoMapper.registerRequestToUser(request);
        user.setPassword(encoder.encode(user.getPassword()));
        userService.save(user);
        return new SimpleResponseDto(true);
    }

    public AuthenticationResponse check() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            log.info("Check - principal - " + authentication.getPrincipal());
            log.info(String.valueOf(authentication.isAuthenticated()));
            User user = (User) authentication.getPrincipal();
            UserLoginResponse userDTO = userDtoMapper.userToLoginResponse(user);
            return new AuthenticationResponse(true, userDTO);
        }
        return null;
    }

    @Transactional
    public SimpleResponseDto sendRestorePasswordMessage(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return new SimpleResponseDto(false);
        }
        String code = createPasswordRestoreCode();
        user.setCode(code);
        userService.save(user);
        String message = "Для восстановления пароля перейдите по ссылке " + restoreCodePath + code;
        emailService.send(email, "Восстановление пароля", message);
        return new SimpleResponseDto(true);
    }

    public SimpleResponseDto setNewPassword(final SetPassRequest request) {
        checkCaptchaCode(request.getCaptchaSecret(), request.getCaptcha());
        String code = request.getCode();
        User user = userService.findByCode(code);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCode(null);
        userService.save(user);
        return new SimpleResponseDto(true);
    }

    private void checkCaptchaCode(String captchaSecret, String captcha) {
        CaptchaCode captchaCode = captchaService.findBySecretCode(captchaSecret);
        if (captchaCode == null) {
            throw new InvalidCaptchaCodeException("Срок действия кода истёк");
        }
        if (!captchaCode.getCode().equals(captcha)) {
            throw new InvalidCaptchaCodeException("Код с картинки введён неверно");
        }
    }

    private String createPasswordRestoreCode() {
        String code = RandomStringUtils.randomAlphanumeric(restoreCodeLength);
        code += getEncodedCurrentTime();
        return code;
    }

    private String getEncodedCurrentTime() {
        long currentTimeMilli = Instant.now(Clock.systemUTC()).toEpochMilli();
        String time = Long.toString(currentTimeMilli);
        return Base64.getEncoder().encodeToString(time.getBytes());
    }
}
