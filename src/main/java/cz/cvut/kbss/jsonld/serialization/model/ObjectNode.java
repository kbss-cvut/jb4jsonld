package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a JSON object node.
 * <p>
 * I.e. it is a set of key-value pairs, which constitute the state of the object.
 */
public class ObjectNode extends CompositeNode {

    public ObjectNode() {
    }

    public ObjectNode(String name) {
        super(name);
    }

    @Override
    Collection<JsonNode> initItems() {
        return new ArrayList<>();
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeObjectStart();
        items.forEach(child -> child.write(writer));
        writer.writeObjectEnd();
    }

    @Override
    public String toString() {
        return super.toString() + items + "}";
    }
}
