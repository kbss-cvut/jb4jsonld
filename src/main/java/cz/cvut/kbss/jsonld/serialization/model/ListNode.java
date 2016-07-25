package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a node serialized as a JSON-LD list.
 * <p>
 * I.e., it is serialized as an object with a single attribute - {@code @list} and its value is a JSON array.
 */
public class ListNode extends CollectionNode {

    public ListNode() {
    }

    public ListNode(String name) {
        super(name);
    }

    @Override
    Collection<JsonNode> initItems() {
        return new ArrayList<>();
    }

    @Override
    void writeValue(final JsonSerializer writer) throws IOException {
        writer.writeObjectStart();
        writer.writeFieldName(Constants.JSON_LD_LIST);
        writer.writeArrayStart();
        items.forEach(item -> item.write(writer));
        writer.writeArrayEnd();
        writer.writeObjectEnd();
    }
}
