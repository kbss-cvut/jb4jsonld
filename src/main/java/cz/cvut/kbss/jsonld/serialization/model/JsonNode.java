package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.Objects;

public abstract class JsonNode {

    private final String name;
    private final boolean root;

    JsonNode() {
        this.name = null;
        this.root = true;
    }

    public JsonNode(String name) {
        this.name = Objects.requireNonNull(name);
        this.root = false;
    }

    public void write(JsonSerializer writer) {
        try {
            if (!root) {
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
}
