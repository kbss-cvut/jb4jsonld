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
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@OWLClass(iri = Vocabulary.PERSON)
public class Person {

    @Id
    public URI uri;

    @OWLDataProperty(iri = Vocabulary.FIRST_NAME)
    private String firstName;

    @OWLDataProperty(iri = Vocabulary.LAST_NAME)
    private String lastName;

    @Properties
    private Map<String, Set<String>> properties;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Map<String, Set<String>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Set<String>> properties) {
        this.properties = properties;
    }

    public static Field getFirstNameField() throws NoSuchFieldException {
        return Person.class.getDeclaredField("firstName");
    }

    public static Field getLastNameField() throws NoSuchFieldException {
        return Person.class.getDeclaredField("lastName");
    }
}
