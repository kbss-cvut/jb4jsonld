package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class XSDTypeCoercer {

    private static final Logger LOG = LoggerFactory.getLogger(XSDTypeCoercer.class);

    private static DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.error("Unable to create XML DatatypeFactory", e);
        }
    }

    public static Object coerceType(String value, String type) {
        switch (type) {
            case XSD.BOOLEAN:
                return Boolean.parseBoolean(value);
            case XSD.BYTE:
                return Byte.parseByte(value);
            case XSD.SHORT:
            case XSD.UNSIGNED_BYTE:
                return Short.parseShort(value);
            case XSD.INT:
            case XSD.INTEGER:
            case XSD.NON_NEGATIVE_INTEGER:
            case XSD.NON_POSITIVE_INTEGER:
            case XSD.POSITIVE_INTEGER:
            case XSD.NEGATIVE_INTEGER:
            case XSD.UNSIGNED_LONG:
                return Integer.parseInt(value);
            case XSD.LONG:
            case XSD.UNSIGNED_INT:
                return Long.parseLong(value);
            case XSD.FLOAT:
                return Float.parseFloat(value);
            case XSD.DOUBLE:
                return Double.parseDouble(value);
            case XSD.DATE:
            case XSD.DATETIME:
                return parseDateTime(value);
            case XSD.TIME:
                return DateTimeFormatter.ISO_TIME.parse(value, LocalTime::from);
            case XSD.DURATION:
                return DATATYPE_FACTORY != null ? DATATYPE_FACTORY.newDuration(value) : Duration.parse(value);
            default:
                throw new IllegalArgumentException("Unsupported type for XSD type coercion: " + type);
        }
    }

    private static Object parseDateTime(String value) {
        try {
            return DateTimeFormatter.ISO_DATE_TIME.parse(value, ZonedDateTime::from);
        } catch (DateTimeParseException e) {
            // If it's not zoned date time, let's try local date time
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(value, LocalDateTime::from);
        }
    }
}
