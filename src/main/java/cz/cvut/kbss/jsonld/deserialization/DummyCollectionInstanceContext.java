/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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

import java.util.Collection;
import java.util.Map;

/**
 * Simulates a collection context, but does nothing.
 * <p>
 * Can be used e.g. when types are being deserialized, but the target object does not contain a {@link
 * cz.cvut.kbss.jopa.model.annotations.Types} field.
 */
class DummyCollectionInstanceContext extends InstanceContext<Collection<?>> {

    DummyCollectionInstanceContext(Map<String, Object> knownInstances) {
        super(null, knownInstances);
    }

    @Override
    void addItem(Object item) {
        // Do nothing
    }

    @Override
    Class<?> getItemType() {
        return Void.class;
    }
}
