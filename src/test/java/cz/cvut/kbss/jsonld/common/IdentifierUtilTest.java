package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierUtilTest {

    @Test
    void isCompactIriReturnsFalseForExpandedAbsoluteIri() {
        assertFalse(IdentifierUtil.isCompactIri(Generator.generateUri().toString()));
    }

    @Test
    void isCompactIriReturnsFalseForBlankNodeIdentifier() {
        assertFalse(IdentifierUtil.isCompactIri(IdentifierUtil.generateBlankNodeId()));
    }

    @Test
    void isCompactIriReturnsTrueForCompactIri() {
        final String compact = "dc:description";
        assertTrue(IdentifierUtil.isCompactIri(compact));
    }
}
