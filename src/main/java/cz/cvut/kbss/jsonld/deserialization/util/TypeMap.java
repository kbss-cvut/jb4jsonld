/**
 * Copyright (C) 2017 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.util;

import java.util.*;

/**
 * Represents a map of type IRIs to their mapped Java classes.
 * <p>
 * Used by the deserialization when determining target class from JSON-LD object types.
 * <p>
 * This class is not synchronized, as it is expected that types will be registered by one thread once and then only queried.
 */
public class TypeMap {

    private final Map<String, Set<Class<?>>> typeMap = new HashMap<>();

    public synchronized void register(String type, Class<?> cls) {
        if (!typeMap.containsKey(type)) {
            // There will usually be only one class, so make the map as small as possible
            typeMap.put(type, new HashSet<>(2));
        }
        typeMap.get(type).add(cls);
    }

    public Set<Class<?>> get(String type) {
        return typeMap.getOrDefault(type, Collections.emptySet());
    }
}
