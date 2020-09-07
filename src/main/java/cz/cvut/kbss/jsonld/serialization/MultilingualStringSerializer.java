package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Map;

/**
 * This use used to serialize {@link MultilingualString} values.
 */
class MultilingualStringSerializer {

    JsonNode serialize(String attName, MultilingualString value) {
        assert value != null;
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return JsonNodeFactory.createLangStringNode(attName, entry.getValue(), entry.getKey());
        }
        final CollectionNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray(attName);
        value.getValue()
             .forEach((lang, val) -> collectionNode.addItem(JsonNodeFactory.createLangStringNode(val, lang)));
        return collectionNode;
    }

    JsonNode serialize(MultilingualString value) {
        assert value != null;
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return JsonNodeFactory.createLangStringNode(entry.getValue(), entry.getKey());
        }
        final CollectionNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray();
        value.getValue()
             .forEach((lang, val) -> collectionNode.addItem(JsonNodeFactory.createLangStringNode(val, lang)));
        return collectionNode;
    }
}
