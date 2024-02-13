package firefly.jobs.writers;

import firefly.constants.Constant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

@Slf4j
@AllArgsConstructor
@StepScope
public class EssayWordCountWriter implements ItemStreamWriter<Map<String,Long>> {

    private int index;
    private ExecutionContext executionContext;

    public EssayWordCountWriter(int index){
        this.index = index;
    }


    @Override
    public void write(List<? extends Map<String,Long>> list) throws Exception {
        Map<String,Long> finalWordCount = (Map<String,Long>) executionContext.get(
            Constant.FINAL_WORD_COUNT);
        log.info("writing for batch: {}, batchSize: {}", index++, list.size());
        list.forEach(stringIntegerMap -> {
            stringIntegerMap.forEach((key, value) -> {
                finalWordCount.put(key, finalWordCount.getOrDefault(key, Constant.LONG_ZERO) + value);
            });
        });
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(Constant.FINAL_WORD_COUNT, new HashMap<>());
        this.executionContext = executionContext;
    }

    @Override
    public void close() throws ItemStreamException {
        // Close resources or connections if needed
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // Update the state of the writer if necessary
        // This method is called to allow the writer to update its state based on the provided ExecutionContext
    }
}
