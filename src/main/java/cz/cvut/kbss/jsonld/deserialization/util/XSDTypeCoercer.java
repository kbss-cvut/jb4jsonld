package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.vocabulary.XSD;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XSDTypeCoercer {

    public static Object coerceType(String value, String type) {
        switch (type) {
            case XSD.BOOLEAN:
                return Boolean.parseBoolean(value);
            case XSD.SHORT:
                return Short.parseShort(value);
            case XSD.INT:
            case XSD.INTEGER:
                return Integer.parseInt(value);
            case XSD.LONG:
                return Long.parseLong(value);
            case XSD.FLOAT:
                return Float.parseFloat(value);
            case XSD.DOUBLE:
                return Double.parseDouble(value);
            case XSD.DATE:
            case XSD.DATETIME:
                return LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(value));
            default:
                throw new IllegalArgumentException("Unsupported type for XSD type coercion: " + type);
        }
    }
}
