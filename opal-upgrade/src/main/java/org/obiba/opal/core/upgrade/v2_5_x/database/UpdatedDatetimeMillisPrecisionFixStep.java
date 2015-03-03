/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_5_x.database;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class UpdatedDatetimeMillisPrecisionFixStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(UpdatedDatetimeMillisPrecisionFixStep.class);

  private static final List<String> tables = Arrays.asList(
          "value_table",
          "value_set",
          "variable_entity",
          "datasource",
          "category",
          "variable");

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Override
  public void execute(Version currentVersion) {
    process(databaseRegistry.getIdentifiersDatabase());
    for(Database database : databaseRegistry.listSqlDatabases()) {
      process(database);
    }
  }

  private void process(Database database) {

      SqlSettings sqlSettings = database.getSqlSettings();
      if (sqlSettings == null || !"com.mysql.jdbc.Driver".equals(sqlSettings.getDriverClass())) {
          return;
      }

      log.info("Upgrade database {}: {}", database.getName(), sqlSettings.getUrl());
      JdbcOperations jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource(database.getName(), null));

      for (String table: tables) {
          execute(jdbcTemplate, String.format("ALTER TABLE %1s MODIFY updated datetime(3)", table));
      }
  }

  private void execute(final JdbcOperations jdbcTemplate, final String sql) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          jdbcTemplate.execute(sql);
          log.info("Executed '{}'", sql);
        } catch(DataAccessException ignored) {
        }
      }
    });
  }

}
