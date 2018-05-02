package cz.cvut.kbss.jsonld.common;

import java.util.Random;

/**
 * Allows to generate blank nodes for identifier-less instances.
 * <p>
 * Although objects in JSON-LD are not required to have id, some tools may have issues processing such data. In addition,
 * multiple references to the same instance cannot be created when serializing the JSON-LD, as there is no identifier to
 * reference.
 */
public class IdentifierUtil {

    private static final Random RANDOM = new Random();

    /**
     * Generates a (pseudo)random blank node identifier.
     *
     * @return Blank node identifier
     */
    public static String generateBlankNodeId() {
        return "_:" + RANDOM.nextInt(Integer.MAX_VALUE);
    }
}
