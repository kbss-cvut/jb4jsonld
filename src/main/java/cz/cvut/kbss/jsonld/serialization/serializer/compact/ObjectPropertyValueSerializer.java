/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.EnumUtil;
import cz.cvut.kbss.jsonld.exception.InvalidEnumMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Value serializer for object property values.
 */
public class ObjectPropertyValueSerializer implements ValueSerializer {

    private final ObjectGraphTraverser graphTraverser;

    public ObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        this.graphTraverser = graphTraverser;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (value.getClass().isEnum()) {
            return serializeEnumConstant((Enum<?>) value, ctx);
        }
        graphTraverser.traverse(ctx);
        return null;
    }

    private JsonNode serializeEnumConstant(Enum<?> constant, SerializationContext<?> ctx) {
        final String iri = resolveMappedIndividual(constant);
        final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
        node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, iri));
        return node;
    }

    private String resolveMappedIndividual(Enum<?> value) {
        return EnumUtil.findMatchingConstant(value.getDeclaringClass(), (e, iri) -> e == value, (e, iri) -> iri).orElseThrow(
                () -> new InvalidEnumMappingException("Missing individual mapping for enum constant " + value));
    }
}
