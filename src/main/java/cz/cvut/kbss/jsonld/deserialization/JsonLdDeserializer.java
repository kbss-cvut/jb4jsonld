/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
import cz.cvut.kbss.jsonld.annotation.JsonLdType;
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

    private Configuration configuration;

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
        final TypeMap typeMap = discoverAvailableTypes(scanPath, configuration.is(ConfigParam.DISABLE_TYPE_MAP_CACHE), configuration.getObject(ConfigParam.CLASS_LOADER));
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
    private static TypeMap discoverAvailableTypes(String scanPath, boolean disableCache, Object classLoader) {
        final TypeMap map = disableCache ? new TypeMap() : TYPE_MAP;
        if (!map.isEmpty()) {
            return map;
        }
		if (!(classLoader instanceof ClassLoader)) {
			classLoader = null;
		}
        new ClasspathScanner(c -> {
            final OWLClass annOwl = c.getDeclaredAnnotation(OWLClass.class);
            if (annOwl != null) {
                map.register(BeanAnnotationProcessor.expandIriIfNecessary(annOwl.iri(), c), c);
            }
			final JsonLdType annJsonLd = c.getDeclaredAnnotation(JsonLdType.class);
            if (annJsonLd != null) {
                map.register(BeanAnnotationProcessor.expandIriIfNecessary(annJsonLd.iri(), c), c);
            }
        }).processClasses((ClassLoader) classLoader, scanPath);
        return map;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

	@Override
	public void updateConfiguration(Configuration configuration) {
		this.configuration = configuration;
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
	 * Cleans up after deserialization. Should be called after the entire list is deserialized
	 * using a custom {@link DeserializationContext}.
	 */
	public abstract void cleanup();

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
