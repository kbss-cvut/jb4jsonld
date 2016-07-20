package cz.cvut.kbss.jsonld.serialization.model;

import java.util.Collection;

public abstract class CollectionNode extends JsonNode {

    Collection<JsonNode> items;

    CollectionNode() {
    }

    CollectionNode(String name) {
        super(name);
    }

    public void addItem(JsonNode item) {
        assert items != null;
        items.add(item);
    }

    @Override
    public String toString() {
        return super.toString() + items + "}";
    }
}
