package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;

class DeserializerConfig {

    private final Configuration configuration;
    private final TargetClassResolver targetResolver;

    DeserializerConfig(Configuration configuration, TargetClassResolver targetResolver) {
        this.configuration = configuration;
        this.targetResolver = targetResolver;
    }

    Configuration getConfiguration() {
        return configuration;
    }

    TargetClassResolver getTargetResolver() {
        return targetResolver;
    }
}
