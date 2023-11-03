/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
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
            final String language = value.getLanguage().orElse(null);
            final MultilingualString element = getFirstAvailable(language);
            element.set(language, value.getValue());
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
