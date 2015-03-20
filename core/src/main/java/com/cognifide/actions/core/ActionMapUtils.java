package com.cognifide.actions.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ActionMapUtils {

	private ActionMapUtils() {
	}

	public static Map<String, String> serializeValues(Map<String, Object> map) {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		for (Entry<String, Object> e : map.entrySet()) {
			ValueMapType type = null;
			final Object value = e.getValue();
			for (ValueMapType t : ValueMapType.values()) {
				if (t.isSupported(value)) {
					type = t;
					break;
				}
			}
			if (type == null) {
				throw new IllegalArgumentException("Invalid map value: " + value);
			}
			result.put(e.getKey(), type.serialize(value));
		}
		return result;
	}
}
