package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a JSON object node.
 * <p>
 * I.e. it is a set of key-value pairs, which constitute the state of the object.
 */
public class ObjectNode extends JsonNode {

    private final Collection<JsonNode> children = new ArrayList<>();

    public ObjectNode() {
    }

    public ObjectNode(String name) {
        super(name);
    }

    public void addChild(JsonNode child) {
        Objects.requireNonNull(child);
        children.add(child);
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeObjectStart();
        children.forEach(child -> child.write(writer));
        writer.writeObjectEnd();
    }

    @Override
    public String toString() {
        return super.toString() + children + "}";
    }
}
