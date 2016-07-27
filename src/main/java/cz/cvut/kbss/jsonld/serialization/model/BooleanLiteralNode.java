package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON boolean literal value.
 */
public class BooleanLiteralNode extends LiteralNode<Boolean> {

    public BooleanLiteralNode(Boolean value) {
        super(value);
    }

    public BooleanLiteralNode(String name, Boolean value) {
        super(name, value);
    }

    @Override
    void writeValue(JsonGenerator writer) throws IOException {
        writer.writeBoolean(value);
    }
}
