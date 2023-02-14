package io.github.xpakx.alingo.user.graphql.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import java.util.Objects;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class RepeatedPasswordValidator  implements ConstraintValidator<RepeatedPassword, Object[]> {
    @Override
    public boolean isValid(Object[] objects, ConstraintValidatorContext constraintValidatorContext) {
        String password = (String) objects[1];
        String passwordRe = (String) objects[2];
        return Objects.equals(password, passwordRe);
    }
}
