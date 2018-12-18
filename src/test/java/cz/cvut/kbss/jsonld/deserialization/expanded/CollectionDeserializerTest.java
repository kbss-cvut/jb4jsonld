package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DefaultInstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionDeserializerTest {

    @Test
    void processValueAddsObjectIdentifiersIntoPropertiesMap() throws Exception {
        final Map<?, ?> jsonLd = (Map<?, ?>) ((List) TestUtil.readAndExpand("objectWithPluralReference.json")).get(0);
        final List<?> collection = (List<?>) jsonLd.get(Vocabulary.HAS_MEMBER);
        final TargetClassResolver resolver = new TargetClassResolver(new TypeMap());
        final InstanceBuilder instanceBuilder = new DefaultInstanceBuilder(resolver);
        final DeserializerConfig config = new DeserializerConfig(new Configuration(), resolver);
        final CollectionDeserializer deserializer = new CollectionDeserializer(instanceBuilder, config,
                Vocabulary.HAS_MEMBER);
        instanceBuilder.openObject(Generator.generateUri().toString(), Person.class);
        deserializer.processValue(collection);
        final Person person = (Person) instanceBuilder.getCurrentRoot();
        final Set<?> values = person.getProperties().get(Vocabulary.HAS_MEMBER);
        assertTrue(values.contains(TestUtil.HALSEY_URI.toString()));
        assertTrue(values.contains(TestUtil.LASKY_URI.toString()));
        assertTrue(values.contains(TestUtil.PALMER_URI.toString()));
    }

    @Test
    void processValueThrowsMissingIdentifierExceptionWhenInstanceToBeAddedIntoPropertiesHasNoIdentifier()
            throws Exception {
        final Map<?, ?> jsonLd = (Map<?, ?>) ((List) TestUtil.readAndExpand("objectWithPluralReference.json")).get(0);
        final List<?> collection = (List<?>) jsonLd.get(Vocabulary.HAS_MEMBER);
        final Map<?, ?> item = (Map<?, ?>) collection.get(0);
        item.remove(JsonLd.ID);
        final TargetClassResolver resolver = new TargetClassResolver(new TypeMap());
        final InstanceBuilder instanceBuilder = new DefaultInstanceBuilder(resolver);
        final DeserializerConfig config = new DeserializerConfig(new Configuration(), resolver);
        final CollectionDeserializer deserializer = new CollectionDeserializer(instanceBuilder, config,
                Vocabulary.HAS_MEMBER);
        instanceBuilder.openObject(Generator.generateUri().toString(), Person.class);
        final MissingIdentifierException result = assertThrows(MissingIdentifierException.class,
                () -> deserializer.processValue(collection));
        assertThat(result.getMessage(),
                containsString("Cannot put an object without an identifier into @Properties. Object: "));
    }
}