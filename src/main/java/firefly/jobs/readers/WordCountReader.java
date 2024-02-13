package firefly.jobs.readers;

import firefly.constants.Constant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

@StepScope
public class WordCountReader implements ItemReader<Map<String,Long>> {

    private int index = 0;
    private Map<String,Long> wordCountMap;

    @Override
    synchronized public Map<String,Long> read()
        throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (Objects.isNull(wordCountMap) || index >= wordCountMap.size()) {
            return null;
        } else {
            Map<String,Long> wordCountResponse = new TreeMap<>(wordCountMap);
            wordCountMap = new TreeMap<>();
            return wordCountResponse;
        }
    }

    @BeforeStep
    public void retrieveSharedData(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        wordCountMap = (ConcurrentHashMap<String,Long>) jobContext.get(Constant.ESSAY_WORD_OUTPUT);
    }
}
