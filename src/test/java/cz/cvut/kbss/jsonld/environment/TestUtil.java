/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.environment;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.model.*;

import java.io.InputStream;

public class TestUtil {

    private TestUtil() {
        throw new AssertionError();
    }

    public static TypeMap getDefaultTypeMap() {
        final TypeMap tm = new TypeMap();
        tm.register(Vocabulary.EMPLOYEE, Employee.class);
        tm.register(Vocabulary.ORGANIZATION, Organization.class);
        tm.register(Vocabulary.PERSON, Person.class);
        tm.register(Vocabulary.STUDY, Study.class);
        tm.register(Vocabulary.USER, User.class);
        return tm;
    }

    public static Object readAndExpand(String fileName) throws Exception {
        final InputStream is = TestUtil.class.getClassLoader().getResourceAsStream(fileName);
        final Object jsonObject = JsonUtils.fromInputStream(is);
        return JsonLdProcessor.expand(jsonObject);
    }
}
