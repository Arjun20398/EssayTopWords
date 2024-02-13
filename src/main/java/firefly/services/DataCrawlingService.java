package firefly.services;

import firefly.constants.Constant;
import firefly.exceptions.TooManyRequestsError;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DataCrawlingService {

    private final static RestTemplate restTemplate = new RestTemplate();

    public String getEssay(String url) {
        String htmlDocument = Strings.EMPTY;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
                htmlDocument = response.getBody();
            } else if (Constant.TOO_MANY_REQUEST_STATUS == response.getStatusCodeValue()) {
                throw new TooManyRequestsError(String.format("%s Failed", url));
            } else {
                log.error("Api failed to fetch essay {}, statusCode {}", url, response.getStatusCodeValue());
            }
        } catch (HttpClientErrorException e){
            log.info("Essay {} not found", url);
            htmlDocument = "NOT_FOUND";
        }
        return htmlDocument;
    }
}
