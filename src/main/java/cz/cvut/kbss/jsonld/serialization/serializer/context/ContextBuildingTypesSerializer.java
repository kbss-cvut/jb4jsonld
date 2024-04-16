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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonGenerator;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.SetNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class ContextBuildingTypesSerializer implements ValueSerializer<Set<String>> {

    @Override
    public JsonNode serialize(Set<String> value, SerializationContext<Set<String>> ctx) {
        final CollectionNode<?> typesNode;
        if (ctx.getField() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), JsonLd.TYPE);
            typesNode = JsonNodeFactory.createSetNode(ctx.getTerm());
        } else {
            typesNode = new ContextBasedTypesNode(ctx);
        }
        value.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        return typesNode;
    }

    /**
     * JSON node representing types ({@link JsonLd#TYPE}).
     * <p>
     * Allows overriding the default JSON attribute when a term mapping for types is provided deeper in the object
     * graph. For example, when an instance without a {@link cz.cvut.kbss.jopa.model.annotations.Types} is serialized,
     * its type would be serialized in the {@link JsonLd#TYPE} attribute. But if it also references an object with a
     * {@link cz.cvut.kbss.jopa.model.annotations.Types} field, the field name will be used for term mapping in the
     * context. This new name is applied to the types attribute representing the parent's type.
     */
    private static class ContextBasedTypesNode extends SetNode {

        private final SerializationContext<Set<String>> ctx;

        private ContextBasedTypesNode(SerializationContext<Set<String>> ctx) {
            super(JsonLd.TYPE);
            this.ctx = ctx;
        }

        @Override
        protected void writeKey(JsonGenerator writer) throws IOException {
            final Optional<String> typesTerm = ctx.getMappedTerm(JsonLd.TYPE);
            if (typesTerm.isPresent()) {
                writer.writeFieldName(typesTerm.get());
            } else {
                super.writeKey(writer);
            }
        }
    }
}
