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

package com.cognifide.actions.msg.push;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.core.api.MessageProducer;
import com.cognifide.actions.msg.push.api.PushSender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
@Service
public class PushMessageProducer implements MessageProducer {

	private static final Gson GSON = new Gson();

	@Reference
	private PushSender sender;

	@Override
	public boolean sendMessage(String type, Map<String, String> message) {
		final JsonObject msg = new JsonObject();
		msg.addProperty("type", type);
		msg.add("payload", GSON.toJsonTree(message));
		final String serializedMsg = GSON.toJson(msg);
		return sender.sendMessage("ACTION", serializedMsg);
	}
}
