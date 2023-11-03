/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld;

/**
 * JSON-LD constants.
 */
public class JsonLd {

    /**
     * JSON-LD {@code @list} keyword.
     */
    public static final String LIST = "@list";

    /**
     * JSON-LD {@code @id} keyword.
     */
    public static final String ID = "@id";

    /**
     * JSON-LD {@code @type} keyword.
     */
    public static final String TYPE = "@type";

    /**
     * JSON-LD {@code @value} keyword.
     */
    public static final String VALUE = "@value";

    /**
     * JSON-LD {@code @language} keyword.
     */
    public static final String LANGUAGE = "@language";

    /**
     * JSON-LD {@code @container} keyword.
     */
    public static final String CONTAINER = "@container";

    /**
     * JSON-LD {@code @none} keyword.
     */
    public static final String NONE = "@none";

    /**
     * JSON-LD {@code @context} keyword.
     */
    public static final String CONTEXT = "@context";

    /**
     * JSON-LD {@code @graph} keyword.
     */
    public static final String GRAPH = "@graph";

    /**
     * JSON-LD media type.
     */
    public static final String MEDIA_TYPE = "application/ld+json";

    private JsonLd() {
        throw new AssertionError();
    }
}
