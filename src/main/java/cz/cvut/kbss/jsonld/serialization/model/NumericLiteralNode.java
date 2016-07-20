package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON numeric literal value.
 */
public class NumericLiteralNode<T extends Number> extends LiteralNode<T> {

    public NumericLiteralNode(T value) {
        super(value);
    }

    public NumericLiteralNode(String name, T value) {
        super(name, value);
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeNumber(value);
    }
}
