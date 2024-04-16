/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
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

        return (getName() == null || getName().equals(that.getName())) &&
                (getName() != null || that.getName() == null) && value.equals(that.value);

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
