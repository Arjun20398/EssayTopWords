package firefly.constants;

import java.util.regex.Pattern;

public class Constant {

    public static final Pattern PARA_PATTERN = Pattern.compile("<p>(.+?)</p>");
    public static final String TAGS_REGEX = "(?s)<.*?>|[!?,.]";
    public static final String ENGLISH_WORD = "([a-z]){3,}";
    public static final String  DICTIONARY_FILE = "/Users/arjunsingh/Desktop/dealshare/assignment/src/main/java/firefly/static/words.txt";

    public static final Integer ZERO = 0;
    public static final Integer ONE = 1;
    public static final Integer FIVE = 5;
    public static final Integer TEN = 10;
    public static final Long LONG_ZERO = 0L;
    public static final String DOCUMENT_OUTPUT = "DOCUMENT_OUTPUT";
    public static final String ESSAY_PROCESS_JOB = "EssayProcessJob";
    public static final Integer API_CALL_CHUNK_SIZE = 100;
    public static final Integer PROCESS_DOCUMENT_CHUNK_SIZE = 10;
    public static final String URL_FILE_PARAM = "urlFile";
    public static final String DATE = "DATE";
    public static final String ESSAY_WORD_OUTPUT = "ESSAY_WORD_OUTPUT";
    public static final String FINAL_WORD_COUNT = "FINAL_WORD_COUNT";
    public static final Integer WORD_COUNT_BATCH = 100;
    public static final Integer FINAL_COUNT_TO_RETURN = 10;
    public static final String SPACE = " ";
    public static final Integer THREAD_SLEEP_MS = 100;
}
