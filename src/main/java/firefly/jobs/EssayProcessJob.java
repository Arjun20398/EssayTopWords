package firefly.jobs;

import firefly.constants.Constant;
import firefly.jobs.processors.EssayApiCaller;
import firefly.jobs.processors.DocumentProcessor;
import firefly.jobs.processors.WordCountProcessor;
import firefly.jobs.readers.DocumentProcessReader;
import firefly.jobs.readers.UrlReader;
import firefly.jobs.readers.WordCountReader;
import firefly.jobs.writers.EssayWordCountWriter;
import firefly.jobs.writers.ProcessedDocumentWriter;
import firefly.jobs.writers.UrlDocumentWriter;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class EssayProcessJob {

    @Autowired
    private EssayReaderService essayReaderService;

    @Bean("EssayProcessJob")
    public Job essayProcessJob(JobBuilderFactory jobBuilderFactory,
                               @Qualifier("urlReadStep") Step urlReadStep,
                               @Qualifier("documentProcessStep") Step documentProcessStep,
                               @Qualifier("countWordStep") Step wordCountStep) {

        return jobBuilderFactory.get("EssayProcessJobBuilder")
            .incrementer(new RunIdIncrementer())
            .start(urlReadStep)
            .next(documentProcessStep)
            .next(wordCountStep)
            .build();
    }

    @Bean("urlReadStep")
    public Step startStep(StepBuilderFactory stepBuilderFactory,
                          @Qualifier("SimpleAsyncTaskExecutor") TaskExecutor simpleAsyncTaskExecutor,
                          @Qualifier("urlsReader") UrlReader urlReader,
                          @Qualifier("essayApiCaller") EssayApiCaller essayApiCaller,
                          @Qualifier("urlDocumentWriter") UrlDocumentWriter urlDocumentWriter){

        return stepBuilderFactory.get("urlReadStep")
            .<List<String>, List<String>>chunk(Constant.API_CALL_CHUNK_SIZE)
            .reader(urlReader)
            .processor(essayApiCaller)
            .writer(urlDocumentWriter)
            .listener(promotionListener())
            .taskExecutor(simpleAsyncTaskExecutor)
            .build();
    }

    @Bean("documentProcessStep")
    public Step documentProcessStep(StepBuilderFactory stepBuilderFactory,
                          @Qualifier("SimpleAsyncTaskExecutor") TaskExecutor simpleAsyncTaskExecutor,
                          @Qualifier("documentProcessReader") DocumentProcessReader documentProcessReader,
                          @Qualifier("documentProcessor") DocumentProcessor documentProcessor,
                          @Qualifier("processedDocumentWriter") ProcessedDocumentWriter processedDocumentWriter){
        return stepBuilderFactory.get("documentProcessStep")
            .<List<String>, Map<String,Long>>chunk(Constant.PROCESS_DOCUMENT_CHUNK_SIZE)
            .reader(documentProcessReader)
            .processor(documentProcessor)
            .writer(processedDocumentWriter)
            .listener(promotionListener())
            .taskExecutor(simpleAsyncTaskExecutor)
            .build();
    }

    @Bean("countWordStep")
    public Step countWordStep(TaskExecutor taskExecutor,
                          StepBuilderFactory stepBuilderFactory,
                          @Qualifier("SimpleAsyncTaskExecutor") TaskExecutor simpleAsyncTaskExecutor,
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
    step 1
    read urls and create list of words
     */

    @Bean(name = "urlsReader", destroyMethod="")
    @StepScope
    public UrlReader urlReader() {
        return new UrlReader();
    }

    @Bean("essayApiCaller")
    public EssayApiCaller essayApiCaller(){
        return new EssayApiCaller(essayReaderService);
    }

    @Bean("urlDocumentWriter")
    public UrlDocumentWriter urlDocumentWriter(){
        return new UrlDocumentWriter();
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
    step 2
    processing a document
     */

    @Bean("documentProcessReader")
    public DocumentProcessReader documentProcessReader(){
        return new DocumentProcessReader();
    }

    @Bean("documentProcessor")
    public DocumentProcessor documentProcessor(){
        return new DocumentProcessor(essayReaderService);
    }

    @Bean("processedDocumentWriter")
    public ProcessedDocumentWriter processedDocumentWriter() {
        return new ProcessedDocumentWriter();
    }

    /*
    step 3
    aggregate word count
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

    @Bean("SimpleAsyncTaskExecutor")
    public TaskExecutor getSimpleAsyncTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        return executor;
    }
}
