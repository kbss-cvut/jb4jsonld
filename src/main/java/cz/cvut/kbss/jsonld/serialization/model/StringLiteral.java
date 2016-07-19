package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON string literal value.
 */
public class StringLiteral extends Literal<String> {

    // TODO add support for strings with language tag


    public StringLiteral(String text) {
        super(text);
    }

    public StringLiteral(String name, String text) {
        super(name, text);
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeString(value);
    }
}
