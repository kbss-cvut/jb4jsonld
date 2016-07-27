package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON string literal value.
 */
public class StringLiteralNode extends LiteralNode<String> {

    // TODO add support for strings with language tag


    public StringLiteralNode(String text) {
        super(text);
    }

    public StringLiteralNode(String name, String text) {
        super(name, text);
    }

    @Override
    void writeValue(JsonGenerator writer) throws IOException {
        writer.writeString(value);
    }
}
