package org.obiba.opal.core.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionServiceImpl implements JobExecutionService {

  @Autowired
  private JobLauncher jobLauncher;

  @Override
  public JobExecution launchJob(Job job, JobParameters parameters) {
    try {
      return jobLauncher.run(job, parameters);
    } catch(JobExecutionAlreadyRunningException e) {
      throw new JobExecutionRuntimeException("This job is currently running", e);
    } catch(JobInstanceAlreadyCompleteException e) {
      throw new JobExecutionRuntimeException(
          "This job was already completed. Maybe you need to change the input parameters?");
    } catch(JobRestartException e) {
      throw new JobExecutionRuntimeException("Unspecified restart exception", e);
    }
  }
}
