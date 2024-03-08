package firefly.jobs.processors;

import firefly.crawl.ExponentialBackOffQueueProcessor;
import firefly.services.DictionaryService;
import firefly.services.EssayReaderService;
import firefly.utils.CommonUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@AllArgsConstructor
@StepScope
public class EssayProcessor implements ItemProcessor<List<String>, Map<String,Long>> {

    private EssayReaderService essayReaderService;
    private DictionaryService dictionaryService;

    @Override
    public Map<String,Long> process(List<String> essayUrls) {
        return readBatchEssaysOnline(essayUrls);
    }

    public Map<String,Long> readBatchEssaysOnline(List<String> essayUrls) {
        long time = System.currentTimeMillis();
        List<String> words = new ArrayList<>();
        ExponentialBackOffQueueProcessor<String> exponentialBackOffQueueProcessor =
            new ExponentialBackOffQueueProcessor<>(new LinkedList<>(essayUrls), essayReaderService);
        while (exponentialBackOffQueueProcessor.urlsPendingToProcess()){
            String document = exponentialBackOffQueueProcessor.processFront(String.class);
            words.addAll(CommonUtils.findWordsInDocument(document)
                .stream().filter(word -> dictionaryService.isValidWord(word))
                .collect(Collectors.toList()));
        }
        log.info("Processed {} essay in {} ms", essayUrls.size(), System.currentTimeMillis() - time);
        return CommonUtils.findTopKOccurrences(words);
    }
}
