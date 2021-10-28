package fanap.dinner.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.collection.spi.PersistentCollection;
import org.json.JSONArray;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Map;

/**
 * the class for util methods around objects.
 */

@Component
public class ModelUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss z")
            .disableHtmlEscaping().create();

    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    @Autowired
    private ModelUtils() {
        MODEL_MAPPER.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setPropertyCondition(context -> !(context.getSource() instanceof PersistentCollection));
    }

    public static ModelMapper getModelMapper() {
        return MODEL_MAPPER;
    }

    public static String toString(Object o) {
        return GSON.toJson(o);
    }

    /**
     * string is null or empty. used for cleaner code.
     *
     * @return string is null or empty.
     */
    public static boolean isEmpty(CharSequence string) {
        return string == null || string.length() == 0;
    }

    /**
     * string is not null and not empty. used for cleaner code.
     *
     * @return string is not null and not empty.
     */
    public static boolean isNotEmpty(CharSequence string) {
        return string != null && string.length() != 0;
    }

    /**
     * collection is null or empty. used for cleaner code.
     *
     * @return collection is null or empty.
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * collection is not null and not empty. used for cleaner code.
     *
     * @return collection is not null and not empty.
     */
    public static boolean isNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * map is null or empty. used for cleaner code.
     *
     * @return map is null or empty.
     */
    public static boolean isEmpty(Map map) {
        return (map == null || map.isEmpty());
    }

    /**
     * map is not null and not empty. used for cleaner code.
     *
     * @return map is not null and not empty.
     */
    public static boolean isNotEmpty(Map map) {
        return map != null && !map.isEmpty();
    }

    /**
     * jsonArray is null or empty. used for cleaner code.
     *
     * @return jsonArray is null or empty.
     */
    public static boolean isEmpty(JSONArray jsonArray) {
        return jsonArray == null || jsonArray.isEmpty();
    }

    /**
     * jsonArray is not null and not empty. used for cleaner code.
     *
     * @return jsonArray is not null and not empty.
     */
    public static boolean isNotEmpty(JSONArray jsonArray) {
        return jsonArray != null && !jsonArray.isEmpty();
    }

}