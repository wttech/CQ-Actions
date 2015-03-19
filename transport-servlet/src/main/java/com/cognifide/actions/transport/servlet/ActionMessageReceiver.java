package com.cognifide.actions.transport.servlet;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.internal.ActionWhiteboard;
import com.cognifide.actions.transport.servlet.active.MessageReceiver;

@Component
@Service
public class ActionMessageReceiver implements MessageReceiver {

	private static final Logger LOG = LoggerFactory.getLogger(ActionMessageReceiver.class);

	@Reference
	private ActionWhiteboard whiteboard;

	@Override
	public void gotMessage(String topic, String msg) {
		if (!"ACTION".equals(topic)) {
			return;
		}
		try {
			final JSONObject json = new JSONObject(msg);
			final String type = json.getString("type");
			final JSONObject payload = json.getJSONObject("payload");
			final Map<String, String> properties = new LinkedHashMap<String, String>();
			final Iterator<String> keys = payload.keys();
			while (keys.hasNext()) {
				final String key = keys.next();
				properties.put(key, payload.getString(key));
			}
			whiteboard.invokeAction(type, properties);
		} catch (JSONException e) {
			LOG.error("Can't parse incoming action", e);
		}
	}
}
