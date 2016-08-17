package com.cognifide.actions.core.api;

import java.util.Map;

import aQute.bnd.annotation.ProviderType;

/**
 * An internal service that transforms incoming message into an action and sends it to appropriate
 * {@link com.cognifide.actions.api.ActionReceiver}.
 * 
 * @author Tomasz Rękawek
 *
 */
@ProviderType
public interface MessageConsumer {
	void consumeMessage(String type, Map<String, String> properties);
}
