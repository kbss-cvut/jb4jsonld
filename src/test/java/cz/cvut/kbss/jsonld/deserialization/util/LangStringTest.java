package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LangStringTest {

    @Test
    void getLanguageReturnsNullForNoneLanguage() {
        final LangString langString = new LangString("test", JsonLd.NONE);
        assertNull(langString.getLanguage());
    }

    @Test
    void getLanguageReturnsLanguageByDefault() {
        final LangString langString = new LangString("test", "en");
        assertEquals("en", langString.getLanguage());
    }
}
