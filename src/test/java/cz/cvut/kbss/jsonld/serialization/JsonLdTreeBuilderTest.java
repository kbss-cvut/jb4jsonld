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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.*;
import cz.cvut.kbss.jsonld.serialization.serializer.LiteralValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.DefaultValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.IdentifierSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.MultilingualStringSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.TypesSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.BeforeEach;
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

public class JsonLdTreeBuilderTest {

    private JsonLdTreeBuilder sut;

    @BeforeEach
    void setUp() {
        final LiteralValueSerializers serializers =
                new LiteralValueSerializers(new DefaultValueSerializer(new MultilingualStringSerializer()));
        serializers.registerIdentifierSerializer(new IdentifierSerializer());
        serializers.registerTypesSerializer(new TypesSerializer());
        this.sut = new JsonLdTreeBuilder(serializers, DummyJsonLdContext.INSTANCE);
    }

    @Test
    void openInstanceCreatesNewObjectNode() {
        final User u = Generator.generateUser();
        sut.openObject(ctx(null, null, u));
        assertTrue(sut.getTreeRoot() instanceof ObjectNode);
    }

    private static <T> SerializationContext<T> ctx(String attId, Field field, T value) {
        return new SerializationContext<>(attId, field, value, DummyJsonLdContext.INSTANCE);
    }

