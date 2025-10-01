/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.Configuration;

/**
 * Marker interface for classes whose instances can provide configuration.
 */
public interface Configured {

    /**
     * Gets the configuration holder for this JSON-LD processor.
     *
     * @return Configuration
     */
    Configuration configuration();


	/**
	 * Updates the configuration for this JSON-LD processor.
	 * @param configuration The {@link Configuration}.
	 */
	void updateConfiguration(Configuration configuration);
}
