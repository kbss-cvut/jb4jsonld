package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Utilities for serializers.
 */
public class SerializerUtils {

    private SerializerUtils() {
        throw new AssertionError("No instances for you!");
    }

    /**
     * Checks whether the specified value in the specified context is an annotation property value referencing an
     * individual (resource).
     *
     * @param value Value to examine
     * @param ctx   Serialization context
     * @return {@code true} if the value is an annotation property value reference, {@code false} otherwise
     */
    public static boolean isAnnotationReference(Object value, SerializationContext<?> ctx) {
        return BeanAnnotationProcessor.isAnnotationProperty(ctx.getField()) && BeanClassProcessor.isIdentifierType(
                value.getClass()) && !(value instanceof String);
    }

    /**
     * Creates a term definition node containing identifier and type attributes.
     * @param term Term whose definition to create
     * @param id Mapped term identifier (IRI)
     * @param type Type of the mapped term
     * @return Term definition node
     */
    public static ObjectNode createTypedTermDefinition(String term, String id, String type) {
        final ObjectNode termDef = JsonNodeFactory.createObjectNode(term);
        termDef.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, id));
        termDef.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, type));
        return termDef;
    }
}
