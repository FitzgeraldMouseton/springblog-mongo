package com.cheeseind.blogengine.validation.constraints;

import com.cheeseind.blogengine.validation.validators.CaptchaExpirationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CaptchaExpirationValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CaptchaNotExpiredConstraint {

    String message() default "Код капчи устарел";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
