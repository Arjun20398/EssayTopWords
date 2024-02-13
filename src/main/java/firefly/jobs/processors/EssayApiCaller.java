package firefly.jobs.processors;

import firefly.services.EssayReaderService;
import firefly.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@AllArgsConstructor
@StepScope
public class EssayApiCaller implements ItemProcessor<List<String>, List<String>> {

    private EssayReaderService essayReaderService;

    @Override
    public List<String> process(List<String> essayUrls) throws Exception {
        return readBatchEssaysOnline(essayUrls);
    }

    public List<String> readBatchEssaysOnline(List<String> essayUrls){
        List<Future<String>> essayWordsFutures = new ArrayList<>();
        long time = System.currentTimeMillis();
        List<String> htmlDocuments = essayUrls.stream()
            .map(url -> essayReaderService.readEssaysOnlineSync(url))
            .collect(Collectors.toList());
//        for (String urls: essayUrls) {
//            essayWordsFutures.add(essayReaderService.readEssaysOnline(urls));
//        }
//        essayWordsFutures.forEach(essayWordsFuture -> {
//            try {
//                htmlDocuments.add(essayWordsFuture.get());
//            } catch (Exception e) {
//                log.info("Error in reading futures {}", e.getMessage());
//            }
//        });
        log.info("Read {} essay in {} ms", essayUrls.size(), System.currentTimeMillis() - time);
        return htmlDocuments;
    }
}
