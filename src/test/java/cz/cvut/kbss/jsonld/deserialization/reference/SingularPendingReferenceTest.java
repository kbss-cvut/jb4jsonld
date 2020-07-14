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
