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
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.GenericMember;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import cz.cvut.kbss.jsonld.exception.UnresolvedReferenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PendingReferenceRegistryTest {

    private final PendingReferenceRegistry sut = new PendingReferenceRegistry();

    @Test
    void addPendingReferenceRegistersTheSpecifiedPendingReferencesForTargetObjectAndField() throws Exception {
        final String iri = Generator.generateUri().toString();
        final Object targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");

        sut.addPendingReference(iri, targetObject, targetField);
        final Map<String, Set<PendingReference>> result = getPendingReferences();
        assertTrue(result.containsKey(iri));
        assertThat(result.get(iri), hasItem(new SingularPendingReference(targetObject, targetField)));
    }

    private Map<String, Set<PendingReference>> getPendingReferences() throws Exception {
        final Field prField = PendingReferenceRegistry.class.getDeclaredField("pendingReferences");
        prField.setAccessible(true);
        return (Map<String, Set<PendingReference>>) prField.get(sut);
    }

    @Test
    void addPendingReferenceRegistersMultiplePendingReferencesForRepeatedInvocationWithSameIdentifier() throws
            Exception {
        final String iri = Generator.generateUri().toString();
        final Object targetObjectOne = new Employee();
        final Field targetFieldOne = Employee.class.getDeclaredField("employer");
        final Object targetObjectTwo = new GenericMember();
        final Field targetFieldTwo = GenericMember.class.getDeclaredField("memberOf");

        sut.addPendingReference(iri, targetObjectOne, targetFieldOne);
        sut.addPendingReference(iri, targetObjectTwo, targetFieldTwo);
        final Map<String, Set<PendingReference>> result = getPendingReferences();
        assertTrue(result.containsKey(iri));
        assertEquals(2, result.get(iri).size());
    }

    @Test
    void verifyNoUnresolvedReferencesExistThrowsUnresolvedReferenceExceptionWhenThereExistPendingReferences() throws
            Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final String iri = Generator.generateUri().toString();
        final Object targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        pendingReferences.put(iri, Collections.singleton(new SingularPendingReference(targetObject, targetField)));
        assertThrows(UnresolvedReferenceException.class, sut::verifyNoUnresolvedReferencesExist);
    }

    @Test
    void verifyNoUnresolvedReferencesExistDoesNothingWhenNoUnresolvedReferencesExist() {
        sut.verifyNoUnresolvedReferencesExist();
    }

    @Test
    void resolveReferencesSetsSpecifiedValueOnPendingReferenceTarget() throws Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final Organization referencedObject = Generator.generateOrganization();
        final String iri = referencedObject.getUri().toString();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        pendingReferences.put(iri, Collections.singleton(new SingularPendingReference(targetObject, targetField)));

        sut.resolveReferences(iri, referencedObject);
        assertEquals(referencedObject, targetObject.getEmployer());
    }

    @Test
    void resolveReferencesRemovesPendingReferencesAfterSuccessfulInvocation() throws Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final Organization referencedObject = Generator.generateOrganization();
        final String iri = referencedObject.getUri().toString();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        pendingReferences.put(iri, Collections.singleton(new SingularPendingReference(targetObject, targetField)));

        sut.resolveReferences(iri, referencedObject);
        assertFalse(pendingReferences.containsKey(iri));
    }

    @Test
    void resolveReferencesThrowsTargetTypeExceptionWhenSpecifiedObjectCannotBeSetOnTargetFieldDueToTypeMismatch() throws
            Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final User referencedObject = Generator.generateUser();
        final String iri = referencedObject.getUri().toString();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        pendingReferences.put(iri, Collections.singleton(new SingularPendingReference(targetObject, targetField)));

        assertThrows(TargetTypeException.class, () -> sut.resolveReferences(iri, referencedObject));
    }

    @Test
    void resolveReferencesSupportsMultiplePendingReferences() throws Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final Organization referencedObject = Generator.generateOrganization();
        final String iri = referencedObject.getUri().toString();
        final Employee targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        final Employee targetTwo = new Employee();
        pendingReferences.put(iri, new HashSet<>(Arrays.asList(
                new SingularPendingReference(targetObject, targetField),
                new SingularPendingReference(targetTwo, targetField))));

        sut.resolveReferences(iri, referencedObject);
        assertEquals(referencedObject, targetObject.getEmployer());
        assertEquals(referencedObject, targetTwo.getEmployer());
    }

    @Test
    void resolveReferencesSupportsAddingReferenceObjectToTargetFieldOfCollectionType() throws Exception {
        final Map<String, Set<PendingReference>> pendingReferences = getPendingReferences();
        final Employee referencedObject = Generator.generateEmployee();
        final String iri = referencedObject.getUri().toString();
        final Organization targetObject = new Organization();
        targetObject.setEmployees(new HashSet<>());
        pendingReferences.put(iri, Collections.singleton(new CollectionPendingReference(targetObject.getEmployees())));

        sut.resolveReferences(iri, referencedObject);
        assertThat(targetObject.getEmployees(), hasItem(referencedObject));
    }
}
