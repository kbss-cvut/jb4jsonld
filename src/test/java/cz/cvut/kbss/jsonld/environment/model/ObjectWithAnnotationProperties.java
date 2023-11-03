/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
public class ObjectWithAnnotationProperties implements GeneratesRdf {

    @Id
    private URI id;

    @OWLAnnotationProperty(iri = Vocabulary.CHANGED_VALUE)
    private String changedValue;

    @OWLAnnotationProperty(iri = Vocabulary.ORIGIN)
    private Set<Object> origins;

    public ObjectWithAnnotationProperties() {
    }

    public ObjectWithAnnotationProperties(URI id) {
        this.id = id;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public URI getUri() {
        return id;
    }

    public String getChangedValue() {
        return changedValue;
    }

    public void setChangedValue(String changedValue) {
        this.changedValue = changedValue;
    }

    public Set<Object> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<Object> origins) {
        this.origins = origins;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(id)) {
            return;
        }
        visited.add(id);
        final IRI iri = vf.createIRI(id.toString());
        model.add(iri, RDF.TYPE, vf.createIRI(Vocabulary.OBJECT_WITH_ANNOTATIONS));
        if (changedValue != null) {
            model.add(iri, vf.createIRI(Vocabulary.CHANGED_VALUE), vf.createLiteral(changedValue));
        }
        if (origins != null) {
            origins.forEach(o -> {
                assert o instanceof URI;
                model.add(iri, vf.createIRI(Vocabulary.ORIGIN), vf.createIRI(o.toString()));
            });
        }
    }
}
