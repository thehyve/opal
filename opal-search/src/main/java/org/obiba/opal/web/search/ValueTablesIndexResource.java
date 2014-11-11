/*
 * Copyright (c) 2014 The Hyve B.V. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiOperation;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.JoinTable;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Component
@Transactional(readOnly = true)
@Scope("request")
@Path("/indexes")
public class ValueTablesIndexResource extends IndexResource {

  private static final Logger log = LoggerFactory.getLogger(ValueTablesIndexResource.class);

  // TODO: verify that the passed table parameters in fact exist, otherwise we should return an error.
  @QueryParam("table")
  Set<String> tables;

  /**
   * Get table indexation status on all tables, or all tables mentioned in the 'tables' query parameter.
   */
  @GET
  @ApiOperation(value = "Get index status of all tables")
  public Response allIndexes() {
    if(!valuesIndexManager.isReady()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    List<Opal.TableIndexStatusDto> dtos = Lists.newArrayList();

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable table : datasource.getValueTables()) {
        if(tables.isEmpty() || tables.contains(table.getTableReference())) {
          dtos.add(getTableIndexationDto(datasource.getName(), table.getName()).build());
        }
      }
    }

    //note: based on /org/jboss/resteasy/core/ResourceMethodInvoker.java line 304...
    return Response.ok().entity(
      new GenericEntity<List<Opal.TableIndexStatusDto>>(dtos){/*no body, only to preserve generic type*/}
    ).build();
  }

  /**
   * Update indexes on all tables mentioned in the 'table' query param, and also update views that depend on one
   * of these tables. If the query parameter is empty, update all indexes.
   */
  @PUT
  public Response updateAllIndexes() {

    if(!esProvider.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable table : datasource.getValueTables()) {
        if(tables.isEmpty() || needsUpdate(table)) {
          updateIndex(table);
        }
      }
    }

    return Response.noContent().build();
  }

  private boolean needsUpdate(ValueTable table) {
    if(table.isView()) {
      return needsUpdate(((ValueTableWrapper) table).getInnermostWrappedValueTable());
    } else if(table instanceof JoinTable) {
      return Iterators.any(((JoinTable) table).getTables().listIterator(),
                           new Predicate<ValueTable>() {
                public boolean apply(ValueTable table) { return needsUpdate(table); }
      });
    } else {
      return tables.contains(table.getTableReference());
    }
  }

  private void updateIndex(ValueTable table) {
    log.info("Re-indexing " + table.getTableReference());
    synchroManager.restartSynchronizeIndex(variablesIndexManager, table, 0);
    synchroManager.restartSynchronizeIndex(valuesIndexManager, table, 0);
  }
}
