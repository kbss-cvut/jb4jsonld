/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpandedJsonLdDeserializer extends JsonLdDeserializer {

    private InstanceBuilder instanceBuilder;

    ExpandedJsonLdDeserializer() {
    }

    ExpandedJsonLdDeserializer(Configuration configuration) {
        super(configuration);
    }

    @Override
    public <T> T deserialize(Object jsonLd, Class<T> resultClass) {
        if (!(jsonLd instanceof List)) {
            throw new JsonLdDeserializationException(
                    "Expanded JSON-LD deserializer requires a JSON-LD array as input.");
        }
        final List<?> input = (List<?>) jsonLd;
        assert input.size() == 1;
        final Map<?, ?> root = (Map<?, ?>) input.get(0);
        this.instanceBuilder = new DefaultInstanceBuilder();
        instanceBuilder.openObject(resultClass);
        processObject(root);
        instanceBuilder.closeObject();
        assert resultClass.isAssignableFrom(instanceBuilder.getCurrentRoot().getClass());
        return resultClass.cast(instanceBuilder.getCurrentRoot());
    }

    private void processObject(Map<?, ?> root) {
        verifyTargetType(root);
        for (Map.Entry<?, ?> e : root.entrySet()) {
            final String property = e.getKey().toString();
            final boolean shouldSkip = verifyPropertyMapping(property);
            if (shouldSkip) {
                continue;
            }
            if (e.getValue() instanceof List) {
                resolveCollectionValue(property, (List<?>) e.getValue());
            } else {
                // Presumably @id
                instanceBuilder.addValue(property, e.getValue());
            }
        }
    }

    private void verifyTargetType(Map<?, ?> jsonLdObject) {
        final Object types = jsonLdObject.get(JsonLd.TYPE);
        final Class<?> targetType = instanceBuilder.getCurrentContextType();
        final String targetTypeIri = BeanAnnotationProcessor.getOwlClass(targetType);
        if (types != null) {
            assert types instanceof List;
            final List<?> typesList = (List<?>) types;
            final Optional<?> type = typesList.stream().filter(t -> t.toString().equals(targetTypeIri)).findAny();
            if (type.isPresent()) {
                return;
            }
        }
        throw new TargetTypeException("Type <" + targetTypeIri + "> mapped by the target Java class " + targetType +
                " not found in input JSON-LD object.");
    }

    private boolean verifyPropertyMapping(String property) {
        if (!instanceBuilder.isPropertyMapped(property)) {
            if (!JsonLd.TYPE.equals(property) && !configure().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
                throw UnknownPropertyException.create(property, instanceBuilder.getCurrentContextType());
            }
            return true;
        }
        return false;
    }

    private void resolveCollectionValue(String property, List<?> value) {
        if (value.size() == 1 && value.get(0) instanceof Map && !instanceBuilder.isPlural(property)) {
            resolvePropertyValue(property, (Map<?, ?>) value.get(0));
        } else {
            instanceBuilder.openCollection(property);
            for (Object item : value) {
                if (item instanceof Map) {
                    resolveValue((Map<?, ?>) item);
                } else {
                    instanceBuilder.addValue(item);
                }
            }
            instanceBuilder.closeCollection();
        }
    }

    private void resolveValue(Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(value.get(JsonLd.VALUE));
        } else {
            final Class<?> elementType = instanceBuilder.getCurrentCollectionElementType();
            instanceBuilder.openObject(elementType);
            processObject(value);
            instanceBuilder.closeObject();
        }
    }

    private void resolvePropertyValue(String property, Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(property, value.get(JsonLd.VALUE));
        } else {
            instanceBuilder.openObject(property);
            processObject(value);
            instanceBuilder.closeObject();
        }
    }
}
