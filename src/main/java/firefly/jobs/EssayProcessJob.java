package firefly.jobs;

import firefly.constants.Constant;
import firefly.jobs.processors.EssayProcessor;
import firefly.jobs.processors.WordCountProcessor;
import firefly.jobs.readers.EssayUrlReader;
import firefly.jobs.readers.WordCountReader;
import firefly.jobs.writers.EssayWordCountWriter;
import firefly.jobs.writers.EssayProcessedWriter;
import firefly.services.DictionaryService;
import firefly.services.EssayReaderService;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class EssayProcessJob {

    @Autowired
    private EssayReaderService essayReaderService;

    @Autowired
    private DictionaryService dictionaryService;

    @Bean("EssayProcessJob")
    public Job essayProcessJob(JobBuilderFactory jobBuilderFactory,
                               @Qualifier("urlReadStep") Step urlReadStep,
                               @Qualifier("documentProcessStep") Step documentProcessStep,
                               @Qualifier("countWordStep") Step wordCountStep) {

        return jobBuilderFactory.get("EssayProcessJobBuilder")
            .incrementer(new RunIdIncrementer())
            .start(urlReadStep)
            .next(wordCountStep)
            .build();
    }

    @Bean("urlReadStep")
    public Step startStep(StepBuilderFactory stepBuilderFactory,
                          TaskExecutor simpleAsyncTaskExecutor,
                          @Qualifier("urlsReader") EssayUrlReader essayUrlReader,
                          @Qualifier("essayProcessor") EssayProcessor essayProcessor,
                          @Qualifier("essayProcessedWriter") EssayProcessedWriter essayProcessedWriter){

        return stepBuilderFactory.get("urlReadStep")
            .<List<String>, Map<String,Long>>chunk(Constant.API_CALL_CHUNK_SIZE)
            .reader(essayUrlReader)
            .processor(essayProcessor)
            .writer(essayProcessedWriter)
            .listener(promotionListener())
            .taskExecutor(simpleAsyncTaskExecutor)
            .build();
    }

    @Bean("countWordStep")
    public Step countWordStep(StepBuilderFactory stepBuilderFactory,
                          TaskExecutor simpleAsyncTaskExecutor,
                          @Qualifier("wordCountReader") WordCountReader wordCountReader,
                          @Qualifier("wordCountProcessor") WordCountProcessor wordCountProcessor,
                          @Qualifier("essayWordWriterSynchronized") SynchronizedItemStreamWriter<Map<String,Long>> wordProcessor){
        return stepBuilderFactory.get("wordCountStep")
            .<Map<String,Long>, Map<String,Long>>chunk(Constant.PROCESS_DOCUMENT_CHUNK_SIZE)
            .reader(wordCountReader)
            .processor(wordCountProcessor)
            .writer(wordProcessor)
            .listener(promotionListener())
            .taskExecutor(simpleAsyncTaskExecutor)
            .build();
    }

    /*
    step: read urls and create list of words
     */

    @Bean(name = "urlsReader", destroyMethod="")
    @StepScope
    public EssayUrlReader urlReader() {
        return new EssayUrlReader();
    }

    @Bean("essayProcessor")
    public EssayProcessor essayProcessor(){
        return new EssayProcessor(essayReaderService, dictionaryService);
    }

    @Bean("essayProcessedWriter")
    public EssayProcessedWriter essayProcessedWriter(){
        return new EssayProcessedWriter();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{Constant.DOCUMENT_OUTPUT, Constant.ESSAY_WORD_OUTPUT,
            Constant.FINAL_WORD_COUNT});
        listener.setStrict(false);
        return listener;
    }

    /*
    step: aggregate word count
     */

    @Bean("wordCountReader")
    public WordCountReader wordCountReader(){
        return new WordCountReader();
    }

    @Bean("wordCountProcessor")
    public WordCountProcessor wordCountProcessor(){
        return new WordCountProcessor();
    }

    @Bean("essayWordWriter")
    public EssayWordCountWriter writer() {
        return new EssayWordCountWriter(Constant.ZERO);
    }

    @Bean("essayWordWriterSynchronized")
    public SynchronizedItemStreamWriter<Map<String,Long>> synchronizedItemStreamWriter() {
        SynchronizedItemStreamWriter<Map<String,Long>> synchronizedItemStreamWriter =
            new SynchronizedItemStreamWriter<>();
        synchronizedItemStreamWriter.setDelegate(new EssayWordCountWriter(Constant.ZERO));
        return synchronizedItemStreamWriter;
    }
}
