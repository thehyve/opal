/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.provider;

import com.google.protobuf.GeneratedMessage;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class ValidationExceptionMapper extends ErrorDtoExceptionMapper<ValidationException> {

    //private static final Logger log = LoggerFactory.getLogger(MagmaRuntimeExceptionMapper.class);

    @Override
    protected Status getStatus() {
        return BAD_REQUEST;
    }

    @Override
    protected GeneratedMessage.ExtendableMessage<?> getErrorDto(ValidationException exception) {
        //log.debug("ValidationException", exception);
        return ClientErrorDtos.getErrorMessage(getStatus(), "ValidationException", exception.getMessage()).build();
    }

}
