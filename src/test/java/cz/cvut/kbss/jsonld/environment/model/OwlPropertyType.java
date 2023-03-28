package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.jopa.vocabulary.OWL;

public enum OwlPropertyType {

    @Individual(iri = OWL.ANNOTATION_PROPERTY)
    ANNOTATION_PROPERTY,
    @Individual(iri = OWL.DATATYPE_PROPERTY)
    DATATYPE_PROPERTY,
    @Individual(iri = OWL.OBJECT_PROPERTY)
    OBJECT_PROPERTY;

    public static String getMappedIndividual(OwlPropertyType constant) {
        switch (constant) {
            case ANNOTATION_PROPERTY:
                return OWL.ANNOTATION_PROPERTY;
            case DATATYPE_PROPERTY:
                return OWL.DATATYPE_PROPERTY;
            default:
                return OWL.OBJECT_PROPERTY;
        }
    }
}
