package firefly.services;

import firefly.config.JobFactory;
import firefly.constants.Constant;
import firefly.models.JobResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Slf4j
public class JobLauncherService {

    private final JobLauncher jobLauncher;
    private final JobFactory jobFactory;

    public JobResponse launchJobs(String jobName, JobParameters jobParameters) {
        JobResponse response = JobResponse.builder().status(Boolean.FALSE).build();
        try {
            JobExecution jobExecution = jobLauncher.run(jobFactory.getJobByName(jobName), jobParameters);
            response.setStatus(jobExecution.getStatus().equals(BatchStatus.COMPLETED));
            response.setMaxWordCount((Map<String,Long>)jobExecution.getExecutionContext()
                .get(Constant.FINAL_WORD_COUNT));
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Error message : {} ", e.getMessage());
        }
        return response;
    }
}
