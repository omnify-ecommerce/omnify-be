package com.omnify.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneContactValidator.class)
@Documented
public @interface AtLeastOneContact {

    String message() default "Phải cung cấp email hoặc số điện thoại";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}