package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ObjectGraphTraverserTest {

    @Mock
    private InstanceVisitor visitor;

    private ObjectGraphTraverser traverser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.traverser = new ObjectGraphTraverser();
        traverser.addVisitor(visitor);
    }

    @Test
    public void traverseVisitsSerializableFieldsOfInstanceWithDataPropertiesOnly() throws Exception {
        final User user = Generator.generateUser();
        traverser.traverse(user);
        final InOrder inOrder = inOrder(visitor);
        // Verify that visitInstance was called before visitField
        inOrder.verify(visitor).visitInstance(user);
        inOrder.verify(visitor, atLeastOnce()).visitField(any(Field.class), any());

        verifyUserFieldsVisited(user);
    }

    private void verifyUserFieldsVisited(User user) throws NoSuchFieldException {
        verify(visitor).visitField(Person.class.getDeclaredField("uri"), user.getUri());
        verify(visitor).visitField(Person.class.getDeclaredField("firstName"), user.getFirstName());
        verify(visitor).visitField(Person.class.getDeclaredField("lastName"), user.getLastName());
        verify(visitor).visitField(User.class.getDeclaredField("username"), user.getUsername());
    }

    @Test
    public void traverseTraversesObjectReferences() throws Exception {
        final Employee employee = Generator.generateEmployee();
        traverser.traverse(employee);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitInstance(employee);
        inOrder.verify(visitor).visitInstance(employee.getEmployer());

        verifyUserFieldsVisited(employee);
        verify(visitor).visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        verify(visitor).visitField(Organization.class.getDeclaredField("uri"), employee.getEmployer().getUri());
        verify(visitor).visitField(Organization.class.getDeclaredField("dateCreated"),
                employee.getEmployer().getDateCreated());
        verify(visitor).visitField(Organization.class.getDeclaredField("name"), employee.getEmployer().getName());
    }

    @Test
    public void traverseTraversesObjectPropertyCollection() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org);
        traverser.traverse(org);

        verify(visitor).visitInstance(org);
        org.getEmployees().forEach(e -> verify(visitor).visitInstance(e));
    }

    private void generateEmployees(Organization org) {
        org.setEmployees(new HashSet<>());
        for (int i = 0; i < Generator.randomCount(10); i++) {
            final Employee emp = new Employee();
            emp.setUri(Generator.generateUri());
            org.getEmployees().add(emp);
        }
    }

    @Test
    public void traverseRecognizesAlreadyVisitedInstances() throws Exception {
        final Employee employee = Generator.generateEmployee();
        // Backward reference
        employee.getEmployer().setEmployees(Collections.singleton(employee));
        traverser.traverse(employee);

        verifyUserFieldsVisited(employee);
        verify(visitor).visitInstance(employee.getEmployer());
        verify(visitor).visitKnownInstance(employee);
    }
}
