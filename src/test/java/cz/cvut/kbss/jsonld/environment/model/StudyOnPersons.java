package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.STUDY)
public class StudyOnPersons {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = RDFS.LABEL)
    private String name;

    @OWLObjectProperty(iri = Vocabulary.HAS_PARTICIPANT)
    private Set<Person> participants;

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

    public Set<Person> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Person> participants) {
        this.participants = participants;
    }
}
