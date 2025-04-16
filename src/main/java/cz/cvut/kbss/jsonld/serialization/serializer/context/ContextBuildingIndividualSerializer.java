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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.EnumUtil;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.IndividualSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingIndividualSerializer extends IndividualSerializer {

    private boolean serializeUsingExtendedDefinition;

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        // Assume term has been already registered in context
        if (serializeUsingExtendedDefinition) {
            if (BeanClassProcessor.isIdentifierType(value.getClass())) {
                return JsonNodeFactory.createStringLiteralNode(ctx.getTerm(), value.toString());
            } else {
                return JsonNodeFactory.createStringLiteralNode(ctx.getTerm(),
                                                               EnumUtil.resolveMappedIndividual((Enum<?>) value));
            }
        }
        return super.serialize(value, ctx);
    }

    @Override
    public void configure(Configuration config) {
        this.serializeUsingExtendedDefinition = config.is(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION);
    }
}
