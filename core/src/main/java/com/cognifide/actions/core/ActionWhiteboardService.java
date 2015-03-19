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

package com.cognifide.actions.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.ActionReceiver;
import com.cognifide.actions.api.internal.ActionWhiteboard;

@Component
@Service
public class ActionWhiteboardService implements ActionWhiteboard {

	@Reference(referenceInterface = ActionReceiver.class, policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private final Set<ActionReceiver> receivers = new CopyOnWriteArraySet<ActionReceiver>();

	@Deactivate
	public void deactivate() {
		receivers.clear();
	}

	public void invokeAction(String type, Map<String, String> properties) {
		for (ActionReceiver receiver : receivers) {
			if (type.equals(receiver.getType())) {
				receiver.handleAction(properties);
			}
		}
	}

	protected void bindReceivers(ActionReceiver receiver) {
		receivers.add(receiver);
	}

	protected void unbindReceivers(ActionReceiver receiver) {
		receivers.remove(receiver);
	}
}
