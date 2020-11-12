package com.cheeseind.blogengine.models.dto.authdto;

import com.cheeseind.blogengine.models.postconstants.UserConstraints;
import com.cheeseind.blogengine.validation.constraints.CaptchaNotExpiredConstraint;
import com.cheeseind.blogengine.validation.constraints.EmailUniqueConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class RegisterRequest {

    @Email
    @JsonProperty("e_mail")
    @EmailUniqueConstraint
    private String email;

    @Size(min = UserConstraints.MIN_USER_NAME_LENGTH, message = "Имя указано неверно")
    private String name;

    @Size(min = UserConstraints.MIN_PASSWORD_LENGTH, message = "Пароль не может быть короче 6 символов")
    private String password;

    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
