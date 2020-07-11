package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.GenericObject;
import cz.cvut.kbss.jsonld.exception.UnresolvedReferenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
        assertThat(result.get(iri), hasItem(new PendingReference(targetObject, targetField)));
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
        final Object targetObjectTwo = new GenericObject();
        final Field targetFieldTwo = GenericObject.class.getDeclaredField("memberOf");

        sut.addPendingReference(iri, targetObjectOne, targetFieldOne);
        sut.addPendingReference(iri, targetObjectTwo, targetFieldTwo);
        final Map<String, Set<PendingReference>> result = getPendingReferences();
        assertTrue(result.containsKey(iri));
        assertEquals(2, result.get(iri).size());
    }

    @Test
    void verifyNoUnresolvedReferencesExistThrowsUnresolvedReferenceExceptionWhenThereExistPendingReferences() throws
                                                                                                              Exception {
        final Map<String, Set<PendingReference>> result = getPendingReferences();
        final String iri = Generator.generateUri().toString();
        final Object targetObject = new Employee();
        final Field targetField = Employee.class.getDeclaredField("employer");
        result.put(iri, Collections.singleton(new PendingReference(targetObject, targetField)));
        assertThrows(UnresolvedReferenceException.class, sut::verifyNoUnresolvedReferencesExist);
    }

    @Test
    void verifyNoUnresolvedReferencesExistDoesNothingWhenNoUnresolvedReferencesExist() {
        sut.verifyNoUnresolvedReferencesExist();
    }
}
