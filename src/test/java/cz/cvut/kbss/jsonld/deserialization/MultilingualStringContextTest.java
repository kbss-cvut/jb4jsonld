package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultilingualStringContextTest {

    private MultilingualStringContext sut;

    @BeforeEach
    void setUp() {
        this.sut = new MultilingualStringContext(new MultilingualString(), Collections.emptyMap());
    }

    @Test
    void addItemAddsLangStringToCurrentInstance() {
        final LangString ls = new LangString("building", "en");
        sut.addItem(ls);
        assertTrue(sut.getInstance().contains("en"));
        assertEquals(ls.getValue(), sut.getInstance().get("en"));
    }

    @Test
    void addItemSetsSimpleLiteralInCurrentInstanceWhenStringIsPassedAsArgument() {
        final String value = "Mjolnir";
        sut.addItem(value);
        assertTrue(sut.getInstance().containsSimple());
        assertEquals(value, sut.getInstance().get());
    }
}
