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
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DefaultInstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.JsonLdDeserializer;
import cz.cvut.kbss.jsonld.deserialization.reference.AssumedTypeReferenceReplacer;
import cz.cvut.kbss.jsonld.deserialization.reference.PendingReferenceRegistry;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class ExpandedJsonLdDeserializer extends JsonLdDeserializer {

	private final Map<String, Object> knownInstances;
	private PendingReferenceRegistry referenceRegistry;

    public ExpandedJsonLdDeserializer() {
		knownInstances = new HashMap<>();
		referenceRegistry = new PendingReferenceRegistry();
    }

    public ExpandedJsonLdDeserializer(Configuration configuration) {
        super(configuration);
		knownInstances = new HashMap<>();
		referenceRegistry = new PendingReferenceRegistry();
    }

    @Override
    public <T> T deserialize(JsonValue jsonLd, Class<T> resultClass) {
        if (jsonLd.getValueType() != JsonValue.ValueType.ARRAY) {
            throw new JsonLdDeserializationException(
                    "Expanded JSON-LD deserializer requires a JSON-LD array as input.");
        }
        final JsonArray input = jsonLd.asJsonArray();
        if (input.size() != 1) {
            throw new JsonLdDeserializationException(
                    "Input is not expanded JSON-LD. The input does not contain exactly one root element.");
        }
		deserializers.configure(configuration());
        final JsonObject root = input.getJsonObject(0);
        if (deserializers.hasCustomDeserializer(resultClass)) {
            final DeserializationContext<T> ctx = new DeserializationContext<>(resultClass, classResolver);
            assert deserializers.getDeserializer(ctx).isPresent();
            return deserializers.getDeserializer(ctx).get().deserialize(root, ctx);
        }
        final InstanceBuilder instanceBuilder = new DefaultInstanceBuilder(classResolver, referenceRegistry, knownInstances);
        new ObjectDeserializer(instanceBuilder, new DeserializerConfig(configuration(), classResolver, deserializers), resultClass)
                .processValue(root);
        if (configuration().is(ConfigParam.ASSUME_TARGET_TYPE)) {
            new AssumedTypeReferenceReplacer().replacePendingReferencesWithAssumedTypedObjects(referenceRegistry);
        }
		if (!configuration().is(ConfigParam.POSTPONE_UNRESOLVED_REFERENCES_CHECK)) {
			referenceRegistry.verifyNoUnresolvedReferencesExist();
			referenceRegistry = new PendingReferenceRegistry();
			knownInstances = new HashMap<>();
		}
        assert resultClass.isAssignableFrom(instanceBuilder.getCurrentRoot().getClass());
        return resultClass.cast(instanceBuilder.getCurrentRoot());
    }

	@Override
	public void cleanup() {
		if (configuration().is(ConfigParam.POSTPONE_UNRESOLVED_REFERENCES_CHECK)) {
			referenceRegistry.verifyNoUnresolvedReferencesExist();
			referenceRegistry = new PendingReferenceRegistry();
		}
	}
}
