package org.obiba.opal.core.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;


@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[0-9a-zA-Z\\-_]+$", message = "must be alpha-numeric or any of '_-'")
@Constraint(validatedBy = { })
public @interface ExtendedAlphaNumeric {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};

}
