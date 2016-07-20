package cz.cvut.kbss.jsonld.serialization.model;

import java.util.Objects;

public abstract class LiteralNode<T> extends JsonNode {

    final T value;

    public LiteralNode(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public LiteralNode(String name, T value) {
        super(name);
        this.value = Objects.requireNonNull(value);
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + value + "}";
    }
}
