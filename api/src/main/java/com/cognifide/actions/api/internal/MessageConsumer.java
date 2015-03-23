package com.cognifide.actions.api.internal;

import java.util.Map;

import com.cognifide.actions.api.ActionReceiver;

import aQute.bnd.annotation.ProviderType;

/**
 * An internal service that transforms incoming message into an action and sends it to appropriate
 * {@link ActionReceiver}.
 * 
 * @author Tomasz Rękawek
 *
 */
@ProviderType
public interface MessageConsumer {
	void consumeMessage(String type, Map<String, String> properties);
}
