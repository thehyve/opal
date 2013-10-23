/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.jdbc;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceFactory {

  public static final int MIN_POOL_SIZE = 3;

  public static final int MAX_POOL_SIZE = 50;

  @Autowired
  private TransactionManager arjunaTransactionManager;

  public DataSource createDataSource(@Nonnull SqlDatabase database) {
    BasicManagedDataSource dataSource = new BasicManagedDataSource();
    dataSource.setTransactionManager(arjunaTransactionManager);
    dataSource.setDriverClassName(database.getDriverClass());
    dataSource.setUrl(database.getUrl());
    dataSource.setUsername(database.getUsername());
    dataSource.setPassword(database.getPassword());
    dataSource.setInitialSize(MIN_POOL_SIZE);
    dataSource.setMaxActive(MAX_POOL_SIZE);
    dataSource.setDefaultAutoCommit(false);

    if("com.mysql.jdbc.Driver".equals(database.getDriverClass())) {
      dataSource.setValidationQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(database.getDriverClass())) {
      dataSource.setValidationQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    }
    //TODO validation query for PostgreSQL

    return dataSource;
  }

}
