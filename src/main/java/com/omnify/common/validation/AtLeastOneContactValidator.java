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
        boolean hasEmail = StringUtils.hasText(value.getEmail());
        boolean hasPhone = StringUtils.hasText(value.getPhone());
        // toan tu XoR, email va phone cung rong hoac cung co gia tri thi tra ve false , //chi 1 trong 2 co value
        return hasEmail ^ hasPhone;
    }
}