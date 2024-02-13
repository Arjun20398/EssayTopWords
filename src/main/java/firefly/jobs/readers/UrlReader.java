package firefly.jobs.readers;

import firefly.constants.Constant;
import firefly.models.EssayMetaData;
import firefly.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class UrlReader implements ItemReader<List<String>> {

    private int next = 0;
    private List<List<String>> batchedEssayUrls = new ArrayList<>();
    private String urlFile;

    @Override
    synchronized public List<String> read() {
        if(batchedEssayUrls.isEmpty() && Objects.nonNull(urlFile)){
            batchedEssayUrls = CommonUtils.readFileOnlineBatched(urlFile);
        }

        List<String> essayUrls = null;
        if (next < batchedEssayUrls.size()) {
            essayUrls = batchedEssayUrls.get(next);
            next++;
        } else {
            return null;
        }
        return essayUrls;
    }

    @Value("#{jobParameters['urlFile']}")
    public void setFileName(final String urlFile) {
        this.urlFile = urlFile;
    }
}
