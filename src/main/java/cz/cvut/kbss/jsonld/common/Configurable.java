package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.Configuration;

/**
 * Marker interface for classes whose instances can be configured.
 */
public interface Configurable {

    /**
     * Gets the configuration holder for this JSON-LD processor.
     *
     * @return Configuration
     */
    Configuration configure();
}
