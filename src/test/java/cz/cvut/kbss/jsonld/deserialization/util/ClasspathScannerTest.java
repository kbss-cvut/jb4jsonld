package cz.cvut.kbss.jsonld.deserialization.util;

import org.junit.jupiter.api.Test;

class ClasspathScannerTest {

    /**
     * Bug #18
     */
    @Test
    void processClassesSupportsMultiReleaseJarFiles() {
        // Empty consumer
        final ClasspathScanner sut = new ClasspathScanner(cls -> {
        });
        sut.processClasses(null);
    }
}
