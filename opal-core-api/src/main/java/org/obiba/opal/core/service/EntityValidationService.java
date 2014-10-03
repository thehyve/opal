package org.obiba.opal.core.service;

import javax.validation.ValidationException;

/**
 * Service that validates beans of opal domain entities (eg: projects, tables, variables, users, etc..)
 */
public interface EntityValidationService {

    void validate(Object obj) throws ValidationException;

}
