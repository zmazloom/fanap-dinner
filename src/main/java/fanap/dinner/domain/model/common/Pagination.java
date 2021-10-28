package fanap.dinner.domain.model.common;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Pagination class
 * <p>
 * You can use any constructor or {@link this#of(Integer, Integer)} method.
 * A managed and null-safe Pagination process.
 */

@Getter
@Accessors(fluent = true)
public class Pagination {

    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 10;

    private final int page;
    private final int size;

    public Pagination(Integer page, Integer size) {
        this(page, size, MAX_SIZE, DEFAULT_SIZE);
    }

    public Pagination(Integer page, Integer size, int maxSize, int defaultSize) {
        this.page = (page == null || page < 1) ? 1 : page;
        size = (size == null || size < 1) ? defaultSize : size;
        this.size = (size > maxSize) ? maxSize : size;
    }

    public static Pagination of(Integer page, Integer size) {
        return new Pagination(page, size);
    }

    public int getOffset(){
        return (this.page - 1) * this.size;
    }
}
