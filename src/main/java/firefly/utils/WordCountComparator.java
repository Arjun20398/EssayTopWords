package firefly.utils;

import java.util.Comparator;
import java.util.Map;

public class WordCountComparator implements Comparator<String> {

    Map<String, Long> map;

    public WordCountComparator(Map<String, Long> map) {
        this.map = map;
    }

    public int compare(String a, String b) {
        return (int)(map.get(b) - map.get(a));
    }
}