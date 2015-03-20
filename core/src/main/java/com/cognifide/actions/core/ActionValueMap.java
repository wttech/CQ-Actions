package com.cognifide.actions.core;

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
