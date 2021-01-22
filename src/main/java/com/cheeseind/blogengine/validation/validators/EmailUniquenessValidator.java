package com.cheeseind.blogengine.validation.validators;

import com.cheeseind.blogengine.services.UserService;
import com.cheeseind.blogengine.validation.constraints.EmailUniqueConstraint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
@RequiredArgsConstructor
public class EmailUniquenessValidator implements ConstraintValidator<EmailUniqueConstraint, String> {

    private final UserService userService;

    @Override
    public void initialize(EmailUniqueConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(final String email, final ConstraintValidatorContext constraintValidatorContext) {
        boolean b = userService.findByEmail(email) == null;
        log.info(String.valueOf(b));
        return b;
    }
}
