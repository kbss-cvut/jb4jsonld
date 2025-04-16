/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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

import java.util.Collection;

/**
 * JSON node representing a collection.
 *
 * @param <T> Node type
 */
public abstract class CollectionNode<T extends Collection<JsonNode>> extends CompositeNode<T> {

    CollectionNode() {
    }

    CollectionNode(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + items + "}";
    }
}
