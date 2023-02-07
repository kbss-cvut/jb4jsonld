package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Map;
import java.util.Optional;

class WriteThroughTermMappingHolder extends TermMappingHolder {

    public WriteThroughTermMappingHolder(TermMappingHolder parentContext) {
        super(parentContext);
    }

    @Override
    boolean hasTermMapping(String term) {
        return parentContext.hasTermMapping(term);
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return parentContext.hasTermMapping(term, mappedNode);
    }

    @Override
    public void registerTermMapping(String term, JsonNode mappedNode) {
        assert canRegisterTermMapping(term, mappedNode);
        parentContext.registerTermMapping(term, mappedNode);
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return parentContext.getTermMapping(term);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return !parentContext.hasTermMapping(term) || parentContext.getTermMapping(term).get().equals(mappedNode);
    }

    @Override
    Map<String, JsonNode> getMapping() {
        return parentContext.getMapping();
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    boolean isRoot() {
        return false;
    }
}
