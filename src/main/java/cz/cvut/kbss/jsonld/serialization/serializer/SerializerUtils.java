package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
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

    /**
     * Serializes the specified value as a JSON object with value ({@link JsonLd#VALUE}) and type ({@link
     * JsonLd#TYPE}).
     *
     * @param term  Term to identify the object in the enclosing object
     * @param value Value to serialize
     * @param type  Value type to use
     * @return Resulting JSON node
     */
    public static JsonNode createdTypedValueNode(String term, String value, String type) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(term);
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, type));
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value));
        return node;
    }
}
