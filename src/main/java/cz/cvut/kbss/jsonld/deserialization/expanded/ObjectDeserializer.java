/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;

import java.lang.reflect.Field;
import java.util.*;

class ObjectDeserializer extends Deserializer<Map<?, ?>> {

    private final String property;
    private final Class<?> targetClass;

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        assert property != null;
        this.property = property;
        this.targetClass = null;
    }

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, Class<?> targetClass) {
        super(instanceBuilder, config);
        assert targetClass != null;
        this.targetClass = targetClass;
        this.property = null;
    }

    @Override
    void processValue(Map<?, ?> value) {
        openObject(value);
        for (Map.Entry<?, ?> e : orderAttributesForProcessing(value).entrySet()) {
            final String property = e.getKey().toString();
            final boolean shouldSkip = shouldSkipProperty(property);
            if (shouldSkip) {
                continue;
            }
            assert e.getValue() instanceof List;
            new CollectionDeserializer(instanceBuilder, config, property).processValue((List<?>) e.getValue());
        }
        instanceBuilder.closeObject();
    }

    private void openObject(Map<?, ?> value) {
        try {
            if (property != null) {
                instanceBuilder.openObject(getId(value), property, getObjectTypes(value));
            } else {
                assert targetClass != null;
                final Class<?> cls = resolveTargetClass(value, targetClass);
                assert targetClass.isAssignableFrom(cls);
                instanceBuilder.openObject(getId(value), cls);
            }
        } catch (UnknownPropertyException e) {
            if (!configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
                throw e;
            }
        }
    }

    private String getId(Map<?, ?> object) {
        return object.containsKey(JsonLd.ID) ? object.get(JsonLd.ID).toString() : IdentifierUtil.generateBlankNodeId();
    }

    private Map<?, ?> orderAttributesForProcessing(Map<?, ?> value) {
        final List<String> propertyOrder = getPropertyOrder();
        if (propertyOrder.isEmpty()) {
            return value;
        }
        final Map result = new LinkedHashMap<>(value.size());
        for (String property : propertyOrder) {
            final Iterator<? extends Map.Entry<?, ?>> it = value.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<?, ?> e = it.next();
                if (property.equals(e.getKey())) {
                    result.put(e.getKey(), e.getValue());
                    it.remove();
                    break;
                }
            }
        }
        result.putAll(value);
        return result;
    }

    private List<String> getPropertyOrder() {
        final Class<?> cls = instanceBuilder.getCurrentContextType();
        if (cls == null) {
            return Collections.emptyList();
        }
        final String[] attributeOrder = BeanAnnotationProcessor.getAttributeOrder(cls);
        if (attributeOrder.length == 0) {
            return Collections.emptyList();
        }
        final List<Field> fields = BeanAnnotationProcessor
                .getMarshallableFields(instanceBuilder.getCurrentContextType());
        final List<String> propertyOrder = new ArrayList<>(attributeOrder.length);
        for (String name : attributeOrder) {
            final Optional<Field> field = fields.stream().filter(f -> f.getName().equals(name)).findFirst();
            if (!field.isPresent()) {
                throw new JsonLdDeserializationException(
                        "Field called " + name + " declared in JsonLdAttributeOrder annotation not found in class " +
                                cls + ".");
            }
            propertyOrder.add(BeanAnnotationProcessor.getAttributeIdentifier(field.get()));
        }
        return propertyOrder;
    }

    private boolean shouldSkipProperty(String property) {
        if (JsonLd.ID.equals(property)) {
            return true;
        }
        if (!instanceBuilder.isPropertyDeserializable(property)) {
            throwUnknownPropertyIfNotIgnored(property);
            return true;
        }
        return false;
    }

    private void throwUnknownPropertyIfNotIgnored(String property) {
        if (!instanceBuilder.isPropertyMapped(property) && !configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
            throw UnknownPropertyException.create(property, instanceBuilder.getCurrentContextType());
        }
    }
}
