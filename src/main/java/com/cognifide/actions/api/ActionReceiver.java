package com.cognifide.actions.api;

import java.util.Map;

/**
 * An OSGi service that handles an incoming action.
 * 
 * @author Tomasz Rękawek
 */
public interface ActionReceiver {

	/**
	 * Return true if the service is interested in the action with given type.
	 * 
	 * @param actionType
	 */
	boolean accepts(String actionType);

	/**
	 * Handle incoming action with given type.
	 * 
	 * @param properties
	 */
	void handleAction(Map<String, String> properties);
}
