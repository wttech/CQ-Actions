/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2015 Cognifide
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

	public ActionSendException(String message) {
		super(message);
	}

}
