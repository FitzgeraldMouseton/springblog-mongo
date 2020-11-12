package com.cheeseind.blogengine.models;

import com.cheeseind.blogengine.validation.constraints.CaptchaNotExpiredConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Document(collection = "captcha_codes")
public class CaptchaCode {

    private static final int CAPTCHA_EXP_TIME = 3600;

    public CaptchaCode(@NotNull String code, @NotNull String secretCode, @NotNull LocalDateTime time) {
        this.code = code;
        this.secretCode = secretCode;
        this.time = time;
    }

    @Id
    private String id;

    @NotNull
    private String code;

    @NotNull
    private String secretCode;

    @Indexed(expireAfterSeconds = CAPTCHA_EXP_TIME)
    private LocalDateTime time;
}
