package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;

import java.util.Collection;
import java.util.Map;

class MultilingualStringCollectionContext<T extends Collection<MultilingualString>> extends InstanceContext<T> {

    MultilingualStringCollectionContext(T instance, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
    }

    @Override
    void addItem(Object item) {
        assert item != null;
        if (item instanceof LangString) {
            final LangString value = (LangString) item;
            final MultilingualString element = getFirstAvailable(value.getLanguage());
            element.set(value.getLanguage(), value.getValue());
        } else {
            final MultilingualString element = getFirstAvailable(null);
            element.set(item.toString());
        }
    }

    private MultilingualString getFirstAvailable(String language) {
        return instance.stream().filter(ms -> !ms.contains(language)).findFirst().orElseGet(() -> {
            final MultilingualString newOne = new MultilingualString();
            instance.add(newOne);
            return newOne;
        });
    }

    @Override
    Class<?> getItemType() {
        return MultilingualString.class;
    }
}
