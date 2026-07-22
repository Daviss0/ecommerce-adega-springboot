package com.adega.adega.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE
})
@Retention(RUNTIME)
public @interface ValidPhone {

    String message() default "Informe um telefone válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
