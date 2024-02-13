package firefly.services;

import firefly.constants.Constant;
import firefly.utils.CommonUtils;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DictionaryService {

    private final HashSet<String> validWords = new HashSet<>();

    public DictionaryService() {
        List<String> linesFromFiles = CommonUtils.readLocalFile(Constant.DICTIONARY_FILE);
        linesFromFiles.forEach(word -> {
            if(word.matches(Constant.ENGLISH_WORD)) {
                addWordToDictionary(word);
            }
        });
    }

    public Boolean isValidWord(String word){
        return validWords.contains(word);
    }

    public void addWordToDictionary(String word){
        this.validWords.add(word);
    }
}
