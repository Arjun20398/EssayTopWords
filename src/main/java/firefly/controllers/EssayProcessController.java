package firefly.controllers;

import firefly.FireFly;
import firefly.services.EssayProcessingService;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EssayProcessController {

    private final EssayProcessingService essayProcessingService;

    @GetMapping("/spring-batch/words")
    public ResponseEntity<?> findTopOccurrencesBySpringBatch(@RequestParam String essayUrlFile,
                                                @RequestParam Integer maxWordCount) {
        long time = System.currentTimeMillis();
        Map<String, Long>
            topWords = essayProcessingService.processUrlForMaxWordCount(essayUrlFile, maxWordCount);
        System.out.println("Time taken " + (System.currentTimeMillis() - time));
        return new ResponseEntity<>(topWords, HttpStatus.OK);
    }

    @GetMapping("/thread-pool/words")
    public ResponseEntity<?> findTopOccurrencesByThreadPool(@RequestParam String essayUrlFile,
                                                @RequestParam Integer maxWordCount) {
        long time = System.currentTimeMillis();
        Map<String, Long>
            topWords = essayProcessingService.processUrlForMaxWordCountUsingThreadPool(essayUrlFile, maxWordCount);
        System.out.println("Time taken " + (System.currentTimeMillis() - time));
        return new ResponseEntity<>(topWords, HttpStatus.OK);
    }
}
