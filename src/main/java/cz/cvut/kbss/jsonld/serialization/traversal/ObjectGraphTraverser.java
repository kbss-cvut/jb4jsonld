package cz.cvut.kbss.jsonld.serialization.traversal;

import java.util.*;

public class ObjectGraphTraverser {

    private final Set<InstanceVisitor> visitors = new HashSet<>(4);

    private Map<Object, Object> knownInstances;

    public void addVisitor(InstanceVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitors.add(visitor);
    }

    private void resetKnownInstances() {
        this.knownInstances = new IdentityHashMap<>();
    }

    public void traverse(Object instance) {
        Objects.requireNonNull(instance);
        resetKnownInstances();
        // TODO
    }
}