    @Test
    void openInstancePushesOriginalCurrentToStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        sut.openObject(ctx(null, null, e));
        sut.openObject(ctx(null, null, org));
        assertTrue(sut.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Stack<JsonNode> getNodeStack() throws Exception {
        final Field stackField = JsonLdTreeBuilder.class.getDeclaredField("nodeStack");
        stackField.setAccessible(true);
        return (Stack<JsonNode>) stackField.get(sut);
    }

    @Test
    void openInstanceDoesNotPushOriginalCurrentToStackWhenItIsAlreadyClosed() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        sut.openObject(ctx(null, null, e));
        assertTrue(getNodeStack().isEmpty());
        sut.closeObject(ctx(null, null, e));
        sut.openObject(ctx(null, null, org));
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    void visitTypesAddsSingularTypeAttributeToNode() {
        final Person p = new Person();
        sut.openObject(ctx(null, null, p));
        assertNotNull(sut.getTreeRoot());
        sut.visitTypes(ctx(JsonLd.TYPE, null, Collections.singleton(Vocabulary.PERSON)));
        assertFalse(sut.getTreeRoot().getItems().isEmpty());
        final CollectionNode<?> typesNode = (CollectionNode<?>) getNode(sut.getTreeRoot(), JsonLd.TYPE);
        assertNotNull(typesNode);
        assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(Vocabulary.PERSON)));
    }

    @Test
    void visitTypesAddsArrayOfTypesToNode() throws Exception {
        final Employee employee = Generator.generateEmployee();
        sut.openObject(ctx(null, null, employee));
        assertTrue(getNodeStack().isEmpty());
        sut.visitTypes(ctx(JsonLd.TYPE, User.class.getDeclaredField("types"),
                           new HashSet<>(
                                   Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.EMPLOYEE))));
        assertFalse(sut.getTreeRoot().getItems().isEmpty());
        final Set<String> types = new HashSet<>(Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.EMPLOYEE));
        final CollectionNode<?> typesNode = (CollectionNode<?>) getNode(sut.getTreeRoot(), JsonLd.TYPE);
        assertNotNull(typesNode);
        for (String t : types) {
            assertTrue(typesNode.getItems().contains(JsonNodeFactory.createLiteralNode(t)));
        }
    }

    @Test
    void openInstanceAddsAttributeValueToItsParentObject() throws Exception {
        final Employee employee = Generator.generateEmployee();
        sut.openObject(ctx(null, null, employee));
        sut.openObject(ctx(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), employee.getEmployer()));
        sut.closeObject(ctx(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), employee.getEmployer()));
        sut.closeObject(ctx(null, null, employee));
        final CompositeNode<?> employerNode =
                (CompositeNode<?>) getNode(sut.getTreeRoot(), Vocabulary.IS_MEMBER_OF);
        assertNotNull(employerNode);
    }

    public static JsonNode getNode(CompositeNode<?> parent, String name) {
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
        sut.openObject(ctx(null, null, u));
        assertTrue(getNodeStack().isEmpty());
        assertTrue(sut.getTreeRoot() instanceof ObjectNode);
        sut.closeObject(ctx(null, null, u));
        assertFalse(sut.getTreeRoot().isOpen());
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    void closeInstancePopsOriginalCurrentFromStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        sut.openObject(ctx(null, null, e));
        sut.openObject(ctx(null, null, org));
        assertTrue(sut.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
        sut.closeObject(ctx(null, null, org));
        assertTrue(getNodeStack().isEmpty());
        assertNotNull(sut.getTreeRoot());
    }

    @Test
    void openCollectionCreatesCollectionNode() {
        sut.openCollection(ctx(null, null, Collections.singleton(Generator.generateEmployee())));
        final CompositeNode<?> root = sut.getTreeRoot();
        assertNotNull(root);
        assertTrue(root instanceof CollectionNode);
    }

    @Test
    void openCollectionPushesCurrentNodeToStack() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(org);
        org.addEmployee(employee);
        sut.openObject(ctx(null, null, org));
        assertTrue(getNodeStack().isEmpty());
        sut.openCollection(ctx(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), org.getEmployees()));
        assertFalse(getNodeStack().isEmpty());
        assertTrue(sut.getTreeRoot() instanceof CollectionNode);
    }

    @Test
    void closeCollectionPopsOriginalFromNodeFromStack() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(org);
        org.addEmployee(employee);
        sut.openObject(ctx(null, null, org));
        sut.openCollection(ctx(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), org.getEmployees()));
        assertFalse(getNodeStack().isEmpty());
        sut.closeCollection(ctx(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), org.getEmployees()));
        assertTrue(getNodeStack().isEmpty());
        assertTrue(sut.getTreeRoot() instanceof ObjectNode);
    }

    @Test
    void visitAttributeDoesNothingWhenFieldValueIsNull() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setFirstName(null);
        sut.visitAttribute(
                ctx(Vocabulary.FIRST_NAME, Person.class.getDeclaredField("firstName"), employee.getFirstName()));
        final CompositeNode<?> node = sut.getTreeRoot();
        assertNull(node);
    }

    @Test
    void visitAttributeExtractsValueOfDataPropertyAndAddsNodeToTheRoot() throws Exception {
        final User user = Generator.generateUser();
        sut.openObject(ctx(null, null, user));
        assertNotNull(sut.getTreeRoot());
        sut.visitAttribute(ctx(Vocabulary.FIRST_NAME, Person.getFirstNameField(), user.getFirstName()));
        assertFalse(sut.getTreeRoot().getItems().isEmpty());
        assertTrue(sut.getTreeRoot().getItems()
                      .contains(JsonNodeFactory.createLiteralNode(Vocabulary.FIRST_NAME, user.getFirstName())));
    }

    @Test
    void visitAttributeExtractsValueOfAnnotationPropertyAndAddsNodeToTheRoot() throws Exception {
        final Organization org = Generator.generateOrganization();
        sut.openObject(ctx(null, null, org));
        sut.visitAttribute(ctx(RDFS.LABEL, Organization.class.getDeclaredField("name"), org.getName()));
        assertFalse(sut.getTreeRoot().getItems().isEmpty());
        assertTrue(sut.getTreeRoot().getItems()
                      .contains(JsonNodeFactory.createLiteralNode(RDFS.LABEL, org.getName())));
    }

    @Test
    void visitAttributeExtractsValuesOfPluralDataPropertyAndAddsCollectionNodeWithValuesToTheRoot() throws Exception {
        final Organization org = Generator.generateOrganization();
        sut.openObject(ctx(null, null, org));
        sut
                .visitAttribute(ctx(Vocabulary.BRAND, Organization.class.getDeclaredField("brands"), org.getBrands()));
        assertFalse(sut.getTreeRoot().getItems().isEmpty());
        final CollectionNode<?> brandsNode = (CollectionNode<?>) getNode(sut.getTreeRoot(), Vocabulary.BRAND);
        assertNotNull(brandsNode);
        assertTrue(brandsNode instanceof SetNode);
        for (String brand : org.getBrands()) {
            assertTrue(brandsNode.getItems().contains(JsonNodeFactory.createLiteralNode(brand)));
        }
    }

    @Test
    void testBuildTreeWithRootCollection() {
        final Set<User> users = Generator.generateUsers();
        sut.openCollection(ctx(null, null, users));
        for (User u : users) {
            sut.openObject(ctx(null, null, u));
            sut.closeObject(ctx(null, null, u));
        }
        sut.closeCollection(ctx(null, null, users));

        final CompositeNode<?> root = sut.getTreeRoot();
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
        sut.openObject(ctx(null, null, p));
        sut.visitIdentifier(ctx(JsonLd.ID, null, p.getUri().toString()));
        final CompositeNode<?> root = sut.getTreeRoot();
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
    void visitAttributeSerializesSingularAnnotationPropertyFieldValueWhichIsIdentifierAsObjectWithIdentifier()
            throws Exception {
        final WithAnnotation instance = new WithAnnotation();
        instance.value = Generator.generateUri();
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(Vocabulary.CHANGED_VALUE, WithAnnotation.class.getDeclaredField("value"), instance.value));
        final CompositeNode<?> root = sut.getTreeRoot();
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
    void visitAttributeSerializesPluralAnnotationPropertyFieldValuesWhichAreIdentifiersAsObjectsWithIdentifier()
            throws Exception {
        final WithAnnotations instance = new WithAnnotations();
        instance.values = IntStream.range(0, 5).mapToObj(i -> Generator.generateUri()).collect(Collectors.toSet());
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(Vocabulary.CHANGED_VALUE, WithAnnotations.class.getDeclaredField("values"), instance.values));
        final CompositeNode<?> root = sut.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(Vocabulary.CHANGED_VALUE, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) valueNode;
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
    void visitAttributeCorrectlySerializesPluralAnnotationPropertyFieldValuesWithMixedIdentifiersAndLiteralValues()
            throws Exception {
        final WithAnnotations instance = new WithAnnotations();
        instance.values = IntStream.range(0, 5).mapToObj(i -> {
            if (i % 2 == 0) {
                return Generator.generateUri();
            }
            return i;
        }).collect(Collectors.toSet());
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(Vocabulary.CHANGED_VALUE, WithAnnotations.class.getDeclaredField("values"), instance.values));
        final CompositeNode<?> root = sut.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(Vocabulary.CHANGED_VALUE, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) valueNode;
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
    void visitAttributeSerializesMultilingualStringIntoArrayOfLangStringObjects() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final String enValue = "building";
        final String csValue = "budova";
        instance.setLabel(new MultilingualString());
        instance.getLabel().set("en", enValue);
        instance.getLabel().set("cs", csValue);
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(RDFS.LABEL, ObjectWithMultilingualString.class.getDeclaredField("label"), instance.getLabel()));
        verifyMultilingualStringSerialization();
    }

    private void verifyMultilingualStringSerialization() {
        final CompositeNode<?> root = sut.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(RDFS.LABEL, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) valueNode;
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> {
            assertInstanceOf(ObjectNode.class, item);
            final ObjectNode n = (ObjectNode) item;
            assertTrue(n.getItems().stream().anyMatch(p -> Objects.equals(p.getName(), JsonLd.VALUE)));
            assertTrue(n.getItems().stream().anyMatch(p -> Objects.equals(p.getName(), JsonLd.LANGUAGE)));
        });
    }

    @Test
    void visitFieldSerializesAnnotationPropertyMultilingualStringIntoArrayOfLangStringObjects() throws Exception {
        final ObjectWithMultilingualStringAnnotation instance = new ObjectWithMultilingualStringAnnotation();
        final String enValue = "building";
        final String csValue = "budova";
        instance.label = new MultilingualString();
        instance.label.set("en", enValue);
        instance.label.set("cs", csValue);
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(RDFS.LABEL, ObjectWithMultilingualStringAnnotation.class.getDeclaredField("label"),
                    instance.label));
        verifyMultilingualStringSerialization();
    }

    @OWLClass(iri = Vocabulary.STUDY)
    public static class ObjectWithMultilingualStringAnnotation {

        @OWLAnnotationProperty(iri = RDFS.LABEL)
        private MultilingualString label;
    }

    @Test
    void visitAttributeSerializesPluralMultilingualStringIntoArrayOfArraysOfLangStringObjects() throws Exception {
        final ObjectWithPluralMultilingualStrings instance = initInstanceWithPluralMultilingualStrings();
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(RDFS.LABEL, ObjectWithPluralMultilingualStrings.class.getDeclaredField("labels"), instance.labels));

        verifyPluralMultilingualStringsSerialization(RDFS.LABEL);
    }

    private void verifyPluralMultilingualStringsSerialization(String property) {
        final CompositeNode<?> root = sut.getTreeRoot();
        assertEquals(1, root.getItems().size());
        final JsonNode valueNode = root.getItems().iterator().next();
        assertEquals(property, valueNode.getName());
        assertThat(valueNode, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) valueNode;
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> {
            assertThat(item, instanceOf(CollectionNode.class));
            final CollectionNode<?> itemCol = (CollectionNode<?>) item;
            assertEquals(2, itemCol.getItems().size());
            itemCol.getItems().forEach(elem -> {
                assertInstanceOf(ObjectNode.class, elem);
                final ObjectNode n = (ObjectNode) elem;
                assertTrue(n.getItems().stream().anyMatch(p -> Objects.equals(p.getName(), JsonLd.VALUE)));
                assertTrue(n.getItems().stream().anyMatch(p -> Objects.equals(p.getName(), JsonLd.LANGUAGE)));
            });
        });
    }

    private ObjectWithPluralMultilingualStrings initInstanceWithPluralMultilingualStrings() {
        final ObjectWithPluralMultilingualStrings instance = new ObjectWithPluralMultilingualStrings();
        final MultilingualString one = new MultilingualString();
        one.set("en", "building");
        one.set("cs", "budova");
        final MultilingualString two = new MultilingualString();
        two.set("en", "construction");
        two.set("cs", "stavba");
        instance.labels = new HashSet<>(Arrays.asList(one, two));
        instance.altLabels = new HashSet<>(Arrays.asList(one, two));
        return instance;
    }

    @OWLClass(iri = Vocabulary.STUDY)
    public static class ObjectWithPluralMultilingualStrings {

        @OWLDataProperty(iri = RDFS.LABEL)
        private Set<MultilingualString> labels;

        @OWLAnnotationProperty(iri = SKOS.ALT_LABEL)
        private Set<MultilingualString> altLabels;
    }

    @Test
    void visitFieldSerializesPluralAnnotationPropertyMultilingualStringIntoArrayOfArraysOfLangStringObjects()
            throws Exception {
        final ObjectWithPluralMultilingualStrings instance = initInstanceWithPluralMultilingualStrings();
        sut.openObject(ctx(null, null, instance));
        sut.visitAttribute(
                ctx(SKOS.ALT_LABEL, ObjectWithPluralMultilingualStrings.class.getDeclaredField("altLabels"),
                    instance.altLabels));

        verifyPluralMultilingualStringsSerialization(SKOS.ALT_LABEL);
    }
}
