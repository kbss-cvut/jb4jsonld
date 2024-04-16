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
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;

import java.util.Optional;

/**
 * Represents the {@literal @context} JSON-LD attribute.
 * <p>
 * Note that this represents the JSON-LD 1.0 context, so no <a href="https://www.w3.org/TR/json-ld/#scoped-contexts">context
 * scoping</a> is supported and attempting to register a different mapping for an already existing term will result in
 * an {@link AmbiguousTermMappingException}.
 */
public class MappingJsonLdContext implements JsonLdContext {

    private TermMappingHolder mappingHolder;

    public MappingJsonLdContext() {
        this.mappingHolder = new EmbeddedTermMappingHolder();
    }

    public MappingJsonLdContext(JsonLdContext parent) {
        assert parent instanceof MappingJsonLdContext;
        this.mappingHolder = new WriteThroughTermMappingHolder(((MappingJsonLdContext) parent).mappingHolder);
    }

    @Override
    public void registerTermMapping(String term, String iri) {
        final JsonNode value = JsonNodeFactory.createStringLiteralNode(term, iri);
        if (!mappingHolder.canRegisterTermMapping(term, value) && !mappingHolder.isRoot()) {
            this.mappingHolder = new EmbeddedTermMappingHolder(mappingHolder);
        }
        mappingHolder.registerTermMapping(term, value);
    }

    @Override
    public void registerTermMapping(String term, ObjectNode mappedNode) {
        if (!mappingHolder.canRegisterTermMapping(term, mappedNode) && !mappingHolder.isRoot()) {
            this.mappingHolder = new EmbeddedTermMappingHolder(mappingHolder);
        }
        mappingHolder.registerTermMapping(term, mappedNode);
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return mappingHolder.getTermMapping(term);
    }

    @Override
    public boolean hasTermMapping(String term) {
        return mappingHolder.hasTermMapping(term);
    }

    @Override
    public Optional<String> getMappedTerm(String iri) {
        return mappingHolder.getMappedTerm(iri);
    }

    @Override
    public boolean isCurrentEmpty() {
        return mappingHolder.isEmpty();
    }

    @Override
    public ObjectNode getContextNode() {
        final ObjectNode node = new ObjectNode(JsonLd.CONTEXT);
        mappingHolder.getMapping().values().forEach(node::addItem);
        return node;
    }
}
