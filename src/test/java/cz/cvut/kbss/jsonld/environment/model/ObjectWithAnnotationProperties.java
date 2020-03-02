package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
public class ObjectWithAnnotationProperties {

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
}
