package cz.cvut.kbss.jsonld;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains configuration of the tool.
 */
public class Configuration {

    private final Map<String, String> config = new HashMap<>();

    public String get(ConfigParam param) {
        Objects.requireNonNull(param);
        return get(param.getName());
    }

    public String get(String param) {
        return config.get(param);
    }

    public String get(ConfigParam param, String defaultValue) {
        Objects.requireNonNull(param);
        return config.getOrDefault(param.getName(), defaultValue);
    }

    public String get(String param, String defaultValue) {
        return config.getOrDefault(param, defaultValue);
    }

    public boolean is(ConfigParam param) {
        Objects.requireNonNull(param);
        return is(param.getName());
    }

    public boolean is(String param) {
        final String value = get(param, Boolean.FALSE.toString());
        return Boolean.parseBoolean(value);
    }

    public void set(ConfigParam param, String value) {
        Objects.requireNonNull(param);
        config.put(param.getName(), value);
    }

    public void set(String param, String value) {
        Objects.requireNonNull(param);
        config.put(param, value);
    }
}
