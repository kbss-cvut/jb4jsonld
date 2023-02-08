package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.ObjectIdNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextBuildingIdentifierSerializerTest {

    private final ContextBuildingIdentifierSerializer sut = new ContextBuildingIdentifierSerializer();

    @Test
    void serializeUsesIdentifierTermRegisteredInContextToBuildJsonNode() {
        final String id = Generator.generateUri().toString();
        final MappingJsonLdContext jsonLdCtx = new MappingJsonLdContext();
        final String fieldName = "uri";
        jsonLdCtx.registerTermMapping(fieldName, JsonLd.ID);
        final SerializationContext<String> ctx = new SerializationContext<>(JsonLd.ID, null, id, jsonLdCtx);

        final ObjectIdNode result = sut.serialize(id, ctx);
        assertEquals(fieldName, result.getName());
        assertEquals(id, result.getIdentifier());
    }
}