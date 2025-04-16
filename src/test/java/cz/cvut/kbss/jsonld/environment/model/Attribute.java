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

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.ATTRIBUTE)
public class Attribute implements GeneratesRdf {

    @Id
    private URI uri;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.HAS_PROPERTY_TYPE)
    private OwlPropertyType propertyType;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.HAS_PLURAL_PROPERTY_TYPE)
    private Set<OwlPropertyType> pluralPropertyType;

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public OwlPropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(OwlPropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public Set<OwlPropertyType> getPluralPropertyType() {
        return pluralPropertyType;
    }

    public void setPluralPropertyType(Set<OwlPropertyType> pluralPropertyType) {
        this.pluralPropertyType = pluralPropertyType;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(getUri())) {
            return;
        }
        model.add(vf.createIRI(uri.toString()), RDF.TYPE, vf.createIRI(Vocabulary.ATTRIBUTE));
        if (propertyType != null) {
            model.add(vf.createIRI(uri.toString()), vf.createIRI(Vocabulary.HAS_PROPERTY_TYPE),
                      vf.createIRI(OwlPropertyType.getMappedIndividual(propertyType)));
        }
        if (getPluralPropertyType() != null) {
            getPluralPropertyType().forEach(
                    t -> model.add(vf.createIRI(uri.toString()), vf.createIRI(Vocabulary.HAS_PLURAL_PROPERTY_TYPE),
                                   vf.createIRI(OwlPropertyType.getMappedIndividual(t))));
        }
    }
}
