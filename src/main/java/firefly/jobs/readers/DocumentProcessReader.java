package firefly.jobs.readers;

import firefly.constants.Constant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DocumentProcessReader implements ItemReader<List<String>> {

    public int index;
    public List<List<String>> batchedDocuments;

    @Override
    public synchronized List<String> read()
        throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (index  < batchedDocuments.size()) {
            return batchedDocuments.get(index++);
        } else {
            index = 0;
            return null;
        }
    }

    @BeforeStep
    public void retrieveSharedData(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        batchedDocuments = (List<List<String>>) jobContext.get(Constant.DOCUMENT_OUTPUT);
    }
}
