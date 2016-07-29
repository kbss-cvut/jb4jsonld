package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

public class DataTypeTransformerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void registerTransformationRuleAddsRuleToTransformationRules() throws Exception {
        final Function<Integer, Double> rule = Integer::doubleValue;
        DataTypeTransformer.registerTransformationRule(Integer.class, Double.class, rule);
        final Field rulesField = DataTypeTransformer.class.getDeclaredField("rules");
        rulesField.setAccessible(true);
        final Map<DataTypeTransformer.TransformationRuleIdentifier<?, ?>, Function<?, ?>> rules = (Map<DataTypeTransformer.TransformationRuleIdentifier<?, ?>, Function<?, ?>>) rulesField
                .get(null);
        assertTrue(
                rules.containsKey(new DataTypeTransformer.TransformationRuleIdentifier<>(Integer.class, Double.class)));
    }

    @Test
    public void transformValueTransformsStringToUri() {
        final String value = Generator.generateUri().toString();
        final Object result = DataTypeTransformer.transformValue(value, URI.class);
        assertEquals(URI.create(value), result);
    }

    @Test
    public void transformValueReturnsNullForUnsupportedTransformation() {
        final String value = "RandomValue";
        assertNull(DataTypeTransformer.transformValue(value, Person.class));
    }

    @Test
    public void testTransformStringToDate() {
        // Get rid of millis in Date, they are not expressed in the string form
        final Date date = new Date((System.currentTimeMillis() / 1000) * 1000);
        assertEquals(date, DataTypeTransformer.transformValue(date.toString(), Date.class));
    }
}
