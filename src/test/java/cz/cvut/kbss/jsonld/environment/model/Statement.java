package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.RDFS;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = RDF.STATEMENT)
public class Statement {

    @OWLObjectProperty(iri = RDF.SUBJECT)
    private Iri subject;
    @OWLObjectProperty(iri = RDF.PREDICATE)
    private Iri predicate;
    @OWLObjectProperty(iri = RDF.OBJECT)
    private Iri object;
    @OWLAnnotationProperty(iri = DC.Terms.SOURCE)
    private Iri context;

    @OWLAnnotationProperty(iri = DC.Terms.IS_REFERENCED_BY)
    private Set<Iri> isReferencedBy;

    public Statement() {
    }

    public Statement(Iri subject, Iri predicate, Iri object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public Statement(Iri subject, Iri predicate, Iri object, Iri context) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
    }

    @OWLClass(iri = RDFS.RESOURCE)
    public record Iri(@Id URI value) {}

    public Iri getSubject() {
        return subject;
    }

    public void setSubject(Iri subject) {
        this.subject = subject;
    }

    public Iri getPredicate() {
        return predicate;
    }

    public void setPredicate(Iri predicate) {
        this.predicate = predicate;
    }

    public Iri getObject() {
        return object;
    }

    public void setObject(Iri object) {
        this.object = object;
    }

    public Iri getContext() {
        return context;
    }

    public void setContext(Iri context) {
        this.context = context;
    }

    public Set<Iri> getIsReferencedBy() {
        return isReferencedBy;
    }

    public void setIsReferencedBy(Set<Iri> isReferencedBy) {
        this.isReferencedBy = isReferencedBy;
    }
}
