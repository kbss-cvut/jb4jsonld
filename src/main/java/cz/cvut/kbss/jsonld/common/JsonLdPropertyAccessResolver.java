/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.annotation.JsonLdProperty;

import java.lang.reflect.Field;
import java.util.Objects;

import static cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor.isInstanceIdentifier;
import static cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor.isTypesField;

/**
 * Resolves property access based on the {@link cz.cvut.kbss.jsonld.annotation.JsonLdProperty} annotation value.
 */
public class JsonLdPropertyAccessResolver implements PropertyAccessResolver {

    @Override
    public boolean isReadable(Field field) {
        Objects.requireNonNull(field);
        final JsonLdProperty annotation = field.getAnnotation(JsonLdProperty.class);
        return annotation == null || annotation.access() != JsonLdProperty.Access.WRITE_ONLY ||
                isInstanceIdentifier(field) || isTypesField(field);
    }

    @Override
    public boolean isWriteable(Field field) {
        Objects.requireNonNull(field);
        final JsonLdProperty annotation = field.getAnnotation(JsonLdProperty.class);
        return annotation == null || annotation.access() != JsonLdProperty.Access.READ_ONLY;
    }
}
