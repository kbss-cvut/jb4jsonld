/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Test
    public void transformationPerformsTypeWideningConversionFromInteger() {
        final Integer value = 117;
        assertEquals(Long.valueOf(value), DataTypeTransformer.transformValue(value, Long.class));
        assertEquals(Float.valueOf(value), DataTypeTransformer.transformValue(value, Float.class));
        assertEquals(Double.valueOf(value), DataTypeTransformer.transformValue(value, Double.class));
    }

    @Test
    public void transformationPerformsTypeWideningConversionFromLong() {
        final Long value = System.currentTimeMillis();
        assertEquals(Float.valueOf(value), DataTypeTransformer.transformValue(value, Float.class));
        assertEquals(Double.valueOf(value), DataTypeTransformer.transformValue(value, Double.class));
    }
}
