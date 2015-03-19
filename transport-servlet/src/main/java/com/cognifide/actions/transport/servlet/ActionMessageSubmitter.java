package com.cognifide.actions.transport.servlet;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.api.ActionSubmitter;
import com.cognifide.actions.transport.servlet.passive.MessageSender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
@Service
public class ActionMessageSubmitter implements ActionSubmitter {

	private static final Gson GSON = new Gson();

	@Reference
	private MessageSender sender;

	@Override
	public void sendAction(String actionType, Map<String, String> properties) throws ActionSendException {
		final JsonObject msg = new JsonObject();
		msg.addProperty("type", actionType);
		msg.add("payload", GSON.toJsonTree(properties));
		if (!sender.sendMessage("ACTION", GSON.toJson(msg))) {
			throw new ActionSendException("Can't send action");
		}
	}

}
