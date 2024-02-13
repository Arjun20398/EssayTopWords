package firefly.config;

import firefly.constants.Constant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JobFactory {

    @Autowired
    @Qualifier(Constant.ESSAY_PROCESS_JOB)
    Job essayProcessJob;

    Map<String,Job> jobMap;


    @PostConstruct
    public void initialize() {
        jobMap = new HashMap<>();
        jobMap.put(Constant.ESSAY_PROCESS_JOB, essayProcessJob);
    }

    public Set<String> getJobList(){
        return jobMap.keySet();
    }

    public Job getJobByName(String jobName){
        return jobMap.get(jobName);
    }
}
