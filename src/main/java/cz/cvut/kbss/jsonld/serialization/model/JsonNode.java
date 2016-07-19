package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.Objects;

public abstract class JsonNode {

    private final String name;
    private final boolean valueNode;

    JsonNode() {
        this.name = null;
        this.valueNode = true;
    }

    public JsonNode(String name) {
        this.name = Objects.requireNonNull(name);
        this.valueNode = false;
    }

    public String getName() {
        return name;
    }

    public boolean isValueNode() {
        return valueNode;
    }

    public void write(JsonSerializer writer) {
        try {
            if (!valueNode) {
                writeKey(writer);
            }
            writeValue(writer);
        } catch (IOException e) {
            throw new JsonLdSerializationException("Exception during serialization of node " + this, e);
        }
    }

    void writeKey(JsonSerializer writer) throws IOException {
        writer.writeFieldName(name);
    }

    abstract void writeValue(JsonSerializer writer) throws IOException;

    @Override
    public String toString() {
        return name == null ? "{" : "{\"" + name + "\": ";
    }
}
