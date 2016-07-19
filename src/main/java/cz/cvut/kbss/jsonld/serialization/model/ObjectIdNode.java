package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Represents a field value that should be serialized as an identifier of the referenced object.
 */
public class ObjectIdNode extends JsonNode {

    private final URI identifier;

    public ObjectIdNode(URI identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public ObjectIdNode(String name, URI identifier) {
        super(name);
        this.identifier = Objects.requireNonNull(identifier);
    }

    @Override
    void writeValue(JsonSerializer writer) throws IOException {
        writer.writeString(identifier.toString());
    }
}
