package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
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
            if (e.getValue() instanceof List) {
                new CollectionDeserializer(instanceBuilder, config, property).processValue((List<?>) e.getValue());
            } else {
                // Presumably @id
                instanceBuilder.addValue(property, e.getValue());
            }
        }
        instanceBuilder.closeObject();
    }

    private void openObject(Map<?, ?> value) {
        if (property != null) {
            instanceBuilder.openObject(property, getObjectTypes(value));
        } else {
            assert targetClass != null;
            final Class<?> cls = resolveTargetClass(value, targetClass);
            assert targetClass.isAssignableFrom(cls);
            instanceBuilder.openObject(cls);
        }
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
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(instanceBuilder.getCurrentRoot());
        final List<String> propertyOrder = new ArrayList<>(attributeOrder.length);
        for (String name : attributeOrder) {
            final Optional<Field> field = fields.stream().filter(f -> f.getName().equals(name)).findFirst();
            if (!field.isPresent()) {
                throw new JsonLdDeserializationException(
                        "Field called " + name + " declared in JsonLdAttributeOrder annotation not found in class " + cls + " .");
            }
            propertyOrder.add(BeanAnnotationProcessor.getAttributeIdentifier(field.get()));
        }
        return propertyOrder;
    }

    private boolean shouldSkipProperty(String property) {
        if (!instanceBuilder.isPropertyMapped(property)) {
            if (configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
                return true;
            }
            throw UnknownPropertyException.create(property, instanceBuilder.getCurrentContextType());
        }
        return false;
    }
}
