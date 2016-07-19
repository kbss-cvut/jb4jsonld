package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;

@OWLClass(iri = Vocabulary.PERSON)
public class Person {

    @Id
    public URI uri;

    @OWLDataProperty(iri = Vocabulary.FIRST_NAME)
    private String firstName;

    @OWLDataProperty(iri = Vocabulary.LAST_NAME)
    private String lastName;
}
