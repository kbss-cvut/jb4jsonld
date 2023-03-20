package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.ATTRIBUTE)
public class Attribute implements GeneratesRdf {

    @Id
    private URI uri;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.HAS_PROPERTY_TYPE)
    private OwlPropertyType propertyType;

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public OwlPropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(OwlPropertyType propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(getUri())) {
            return;
        }
        model.add(vf.createIRI(uri.toString()), RDF.TYPE, vf.createIRI(Vocabulary.ATTRIBUTE));
        model.add(vf.createIRI(uri.toString()), vf.createIRI(Vocabulary.HAS_PROPERTY_TYPE),
                  vf.createIRI(OwlPropertyType.getMappedIndividual(propertyType)));
    }
}
