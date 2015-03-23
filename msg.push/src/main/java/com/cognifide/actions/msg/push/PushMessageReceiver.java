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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.internal.MessageConsumer;
import com.cognifide.actions.msg.push.api.PushReceiver;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
@Service
public class PushMessageReceiver implements PushReceiver {

	private static final Gson GSON = new Gson();

	@Reference
	private MessageConsumer messageConsumer;

	@Override
	public void gotMessage(String topic, String msg) {
		if (!"ACTION".equals(topic)) {
			return;
		}
		final JsonObject json = GSON.fromJson(msg, JsonObject.class);
		final String type = json.get("type").getAsString();
		final JsonObject payload = json.get("payload").getAsJsonObject();
		final Map<String, String> properties = new LinkedHashMap<String, String>();
		for (Entry<String, JsonElement> e : payload.entrySet()) {
			properties.put(e.getKey(), e.getValue().getAsString());
		}
		messageConsumer.consumeMessage(type, properties);
	}
}
