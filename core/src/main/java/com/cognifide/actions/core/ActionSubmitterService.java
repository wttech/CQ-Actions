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

package com.cognifide.actions.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.api.ActionSubmitter;
import com.cognifide.actions.core.api.MessageProducer;
import com.cognifide.actions.core.persist.MessagePersistenceService;
import com.cognifide.actions.core.serializer.ValueMapType;

@Component
@Service
public class ActionSubmitterService implements ActionSubmitter {

	@Reference
	private MessageProducer producer;

	@Reference
	private MessagePersistenceService persistence;

	@Override
	public void sendAction(String actionType, Map<String, Object> properties) throws ActionSendException {
		final Map<String, String> message = serializeValues(properties);
		if (!producer.sendMessage(actionType, message)) {
			try {
				persistence.persist(actionType, message);
			} catch (LoginException | PersistenceException e) {
				throw new ActionSendException(e);
			}
		}
	}

	private static Map<String, String> serializeValues(Map<String, Object> map) {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		for (Entry<String, Object> e : map.entrySet()) {
			ValueMapType type = null;
			final Object value = e.getValue();
			for (ValueMapType t : ValueMapType.values()) {
				if (t.isSupported(value)) {
					type = t;
					break;
				}
			}
			if (type == null) {
				throw new IllegalArgumentException("Invalid map value: " + value);
			}
			result.put(e.getKey(), type.serialize(value));
		}
		return result;
	}
}
