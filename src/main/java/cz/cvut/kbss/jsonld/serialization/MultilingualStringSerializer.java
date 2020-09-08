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
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }

    private void addTranslationsToCollectionNode(MultilingualString str, CollectionNode target) {
        str.getValue()
           .forEach((lang, val) -> target.addItem(JsonNodeFactory.createLangStringNode(val, lang)));
    }

    JsonNode serialize(MultilingualString value) {
        assert value != null;
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return JsonNodeFactory.createLangStringNode(entry.getValue(), entry.getKey());
        }
        final CollectionNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray();
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }
}
