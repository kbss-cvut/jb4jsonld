package cz.cvut.kbss.jsonld.serialization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class CompositeNode extends JsonNode {

    final Collection<JsonNode> items;
    private boolean open;

    public CompositeNode() {
        this.items = initItems();
        this.open = true;
    }

    public CompositeNode(String name) {
        super(name);
        this.items = initItems();
        this.open = true;
    }

    abstract Collection<JsonNode> initItems();

    public void addItem(JsonNode item) {
        Objects.requireNonNull(item);
        items.add(item);
    }

    public Collection<JsonNode> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public void close() {
        this.open = false;
    }

    public boolean isOpen() {
        return open;
    }
}
