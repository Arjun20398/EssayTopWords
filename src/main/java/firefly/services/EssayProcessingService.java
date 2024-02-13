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
import org.apache.logging.log4j.util.Strings;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EssayProcessingService {

    private final JobLauncherService jobLauncherService;
    private final EssayReaderService essayReaderService;

    public Map<String, Long> processUrlForMaxWordCount(String urlFile, Integer topWordCount){
        JobParameters jobParameters = new JobParametersBuilder()
            .addString(Constant.URL_FILE_PARAM, urlFile, true)
            .addDate(Constant.DATE, new Date())
            .toJobParameters();
        JobResponse jobResponse = jobLauncherService.launchJobs(Constant.ESSAY_PROCESS_JOB, jobParameters);
        return CommonUtils.findTopKOccurrences(jobResponse.getMaxWordCount(),topWordCount);
    }

    public Map<String, Long> processUrlForMaxWordCountUsingThreadPool(String urlFile, Integer topWordCount){
        List<List<String>> batchedUrls = CommonUtils.getBatches(CommonUtils.readLocalFile(urlFile),
            Constant.API_CALL_CHUNK_SIZE);
        Map<String,Long> wordCountMap = new ConcurrentHashMap<>();
        for(List<String> batchedUrl:batchedUrls) {
            List<CompletableFuture<String>> essaysFutures =
                batchedUrl.stream().map(essayReaderService::readEssaysOnlineAsync).collect(Collectors.toList());
            wordCountMap = processEssayFuturesBatch(essaysFutures, wordCountMap);
            log.info("{} urls processed", batchedUrl.size());
        }
        return CommonUtils.findTopKOccurrences(wordCountMap,topWordCount);
    }

    private Map<String,Long> processEssayFuturesBatch(List<CompletableFuture<String>> essayFuturesBatch, Map<String,Long> wordCountMap) {
        try {
            List<String> essays = new ArrayList<>();
            for(CompletableFuture<String> essayFuture:essayFuturesBatch) {
                essays.add(essayFuture.exceptionally((ex) -> {
                    log.error("Error in processEssayFuturesBatch: {}",ex.getMessage());
                    return Strings.EMPTY;
                }).get());
            }
            CompletableFuture<List<String>> words = essayReaderService.processEssaysFromHtml(essays);
            return CommonUtils.mergeWordMaps(wordCountMap, CommonUtils.findTopKOccurrences(words.get()));
        } catch (Exception e) {
            log.error("Error in fetching essay Responses size {}, message {}", essayFuturesBatch.size(), e.getMessage());
        }
        return wordCountMap;
    }
}
