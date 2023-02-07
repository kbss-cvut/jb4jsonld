package cz.cvut.kbss.jsonld.serialization.context;

public class MappingJsonLdContextFactory implements JsonLdContextFactory {

    @Override
    public JsonLdContext createJsonLdContext() {
        return new MappingJsonLdContext();
    }

    @Override
    public JsonLdContext createJsonLdContext(JsonLdContext parent) {
        return new MappingJsonLdContext(parent);
    }
}
