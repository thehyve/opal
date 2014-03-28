/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.batch;

import java.util.LinkedList;

import org.obiba.opal.core.service.DataExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component("dataExportTasklet")
@Scope("step")
public class DataExportTasklet extends StepExecutionListenerSupport implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(DataExportTasklet.class);

  @Autowired
  private DataExportService dataExportService;

  private JobParameters jobParameters;

  private LinkedList<String> tablesToBeProcessed = Lists.newLinkedList();

  @Override
  public void beforeStep(StepExecution stepExecution) {
    jobParameters = stepExecution.getJobParameters();
    String[] tables = jobParameters.getString("tables").split(",");
    for (String table : tables) {
      tablesToBeProcessed.add(table);
    }
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    if (tablesToBeProcessed.isEmpty()) return RepeatStatus.FINISHED;

    contribution.incrementReadCount();

    String table = tablesToBeProcessed.removeFirst();
    log.info("Exporting {}...", table);


    contribution.incrementWriteCount(1);

    return tablesToBeProcessed.isEmpty() ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
  }

}
