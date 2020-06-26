package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class XSDTypeCoercerTest {

    @ParameterizedTest
    @MethodSource("xsdLiterals")
    void coerceTypeTransformsStringLiteralToExpectedType(String value, String type, Object expected) {
        assertEquals(expected, XSDTypeCoercer.coerceType(value, type));
    }

    private static Stream<Arguments> xsdLiterals() throws Exception {
        return Stream.of(
                Arguments.arguments("117", XSD.INT, 117),
                Arguments.arguments("117", XSD.INTEGER, 117),
                Arguments.arguments("117", XSD.SHORT, (short) 117),
                Arguments.arguments("true", XSD.BOOLEAN, true),
                Arguments.arguments("3.14", XSD.FLOAT, 3.14f),
                Arguments.arguments("3.14", XSD.DOUBLE, 3.14),
                Arguments.arguments("2020-06-16T08:27:25Z", XSD.DATETIME,
                        ISO_DATE_TIME.parse("2020-06-16T08:27:25Z", ZonedDateTime::from)),
                Arguments.arguments("2020-06-16T08:27:25", XSD.DATETIME,
                        ISO_DATE_TIME.parse("2020-06-16T08:27:25", LocalDateTime::from)),
                Arguments.arguments("13:47:30", XSD.TIME, LocalTime.of(13, 47, 30)),
                Arguments.arguments("PT17S", XSD.DURATION, DatatypeFactory.newInstance().newDuration(17000))
        );
    }
}
