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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.SetNode;

import java.util.Map;

/**
 * This use used to serialize {@link MultilingualString} values.
 */
class MultilingualStringSerializer {

    JsonNode serialize(String attName, MultilingualString value) {
        assert value != null;
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return JsonNodeFactory.createLangStringNode(attName, entry.getValue(), entry.getKey());
        }
        final SetNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray(attName);
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }

    private void addTranslationsToCollectionNode(MultilingualString str, SetNode target) {
        str.getValue()
           .forEach((lang, val) -> target.addItem(JsonNodeFactory.createLangStringNode(val, lang)));
    }

    JsonNode serialize(MultilingualString value) {
        assert value != null;
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return JsonNodeFactory.createLangStringNode(entry.getValue(), entry.getKey());
        }
        final SetNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray();
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }
}
