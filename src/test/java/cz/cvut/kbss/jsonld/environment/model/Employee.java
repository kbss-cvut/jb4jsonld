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

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

@OWLClass(iri = Vocabulary.EMPLOYEE)
public class Employee extends User {

    @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
    private Organization employer;

    public Organization getEmployer() {
        return employer;
    }

    public void setEmployer(Organization employer) {
        this.employer = employer;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(uri)) {
            return;
        }
        visited.add(uri);
        super.toRdf(model, vf, visited);
        final IRI id = vf.createIRI(uri.toString());
        model.add(id, RDF.TYPE, vf.createIRI(Vocabulary.EMPLOYEE));
        if (employer != null) {
            model.add(id, vf.createIRI(Vocabulary.IS_MEMBER_OF), vf.createIRI(employer.getUri().toString()));
            employer.toRdf(model, vf, visited);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(getUri(), employee.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    public static Field getEmployerField() throws NoSuchFieldException {
        return Employee.class.getDeclaredField("employer");
    }
}
