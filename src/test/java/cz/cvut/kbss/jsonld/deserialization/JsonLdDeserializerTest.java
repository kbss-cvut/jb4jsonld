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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.environment.model.StudyWithNamespaces;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static cz.cvut.kbss.jsonld.environment.TestUtil.readAndExpand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLdDeserializerTest {

    @Test
    void constructionScansClasspathAndBuildsTypeMap() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        assertFalse(typeMap(deserializer).get(Vocabulary.STUDY).isEmpty());
        assertTrue(typeMap(deserializer).get(Vocabulary.STUDY).contains(Study.class));
    }

    private TypeMap typeMap(JsonLdDeserializer deserializer) throws Exception {
        final Field typeMapField = TargetClassResolver.class.getDeclaredField("typeMap");
        typeMapField.setAccessible(true);
        return (TypeMap) typeMapField.get(deserializer.classResolver);
    }

    @Test
    void constructionScansClasspathWithSpecifiedPackageAndBuildsTypeMap() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.deserialization");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        assertTrue(typeMap(deserializer).get(Vocabulary.GENERIC_MEMBER).isEmpty());
        assertTrue(typeMap(deserializer).get(Vocabulary.AGENT).contains(TestClass.class));
    }

    @OWLClass(iri = Vocabulary.AGENT)
    private static class TestClass {
    }

    @Test
    void deserializationThrowsJsonLdDeserializationExceptionWhenInputIsNotValidJsonLd() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        final JsonValue input = readAndExpand("invalidJsonLd.json");
        assertThrows(JsonLdDeserializationException.class, () -> deserializer.deserialize(input, User.class));
    }

    @Test
    void constructionExpandsCompactIrisWhenBuildingTypeMap() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        assertFalse(typeMap(deserializer).get(Vocabulary.STUDY).isEmpty());
        assertThat(typeMap(deserializer).get(Vocabulary.STUDY), hasItem(StudyWithNamespaces.class));
    }
}
