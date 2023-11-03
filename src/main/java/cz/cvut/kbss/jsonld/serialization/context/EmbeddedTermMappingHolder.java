/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.*;

class EmbeddedTermMappingHolder extends TermMappingHolder {

    private final Map<String, JsonNode> mapping = new HashMap<>();

    EmbeddedTermMappingHolder() {
        super(DummyTermMappingHolder.INSTANCE);
    }

    EmbeddedTermMappingHolder(TermMappingHolder parentContext) {
        super(parentContext);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return true;
    }

    @Override
    void registerTermMapping(String term, JsonNode mappedNode) {
        Objects.requireNonNull(term);
        Objects.requireNonNull(mappedNode);
        if (parentContext.hasTermMapping(term, mappedNode)) {
            // Already mapped in parent
            return;
        }
        if (!isRoot() && !parentContext.hasTermMapping(term)) {
            parentContext.registerTermMapping(term, mappedNode);
        } else {
            verifyMappingUnique(term, mappedNode);
            mapping.put(term, mappedNode);
        }
    }

    boolean isRoot() {
        return parentContext == DummyTermMappingHolder.INSTANCE;
    }

    private void verifyMappingUnique(String term, JsonNode value) {
        if (mapping.containsKey(term) && !Objects.equals(mapping.get(term), value)) {
            throw new AmbiguousTermMappingException("Context already contains mapping for term '" + term + "'.");
        }
    }

    @Override
    public Map<String, JsonNode> getMapping() {
        return Collections.unmodifiableMap(mapping);
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return mapping.containsKey(term) ? Optional.of(mapping.get(term)) : parentContext.getTermMapping(term);
    }

    @Override
    public boolean hasTermMapping(String term) {
        return mapping.containsKey(term) || parentContext.hasTermMapping(term);
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return mapping.containsKey(term) && mapping.get(term).equals(mappedNode);
    }

    @Override
    public Optional<String> getMappedTerm(String iri) {
        final Optional<String> result = super.getMappedTerm(iri);
        if (result.isPresent()) {
            return result;
        }
        return parentContext.getMappedTerm(iri);
    }

    @Override
    boolean isEmpty() {
        return mapping.isEmpty();
    }
}
