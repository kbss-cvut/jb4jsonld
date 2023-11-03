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
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingularPendingReferenceTest {

    @Test
    void applySetsSpecifiedReferencedObjectAsTargetFieldValue() throws Exception {
        final Organization referencedObject = Generator.generateOrganization();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        final PendingReference sut = new SingularPendingReference(targetObject, targetField);

        sut.apply(referencedObject);
        assertEquals(referencedObject, targetObject.getEmployer());
    }

    @Test
    void applyThrowsTargetTypeExceptionWhenSpecifiedObjectCannotBeSetOnTargetFieldDueToTypeMismatch() throws Exception {
        final User referencedObject = Generator.generateUser();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        final PendingReference sut = new SingularPendingReference(targetObject, targetField);

        assertThrows(TargetTypeException.class, () -> sut.apply(referencedObject));
    }

}
