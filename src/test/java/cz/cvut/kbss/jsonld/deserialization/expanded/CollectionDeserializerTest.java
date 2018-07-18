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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

public class CollectionDeserializerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void processValueAddsObjectIdentifiersIntoPropertiesMap() throws Exception {
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
    public void processValueThrowsMissingIdentifierExceptionWhenInstanceToBeAddedIntoPropertiesHasNoIdentifier()
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
        thrown.expect(MissingIdentifierException.class);
        thrown.expectMessage(containsString("Cannot put an object without an identifier into @Properties. Object: "));
        deserializer.processValue(collection);
    }
}