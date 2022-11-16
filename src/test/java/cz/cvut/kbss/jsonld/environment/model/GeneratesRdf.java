package cz.cvut.kbss.jsonld.environment.model;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;

import java.net.URI;
import java.util.Set;

public interface GeneratesRdf {

    URI getUri();

    void toRdf(Model model, ValueFactory vf, Set<URI> visited);
}
