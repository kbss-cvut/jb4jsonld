/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.annotation.JsonLdProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.USER)
public class User extends Person {

    @OWLDataProperty(iri = Vocabulary.USERNAME)
    private String username;

    @OWLAnnotationProperty(iri = Vocabulary.IS_ADMIN)
    private Boolean admin;

    @JsonLdProperty(access = JsonLdProperty.Access.WRITE_ONLY)
    @OWLDataProperty(iri = Vocabulary.PASSWORD)
    private String password;

    @OWLDataProperty(iri = Vocabulary.ROLE)
    private Role role;

    @Types
    private Set<String> types;

    public User() {
    }

    public User(URI uri, String firstName, String lastName, String username, Boolean admin) {
        this.setUri(uri);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.username = username;
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        super.toRdf(model, vf, visited);
        final IRI id = vf.createIRI(uri.toString());
        model.add(id, RDF.TYPE, vf.createIRI(Vocabulary.USER));
        model.add(id, vf.createIRI(Vocabulary.USERNAME), vf.createLiteral(username));
        model.add(id, vf.createIRI(Vocabulary.IS_ADMIN), vf.createLiteral(admin));
        // Skip password, it is write-only
        if (role != null) {
            model.add(id, vf.createIRI(Vocabulary.ROLE), vf.createLiteral(role.toString()));
        }
        if (types != null) {
            types.forEach(t -> model.add(id, RDF.TYPE, vf.createIRI(t)));
        }
    }

    public static Field getUsernameField() throws NoSuchFieldException {
        return User.class.getDeclaredField("username");
    }
}
