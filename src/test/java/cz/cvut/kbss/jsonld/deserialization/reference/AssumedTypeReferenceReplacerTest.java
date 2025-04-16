/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssumedTypeReferenceReplacerTest {

    private final PendingReferenceRegistry registry = new PendingReferenceRegistry();

    private final AssumedTypeReferenceReplacer sut = new AssumedTypeReferenceReplacer();

    @Test
    void replacePendingReferencesWithAssumedTypedObjectsReplacesPendingSingularReferenceWithObjectWithId() throws Exception {
        final URI id = Generator.generateUri();
        final Employee target = Generator.generateEmployee();
        target.setEmployer(null);
        registry.addPendingReference(id.toString(), target, Employee.getEmployerField());
        sut.replacePendingReferencesWithAssumedTypedObjects(registry);
        assertNotNull(target.getEmployer());
        assertEquals(id, target.getEmployer().getUri());
    }

    @Test
    void replacePendingReferenceReplacesAllReferencesWithSameObject() throws Exception {
        final URI id = Generator.generateUri();
        final Employee targetOne = Generator.generateEmployee();
        targetOne.setEmployer(null);
        registry.addPendingReference(id.toString(), targetOne, Employee.getEmployerField());
        final Employee targetTwo = Generator.generateEmployee();
        targetTwo.setEmployer(null);
        registry.addPendingReference(id.toString(), targetTwo, Employee.getEmployerField());
        sut.replacePendingReferencesWithAssumedTypedObjects(registry);
        assertNotNull(targetOne.getEmployer());
        assertEquals(id, targetOne.getEmployer().getUri());
        assertNotNull(targetTwo.getEmployer());
        assertSame(targetOne.getEmployer(), targetTwo.getEmployer());
    }

    @Test
    void replacePendingReferencesThrowsUnknownPropertyExceptionWhenTargetTypeDoesNotHaveIdentifierField() throws Exception {
        final URI id = Generator.generateUri();
        final Employee target = Generator.generateEmployee();
        registry.addPendingReference(id.toString(), target, User.getUsernameField());
        assertThrows(UnknownPropertyException.class, () -> sut.replacePendingReferencesWithAssumedTypedObjects(registry));
    }

    @Test
    void replacePendingReferencesSkipsPendingReferencesWithoutClearTargetType() {
        final URI id = Generator.generateUri();
        registry.addPendingReference(id.toString(), new ArrayList<>());

        sut.replacePendingReferencesWithAssumedTypedObjects(registry);
        assertFalse(registry.getPendingReferences().isEmpty());
    }
}