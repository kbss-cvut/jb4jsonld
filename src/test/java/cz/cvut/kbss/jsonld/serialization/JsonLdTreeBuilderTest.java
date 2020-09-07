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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.serialization.model.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JsonLdTreeBuilderTest {

    private final JsonLdTreeBuilder treeBuilder = new JsonLdTreeBuilder();

    @Test
    void openInstanceCreatesNewObjectNode() {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
    }

    @Test
    void openInstancePushesOriginalCurrentToStack() throws Exception {
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
    void openInstanceDoesNotPushOriginalCurrentToStackWhenItIsAlreadyClosed() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        assertTrue(getNodeStack().isEmpty());
        treeBuilder.closeInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    void visitTypesAddsSingularTypeAttributeToNode() {
        final Person p = new Person();
        treeBuilder.openInstance(p);
        assertNotNull(treeBuilder.getTreeRoot());
        treeBuilder.visitTypes(Collections.singleton(Vocabulary.PERSON), p);
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        final CollectionNode typesNode = (CollectionNode) getNode(treeBuilder.getTreeRoot(), JsonLd.TYPE);
        assertNotNull(typesNode);
        assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(Vocabulary.PERSON)));
    }

    @Test
    void visitTypesAddsArrayOfTypesToNode() throws Exception {
        final Employee employee = Generator.generateEmployee();
        treeBuilder.openInstance(employee);
        assertTrue(getNodeStack().isEmpty());
        treeBuilder.visitTypes(Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.EMPLOYEE), employee);
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        final Set<String> types = new HashSet<>(Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.EMPLOYEE));
        final CollectionNode typesNode = (CollectionNode) getNode(treeBuilder.getTreeRoot(), JsonLd.TYPE);
        assertNotNull(typesNode);
        for (String t : types) {
            assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(t)));
        }
    }

    @Test
    void openInstanceAddsAttributeValueToItsParentObject() throws Exception {
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
    void closeInstanceClosesNodeAndDoesNothingWhenStackIsEmpty() throws Exception {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(getNodeStack().isEmpty());
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        treeBuilder.closeInstance(u);
        assertFalse(treeBuilder.getTreeRoot().isOpen());
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    void closeInstancePopsOriginalCurrentFromStack() throws Exception {
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
    void openCollectionCreatesCollectionNode() {
        treeBuilder.openCollection(Collections.singleton(Generator.generateEmployee()));
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertNotNull(root);
        assertTrue(root instanceof CollectionNode);
    }

    @Test
    void openCollectionPushesCurrentNodeToStack() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(org);
        org.addEmployee(employee);
        treeBuilder.openInstance(org);
        assertTrue(getNodeStack().isEmpty());
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), org.getEmployees());
        treeBuilder.openCollection(org.getEmployees());
        assertFalse(getNodeStack().isEmpty());
        assertTrue(treeBuilder.getTreeRoot() instanceof CollectionNode);
    }

    @Test
    void closeCollectionPopsOriginalFromNodeFromStack() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(org);
        org.addEmployee(employee);
        treeBuilder.openInstance(org);
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), org.getEmployees());
        treeBuilder.openCollection(org.getEmployees());
        assertFalse(getNodeStack().isEmpty());
        treeBuilder.closeCollection(org.getEmployees());
        assertTrue(getNodeStack().isEmpty());
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
    }

    @Test
    void visitFieldDoesNothingWhenFieldValueIsNull() throws Exception {
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
    void visitFieldStoresFieldWhenItIsObjectProperty() throws Exception {
        final Employee employee = Generator.generateEmployee();
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        final Field visitedFieldField = JsonLdTreeBuilder.class.getDeclaredField("visitedField");
        visitedFieldField.setAccessible(true);
        final Field visitedField = (Field) visitedFieldField.get(treeBuilder);
        assertNotNull(visitedField);
        assertEquals(Employee.class.getDeclaredField("employer"), visitedField);
    }

    @Test
    void visitFieldExtractsValueOfDataPropertyAndAddsNodeToTheRoot() throws Exception {
        final User user = Generator.generateUser();
        treeBuilder.openInstance(user);
        assertNotNull(treeBuilder.getTreeRoot());
        treeBuilder.visitField(Person.class.getDeclaredField("firstName"), user.getFirstName());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        assertTrue(treeBuilder.getTreeRoot().getItems()
                              .contains(JsonNodeFactory.createLiteralNode(Vocabulary.FIRST_NAME, user.getFirstName())));
    }

    @Test
    void visitFieldExtractsValueOfAnnotationPropertyAndAddsNodeToTheRoot() throws Exception {
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(org);
        treeBuilder.visitField(Organization.class.getDeclaredField("name"), org.getName());
        assertFalse(treeBuilder.getTreeRoot().getItems().isEmpty());
        assertTrue(treeBuilder.getTreeRoot().getItems()
                              .contains(JsonNodeFactory.createLiteralNode(RDFS.LABEL, org.getName())));
    }

    @Test
    void visitFieldExtractsValueOfPluralDataPropertyAndAddsCollectionNodeWithValuesToTheRoot() throws Exception {
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
    void visitKnownInstanceAddsNodeWithObjectIdAttributeToTheParent() throws Exception {
        final Employee employee = Generator.generateEmployee();
        final Organization employer = employee.getEmployer();
        employer.setEmployees(Collections.singleton(employee));
        treeBuilder.openInstance(employer);
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), employer.getEmployees());
        treeBuilder.openCollection(employer.getEmployees());
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.visitKnownInstance(employee.getEmployer().getUri().toString(), employee.getEmployer());
        final JsonNode referenceNode = getNode(treeBuilder.getTreeRoot(), Vocabulary.IS_MEMBER_OF);
        assertNotNull(referenceNode);
        assertTrue(referenceNode instanceof ObjectNode);
        final Collection<JsonNode> attributes = ((ObjectNode) referenceNode).getItems();
        assertEquals(1, attributes.size());
        assertEquals(JsonLd.ID, attributes.iterator().next().getName());
    }

    @Test
    void visitKnownInstanceAddsCollectionOfObjectsWithIdAttributeToTheParent() throws Exception {
        final Employee employee = Generator.generateEmployee();
        final Organization employer = employee.getEmployer();
        employer.setEmployees(Collections.singleton(employee));
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.openInstance(employee.getEmployer());
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), employer.getEmployees());
        treeBuilder.openCollection(employer.getEmployees());
        treeBuilder.visitKnownInstance(employee.getUri().toString(), employee);
        final CompositeNode node = treeBuilder.getTreeRoot();
        assertTrue(node instanceof CollectionNode);
        node.getItems().forEach(item -> {
            assertTrue(item instanceof ObjectNode);
            final Collection<JsonNode> attributes = ((ObjectNode) item).getItems();
            assertEquals(1, attributes.size());
            assertEquals(JsonLd.ID, attributes.iterator().next().getName());
        });
    }

    @Test
    void testAddCollectionItemAfterVisitKnownInstanceWasCalledInObject() throws Exception {
        final Employee employee = Generator.generateEmployee();
        final Employee employeeTwo = Generator.generateEmployee();
        final Organization employer = employee.getEmployer();
        employer.addEmployee(employee);
        employer.addEmployee(employeeTwo);
        employeeTwo.setEmployer(employer);
        treeBuilder.openInstance(employer);
        treeBuilder.visitField(Organization.class.getDeclaredField("employees"), employer.getEmployees());
        treeBuilder.openCollection(employer.getEmployees());
        treeBuilder.openInstance(employee);
        treeBuilder.visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        treeBuilder.visitKnownInstance(employee.getEmployer().getUri().toString(), employee.getEmployer());
        treeBuilder.closeInstance(employee);
        treeBuilder.openInstance(employeeTwo);
        assertNull(treeBuilder.getTreeRoot().getName());
    }

    @Test
    void testBuildTreeWithRootCollection() {
        final Set<User> users = Generator.generateUsers();
        treeBuilder.openCollection(users);
        for (User u : users) {
            treeBuilder.openInstance(u);
            treeBuilder.closeInstance(u);
        }
        treeBuilder.closeCollection(users);

        final CompositeNode root = treeBuilder.getTreeRoot();
        assertFalse(root.isOpen());
        assertEquals(users.size(), root.getItems().size());
        for (JsonNode item : root.getItems()) {
            assertTrue(item instanceof ObjectNode);
            assertNull(item.getName());
        }
    }

    @Test
    void visitIdentifierAddsIdNodeToCurrentObjectNode() throws Exception {
        final Person p = Generator.generatePerson();
        treeBuilder.openInstance(p);
        treeBuilder.visitIdentifier(p.getUri().toString(), p);
        final CompositeNode root = treeBuilder.getTreeRoot();
        final Collection<JsonNode> nodes = root.getItems();
        final Optional<JsonNode> idNode = nodes.stream().filter(n -> n.getName().equals(JsonLd.ID)).findAny();
        assertTrue(idNode.isPresent());
        assertTrue(idNode.get() instanceof ObjectIdNode);
        final ObjectIdNode node = (ObjectIdNode) idNode.get();
        JsonGenerator generator = mock(JsonGenerator.class);
        node.write(generator);
        verify(generator).writeString(p.getUri().toString());
    }

    @Test
    void visitFieldSerializesSingularAnnotationPropertyFieldValueWhichIsIdentifierAsObjectWithIdentifier()
            throws Exception {
        final WithAnnotation instance = new WithAnnotation();
        instance.value = Generator.generateUri();
        treeBuilder.openInstance(instance);
        treeBuilder.visitField(WithAnnotation.class.getDeclaredField("value"), instance.value);
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(Vocabulary.CHANGED_VALUE, valueNode.getName());
        verifyObjectIdNode(valueNode);
    }

    @OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
    private static class WithAnnotation {
        @OWLAnnotationProperty(iri = Vocabulary.CHANGED_VALUE)
        private Object value;
    }

    @Test
    void visitFieldSerializesPluralAnnotationPropertyFieldValuesWhichAreIdentifiersAsObjectsWithIdentifier()
            throws Exception {
        final WithAnnotations instance = new WithAnnotations();
        instance.values = IntStream.range(0, 5).mapToObj(i -> Generator.generateUri()).collect(Collectors.toSet());
        treeBuilder.openInstance(instance);
        treeBuilder.visitField(WithAnnotations.class.getDeclaredField("values"), instance.values);
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(Vocabulary.CHANGED_VALUE, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) valueNode;
        assertEquals(instance.values.size(), colNode.getItems().size());
        colNode.getItems().forEach(this::verifyObjectIdNode);
    }

    private void verifyObjectIdNode(JsonNode vn) {
        assertThat(vn, instanceOf(ObjectNode.class));
        final ObjectNode on = (ObjectNode) vn;
        assertEquals(1, on.getItems().size());
        assertEquals(JsonLd.ID, on.getItems().iterator().next().getName());
    }

    @OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
    private static class WithAnnotations {
        @OWLAnnotationProperty(iri = Vocabulary.CHANGED_VALUE)
        private Set<Object> values;
    }

    @Test
    void visitFieldCorrectlySerializesPluralAnnotationPropertyFieldValuesWithMixedIdentifiersAndLiteralValues()
            throws Exception {
        final WithAnnotations instance = new WithAnnotations();
        instance.values = IntStream.range(0, 5).mapToObj(i -> {
            if (i % 2 == 0) {
                return Generator.generateUri();
            }
            return i;
        }).collect(Collectors.toSet());
        treeBuilder.openInstance(instance);
        treeBuilder.visitField(WithAnnotations.class.getDeclaredField("values"), instance.values);
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(Vocabulary.CHANGED_VALUE, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) valueNode;
        assertEquals(instance.values.size(), colNode.getItems().size());
        final Iterator<Object> valuesIt = instance.values.iterator();
        final Iterator<JsonNode> nodeIt = colNode.getItems().iterator();
        while (valuesIt.hasNext() && nodeIt.hasNext()) {
            final JsonNode node = nodeIt.next();
            final Object value = valuesIt.next();
            if (value instanceof URI) {
                verifyObjectIdNode(node);
            } else {
                assertThat(node, instanceOf(LiteralNode.class));
                assertEquals(value, ((LiteralNode<?>) node).getValue());
            }
        }
    }

    @Test
    void visitFieldSerializesMultilingualStringIntoArrayOfLangStringObjects() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final String enValue = "building";
        final String csValue = "budova";
        instance.setLabel(new MultilingualString());
        instance.getLabel().set("en", enValue);
        instance.getLabel().set("cs", csValue);
        treeBuilder.openInstance(instance);
        treeBuilder.visitField(ObjectWithMultilingualString.class.getDeclaredField("label"), instance.getLabel());
        final CompositeNode root = treeBuilder.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(RDFS.LABEL, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) valueNode;
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }
}
