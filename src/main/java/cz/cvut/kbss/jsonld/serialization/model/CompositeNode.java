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
package cz.cvut.kbss.jsonld.serialization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class CompositeNode<T extends Collection<JsonNode>> extends JsonNode {

    final T items;
    private boolean open;

    public CompositeNode() {
        this.items = initItems();
        this.open = true;
    }

    public CompositeNode(String name) {
        super(name);
        this.items = initItems();
        this.open = true;
    }

    abstract T initItems();

    public void addItem(JsonNode item) {
        Objects.requireNonNull(item);
        items.add(item);
    }

    public void prependItem(JsonNode item) {
        throw new UnsupportedOperationException("Prepending items is not supported by most composite nodes.");
    }

    public Collection<JsonNode> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public void close() {
        this.open = false;
    }

    public boolean isOpen() {
        return open;
    }
}
