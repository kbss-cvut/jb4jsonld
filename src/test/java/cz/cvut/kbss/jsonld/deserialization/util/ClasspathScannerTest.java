/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.util;

import static org.junit.jupiter.api.Assertions.fail;


import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Test
    void processClassesSupportsWarFiles() throws IOException {
        processClassesSupportsJarFiles("testjar.war", "cz.cvut.kbss.testjar.model");
    }

    @Test
    void processClassesSupportsSpringBootJarFiles() throws IOException, ClassNotFoundException {
        processClassesSupportsJarFiles("testjar.jar", "cz.cvut.kbss.testjar.model");
    }

    void processClassesSupportsJarFiles(String jarFile, String pkg)
        throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResources("./bug-31/" + jarFile).nextElement();
        AtomicBoolean a = new AtomicBoolean(false);
        final ClasspathScanner sut = new ClasspathScanner(cls -> {
            a.set(true);
        });
        sut.processJarFile(url, pkg);
        if (!a.get()) {
            fail();
        }
    }
}
