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
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Replaces pending references with objects of assumed target type.
 * <p>
 * These objects have only identifier value set.
 */
public class AssumedTypeReferenceReplacer {

    private static final Logger LOG = LoggerFactory.getLogger(AssumedTypeReferenceReplacer.class);

    /**
     * Replaces pending references from the specified {@link PendingReferenceRegistry} with empty objects of the assumed
     * target type.
     * <p>
     * The objects will have only the identifiers set.
     * <p>
     * If unable to determine target type (typically for pending collection item references), the pending reference is
     * skipped.
     *
     * @param registry Registry from which the pending references should be replaced
     */
    public void replacePendingReferencesWithAssumedTypedObjects(PendingReferenceRegistry registry) {
        Objects.requireNonNull(registry);
        final Map<String, Class<?>> idsToTypes = getAssumedTargetTypes(registry);
        idsToTypes.forEach((id, type) -> {
            final Object instance = BeanClassProcessor.createInstance(type);
            final Optional<Field> idField = BeanAnnotationProcessor.getIdentifierField(type);
            if (idField.isEmpty()) {
                throw UnknownPropertyException.create(JsonLd.ID, type);
            }
            Object identifier = id;
            if (!idField.get().getType().isAssignableFrom(String.class)) {
                identifier = DataTypeTransformer.transformValue(id, idField.get().getType());
            }
            BeanClassProcessor.setFieldValue(idField.get(), instance, identifier);
            registry.resolveReferences(id, instance);
        });
    }

    private static Map<String, Class<?>> getAssumedTargetTypes(PendingReferenceRegistry registry) {
        final Map<String, Class<?>> idsToTypes = new HashMap<>();
        registry.getPendingReferences().forEach((id, refs) -> {
            assert refs != null;
            assert !refs.isEmpty();
            final Optional<Class<?>> targetType =
                    refs.stream().filter(ref -> ref.getTargetType().isPresent()).findFirst().flatMap(
                            PendingReference::getTargetType);
            if (targetType.isEmpty()) {
                LOG.debug("No assumed target type found for reference with id '{}'. Skipping the reference.", id);
            } else {
                idsToTypes.put(id, targetType.get());
            }
        });
        return idsToTypes;
    }
}
