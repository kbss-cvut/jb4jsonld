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

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.annotation.JsonLdProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.net.URI;
import java.util.Set;

@JsonLdAttributeOrder({"uri", "name", "participants", "members"})
@OWLClass(iri = Vocabulary.STUDY)
public class Study implements GeneratesRdf {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = RDFS.LABEL)
    private String name;

    @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
    private Set<Employee> members;

    @OWLObjectProperty(iri = Vocabulary.HAS_PARTICIPANT)
    private Set<Employee> participants;

    @JsonLdProperty(access = JsonLdProperty.Access.READ_ONLY)
    @OWLDataProperty(iri = Vocabulary.NUMBER_OF_PEOPLE_INVOLVED)
    private Integer noOfPeopleInvolved;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Employee> getMembers() {
        return members;
    }

    public void setMembers(Set<Employee> members) {
        this.members = members;
        calculatePeopleInvolvedCount();
    }

    public Set<Employee> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Employee> participants) {
        this.participants = participants;
        calculatePeopleInvolvedCount();
    }

    private void calculatePeopleInvolvedCount() {
        this.noOfPeopleInvolved =
                (members != null ? members.size() : 0) + (participants != null ? participants.size() : 0);
    }

    public Integer getNoOfPeopleInvolved() {
        return noOfPeopleInvolved;
    }

    public void setNoOfPeopleInvolved(Integer noOfPeopleInvolved) {
        this.noOfPeopleInvolved = noOfPeopleInvolved;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(uri)) {
            return;
        }
        visited.add(uri);
        final IRI id = vf.createIRI(uri.toString());
        model.add(id, RDF.TYPE, vf.createIRI(Vocabulary.STUDY));
        model.add(id, vf.createIRI(RDFS.LABEL), vf.createLiteral(name));
        model.add(id, vf.createIRI(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED),
                  vf.createLiteral(noOfPeopleInvolved.toString(), vf.createIRI(XSD.INTEGER)));
        if (members != null) {
            members.forEach(m -> {
                model.add(id, vf.createIRI(Vocabulary.HAS_MEMBER), vf.createIRI(m.getUri().toString()));
                m.toRdf(model, vf, visited);
            });
        }
        if (participants != null) {
            participants.forEach(p -> {
                model.add(id, vf.createIRI(Vocabulary.HAS_PARTICIPANT), vf.createIRI(p.getUri().toString()));
                p.toRdf(model, vf, visited);
            });
        }
    }
}
