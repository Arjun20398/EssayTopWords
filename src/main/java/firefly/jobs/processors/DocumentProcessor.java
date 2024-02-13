package firefly.jobs.processors;

import firefly.services.EssayReaderService;
import firefly.utils.CommonUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@StepScope
public class DocumentProcessor implements ItemProcessor<List<String>, Map<String,Long>> {

    private EssayReaderService essayReaderService;

    public DocumentProcessor(EssayReaderService essayReaderService){
        this.essayReaderService = essayReaderService;
    }

    @Override
    public Map<String,Long> process(List<String> htmlDocuments) throws Exception {
        Future<List<String>> htmlDocumentWords = essayReaderService.processEssaysFromHtml(htmlDocuments);
        return CommonUtils.findTopKOccurrences(htmlDocumentWords.get());
    }
}
