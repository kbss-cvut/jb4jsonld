package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;

import java.util.Map;

public class MultilingualStringContext extends InstanceContext<MultilingualString> {

    MultilingualStringContext(MultilingualString instance, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
    }

    @Override
    void addItem(Object item) {
        assert item != null;
        if (item instanceof LangString) {
            final LangString ls = (LangString) item;
            instance.set(ls.getLanguage(), ls.getValue());
        } else {
            instance.set(item.toString());
        }
    }
}
