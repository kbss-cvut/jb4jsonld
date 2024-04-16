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
package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DummyTermMappingHolder extends TermMappingHolder {

    static final DummyTermMappingHolder INSTANCE = new DummyTermMappingHolder();

    private DummyTermMappingHolder() {
        super(null);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return true;
    }

    @Override
    void registerTermMapping(String Term, JsonNode node) {
        // Do nothing
    }

    @Override
    Optional<JsonNode> getTermMapping(String term) {
        return Optional.empty();
    }

    @Override
    Map<String, JsonNode> getMapping() {
        return Collections.emptyMap();
    }

    @Override
    boolean hasTermMapping(String term) {
        return false;
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return false;
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    boolean isRoot() {
        return true;
    }
}
