package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;

@OWLClass(iri = Vocabulary.GENERIC_MEMBER)
public class GenericMember {

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
}
