package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Represents a node serialized as a simple JSON array.
 * <p>
 * Note that in JSON-LD, the JSON array represents a set and is thus unordered.
 */
public class SetNode extends CollectionNode {

    public SetNode() {
    }

    public SetNode(String name) {
        super(name);
    }

    @Override
    Collection<JsonNode> initItems() {
        return new LinkedHashSet<>();
    }

    @Override
    void writeValue(final JsonSerializer writer) throws IOException {
        writer.writeArrayStart();
        items.forEach(item -> item.write(writer));
        writer.writeArrayEnd();
    }
}
