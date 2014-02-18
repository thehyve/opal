/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.magma.js.validation.VariableScriptValidationException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class VariableScriptValidationExceptionMapper implements ExceptionMapper<VariableScriptValidationException> {

  private static final Logger log = LoggerFactory.getLogger(VariableScriptValidationExceptionMapper.class);

  @Override
  public Response toResponse(VariableScriptValidationException exception) {
    log.debug("VariableScriptValidationException", exception);
    Ws.ClientErrorDto errorDto = ClientErrorDtos.getErrorMessage(BAD_REQUEST, "InvalidVariableScript")
        .addArguments(exception.getCause().getMessage()).build();
    return Response.status(BAD_REQUEST).entity(errorDto).build();
  }
}
