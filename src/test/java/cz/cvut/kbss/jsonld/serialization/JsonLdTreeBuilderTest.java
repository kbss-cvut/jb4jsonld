package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.model.*;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

public class JsonLdTreeBuilderTest {

    private JsonLdTreeBuilder treeBuilder = new JsonLdTreeBuilder();

    @Test
    public void openInstanceCreatesNewObjectNode() {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
    }

    @Test
    public void openInstancePushesOriginalCurrentToStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Stack<JsonNode> getNodeStack() throws Exception {
        final Field stackField = JsonLdTreeBuilder.class.getDeclaredField("nodeStack");
        stackField.setAccessible(true);
        return (Stack<JsonNode>) stackField.get(treeBuilder);
    }

    @Test
    public void openInstanceDoesNotPushOriginalCurrentToStackWhenItIsAlreadyClosed() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        assertTrue(getNodeStack().isEmpty());
        treeBuilder.closeInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    public void openInstanceAddsSingularTypeAttributeToNode() {
        final Person p = new Person();
        treeBuilder.openInstance(p);
        assertNotNull(treeBuilder.getTreeRoot());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        final CollectionNode typesNode = (CollectionNode) getNode(treeBuilder.getTreeRoot(), Constants.JSON_LD_TYPE);
        assertNotNull(typesNode);
        assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(Vocabulary.PERSON)));
    }

    @Test
    public void openInstanceAddsArrayOfTypesToNode() throws Exception {
        final Employee employee = Generator.generateEmployee();
        treeBuilder.openInstance(employee);
        assertTrue(getNodeStack().isEmpty());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        final Set<String> types = new HashSet<>(Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.EMPLOYEE));
        final CollectionNode typesNode = (CollectionNode) getNode(treeBuilder.getTreeRoot(), Constants.JSON_LD_TYPE);
        assertNotNull(typesNode);
        for (String t : types) {
            assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(t)));
        }
    }

    @Test
    public void openInstanceAddsAttributeValueToItsParentObject() throws Exception {
        final Employee employee = Generator.generateEmployee();
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.openInstance(employee.getEmployer());
        treeBuilder.closeInstance(employee.getEmployer());
        treeBuilder.closeInstance(employee);
        final CompositeNode employerNode = (CompositeNode) getNode(treeBuilder.getTreeRoot(), Vocabulary.IS_MEMBER_OF);
        assertNotNull(employerNode);
    }

    private JsonNode getNode(CompositeNode parent, String name) {
        for (JsonNode n : parent.getItems()) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    @Test
    public void closeInstanceClosesNodeAndDoesNothingWhenStackIsEmpty() throws Exception {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(getNodeStack().isEmpty());
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        treeBuilder.closeInstance(u);
        assertFalse(treeBuilder.getTreeRoot().isOpen());
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    public void closeInstancePopsOriginalCurrentFromStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
        treeBuilder.closeInstance(org);
        assertTrue(getNodeStack().isEmpty());
        assertNotNull(treeBuilder.getTreeRoot());
    }

    @Test
    public void openCollectionCreatesCollectionNode() throws Exception {
        treeBuilder.openCollection(Collections.singleton(Generator.generateEmployee()));
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertNotNull(root);
        assertTrue(root instanceof CollectionNode);
    }

    @Test
    public void visitFieldDoesNothingWhenFieldValueIsNull() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        final Field visitedFieldField = JsonLdTreeBuilder.class.getDeclaredField("visitedField");
        visitedFieldField.setAccessible(true);
        final Field visitedField = (Field) visitedFieldField.get(treeBuilder);
        assertNull(visitedField);
        assertNull(treeBuilder.getTreeRoot());
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    public void visitFieldStoresFieldWhenItIsObjectProperty() throws Exception {
        final Employee employee = Generator.generateEmployee();
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        final Field visitedFieldField = JsonLdTreeBuilder.class.getDeclaredField("visitedField");
        visitedFieldField.setAccessible(true);
        final Field visitedField = (Field) visitedFieldField.get(treeBuilder);
        assertNotNull(visitedField);
        assertEquals(Employee.class.getDeclaredField("employer"), visitedField);
    }

    @Test
    public void visitFieldExtractsValueOfDataPropertyAndAddsNodeToTheRoot() throws Exception {
        final User user = Generator.generateUser();
        treeBuilder.openInstance(user);
        assertNotNull(treeBuilder.getTreeRoot());
        treeBuilder.visitField(Person.class.getDeclaredField("firstName"), user.getFirstName());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        assertTrue(treeBuilder.getTreeRoot().getItems()
                              .contains(JsonNodeFactory.createLiteralNode(Vocabulary.FIRST_NAME, user.getFirstName())));
    }

    @Test
    public void visitFieldExtractsValueOfAnnotationPropertyAndAddsNodeToTheRoot() throws Exception {
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(org);
        treeBuilder.visitField(Organization.class.getDeclaredField("name"), org.getName());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        assertTrue(treeBuilder.getTreeRoot().getItems()
                              .contains(JsonNodeFactory.createLiteralNode(CommonVocabulary.RDFS_LABEL, org.getName())));
    }

    @Test
    public void visitFieldExtractsValueOfPluralDataPropertyAndAddsCollectionNodeWithValuesToTheRoot() throws Exception {
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(org);
        treeBuilder.visitField(Organization.class.getDeclaredField("brands"), org.getBrands());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        final CollectionNode brandsNode = (CollectionNode) getNode(treeBuilder.getTreeRoot(), Vocabulary.BRAND);
        assertNotNull(brandsNode);
        assertTrue(brandsNode instanceof SetNode);
        for (String brand : org.getBrands()) {
            assertTrue(brandsNode.getItems().contains(JsonNodeFactory.createLiteralNode(brand)));
        }
    }

    @Test
    public void visitKnownInstanceAddsSingularObjectIdAttributeToTheParent() throws Exception {
        final Employee employee = Generator.generateEmployee();
        final Organization employer = employee.getEmployer();
        employer.setEmployees(Collections.singleton(employee));
        treeBuilder.openInstance(employer);
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), employer.getEmployees());
        treeBuilder.openCollection(employer.getEmployees());
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.visitKnownInstance(employee.getEmployer());
        final JsonNode referenceNode = getNode(treeBuilder.getTreeRoot(), Vocabulary.IS_MEMBER_OF);
        assertNotNull(referenceNode);
        assertTrue(referenceNode instanceof ObjectIdNode);
    }

    @Test
    public void visitKnownInstanceAddsCollectionOfObjectIdsToTheParent() throws Exception {
        final Employee employee = Generator.generateEmployee();
        final Organization employer = employee.getEmployer();
        employer.setEmployees(Collections.singleton(employee));
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.openInstance(employee.getEmployer());
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), employer.getEmployees());
        treeBuilder.openCollection(employer.getEmployees());
        treeBuilder.visitKnownInstance(employer.getEmployees().iterator().next());
        final CompositeNode node = treeBuilder.getTreeRoot();
        assertTrue(node instanceof CollectionNode);
        final JsonNode item = node.getItems().iterator().next();
        assertTrue(item instanceof ObjectIdNode);
    }

    // TODO test collection of objects as root
}
