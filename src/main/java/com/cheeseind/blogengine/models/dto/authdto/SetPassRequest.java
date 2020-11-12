package com.cheeseind.blogengine.models.dto.authdto;

import com.cheeseind.blogengine.validation.constraints.CodeNotExpiredConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetPassRequest {

    //TODO проверить CodeNotExpiredConstraint
    private String password;

    @CodeNotExpiredConstraint
    private String code;

    private String captcha;

//    @CaptchaNotExpiredConstraint
    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
