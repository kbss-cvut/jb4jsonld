package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;

/**
 * Represents a single string with a language tag.
 * <p>
 * It is written out as an object with two attributes - {@code @language} and {@code @value} with the corresponding
 * values.
 */
public class LangStringNode extends ObjectNode {

    public LangStringNode(String value, String language) {
        addItems(value, language);
    }

    private void addItems(String value, String language) {
        // @none is a JSON-LD 1.1 feature
        addItem(JsonNodeFactory.createLiteralNode(JsonLd.LANGUAGE, language != null ? language : JsonLd.NONE));
        addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value));
    }

    public LangStringNode(String name, String value, String language) {
        super(name);
        addItems(value, language);
    }
}
