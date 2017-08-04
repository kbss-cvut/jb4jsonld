package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Serializes field value.
 */
interface FieldSerializer {

    /**
     * Serializes the specified field, returning a list of JSON-LD nodes representing it.
     * <p>
     * The result is a list because maps (e.g. {@link cz.cvut.kbss.jopa.model.annotations.Properties}) cannot be
     * serialized as a single attribute.
     *
     * @param field The field to serialize
     * @param value Value of the field
     * @return Serialization result
     */
    List<JsonNode> serializeField(Field field, Object value);
}
