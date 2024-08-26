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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.Configured;
import cz.cvut.kbss.jsonld.deserialization.expanded.ExpandedJsonLdDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ClasspathScanner;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolverConfig;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import jakarta.json.JsonValue;

import java.util.Objects;

/**
 * Takes a pre-processed JSON-LD structure and deserializes it.
 */
public abstract class JsonLdDeserializer implements Configured {

    private static final TypeMap TYPE_MAP = new TypeMap();

    private final Configuration configuration;

    protected final TargetClassResolver classResolver;

    protected ValueDeserializers deserializers = new CommonValueDeserializers();

    protected JsonLdDeserializer() {
        this.configuration = new Configuration();
        this.classResolver = initializeTargetClassResolver();
    }

    protected JsonLdDeserializer(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        this.classResolver = initializeTargetClassResolver();
    }

    private TargetClassResolver initializeTargetClassResolver() {
        final String scanPath = configuration.get(ConfigParam.SCAN_PACKAGE, "");
        final TypeMap typeMap = discoverAvailableTypes(scanPath, configuration.is(ConfigParam.DISABLE_TYPE_MAP_CACHE));
        return new TargetClassResolver(typeMap,
                                       new TargetClassResolverConfig(
                                               configuration.is(ConfigParam.ASSUME_TARGET_TYPE),
                                               configuration().is(ConfigParam.ENABLE_OPTIMISTIC_TARGET_TYPE_RESOLUTION),
                                               configuration().is(ConfigParam.PREFER_SUPERCLASS)));
    }

    /**
     * Finds potential deserialization target types on the classpath.
     *
     * @param scanPath Path to scan on classpath
     * @return Map of types to Java classes
     */
    private static TypeMap discoverAvailableTypes(String scanPath, boolean disableCache) {
        final TypeMap map = disableCache ? new TypeMap() : TYPE_MAP;
        if (!map.isEmpty()) {
            return map;
        }
        new ClasspathScanner(c -> {
            final OWLClass ann = c.getDeclaredAnnotation(OWLClass.class);
            if (ann != null) {
                map.register(BeanAnnotationProcessor.expandIriIfNecessary(ann.iri(), c), c);
            }
        }).processClasses(scanPath);
        return map;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Registers a custom deserializer for the specified type.
     * <p>
     * If a deserializer already existed for the type, it is replaced by the new one.
     *
     * @param type         Target type to register the deserializer for
     * @param deserializer Deserializer to register
     * @param <T>          Target type
     */
    public <T> void registerDeserializer(Class<T> type, ValueDeserializer<T> deserializer) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(deserializer);
        deserializers.registerDeserializer(type, deserializer);
    }

    /**
     * Deserializes the specified JSON-LD data.
     *
     * @param <T>         The type of the target object
     * @param jsonLd      JSON-LD structure
     * @param resultClass Type of the result instance
     * @return Deserialized Java instance
     */
    public abstract <T> T deserialize(JsonValue jsonLd, Class<T> resultClass);

    /**
     * Creates deserializer for expanded JSON-LD, initialized with the specified configuration.
     *
     * @param configuration Configuration of the deserializer
     * @return New deserializer
     */
    public static JsonLdDeserializer createExpandedDeserializer(Configuration configuration) {
        return new ExpandedJsonLdDeserializer(configuration);
    }

    /**
     * Creates deserializer for expanded JSON-LD.
     *
     * @return New deserializer
     */
    public static JsonLdDeserializer createExpandedDeserializer() {
        return new ExpandedJsonLdDeserializer();
    }
}
