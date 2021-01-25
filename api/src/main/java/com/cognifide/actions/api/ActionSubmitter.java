/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2015 Wunderman Thompson Technology
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

import java.util.Map;

import aQute.bnd.annotation.ProviderType;

/**
 * Service that allows to send an action.
 * 
 * @author Tomasz Rękawek
 *
 */
@ProviderType
public interface ActionSubmitter {

	/**
	 * Send action with given type and properties
	 * 
	 * @param actionType Type of the action (must be the same as the one accepted by {@link ActionReceiver}.
	 * @param properties Properties of the action
	 * @throws ActionSendException Exception that is thrown when it was impossible to send action
	 */
	void sendAction(String actionType, Map<String, Object> properties) throws ActionSendException;
}
