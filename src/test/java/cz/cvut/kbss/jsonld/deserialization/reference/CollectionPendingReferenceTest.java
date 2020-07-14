package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

class CollectionPendingReferenceTest {

    @Test
    void applyInsertsSpecifiedReferencedObjectIntoTargetCollection() {
        final Set<Employee> target = new HashSet<>();
        final Employee referencedObject = Generator.generateEmployee();
        final PendingReference sut = new CollectionPendingReference(target);
        sut.apply(referencedObject);
        assertThat(target, hasItem(referencedObject));
    }
}
