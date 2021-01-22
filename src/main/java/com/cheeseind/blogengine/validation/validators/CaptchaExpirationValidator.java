package com.cheeseind.blogengine.validation.validators;

import com.cheeseind.blogengine.services.CaptchaService;
import com.cheeseind.blogengine.validation.constraints.CaptchaNotExpiredConstraint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
@RequiredArgsConstructor
public class CaptchaExpirationValidator implements ConstraintValidator<CaptchaNotExpiredConstraint, String> {

    private final CaptchaService captchaService;

    @Override
    public void initialize(CaptchaNotExpiredConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String captcha, ConstraintValidatorContext constraintValidatorContext) {
        return captchaService.findBySecretCode(captcha) != null;
    }
}
