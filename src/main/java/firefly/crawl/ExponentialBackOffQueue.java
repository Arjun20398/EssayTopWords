package firefly.crawl;

public interface ExponentialBackOffQueue<T> {

    T processFront(Class<T> tClass);
}
