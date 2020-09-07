package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;

/**
 * Represents a {@code null} value written in JSON.
 */
public class NullNode extends JsonNode {

    public NullNode() {
        super();
    }

    public NullNode(String name) {
        super(name);
    }

    @Override
    void writeValue(JsonGenerator writer) throws IOException {
        writer.writeNull();
    }
}
