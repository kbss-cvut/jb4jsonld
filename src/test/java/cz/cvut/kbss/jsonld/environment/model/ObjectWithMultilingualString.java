package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;

@OWLClass(iri = Vocabulary.STUDY)
public class ObjectWithMultilingualString {

    @Id
    private URI id;

    @OWLDataProperty(iri = RDFS.LABEL)
    private MultilingualString label;

    public ObjectWithMultilingualString() {
    }

    public ObjectWithMultilingualString(URI id) {
        this.id = id;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public MultilingualString getLabel() {
        return label;
    }

    public void setLabel(MultilingualString label) {
        this.label = label;
    }
}
