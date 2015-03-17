package com.cognifide.actions.api.internal;

import java.util.Map;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ActionWhiteboard {
	void invokeAction(String type, Map<String, String> properties);
}
