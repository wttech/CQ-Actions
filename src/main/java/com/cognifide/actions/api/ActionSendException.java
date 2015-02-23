package com.cognifide.actions.api;

/**
 * Exception that prevents an action from being sent.
 * 
 * @author Tomasz Rękawek
 */
public class ActionSendException extends Exception {

	private static final long serialVersionUID = 8525988534376434710L;

	public ActionSendException(Throwable cause) {
		super(cause);
	}

}
