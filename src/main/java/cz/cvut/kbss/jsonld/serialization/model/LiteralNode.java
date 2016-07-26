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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralNode<?> that = (LiteralNode<?>) o;

        if (getName() != null && !getName().equals(that.getName()) || getName() == null && that.getName() != null) {
            return false;
        }
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        if (getName() != null) {
            result = 31 * result + getName().hashCode();
        }
        return result;
    }
}
