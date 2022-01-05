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
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.SKOS;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = SKOS.CONCEPT)
public class ObjectWithPluralMultilingualString {

    @Id
    URI uri;

    @OWLDataProperty(iri = SKOS.ALT_LABEL)
    private Set<MultilingualString> altLabel;

    public ObjectWithPluralMultilingualString() {
    }

    public ObjectWithPluralMultilingualString(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Set<MultilingualString> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(Set<MultilingualString> altLabel) {
        this.altLabel = altLabel;
    }
}
