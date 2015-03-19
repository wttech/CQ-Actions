package com.cognifide.actions.transport.servlet;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.api.ActionSubmitter;
import com.cognifide.actions.transport.servlet.passive.MessageSender;

@Component
@Service
public class ActionMessageSubmitter implements ActionSubmitter {

	@Reference
	private MessageSender sender;

	@Override
	public void sendAction(String actionType, Map<String, String> properties) throws ActionSendException {
		final JSONObject msg = new JSONObject();
		try {
			msg.put("type", actionType);
			msg.put("payload", properties);
			if (!sender.sendMessage("ACTION", msg.toString())) {
				throw new ActionSendException("Can't send action");
			}
		} catch (JSONException e) {
			throw new ActionSendException(e);
		}
	}

}
