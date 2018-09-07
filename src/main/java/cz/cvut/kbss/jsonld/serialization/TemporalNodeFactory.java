package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.model.LiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;

import java.util.Date;

class TemporalNodeFactory {

    private TemporalNodeFactory() {
        throw new AssertionError();
    }

    static LiteralNode createLiteralNode(String name, Object value) {
        assert value != null;
        if (value instanceof Date) {
            final Date date = (Date) value;
            return name != null ? new NumericLiteralNode<>(name, date.getTime()) :
                   new NumericLiteralNode<>(date.getTime());
        } else {
            throw new JsonLdSerializationException(
                    "Unsupported temporal type " + value.getClass() + " of value " + value);
        }
    }
}
