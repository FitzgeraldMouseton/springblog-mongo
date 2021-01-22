package com.cheeseind.blogengine.validation.constraints;

import com.cheeseind.blogengine.validation.validators.RestoreCodeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RestoreCodeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeNotExpiredConstraint {

    String message() default "Ссылка для восстановления пароля устарела.<a href=\"/auth/restore\">Запросить ссылку снова</a>";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
