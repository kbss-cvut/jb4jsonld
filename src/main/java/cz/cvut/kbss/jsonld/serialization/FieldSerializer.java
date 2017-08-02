package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.List;

interface FieldSerializer {

    List<JsonNode> serializeField(Field field, Object value);
}
