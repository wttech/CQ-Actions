package com.cognifide.actions.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.ActionReceiver;

@Component
@Service(ActionWhiteboard.class)
public class ActionWhiteboard {

	@Reference(referenceInterface = ActionReceiver.class, policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private final Set<ActionReceiver> receivers = new CopyOnWriteArraySet<ActionReceiver>();

	@Activate
	public void activate() {
		receivers.clear();
	}

	public void invokeAction(String type, Map<String, String> properties) {
		for (ActionReceiver receiver : receivers) {
			if (receiver.accepts(type)) {
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
