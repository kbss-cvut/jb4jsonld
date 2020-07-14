/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
public class ObjectWithAnnotationProperties {

    @Id
    private URI id;

    @OWLAnnotationProperty(iri = Vocabulary.CHANGED_VALUE)
    private String changedValue;

    @OWLAnnotationProperty(iri = Vocabulary.ORIGIN)
    private Set<Object> origins;

    public ObjectWithAnnotationProperties() {
    }

    public ObjectWithAnnotationProperties(URI id) {
        this.id = id;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getChangedValue() {
        return changedValue;
    }

    public void setChangedValue(String changedValue) {
        this.changedValue = changedValue;
    }

    public Set<Object> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<Object> origins) {
        this.origins = origins;
    }
}
