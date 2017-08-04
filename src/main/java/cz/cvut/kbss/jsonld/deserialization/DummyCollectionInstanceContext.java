package cz.cvut.kbss.jsonld.deserialization;

import java.util.Collection;
import java.util.Map;

/**
 * Simulates a collection context, but does nothing.
 * <p>
 * Can be used e.g. when types are being deserialized, but the target object does not contain a {@link
 * cz.cvut.kbss.jopa.model.annotations.Types} field.
 */
class DummyCollectionInstanceContext extends InstanceContext<Collection<?>> {

    DummyCollectionInstanceContext(Map<String, Object> knownInstances) {
        super(null, knownInstances);
    }

    @Override
    void addItem(Object item) {
        // Do nothing
    }

    @Override
    Class<?> getItemType() {
        return Void.class;
    }
}
