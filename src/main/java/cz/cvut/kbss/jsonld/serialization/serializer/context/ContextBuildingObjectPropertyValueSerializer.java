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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.ObjectPropertyValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;

public class ContextBuildingObjectPropertyValueSerializer extends ObjectPropertyValueSerializer {

    private boolean serializeUsingExtendedDefinition;

    public ContextBuildingObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        super(graphTraverser);
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (ctx.getTerm() != null) {
            registerTermDefinition(ctx);
        }
        return super.serialize(value, ctx);
    }

    private void registerTermDefinition(SerializationContext<?> ctx) {
        if (serializeUsingExtendedDefinition && isIndividual(ctx)) {
            ctx.registerTermMapping(ctx.getFieldName(),
                                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(),
                                                                              JsonLd.ID));
        } else {
            ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        }
    }

    private static boolean isIndividual(SerializationContext<?> ctx) {
        if (BeanClassProcessor.isIndividualType(ctx.getValue().getClass())) {
            return true;
        }
        if (ctx.getValue() instanceof Collection) {
            final Collection<?> c = (Collection<?>) ctx.getValue();
            for (Object elem : c) {
                if (elem != null) {
                    return BeanClassProcessor.isIndividualType(elem.getClass());
                }
            }
        }
        return false;
    }

    @Override
    public void configure(Configuration config) {
        this.serializeUsingExtendedDefinition = config.is(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION);
    }
}
