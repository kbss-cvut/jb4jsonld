package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

@OWLClass(iri = Vocabulary.USER)
public class User extends Person {

    @OWLDataProperty(iri = Vocabulary.USERNAME)
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
