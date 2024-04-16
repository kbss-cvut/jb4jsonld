/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.environment;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.environment.model.User;
import jakarta.json.JsonArray;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class TestUtil {

    public static final URI HALSEY_URI = URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Catherine+Halsey");
    public static final URI LASKY_URI = URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Thomas+Lasky");
    public static final URI PALMER_URI = URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Sarah+Palmer");
    public static final URI UNSC_URI = URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#UNSC");

    public static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    public static final String ID_FIELD_NAME = "uri";

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

    /**
     * Reads and expands JSON-LD from a file on classpath with the specified name.
     *
     * @param fileName Name of file
     * @return Expanded JSON-LD content of the file
     * @throws Exception When reading, parsing or expansion fail
     */
    public static JsonArray readAndExpand(String fileName) throws Exception {
        final InputStream is = TestUtil.class.getClassLoader().getResourceAsStream(fileName);
        assert is != null;
        final Document doc = JsonDocument.of(is);
        return JsonLd.expand(doc).get();
    }

    /**
     * Parses and expands the specified JSON-LD.
     *
     * @param jsonLdContent JSON-LD string
     * @return Expanded JSON-LD
     * @throws Exception When parsing or expansion fail
     */
    public static JsonArray parseAndExpand(String jsonLdContent) throws Exception {
        final Document doc = JsonDocument.of(new ByteArrayInputStream(jsonLdContent.getBytes(StandardCharsets.UTF_8)));
        return JsonLd.expand(doc).get();
    }
}
