/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime.jdbc;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

  private static final int MIN_POOL_SIZE = 3;

  private static final int MAX_POOL_SIZE = 100;

  private static final int MAX_WAIT = 10 * 1000; // 10s

  private static final int MAX_IDLE = 10;

  @Autowired
  private TransactionManager transactionManager;

  private String driverClass;

  private String url;

  private String username;

  private String password;

  @Override
  public DataSource getObject() {
    BasicManagedDataSource dataSource = new BasicManagedDataSource();
    dataSource.setTransactionManager(transactionManager);
    dataSource.setDriverClassName(driverClass);
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setInitialSize(MIN_POOL_SIZE);
    dataSource.setMaxActive(MAX_POOL_SIZE);
    dataSource.setMaxWait(MAX_WAIT);
    dataSource.setMaxIdle(MAX_IDLE);
    dataSource.setDefaultAutoCommit(false);
    dataSource.setTestOnBorrow(true);

    if("com.mysql.jdbc.Driver".equals(driverClass)) {
      dataSource.setValidationQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(driverClass)) {
      dataSource.setValidationQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    } else {
      throw new IllegalArgumentException("Unsupported JDBC driver: " + driverClass);
    }
    //TODO validation query for PostgreSQL

    return dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
}
