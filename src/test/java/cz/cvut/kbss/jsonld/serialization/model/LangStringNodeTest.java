/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class LangStringNodeTest extends AbstractNodeTest {

    @Test
    void writeValueWritesValueAndLanguageAsStrings() throws Exception {
        final String value = "test";
        final String language = "en";
        final LangStringNode sut = new LangStringNode(value, language);
        sut.write(serializerMock);
        final InOrder inOrder = Mockito.inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LANGUAGE);
        inOrder.verify(serializerMock).writeString(language);
        inOrder.verify(serializerMock).writeFieldName(JsonLd.VALUE);
        inOrder.verify(serializerMock).writeString(value);
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    @Test
    void writeValueWritesValueAndNoneWhenLanguageTagIsNotSpecified() throws Exception {
        final String value = "test";
        final LangStringNode sut = new LangStringNode(value, null);
        sut.write(serializerMock);
        final InOrder inOrder = Mockito.inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LANGUAGE);
        inOrder.verify(serializerMock).writeString(JsonLd.NONE);
        inOrder.verify(serializerMock).writeFieldName(JsonLd.VALUE);
        inOrder.verify(serializerMock).writeString(value);
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
