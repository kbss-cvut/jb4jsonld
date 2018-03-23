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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.Configurable;
import cz.cvut.kbss.jsonld.deserialization.util.ClasspathScanner;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;

import java.util.Objects;

/**
 * Takes a pre-processed JSON-LD structure and deserializes it.
 */
public abstract class JsonLdDeserializer implements Configurable {

    private final Configuration configuration;

    protected final TypeMap typeMap = new TypeMap();

    protected JsonLdDeserializer() {
        this.configuration = new Configuration();
        buildTypeMap();
    }

    protected JsonLdDeserializer(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        buildTypeMap();
    }

    private void buildTypeMap() {
        final String scanPath = configuration.get(ConfigParam.SCAN_PACKAGE, "");
        new ClasspathScanner(c -> {
            final OWLClass ann = c.getDeclaredAnnotation(OWLClass.class);
            if (ann != null) {
                typeMap.register(ann.iri(), c);
            }
        }).processClasses(scanPath);
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Deserializes the specified JSON-LD data.
     *
     * @param jsonLd      JSON-LD structure
     * @param resultClass Type of the result instance
     * @return Deserialized Java instance
     */
    public abstract <T> T deserialize(Object jsonLd, Class<T> resultClass);

    /**
     * Creates deserializer for expanded JSON-LD, initialized with the specified configuration.
     *
     * @param configuration Configuration of the deserializer
     * @return New deserializer
     */
    public static JsonLdDeserializer createExpandedDeserializer(Configuration configuration) {
        return new ExpandedJsonLdDeserializer(configuration);
    }

    /**
     * Creates deserializer for expanded JSON-LD.
     *
     * @return New deserializer
     */
    public static JsonLdDeserializer createExpandedDeserializer() {
        return new ExpandedJsonLdDeserializer();
    }
}
