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
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.jopa.vocabulary.OWL;

public enum OwlPropertyType {

    @Individual(iri = OWL.ANNOTATION_PROPERTY)
    ANNOTATION_PROPERTY,
    @Individual(iri = OWL.DATATYPE_PROPERTY)
    DATATYPE_PROPERTY,
    @Individual(iri = OWL.OBJECT_PROPERTY)
    OBJECT_PROPERTY;

    public static String getMappedIndividual(OwlPropertyType constant) {
        switch (constant) {
            case ANNOTATION_PROPERTY:
                return OWL.ANNOTATION_PROPERTY;
            case DATATYPE_PROPERTY:
                return OWL.DATATYPE_PROPERTY;
            default:
                return OWL.OBJECT_PROPERTY;
        }
    }
}
