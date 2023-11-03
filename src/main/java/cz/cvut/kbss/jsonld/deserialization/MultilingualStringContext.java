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
            instance.set(ls.getLanguage().orElse(null), ls.getValue());
        } else {
            instance.set(item.toString());
        }
    }

    /**
     * Returns {@link LangString} as item type supported by this context.
     *
     * This is because a {@link MultilingualString} is essentially a container for a collection of translations of the
     * same string, and it simplifies client code.
     * @return {@code LangString} class
     */
    @Override
    Class<?> getItemType() {
        return LangString.class;
    }
}
