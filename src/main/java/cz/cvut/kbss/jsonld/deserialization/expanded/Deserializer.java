package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class Deserializer<X> {

    final InstanceBuilder instanceBuilder;
    final DeserializerConfig config;

    Deserializer(InstanceBuilder instanceBuilder, DeserializerConfig config) {
        this.instanceBuilder = instanceBuilder;
        this.config = config;
    }

    Configuration configuration() {
        return config.getConfiguration();
    }

    <T> Class<? extends T> resolveTargetClass(Object jsonRoot, Class<T> resultClass) {
        if (BeanClassProcessor.isIdentifierType(resultClass)) {
            return resultClass;
        }
        final List<String> types = getObjectTypes(jsonRoot);
        return config.getTargetResolver().getTargetClass(resultClass, types);
    }

    List<String> getObjectTypes(Object jsonLdObject) {
        assert jsonLdObject instanceof Map;
        final Object types = ((Map<?, ?>) jsonLdObject).get(JsonLd.TYPE);
        if (types == null) {
            return Collections.emptyList();
        }
        assert types instanceof List;
        return (List<String>) types;
    }

    abstract void processValue(X value);
}
