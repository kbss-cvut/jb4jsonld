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

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.lang.reflect.Field;
import java.net.URI;

@OWLClass(iri = Vocabulary.STUDY)
public class ObjectWithMultilingualString {

    @Id
    private URI id;

    @OWLDataProperty(iri = RDFS.LABEL)
    private MultilingualString label;

    @OWLAnnotationProperty(iri = SKOS.SCOPE_NOTE)
    private MultilingualString scopeNote;

    public ObjectWithMultilingualString() {
    }

    public ObjectWithMultilingualString(URI id) {
        this.id = id;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public MultilingualString getLabel() {
        return label;
    }

    public void setLabel(MultilingualString label) {
        this.label = label;
    }

    public MultilingualString getScopeNote() {
        return scopeNote;
    }

    public void setScopeNote(MultilingualString scopeNote) {
        this.scopeNote = scopeNote;
    }

    public static Field getLabelField() throws NoSuchFieldException {
        return ObjectWithMultilingualString.class.getDeclaredField("label");
    }

    public static Field getScopeNoteField() throws NoSuchFieldException {
        return ObjectWithMultilingualString.class.getDeclaredField("scopeNote");
    }
}
