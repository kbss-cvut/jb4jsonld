package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

@OWLClass(iri = Vocabulary.EMPLOYEE)
public class Employee extends User {

    @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
    private Organization employer;

    public Organization getEmployer() {
        return employer;
    }

    public void setEmployer(Organization employer) {
        this.employer = employer;
    }
}
