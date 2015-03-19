package com.cognifide.actions.transport.servlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.internal.ActionWhiteboard;
import com.cognifide.actions.transport.servlet.active.MessageReceiver;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
@Service
public class ActionMessageReceiver implements MessageReceiver {

	private static final Gson GSON = new Gson();

	@Reference
	private ActionWhiteboard whiteboard;

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
		whiteboard.invokeAction(type, properties);
	}
}
