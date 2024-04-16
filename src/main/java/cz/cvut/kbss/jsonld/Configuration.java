/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains configuration of the tool.
 */
public class Configuration {

    private final Map<String, String> config = new HashMap<>();

    public Configuration() {
    }

    public Configuration(Configuration other) {
        Objects.requireNonNull(other);
        config.putAll(other.config);
    }

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

    public boolean has(String param) {
        return config.containsKey(param);
    }

    public boolean has(ConfigParam param) {
        return has(param.getName());
    }
}
