/**
 * Copyright (C) 2022 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.datatype.exception.UnsupportedTypeTransformationException;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.Role;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DataTypeTransformerTest {

    @Test
    void transformValueTransformsStringToUri() {
        final String value = Generator.generateUri().toString();
        final Object result = DataTypeTransformer.transformValue(value, URI.class);
        assertEquals(URI.create(value), result);
    }

    @Test
    void transformValueThrowsUnsupportedTypeTransformationExceptionForUnsupportedTransformation() {
        final String value = "RandomValue";
        assertThrows(UnsupportedTypeTransformationException.class, () -> DataTypeTransformer.transformValue(value, Person.class));
    }

    @Test
    void testTransformStringToDate() {
        // Get rid of millis in Date, they are not expressed in the string form
        final Date date = new Date((System.currentTimeMillis() / 1000) * 1000);
        assertEquals(date, DataTypeTransformer.transformValue(date.toString(), Date.class));
    }

    @Test
    void transformationPerformsTypeWideningConversionFromInteger() {
        final int value = 117;
        assertEquals(Long.valueOf(value), DataTypeTransformer.transformValue(value, Long.class));
        assertEquals(Float.valueOf(value), DataTypeTransformer.transformValue(value, Float.class));
        assertEquals(Double.valueOf(value), DataTypeTransformer.transformValue(value, Double.class));
    }

    @Test
    void transformationPerformsTypeWideningConversionFromLong() {
        final long value = System.currentTimeMillis();
        assertEquals(Float.valueOf(value), DataTypeTransformer.transformValue(value, Float.class));
        assertEquals(Double.valueOf(value), DataTypeTransformer.transformValue(value, Double.class));
    }

    @Test
    void transformationReturnsValueIfItsTypeAlreadyCorrespondsToTarget() {
        final String value = "halsey@unsc.org";
        assertSame(value, DataTypeTransformer.transformValue(value, String.class));
    }

    @Test
    void transformationTransformsAnyValueToString() {
        assertEquals(Boolean.TRUE.toString(), DataTypeTransformer.transformValue(true, String.class));
        assertEquals(Integer.toString(117), DataTypeTransformer.transformValue(117, String.class));
        assertEquals(Float.toString(3.141792F), DataTypeTransformer.transformValue(3.141792F, String.class));
    }

    @Test
    void transformationTransformsStringValueToEnumConstant() {
        assertEquals(Role.GUEST, DataTypeTransformer.transformValue(Role.GUEST.toString(), Role.class));
    }

    @Test
    void transformationTransformsOffsetDateTimeToLocalDateTime() {
        final OffsetDateTime value = OffsetDateTime.now();
        assertEquals(value.toLocalDateTime(), DataTypeTransformer.transformValue(value, LocalDateTime.class));
    }

    @Test
    void transformationTransformsLangStringToMultilingualString() {
        final LangString value = new LangString("building", "en");
        assertEquals(new MultilingualString(Collections.singletonMap(value.getLanguage().get(), value.getValue())),
                DataTypeTransformer.transformValue(value, MultilingualString.class));
    }
}
