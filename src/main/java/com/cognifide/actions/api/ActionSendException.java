package com.cognifide.actions.api;

import aQute.bnd.annotation.ProviderType;

/**
 * Exception that prevents an action from being sent.
 * 
 * @author Tomasz Rękawek
 */
@ProviderType
public class ActionSendException extends Exception {

	private static final long serialVersionUID = 8525988534376434710L;

	public ActionSendException(Throwable cause) {
		super(cause);
	}

}
