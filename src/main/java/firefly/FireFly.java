package firefly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FireFly {

    public static void main(String[] args) {
        SpringApplication.run(FireFly.class, args).getBean(FireFly.class);
    }
}
