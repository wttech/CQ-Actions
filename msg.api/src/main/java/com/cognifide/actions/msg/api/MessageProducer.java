package com.cognifide.actions.msg.api;

import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

/**
 * An internal service that sends a message to the author instance.
 * 
 * @author Tomasz Rękawek
 */
@ConsumerType
public interface MessageProducer {
	boolean sendMessage(String type, Map<String, String> message);
}
