package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.*;

class EmbeddedTermMappingHolder extends TermMappingHolder {

    private final Map<String, JsonNode> mapping = new HashMap<>();

    EmbeddedTermMappingHolder() {
        super(DummyTermMappingHolder.INSTANCE);
    }

    EmbeddedTermMappingHolder(TermMappingHolder parentContext) {
        super(parentContext);
    }

    @Override
    boolean canRegisterTermMapping(String term, JsonNode mappedNode) {
        return true;
    }

    @Override
    void registerTermMapping(String term, JsonNode mappedNode) {
        Objects.requireNonNull(term);
        Objects.requireNonNull(mappedNode);
        if (!isRoot() && !parentContext.hasTermMapping(term)) {
            parentContext.registerTermMapping(term, mappedNode);
        } else {
            mapping.put(term, mappedNode);
        }
    }

    private boolean isRoot() {
        return parentContext == DummyTermMappingHolder.INSTANCE;
    }

    @Override
    public Map<String, JsonNode> getMapping() {
        return Collections.unmodifiableMap(mapping);
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return mapping.containsKey(term) ? Optional.of(mapping.get(term)) : parentContext.getTermMapping(term);
    }

    @Override
    public boolean hasTermMapping(String term) {
        return mapping.containsKey(term) || parentContext.hasTermMapping(term);
    }

    @Override
    boolean hasTermMapping(String term, JsonNode mappedNode) {
        return mapping.containsKey(term) && mapping.get(term).equals(mappedNode);
    }

    @Override
    boolean isEmpty() {
        return mapping.isEmpty();
    }
}
