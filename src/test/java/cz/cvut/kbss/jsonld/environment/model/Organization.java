package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Date;

@OWLClass(iri = Vocabulary.ORGANIZATION)
public class Organization {

    private static final String DEFAULT_COUNTRY = "Czech Republic";

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = Vocabulary.DATE_CREATED)
    private Date dateCreated;

    private Long age;
}
