package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DeserializerTest {

    @Test
    public void resolveTargetClassReturnsExpectedClassWhenItIsPlainIdentifierType() throws Exception {
        final Deserializer<Map<?, ?>> deserializer =
                new ObjectDeserializer(null,
                        new DeserializerConfig(new Configuration(), new TargetClassResolver(new TypeMap())),
                        URI.class);
        assertEquals(URI.class,
                deserializer.resolveTargetClass(TestUtil.readAndExpand("objectWithDataProperties.json"), URI.class));
    }
}