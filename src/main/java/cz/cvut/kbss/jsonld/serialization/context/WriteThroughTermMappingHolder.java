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

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Map;
import java.util.Optional;

class WriteThroughTermMappingHolder extends TermMappingHolder {

    public WriteThroughTermMappingHolder(TermMappingHolder parentContext) {
        super(parentContext);
    }

    @Override
    boolean hasTermMapping(String term) {
        return parentContext.hasTermMapping(term);
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return parentContext.hasTermMapping(term, mappedNode);
    }

    @Override
    public void registerTermMapping(String term, JsonNode mappedNode) {
        assert canRegisterTermMapping(term, mappedNode);
        parentContext.registerTermMapping(term, mappedNode);
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return parentContext.getTermMapping(term);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return !parentContext.hasTermMapping(term) || parentContext.getTermMapping(term).get().equals(mappedNode);
    }

    @Override
    Map<String, JsonNode> getMapping() {
        return parentContext.getMapping();
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    boolean isRoot() {
        return false;
    }
}
