package firefly.services;

import com.google.common.util.concurrent.RateLimiter;
import firefly.constants.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EnGadgetService {

    private final static RestTemplate restTemplate = new RestTemplate();
    private final RateLimiter rateLimiter = RateLimiter.create(8);

    public String getEssay(String url) {
        rateLimiter.acquire(Constant.ONE);
        String htmlDocument = Strings.EMPTY;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
                htmlDocument = response.getBody();
            } else {
                log.error("Api failed to fetch essay {}, statusCode {}", url, response.getStatusCodeValue());
                Thread.sleep(100);
            }
        } catch (HttpClientErrorException e){
            log.info("Client Error for essay {}", url);
            htmlDocument = "NOT_FOUND";
        } catch (Exception e){
            log.info("Exception: {}", e.getMessage());
        }
        return htmlDocument;
    }
}
