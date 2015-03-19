package com.cognifide.actions.transport.servlet.active;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;

@Component
@Properties({ @Property(name = "serverUrl", value = { "http://localhost:4503" }) })
@References({ @Reference(name = "receiver", referenceInterface = MessageReceiver.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC) })
public class ActiveClient {

	private final Set<MessageReceiver> receivers = new CopyOnWriteArraySet<MessageReceiver>();

	private List<ClientLoop> loops = new ArrayList<ClientLoop>();

	@Reference
	private SlingSettingsService settings;

	@Activate
	public void activate(Map<String, Object> props) {
		if (!settings.getRunModes().contains("author")) {
			return;
		}
		for (String url : PropertiesUtil.toStringArray(props.get("serverUrl"))) {
			ClientLoop loop = new ClientLoop(receivers, url);
			Executors.newSingleThreadExecutor().execute(loop);
			loops.add(loop);
		}
	}

	@Deactivate
	public void deactivate() {
		for (ClientLoop loop : loops) {
			loop.stop();
		}
		loops.clear();
	}

	protected void bindReceiver(MessageReceiver receiver) {
		receivers.add(receiver);
	}

	protected void unbindReceiver(MessageReceiver receiver) {
		receivers.remove(receiver);
	}
}
