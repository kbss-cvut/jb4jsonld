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

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;

import java.util.Optional;

/**
 * JSON-LD that does nothing.
 * <p>
 * It can be used in serialization that does not create a JSON-LD context (e.g., expanded, context-less compacted).
 */
public class DummyJsonLdContext implements JsonLdContext, JsonLdContextFactory {

    public static final DummyJsonLdContext INSTANCE = new DummyJsonLdContext();

    @Override
    public void registerTermMapping(String term, String iri) {
        // Do nothing
    }

    @Override
    public void registerTermMapping(String term, ObjectNode mappedNode) {
        // Do nothing
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return Optional.empty();
    }

    @Override
    public boolean hasTermMapping(String term) {
        return false;
    }

    @Override
    public Optional<String> getMappedTerm(String iri) {
        return Optional.empty();
    }

    @Override
    public boolean isCurrentEmpty() {
        return true;
    }

    @Override
    public ObjectNode getContextNode() {
        return new ObjectNode(JsonLd.CONTEXT);
    }

    @Override
    public JsonLdContext createJsonLdContext() {
        return INSTANCE;
    }

    @Override
    public JsonLdContext createJsonLdContext(JsonLdContext parent) {
        return INSTANCE;
    }
}
