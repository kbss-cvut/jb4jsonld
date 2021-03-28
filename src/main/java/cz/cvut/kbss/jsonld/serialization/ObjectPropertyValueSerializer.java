package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Value serializer for object property values.
 */
public class ObjectPropertyValueSerializer implements ValueSerializer {

    private final ObjectGraphTraverser graphTraverser;

    public ObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        this.graphTraverser = graphTraverser;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        graphTraverser.traverse(ctx);
        return null;
    }
}
