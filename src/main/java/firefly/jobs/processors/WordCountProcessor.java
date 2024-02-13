package firefly.jobs.processors;

import firefly.constants.Constant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

@StepScope
public class WordCountProcessor implements ItemProcessor<Map<String,Long>, Map<String,Long>> {

    @Override
    public Map<String, Long> process(Map<String,Long> wordCountMap) throws Exception {
        return wordCountMap.entrySet().stream()
            .sorted((wordCountOne, wordCountTwo) -> (int)(wordCountTwo.getValue() - wordCountOne.getValue()))
            .limit(Constant.WORD_COUNT_BATCH)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
