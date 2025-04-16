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

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.GENERIC_MEMBER)
public class GenericMember implements GeneratesRdf {

    @Id
    private URI uri;

    @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
    private Object memberOf;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(Object memberOf) {
        this.memberOf = memberOf;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(uri)) {
            return;
        }
        visited.add(uri);
        final IRI id = vf.createIRI(uri.toString());
        model.add(id, RDF.TYPE, vf.createIRI(Vocabulary.GENERIC_MEMBER));
        if (memberOf != null && memberOf instanceof GeneratesRdf org) {
            model.add(id, vf.createIRI(Vocabulary.IS_MEMBER_OF), vf.createIRI(org.getUri().toString()));
            org.toRdf(model, vf, visited);
        }
    }
}
