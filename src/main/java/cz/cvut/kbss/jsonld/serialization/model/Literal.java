package cz.cvut.kbss.jsonld.serialization.model;

import java.util.Objects;

abstract class Literal<T> extends JsonNode {

    final T value;

    public Literal(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public Literal(String name, T value) {
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
