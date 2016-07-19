package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON boolean literal value.
 */
public class BooleanLiteral extends Literal<Boolean> {

    public BooleanLiteral(Boolean value) {
        super(value);
    }

    public BooleanLiteral(String name, Boolean value) {
        super(name, value);
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeBoolean(value);
    }
}
