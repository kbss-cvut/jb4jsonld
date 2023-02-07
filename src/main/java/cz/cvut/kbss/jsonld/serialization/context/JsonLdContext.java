package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;

import java.util.Optional;

/**
 * Represents the {@literal @context} JSON-LD attribute.
 */
public interface JsonLdContext {

    /**
     * Registers the specified term mapping in this context.
     * <p>
     * Typically, the {@code term} would be Java attribute (field) name and {@code iri} would be the IRI to which this
     * field is mapped.
     *
     * @param term Mapped term
     * @param iri  IRI to which the term is mapped
     */
    void registerTermMapping(String term, String iri);

    /**
     * Registers the specified term mapping in this context.
     * <p>
     * Compared to {@link #registerTermMapping(String, String)}, this method allows registering more complex mapping
     * like language containers or typed literals.
     *
     * @param term       Mapped term
     * @param mappedNode Node to which the term is mapped
     */
    void registerTermMapping(String term, ObjectNode mappedNode);

    /**
     * Gets the mapping for the specified term (if it exists).
     *
     * @param term Term to get mapping for
     * @return Optional mapping node
     */
    Optional<JsonNode> getTermMapping(String term);

    /**
     * Checks whether this JSON-LD context contains mapping for the specified term.
     *
     * @param term Term to search mapping for
     * @return {@code true} if a mapping is already defined for the term, {@code false} otherwise
     */
    default boolean hasTermMapping(String term) {
        return getTermMapping(term).isPresent();
    }

    /**
     * Gets the term mapped to the specified identifier (if it exists).
     * <p>
     * This method checks term mapping in this context and finds a term that is mapped to the specified identifier.
     *
     * @param iri Identifier the term is mapped to
     * @return Optional mapped term, empty optional if there is no such term mapping
     */
    Optional<String> getMappedTerm(String iri);

    /**
     * Checks whether this particular JSON-LD context is empty.
     * <p>
     * Term mapping inherited from any parent contexts is not considered.
     *
     * @return {@code true} if this context is empty, {@code false} otherwise
     */
    boolean isCurrentEmpty();

    /**
     * Returns an {@link ObjectNode} representing this context.
     * <p>
     * The result can thus be added to serialization output.
     *
     * @return {@code JsonNode} with registered mappings
     */
    ObjectNode getContextNode();
}
