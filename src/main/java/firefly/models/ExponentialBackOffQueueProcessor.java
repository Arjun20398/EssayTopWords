package firefly.models;

import firefly.constants.Constant;
import firefly.exceptions.TooManyRequestsError;
import firefly.services.EssayReaderService;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
public class ExponentialBackOffQueueProcessor {

    private Queue<String> urlQueue;
    private Integer waitTime;
    private EssayReaderService essayReaderService;


    public ExponentialBackOffQueueProcessor(Queue<String> urlQueue,EssayReaderService essayReaderService){
        this.urlQueue = urlQueue;
        this.waitTime = Constant.ONE;
        this.essayReaderService = essayReaderService;
    }

    public String processFront() {
        String htmlDocument = Strings.EMPTY;
        String url = this.urlQueue.poll();
        try {
            TimeUnit.SECONDS.sleep(this.waitTime);
            htmlDocument = essayReaderService.readEssaysOnline(url);
            if(this.waitTime != 1){
                this.waitTime /= 2;
                log.info("changing wait time to {}", this.waitTime);
            }
        } catch (TooManyRequestsError error) {
            log.error("TooManyRequestsError: changing waitTime to {}", this.waitTime * 2);
            this.waitTime *= 2;
            this.urlQueue.add(url);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            this.urlQueue.add(url);
        }
        return htmlDocument;
    }

    public Boolean urlsPendingToProcess(){
        return !this.urlQueue.isEmpty();
    }
}
