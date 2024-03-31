package firefly.utils;

import firefly.constants.Constant;
import firefly.exceptions.ValidationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
public class CommonUtils {

    public static List<List<String>> readFileOnlineBatched(String fileUrl){
        return getBatches(readLocalFile(fileUrl), Constant.API_CALL_CHUNK_SIZE);
    }

    public static List<String> readLocalFile(String fileUrl){
        List<String> linesFromFiles = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileUrl));
            String line;
            while((line = reader.readLine()) != null) {
                linesFromFiles.add(line);
            }
        } catch (Exception e){
            log.info("Error in reading {}", fileUrl);
            throw new ValidationException(String.format("Error in reading %s", fileUrl));
        } finally {
            if (Objects.nonNull(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return linesFromFiles;
    }

    public static List<String> findWordsInDocument(String htmlDocument){
        List<String> words = new ArrayList<>();
        if(Strings.isEmpty(htmlDocument)){
            return words;
        }
        final Matcher matcher = Constant.PARA_PATTERN.matcher(htmlDocument);
        List<String> paragraphs = new ArrayList<>();
        while (matcher.find()) {
            paragraphs.add(matcher.group(1).replaceAll(Constant.TAGS_REGEX, Strings.EMPTY).toLowerCase());
        }
        paragraphs.forEach(para -> {
            words.addAll(Arrays.asList(para.split(Constant.SPACE)));
        });
        return words;
    }

    public static <T> List<List<T>> getBatches(List<T> collection, int batchSize) {
        return IntStream.iterate(0, index -> index < collection.size(), index -> index + batchSize)
            .mapToObj(index -> collection.subList(index, Math.min(index + batchSize, collection.size())))
            .collect(Collectors.toList());
    }

    public static Map<String,Long> findTopKOccurrences(List<String> words){
        Map<String, Long> map = words.stream()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        return map.entrySet().stream()
            .sorted((wordCountOne, wordCountTwo) -> (int)(wordCountTwo.getValue() - wordCountOne.getValue()))
            .limit(Constant.WORD_COUNT_BATCH)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String,Long> findTopKOccurrences(Map<String, Long> map, Integer topCountValue) {
        return map.entrySet().stream()
            .sorted((wordCountOne, wordCountTwo) -> (int)(wordCountTwo.getValue() - wordCountOne.getValue()))
            .limit(topCountValue)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String,Long> mergeWordMaps(Map<String,Long> resultMap, Map<String,Long> wordMap){
        wordMap.forEach((key,value) -> {
            resultMap.put(key, resultMap.getOrDefault(key,Constant.LONG_ZERO) + value);
        });
        return resultMap.entrySet().stream()
            .sorted((wordCountOne, wordCountTwo) -> (int)(wordCountTwo.getValue() - wordCountOne.getValue()))
            .limit(Constant.WORD_COUNT_BATCH)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
