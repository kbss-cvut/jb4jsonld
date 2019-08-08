/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ObjectDeserializerTest {

    @Mock
    private InstanceBuilder instanceBuilderMock;

    @Mock
    private TargetClassResolver tcResolverMock;

    private ObjectDeserializer sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void processValueProcessesAttributesInOrderSpecifiedByJsonLdAttributeOrder() throws Exception {
        doReturn(Study.class).when(tcResolverMock).getTargetClass(eq(Study.class), anyCollection());
        doReturn(Employee.class).when(tcResolverMock).getTargetClass(eq(Employee.class), anyCollection());
        when(instanceBuilderMock.getCurrentRoot()).thenReturn(new Study());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        when(instanceBuilderMock.isPlural(Vocabulary.HAS_MEMBER)).thenReturn(true);
        when(instanceBuilderMock.isPlural(Vocabulary.HAS_PARTICIPANT)).thenReturn(true);
        doAnswer(inv -> Study.class).when(instanceBuilderMock).getCurrentContextType();
        doAnswer(inv -> Employee.class).when(instanceBuilderMock).getCurrentCollectionElementType();
        this.sut =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        Study.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithPluralReferenceSharingObject.json");
        sut.processValue((Map<?, ?>) input.get(0));

        final InOrder inOrder = inOrder(instanceBuilderMock);
        inOrder.verify(instanceBuilderMock).addValue(eq(RDFS.LABEL), any());
        inOrder.verify(instanceBuilderMock).openCollection(Vocabulary.HAS_PARTICIPANT);
        inOrder.verify(instanceBuilderMock).openCollection(Vocabulary.HAS_MEMBER);
    }

    @Test
    void processValueThrowsJsonLdDeserializationExceptionWhenUnknownFieldNameIsUsedInAttributeOrderSpecification()
            throws
            Exception {
        doReturn(InvalidOrder.class).when(tcResolverMock).getTargetClass(eq(InvalidOrder.class), anyCollection());
        when(instanceBuilderMock.getCurrentRoot()).thenReturn(new InvalidOrder());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        doAnswer(inv -> InvalidOrder.class).when(instanceBuilderMock).getCurrentContextType();
        this.sut =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        InvalidOrder.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithPluralReferenceSharingObject.json");

        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> sut.processValue((Map<?, ?>) input.get(0)));
        assertThat(result.getMessage(),
                containsString("Field called unknown declared in JsonLdAttributeOrder annotation not found in class " +
                        InvalidOrder.class));
    }

    @OWLClass(iri = Vocabulary.ORGANIZATION)
    @JsonLdAttributeOrder({"unknown", "name"})
    private static class InvalidOrder {

        @Id
        private URI uri;

        @OWLAnnotationProperty(iri = RDFS.LABEL)
        private String name;
    }

    @Test
    void processValueOpensObjectWithId() throws Exception {
        doReturn(User.class).when(tcResolverMock).getTargetClass(eq(User.class), anyCollection());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        this.sut =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithDataProperties.json");
        sut.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
    }

    @Test
    void processValueOpensObjectAsAttributeValueWithId() throws Exception {
        doReturn(User.class).when(tcResolverMock).getTargetClass(eq(User.class), anyCollection());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        this.sut =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithSingularReference.json");
        sut.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
        verify(instanceBuilderMock).openObject(TestUtil.UNSC_URI.toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
    }

    @Test
    void processValueDoesNotAddIdToInstanceBuilder() throws Exception {
        doReturn(User.class).when(tcResolverMock).getTargetClass(eq(User.class), anyCollection());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        this.sut =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithDataProperties.json");
        sut.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
        verify(instanceBuilderMock, never()).addValue(eq(JsonLd.ID), any());
    }

    @Test
    void processValueGeneratesIdWhenIncomingObjectDoesNotContainIt() throws Exception {
        doReturn(User.class).when(tcResolverMock).getTargetClass(eq(User.class), anyCollection());
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        this.sut = new ObjectDeserializer(instanceBuilderMock,
                new DeserializerConfig(new Configuration(), tcResolverMock), User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithDataProperties.json");
        ((Map<?, ?>) input.get(0)).remove(JsonLd.ID);
        sut.processValue((Map<?, ?>) input.get(0));
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(instanceBuilderMock).openObject(captor.capture(), eq(User.class));
        assertNotNull(captor.getValue());
        assertThat(captor.getValue(), StringStartsWith.startsWith("_:"));
    }

    @Test
    void processValueSkipsPropertyMappedToFieldWithReadOnlyAccess() throws Exception {
        doReturn(Study.class).when(tcResolverMock).getTargetClass(eq(Study.class), anyCollection());
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        when(instanceBuilderMock.isPropertyDeserializable(any())).thenReturn(true);
        when(instanceBuilderMock.isPropertyDeserializable(eq(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED))).thenReturn(false);
        this.sut = new ObjectDeserializer(instanceBuilderMock,
                new DeserializerConfig(new Configuration(), tcResolverMock), Study.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithReadOnlyPropertyValue.json");
        ((Map<?, ?>) input.get(0)).remove(Vocabulary.HAS_MEMBER);
        ((Map<?, ?>) input.get(0)).remove(Vocabulary.HAS_PARTICIPANT);
        sut.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock, never()).addValue(eq(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED), any());
    }
}
