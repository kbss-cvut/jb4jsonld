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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.time.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class XSDTypeCoercerTest {

    @ParameterizedTest
    @MethodSource("xsdLiterals")
    void coerceTypeTransformsStringLiteralToExpectedType(String value, String type, Object expected) {
        assertEquals(expected, XSDTypeCoercer.coerceType(value, type));
    }

    private static Stream<Arguments> xsdLiterals() {
        return Stream.of(
                Arguments.arguments("117", XSD.INT, 117),
                Arguments.arguments("117", XSD.INTEGER, new BigInteger(Integer.toString(117))),
                Arguments.arguments("117", XSD.SHORT, (short) 117),
                Arguments.arguments("true", XSD.BOOLEAN, true),
                Arguments.arguments("3.14", XSD.FLOAT, 3.14f),
                Arguments.arguments("3.14", XSD.DOUBLE, 3.14),
                Arguments.arguments("2020-06-16", XSD.DATE, LocalDate.of(2020, 6, 16)),
                Arguments.arguments("2020-06-16T08:27:25Z", XSD.DATETIME,
                        ISO_DATE_TIME.parse("2020-06-16T08:27:25Z", OffsetDateTime::from)),
                Arguments.arguments("2020-06-16T08:27:25", XSD.DATETIME,
                        ISO_DATE_TIME.parse("2020-06-16T08:27:25", LocalDateTime::from).atOffset(DateTimeUtil.SYSTEM_OFFSET)),
                Arguments.arguments("13:47:30", XSD.TIME, LocalTime.of(13, 47, 30).atOffset(DateTimeUtil.SYSTEM_OFFSET)),
                Arguments.arguments("PT17S", XSD.DURATION, Duration.ofSeconds(17))
        );
    }
}
