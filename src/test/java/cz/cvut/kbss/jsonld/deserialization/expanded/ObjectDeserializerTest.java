package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.*;

public class ObjectDeserializerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private InstanceBuilder instanceBuilderMock;

    @Mock
    private TargetClassResolver tcResolverMock;

    private ObjectDeserializer deserializer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processValueProcessesAttributesInOrderSpecifiedByJsonLdAttributeOrder() throws Exception {
        when(tcResolverMock.getTargetClass(eq(Study.class), anyCollection())).thenReturn(Study.class);
        when(tcResolverMock.getTargetClass(eq(Employee.class), anyCollection())).thenReturn(Employee.class);
        when(instanceBuilderMock.getCurrentRoot()).thenReturn(new Study());
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        when(instanceBuilderMock.isPlural(Vocabulary.HAS_MEMBER)).thenReturn(true);
        when(instanceBuilderMock.isPlural(Vocabulary.HAS_PARTICIPANT)).thenReturn(true);
        doAnswer(inv -> Study.class).when(instanceBuilderMock).getCurrentContextType();
        doAnswer(inv -> Employee.class).when(instanceBuilderMock).getCurrentCollectionElementType();
        this.deserializer =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        Study.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithPluralReferenceSharingObject.json");
        deserializer.processValue((Map<?, ?>) input.get(0));

        final InOrder inOrder = inOrder(instanceBuilderMock);
        inOrder.verify(instanceBuilderMock).addValue(eq(CommonVocabulary.RDFS_LABEL), any());
        inOrder.verify(instanceBuilderMock).openCollection(Vocabulary.HAS_PARTICIPANT);
        inOrder.verify(instanceBuilderMock).openCollection(Vocabulary.HAS_MEMBER);
    }

    @Test
    public void processValueThrowsJsonLdDeserializationExceptionWhenUnknownFieldNameIsUsedInAttributeOrderSpecification() throws
                                                                                                                          Exception {
        when(tcResolverMock.getTargetClass(eq(InvalidOrder.class), anyCollection())).thenReturn(InvalidOrder.class);
        when(instanceBuilderMock.getCurrentRoot()).thenReturn(new InvalidOrder());
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        doAnswer(inv -> InvalidOrder.class).when(instanceBuilderMock).getCurrentContextType();
        this.deserializer =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        InvalidOrder.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithPluralReferenceSharingObject.json");

        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage(
                "Field called unknown declared in JsonLdAttributeOrder annotation not found in class " + InvalidOrder.class + " .");
        deserializer.processValue((Map<?, ?>) input.get(0));
    }

    @OWLClass(iri = Vocabulary.ORGANIZATION)
    @JsonLdAttributeOrder({"unknown", "name"})
    public static class InvalidOrder {

        @Id
        private URI uri;

        @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
        private String name;
    }

    @Test
    public void processValueOpensObjectWithId() throws Exception {
        when(tcResolverMock.getTargetClass(eq(User.class), anyCollection())).thenReturn(User.class);
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        this.deserializer =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithDataProperties.json");
        deserializer.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
    }

    @Test
    public void processValueOpensObjectAsAttributeValueWithId() throws Exception {
        when(tcResolverMock.getTargetClass(eq(User.class), anyCollection())).thenReturn(User.class);
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        this.deserializer =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithSingularReference.json");
        deserializer.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
        verify(instanceBuilderMock).openObject(TestUtil.UNSC_URI.toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
    }

    @Test
    public void processValueDoesNotAddIdToInstanceBuilder() throws Exception {
        when(tcResolverMock.getTargetClass(eq(User.class), anyCollection())).thenReturn(User.class);
        when(instanceBuilderMock.isPropertyMapped(any())).thenReturn(true);
        this.deserializer =
                new ObjectDeserializer(instanceBuilderMock, new DeserializerConfig(new Configuration(), tcResolverMock),
                        User.class);
        final List<?> input = (List<?>) TestUtil.readAndExpand("objectWithDataProperties.json");
        deserializer.processValue((Map<?, ?>) input.get(0));
        verify(instanceBuilderMock).openObject(TestUtil.HALSEY_URI.toString(), User.class);
        verify(instanceBuilderMock, never()).addValue(eq(JsonLd.ID), any());
    }
}