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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchJobExceptionMapper implements ExceptionMapper<NoSuchJobException> {

  private static final Logger log = LoggerFactory.getLogger(NoSuchJobExceptionMapper.class);

  @Override
  public Response toResponse(NoSuchJobException exception) {
    log.error("No such job definition: {}", exception.getMessage(), exception);
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

}
