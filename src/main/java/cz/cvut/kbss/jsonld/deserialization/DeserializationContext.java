package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.deserialization.reference.PendingReferenceRegistry;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;

import java.util.Map;

public class DeserializationContext<T> {

    private final Class<T> targetType;

    private final Map<String, Object> knownInstances;

    private final TargetClassResolver classResolver;

    private final PendingReferenceRegistry pendingReferenceRegistry;

    public DeserializationContext(Class<T> targetType, Map<String, Object> knownInstances,
                                  TargetClassResolver classResolver, PendingReferenceRegistry pendingReferenceRegistry) {
        this.targetType = targetType;
        this.knownInstances = knownInstances;
        this.classResolver = classResolver;
        this.pendingReferenceRegistry = pendingReferenceRegistry;
    }

    public Class<T> getTargetType() {
        return targetType;
    }

    /**
     * Gets an unmodifiable map of already processed instances.
     *
     * @return Unmodifiable map of identifiers to instances
     */
    public Map<String, Object> getKnownInstances() {
        return knownInstances;
    }

    public TargetClassResolver getClassResolver() {
        return classResolver;
    }

    public PendingReferenceRegistry getPendingReferenceRegistry() {
        return pendingReferenceRegistry;
    }
}
