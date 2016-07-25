package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Date;
import java.util.Set;

@OWLClass(iri = Vocabulary.ORGANIZATION)
public class Organization {

    private static final String DEFAULT_COUNTRY = "Czech Republic";

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = Vocabulary.DATE_CREATED)
    private Date dateCreated;

    @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
    private String name;

    private Long age;

    @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
    private Set<Employee> employees;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
