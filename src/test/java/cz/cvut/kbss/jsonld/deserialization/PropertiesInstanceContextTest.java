package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertiesInstanceContextTest {

    private static final String VALUE = "halsey@unsc.org";

    private Field personProperties;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.personProperties = Person.class.getDeclaredField("properties");
    }

    @Test
    public void addItemInsertsCollectionIntoPropertiesMapAndAddsValueToIt() {
        final Map<String, Set<String>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME, personProperties);
        ctx.addItem(VALUE);
        assertTrue(map.containsKey(Vocabulary.USERNAME));
        assertEquals(1, map.get(Vocabulary.USERNAME).size());
        assertTrue(map.get(Vocabulary.USERNAME).contains(VALUE));
    }

    @Test
    public void addItemReusesPropertyCollectionWhenItIsAlreadyPresentInPropertiesMap() {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put(Vocabulary.USERNAME, new HashSet<>());
        map.get(Vocabulary.USERNAME).add(VALUE);
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME, personProperties);
        final String newValue = "halsey@oni.org";
        ctx.addItem(newValue);
        assertEquals(2, map.get(Vocabulary.USERNAME).size());
        assertTrue(map.get(Vocabulary.USERNAME).contains(VALUE));
        assertTrue(map.get(Vocabulary.USERNAME).contains(newValue));
    }

    @Test
    public void addItemPutsSingleValueIntoPropertiesMapWhenMapIsConfiguredAsSingleValued() throws Exception {
        final Map<?, ?> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                SingleValued.class.getDeclaredField("properties"));
        ctx.addItem(VALUE);
        assertEquals(VALUE, map.get(Vocabulary.USERNAME));
    }

    private static class SingleValued {
        @Properties
        private Map<String, String> properties;
    }

    @Test
    public void addItemConvertsPropertyIdentifierToCorrectType() throws Exception {
        final Map<URI, Set<?>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                TypedProperties.class.getDeclaredField("properties"));
        ctx.addItem(VALUE);
        assertTrue(map.containsKey(URI.create(Vocabulary.USERNAME)));
    }

    private static class TypedProperties {
        @Properties
        private Map<URI, Set<?>> properties;
    }

    @Test
    public void addItemInsertsValuesOfCorrectTypes() throws Exception {
        final Map<URI, Set<?>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.IS_ADMIN,
                TypedProperties.class.getDeclaredField("properties"));
        ctx.addItem(true);
        assertEquals(Boolean.TRUE, map.get(URI.create(Vocabulary.IS_ADMIN)).iterator().next());
    }

    @Test
    public void addItemConvertsValuesToCorrectType() throws Exception {
        final Map<String, Set<String>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.IS_ADMIN, personProperties);
        ctx.addItem(true);
        assertEquals(Boolean.TRUE.toString(), map.get(Vocabulary.IS_ADMIN).iterator().next());
    }

    @Test
    public void addItemThrowsExceptionWhenMultipleItemsForSingularPropertyAreAdded() throws Exception {
        final Map<String, String> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                SingleValued.class.getDeclaredField("properties"));
        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage(
                containsString("Encountered multiple values of property " + Vocabulary.USERNAME));
        ctx.addItem(VALUE);
        ctx.addItem("halsey@oni.org");
    }
}
