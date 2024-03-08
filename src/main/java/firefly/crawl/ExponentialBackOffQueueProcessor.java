package firefly.crawl;

import firefly.constants.Constant;
import firefly.exceptions.TooManyRequestsError;
import firefly.services.EssayReaderService;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExponentialBackOffQueueProcessor<T> implements ExponentialBackOffQueue<T> {

    private Queue<String> urlQueue;
    private Integer waitTime;
    private EssayReaderService essayReaderService;


    public ExponentialBackOffQueueProcessor(Queue<String> urlQueue,EssayReaderService essayReaderService){
        this.urlQueue = urlQueue;
        this.waitTime = Constant.ONE;
        this.essayReaderService = essayReaderService;
    }

    @Override
    public T processFront(Class<T> tClass) {
        T tResponse = null;
        String url = this.urlQueue.poll();
        try {
            TimeUnit.SECONDS.sleep(this.waitTime);
            tResponse = essayReaderService.readEssaysOnline(url,tClass);
            if(this.waitTime != 1){
                this.waitTime /= 2;
                log.info("Reducing wait time to half {}", this.waitTime);
            }
        } catch (TooManyRequestsError error) {
            log.error("TooManyRequestsError: doubling waitTime to {}", this.waitTime * 2);
            this.waitTime *= 2;
            this.urlQueue.add(url);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            this.urlQueue.add(url);
        }
        return tResponse;
    }

    public Boolean urlsPendingToProcess(){
        return !this.urlQueue.isEmpty();
    }
}
