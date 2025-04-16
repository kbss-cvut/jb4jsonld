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
package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

abstract class TermMappingHolder {

    final TermMappingHolder parentContext;

    TermMappingHolder(TermMappingHolder parentContext) {
        this.parentContext = parentContext;
    }

    abstract boolean canRegisterTermMapping(String term, JsonNode mappedNode);

    abstract void registerTermMapping(String Term, JsonNode node);

    abstract Optional<JsonNode> getTermMapping(String term);

    abstract Map<String, JsonNode> getMapping();

    abstract boolean hasTermMapping(String term);

    abstract boolean hasTermMapping(String term, JsonNode mappedNode);

    abstract boolean isEmpty();

    abstract boolean isRoot();

    public Optional<String> getMappedTerm(String iri) {
        Objects.requireNonNull(iri);
        for (Map.Entry<String, JsonNode> e : getMapping().entrySet()) {
            final Optional<String> id = extractId(e.getValue());
            if (id.isPresent() && iri.equals(id.get())) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> extractId(JsonNode node) {
        assert node instanceof StringLiteralNode || node instanceof ObjectNode;
        if (node instanceof StringLiteralNode) {
            return Optional.of(((StringLiteralNode) node).getValue());
        } else {
            final ObjectNode on = (ObjectNode) node;
            return on.getItems().stream().filter(item -> JsonLd.ID.equals(item.getName()))
                     .map(idNode -> ((StringLiteralNode) idNode).getValue()).findAny();
        }
    }
}
