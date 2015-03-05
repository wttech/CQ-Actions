package com.cognifide.actions.api;

import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

/**
 * An OSGi service that handles an incoming action.
 * 
 * @author Tomasz Rękawek
 */
@ConsumerType
public interface ActionReceiver {

	/**
	 * Returns action type for this action receiver
	 */
	String getType();

	/**
	 * Handle incoming action with given type.
	 * 
	 * @param properties
	 */
	void handleAction(Map<String, String> properties);
}
