package firefly.services;

import firefly.constants.Constant;
import firefly.models.JobResponse;
import firefly.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EssayProcessingService {

    private final JobLauncherService jobLauncherService;
    private final EssayReaderService essayReaderService;
    private final Executor executor = Executors.newFixedThreadPool(Constant.FIVE);

    public Map<String, Long> processUrlForMaxWordCount(String urlFile, Integer topWordCount){
        JobParameters jobParameters = new JobParametersBuilder()
            .addString(Constant.URL_FILE_PARAM, urlFile, true)
            .addDate(Constant.DATE, new Date())
            .toJobParameters();
        JobResponse jobResponse = jobLauncherService.launchJobs(Constant.ESSAY_PROCESS_JOB, jobParameters);
        return CommonUtils.findTopKOccurrences(jobResponse.getMaxWordCount());
    }

    public Map<String, Long> processUrlForMaxWordCountUsingThreadPool(String urlFile, Integer topWordCount){
        List<List<String>> batchedUrls = CommonUtils.getBatches(CommonUtils.readLocalFile(urlFile),
            Constant.API_CALL_CHUNK_SIZE);
        Map<String,Long> wordCountMap = new ConcurrentHashMap<>();
        for(List<String> batchedUrl:batchedUrls) {
            List<CompletableFuture<String>> essaysFutures =
                batchedUrl.stream().map(essayReaderService::readEssaysOnline).collect(Collectors.toList());
            List<List<CompletableFuture<String>>> essayFuturesBatches =
                CommonUtils.getBatches(essaysFutures, Constant.TEN);
            for(List<CompletableFuture<String>> essayFuturesBatch:essayFuturesBatches) {
                wordCountMap = processEssayFuturesBatch(essayFuturesBatch, wordCountMap);
            }
            log.info("{} urls processed", batchedUrl.size());
        }
        return wordCountMap;
    }

    private Map<String,Long> processEssayFuturesBatch(List<CompletableFuture<String>> essayFuturesBatch, Map<String,Long> wordCountMap) {
        try {
            List<String> essays = new ArrayList<>();
            for(CompletableFuture<String> essayFuture:essayFuturesBatch) {
                String essay =  essayFuture.get();
                if(essay.isEmpty()){
//                    log.info("Waiting for {}ms", Constant.THREAD_SLEEP_MS);
//                    Thread.sleep(Constant.THREAD_SLEEP_MS);
                } else {
                    essays.add(essayFuture.get());
                }
            }
            CompletableFuture<List<String>> words = essayReaderService.processEssaysFromHtml(essays);
            return CommonUtils.mergeWordMaps(wordCountMap, CommonUtils.findTopKOccurrences(words.get()));
        } catch (Exception e) {
            log.error("Error in fetching essay Responses size {}, message {}", essayFuturesBatch.size(), e.getMessage());
        }
        return wordCountMap;
    }

    public Map<String, Long> processUrlForMaxWordCountUsingFixedPool(String urlFile, Integer topWordCount){
        List<String> urls = CommonUtils.readLocalFile(urlFile);
        List<CompletableFuture<String>> htmlDocumentsFutures = urls.stream().map(url ->
                CompletableFuture.supplyAsync(() -> essayReaderService.readEssaysOnlineSync(url),executor))
            .collect(Collectors.toList());
        CompletableFuture<Void> essayFetcher =
            CompletableFuture.allOf(htmlDocumentsFutures.toArray(new CompletableFuture[0]));
        essayFetcher.join();
        Map<String,Long> wordCountMap = new ConcurrentHashMap<>();
        for(CompletableFuture<String> documentFuture :htmlDocumentsFutures){
            try {
                wordCountMap = CommonUtils.mergeWordMaps(wordCountMap,
                    CommonUtils.findTopKOccurrences(CommonUtils.findParagraphString(documentFuture.get())));
            } catch (Exception e) {
                log.info("Error in processing essay");
            }
        }
        return wordCountMap;
    }
}
