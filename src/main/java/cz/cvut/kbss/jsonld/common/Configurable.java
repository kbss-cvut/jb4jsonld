package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.Configuration;

/**
 * Indicates that this object may be configured.
 */
public interface Configurable {

    /**
     * Applies the specified configuration on this object.
     *
     * @param configuration Configuration to apply
     */
    default void configure(Configuration configuration) {
    }
}
