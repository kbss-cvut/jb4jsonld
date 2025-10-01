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
package cz.cvut.kbss.jsonld;

/**
 * Configuration parameters.
 */
public enum ConfigParam {

    /**
     * Whether to ignore unknown properties when deserializing JSON-LD.
     * <p>
     * If set to {@code false}, an exception will be thrown when unknown property is encountered.
     */
    IGNORE_UNKNOWN_PROPERTIES("ignoreUnknownProperties"),

    /**
     * Package in which to look for mapped classes.
     * <p>
     * The scan is important for support for polymorphism in object deserialization.
     */
    SCAN_PACKAGE("scanPackage"),

    /**
     * Whether to require an identifier when serializing an object.
     * <p>
     * If set to {@code true} and no identifier is found (either there is no identifier field or its value is
     * {@code null}), an exception will be thrown. If configured to {@code false}, a blank node identifier is generated
     * if no id is present.
     */
    REQUIRE_ID("requireId"),

    /**
     * Allows assuming target type from the provided Java type when no types are specified in JSON-LD.
     * <p>
     * If set to {@code true}, JB4JSON-LD will attempt to use the provided Java type as the target type when
     * deserializing a JSON-LD object which has no types declared.
     * <p>
     * Defaults to {@code false}, in which case an exception is thrown for a typeless JSON-LD object.
     */
    ASSUME_TARGET_TYPE("assumeTargetType"),

    /**
     * Enables optimistic target type resolution.
     * <p>
     * This means that when a an ambiguous target type is encountered during deserialization of an object (i.e.,
     * multiple concrete classes match the data type), instead of throwing an
     * {@link cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException}, one of the classes will be selected for
     * instantiation.
     * <p>
     * Note that enabling this behavior should probably be done together with setting {@link #IGNORE_UNKNOWN_PROPERTIES}
     * to true, so that any JSON-LD data for which the selected target class has no mapping are ignored and do not cause
     * an exception to be thrown.
     * <p>
     * Defaults to {@code false}.
     */
    ENABLE_OPTIMISTIC_TARGET_TYPE_RESOLUTION("enableOptimisticTargetTypeResolution"),

    /**
     * Configures optimistic type resolution to prefer concrete superclasses if possible.
     * <p>
     * If optimistic target type resolution is enabled, the target type resolver will select one of the matching classes
     * for instantiation. If this parameter is set tot {@code true}, a parent class (if concrete) will be preferred for
     * instantiation. If not, any of the classes may be selected.
     * <p>
     * Defaults to {@code false}.
     *
     * @see #ENABLE_OPTIMISTIC_TARGET_TYPE_RESOLUTION
     */
    PREFER_SUPERCLASS("preferSuperclass"),

    /**
     * Whether to serialize date/time values as the number of milliseconds since epoch (if applicable).
     * <p>
     * Serialization as the number of millis since epoch is the default way for {@link java.util.Date}, but is not very
     * useful for the Java 8 datetime API. If set to {@code false}, date/time values will be serialized as String in the
     * ISO 8601 format.
     * <p>
     * To provide consistent behavior of various datetime representations, this property defaults to false.
     */
    SERIALIZE_DATETIME_AS_MILLIS("serializeDatetimeAsMillis"),

    /**
     * Format string used to serialize and deserialize datetime values.
     * <p>
     * Note that if {@link #SERIALIZE_DATETIME_AS_MILLIS} is enabled, this parameter has no effect on serialization of
     * datetime.
     * <p>
     * Also note that this format applies only to full datetime values. Date or time values have to be formatted
     * per-attribute.
     */
    DATE_TIME_FORMAT("datetimeFormat"),

    /**
     * Whether to serialize individuals using expanded term definition in context.
     * <p>
     * This basically means that the individual's identifier is provided directly as a string and an expanded term
     * definition (consisting of a {@literal @id} and {@literal @type}) is added into the context, specifying that the
     * string is an identifier.
     */
    SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION("serializeIndividualsUsingExpandedDefinition"),

    /**
     * Whether to disable the type map cache.
     * <p>
     * Type map is used to map JSON-LD types to Java classes for deserialization. Since this map is built by scanning
     * the classpath (see the {@link #SCAN_PACKAGE} parameter), it is cached by default to avoid repeated scanning of
     * the classpath.
     * <p>
     * If every deserializer instance should get a fresh type map based on the current configuration, disable this
     * cache.
     */
    DISABLE_TYPE_MAP_CACHE("disableTypeMapCache"),

	/**
	 * Whether to disable the unresolved references check after deserialization.
	 * <p>
	 * When a JSON-LD is used with @list in it, it refers to objects later in the same JSON-LD.
	 * In this case, the unresolved references can only be checked after deserializing all entries.
	 */
	DISABLE_UNRESOLVED_REFERENCES_CHECK("disableUnresolvedReferencesCheck");

    private final String name;

    ConfigParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
