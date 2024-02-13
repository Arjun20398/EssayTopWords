package firefly.jobs.writers;

import firefly.constants.Constant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

@StepScope
public class UrlDocumentWriter implements ItemStreamWriter<List<String>> {

    private ExecutionContext executionContext;

    @Override
    public void write(List<? extends List<String>> urlDocuments) throws Exception {
        List<List<String>> documentsToPassToNextStep =
            (List<List<String>>) executionContext.get(Constant.DOCUMENT_OUTPUT);
        documentsToPassToNextStep.addAll(urlDocuments);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(Constant.DOCUMENT_OUTPUT, new ArrayList<>());
        this.executionContext = executionContext;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}
