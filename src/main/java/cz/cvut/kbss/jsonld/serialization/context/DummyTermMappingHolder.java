package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DummyTermMappingHolder extends TermMappingHolder {

    static final DummyTermMappingHolder INSTANCE = new DummyTermMappingHolder();

    private DummyTermMappingHolder() {
        super(null);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return true;
    }

    @Override
    void registerTermMapping(String Term, JsonNode node) {
        // Do nothing
    }

    @Override
    Optional<JsonNode> getTermMapping(String term) {
        return Optional.empty();
    }

    @Override
    Map<String, JsonNode> getMapping() {
        return Collections.emptyMap();
    }

    @Override
    boolean hasTermMapping(String term) {
        return false;
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return false;
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    boolean isRoot() {
        return true;
    }
}
