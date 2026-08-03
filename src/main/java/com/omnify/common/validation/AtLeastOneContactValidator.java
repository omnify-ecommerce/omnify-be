package com.omnify.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class AtLeastOneContactValidator implements ConstraintValidator<AtLeastOneContact, EmailOrPhoneCarrier> {

    @Override
    public boolean isValid(EmailOrPhoneCarrier value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return StringUtils.hasText(value.getEmail()) || StringUtils.hasText(value.getPhone());
    }
}