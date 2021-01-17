package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Set;

@Namespaces({@Namespace(prefix = "jb4jsonld", namespace = Vocabulary.DEFAULT_PREFIX),
             @Namespace(prefix = "rdfs", namespace = RDFS.NAMESPACE)})
@OWLClass(iri = "jb4jsonld:Study")
public class StudyWithNamespaces {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = "rdfs:label")
    private String name;

    @OWLObjectProperty(iri = "jb4jsonld:hasMember")
    private Set<Employee> members;

    @OWLObjectProperty(iri = "jb4jsonld:hasParticipant")
    private Set<Employee> participants;

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
    }

    public Set<Employee> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Employee> participants) {
        this.participants = participants;
    }
}
