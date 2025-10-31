package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jsonld.annotation.JsonLdType;
import java.net.URI;

/**
 * @author Yaris van Thiel
 */
@JsonLdType
public abstract class AbstractLdType {

	@Id
    public URI uri;
}
