package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.SKOS;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = SKOS.CONCEPT)
public class ObjectWithPluralMultilingualString {

    @Id
    URI uri;

    @OWLDataProperty(iri = SKOS.ALT_LABEL)
    private Set<MultilingualString> altLabel;

    public ObjectWithPluralMultilingualString() {
    }

    public ObjectWithPluralMultilingualString(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Set<MultilingualString> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(Set<MultilingualString> altLabel) {
        this.altLabel = altLabel;
    }
}
