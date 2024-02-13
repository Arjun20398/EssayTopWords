package firefly.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ProjectConfig {

//    @Bean
//    public Executor customTaskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(4);
//        executor.setMaxPoolSize(4);
//        executor.setQueueCapacity(10);
//        executor.setThreadNamePrefix("CustomAsyncProcessor - ");
//        executor.initialize();
//        return executor;
//    }

    @Bean
    public Executor customTaskExecutor() {
        return Executors.newFixedThreadPool(5);
    }
}
