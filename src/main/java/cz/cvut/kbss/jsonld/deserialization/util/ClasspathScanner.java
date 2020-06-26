/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jsonld.exception.JsonLdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Processes classpath accessible to the application and passes all discovered classes to the registered listener.
 */
public class ClasspathScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ClasspathScanner.class);

    private static final String JAR_FILE_SUFFIX = ".jar";
    private static final String CLASS_FILE_SUFFIX = ".class";

    private final Consumer<Class<?>> listener;

    public ClasspathScanner(Consumer<Class<?>> listener) {
        this.listener = Objects.requireNonNull(listener);
    }

    /**
     * Scans classpath accessible from the current thread's class loader.
     * <p>
     * All available classes are passed to the registered consumer.
     * <p>
     * The {@code scanPath} parameter means that only the specified package (and it subpackages) should be searched.
     * This parameter is optional, but it is highly recommended to specify it, as it can speed up the process
     * dramatically.
     * <p>
     * Inspired by https://github.com/ddopson/java-class-enumerator
     *
     * @param scanPath Package narrowing down the scan space. Optional
     */
    public void processClasses(String scanPath) {
        if (scanPath == null) {
            scanPath = "";
        }
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> urls = loader.getResources(scanPath.replace('.', '/'));
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                if (isJar(url.toString())) {
                    processJarFile(url, scanPath);
                } else {
                    processDirectory(new File(getUrlAsUri(url).getPath()), scanPath);
                }
            }
            // Scan jar files on classpath
            Enumeration<URL> resources = loader.getResources(".");
            while (resources.hasMoreElements()) {
                URL resourceURL = resources.nextElement();
                if (isJar(resourceURL.toString()))
                    processJarFile(resourceURL, scanPath);
            }
        } catch (IOException e) {
            throw new JsonLdException("Unable to scan packages.", e);
        }
    }

    private static boolean isJar(String filePath) {
        return filePath.startsWith("jar:") || filePath.endsWith(JAR_FILE_SUFFIX);
    }

    private static URI getUrlAsUri(URL url) {
        try {
            // Transformation to URI handles encoding, e.g. of whitespaces in the path
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new JsonLdException("Unable to scan resource " + url + ". It is not a valid URI.", ex);
        }
    }

    private void processJarFile(URL jarResource, String packageName) {
        final String relPath = packageName.replace('.', '/');
        final String jarPath = jarResource.getPath().replaceFirst("[.]jar[!].*", JAR_FILE_SUFFIX)
                                          .replaceFirst("file:", "");

        LOG.trace("Scanning jar file {} for classes.", jarPath);
        try (final JarFile jarFile = new JarFile(jarPath)) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();
                String className = null;
                if (shouldSkipEntry(entryName)) {
                    continue;
                }
                if (entryName.endsWith(CLASS_FILE_SUFFIX) && entryName.startsWith(relPath)) {
                    // Remove prefix from multi-release JAR class names
                    className = entryName.replaceFirst("META-INF/versions/[1-9][0-9]*/", "");
                    className = className.replace('/', '.').replace('\\', '.');
                    className = className.substring(0, className.length() - CLASS_FILE_SUFFIX.length());
                }
                if (className != null) {
                    processClass(className);
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to scan classes in JAR file " + jarPath, e);
        }
    }

    private static boolean shouldSkipEntry(String entryName) {
        // Skip module-info.class files
        return entryName.endsWith("module-info" + CLASS_FILE_SUFFIX);
    }

    private void processClass(String className) {
        try {
            final Class<?> cls = Class.forName(className);
            listener.accept(cls);
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to process class " + className, e);
        }
    }

    private void processDirectory(File dir, String packageName)
            throws MalformedURLException {
        LOG.trace("Scanning directory {}.", dir);
        // Get the list of the files contained in the package
        final String[] files = dir.list();
        if (files == null) {
            return;
        }
        for (String fileName : files) {
            String className = null;
            // we are only interested in .class files
            if (fileName.endsWith(CLASS_FILE_SUFFIX)) {
                // removes the .class extension
                className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
            }
            if (className != null) {
                processClass(className);
            }
            final File subDir = new File(dir, fileName);
            if (subDir.isDirectory()) {
                processDirectory(subDir, packageName + (!packageName.isEmpty() ? '.' : "") + fileName);
            } else if (isJar(subDir.getAbsolutePath())) {
                processJarFile(subDir.toURI().toURL(), packageName);
            }
        }
    }
}
