package firefly.services;

import firefly.constants.Constant;
import firefly.utils.CommonUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EssayReaderService {

    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private EnGadgetService enGadgetService;

    public EssayReaderService(){
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Async("customTaskExecutor")
    public CompletableFuture<String> readEssaysOnline(String url){
        return CompletableFuture.completedFuture(enGadgetService.getEssay(url));
    }

    public String readEssaysOnlineSync(String url){
        return enGadgetService.getEssay(url);
    }

    @Async("customTaskExecutor")
    public CompletableFuture<List<String>> processEssaysFromHtml(List<String> htmlDocuments){
        return CompletableFuture.supplyAsync(() -> {
            String mergedHtmlDocuments = String.join(Constant.SPACE, htmlDocuments);
            return CommonUtils.findParagraphString(mergedHtmlDocuments).stream()
                .filter(word -> dictionaryService.isValidWord(word)).collect(Collectors.toList());
        });
    }
}
