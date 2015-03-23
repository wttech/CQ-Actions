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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;

public class ActionValueMap extends LinkedHashMap<String, Object> implements ValueMap {

	private static final long serialVersionUID = -7271809345473635972L;

	public ActionValueMap(Map<String, String> properties) {
		super(properties);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name, Class<T> clazz) {
		ValueMapType type = null;
		for (ValueMapType t : ValueMapType.values()) {
			if (clazz.isAssignableFrom(t.getType())) {
				type = t;
				break;
			}
		}
		if (type == null) {
			throw new IllegalArgumentException("Invalid class: " + clazz);
		}
		return (T) type.deserialize((String) get(name));
	}

	@Override
	public <T> T get(String name, T defaultValue) {
		@SuppressWarnings("unchecked")
		T value = (T) get(name, defaultValue.getClass());
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

}
