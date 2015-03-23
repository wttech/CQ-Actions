/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2015 Cognifide
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.cognifide.actions.core.serializer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class MessageSerializer {

	static final String ACTION_PROPERTIES = "_actionProperties";

	private MessageSerializer() {
	}

	public static void saveMessageToResource(Resource resource, String type, Map<String, String> message) {
		final ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
		map.put("sling:resourceType", type);
		map.putAll(message);
		map.put(ACTION_PROPERTIES, getArray(message.keySet()));
	}

	public static Map<String, String> getMessageFromResource(Resource resource) {
		final ValueMap properties = resource.adaptTo(ValueMap.class);
		final Map<String, String> map = new LinkedHashMap<String, String>();
		final String[] actionProperties = properties.get("_actionProperties", String[].class);
		if (actionProperties == null) {
			return Collections.emptyMap();
		}
		for (final String key : actionProperties) {
			map.put(key, properties.get(key, String.class));
		}
		final Map<String, String> immutableMap = Collections.unmodifiableMap(map);
		return immutableMap;
	}

	private static String[] getArray(Collection<String> collection) {
		final String[] arr = new String[collection.size()];
		int i = 0;
		for (String e : collection) {
			arr[i++] = e;
		}
		return arr;
	}

}
