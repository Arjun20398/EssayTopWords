package firefly.jobs.writers;

import firefly.constants.Constant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

@StepScope
public class EssayProcessedWriter implements ItemStreamWriter<Map<String,Long>> {

    private ExecutionContext executionContext;

    @Override
    public void write(List<? extends Map<String,Long>> urlDocumentWords) throws Exception {
        Map<String,Long> processedWordsForNextStep =
            (Map<String,Long>) executionContext.get(Constant.ESSAY_WORD_OUTPUT);
        for(Map<String,Long> processedWords: urlDocumentWords){
            processedWords.forEach((key,value) -> {
                processedWordsForNextStep.put(key,
                    processedWordsForNextStep.getOrDefault(key,Constant.LONG_ZERO) + value);
            });
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(Constant.ESSAY_WORD_OUTPUT, new ConcurrentHashMap<>());
        this.executionContext = executionContext;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}
