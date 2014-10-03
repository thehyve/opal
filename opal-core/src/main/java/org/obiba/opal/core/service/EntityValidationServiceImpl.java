package org.obiba.opal.core.service;

import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.*;
import java.io.IOException;
import java.util.Set;

/**
 *
 */
@Component
public class EntityValidationServiceImpl implements EntityValidationService {

    @Value("${customEntityValidation}")
    String customEntityValidationConfig;

    Validator validator;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void initValidator() throws IOException {

        if (customEntityValidationConfig != null) {
            Resource res = resourceLoader.getResource(customEntityValidationConfig);
            if (res.exists()) {
                Configuration<?> configuration = Validation.byProvider(HibernateValidator.class).configure();
                configuration.ignoreXmlConfiguration();
                configuration.addMapping(res.getInputStream());
                validator = configuration.buildValidatorFactory().getValidator();
            }
        }
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        if (validator == null) {
            return; //if no validator, validation is disabled
        }
        Set<ConstraintViolation<Object>> errors = validator.validate(obj);
        System.out.println(errors);
        if (!errors.isEmpty()) {
            ConstraintViolation<Object> first = errors.iterator().next();
            String msg = String.format("%s %s", first.getPropertyPath(), first.getMessage());
            throw new ValidationException(msg);
        }
    }

}
