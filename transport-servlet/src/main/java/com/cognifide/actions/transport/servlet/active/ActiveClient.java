package com.cognifide.actions.transport.servlet.active;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.ConfigManager;
import com.day.cq.replication.ConfigManager.ConfigEvent;
import com.day.cq.replication.ConfigManager.ConfigEventListener;

@Component
@Properties({ @Property(name = "serverUrl", value = { "http://localhost:4503" }) })
@References({ @Reference(name = "receiver", referenceInterface = MessageReceiver.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC) })
public class ActiveClient implements ConfigEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ActiveClient.class);

	private final Set<MessageReceiver> receivers = new CopyOnWriteArraySet<MessageReceiver>();

	private List<ClientLoop> loops = new ArrayList<ClientLoop>();

	@Reference
	private SlingSettingsService settings;

	@Reference
	private ConfigManager agentConfigManager;

	@Activate
	public void activate(Map<String, Object> props) {
		if (!settings.getRunModes().contains("author")) {
			return;
		}
		startLoops();
		agentConfigManager.registerListener(this);
	}

	@Deactivate
	public void deactivate() {
		agentConfigManager.unregisterListener(this);
		stopLoops();
	}

	@Override
	public void onConfigEvent(ConfigEvent paramConfigEvent) {
		stopLoops();
		startLoops();
	}

	private void startLoops() {
		for (AgentConfig config : agentConfigManager.getConfigurations().values()) {
			if (config.isEnabled() && config.usedForReverseReplication()) {
				try {
					final ClientLoop loop = createLoop(config);
					Executors.newSingleThreadExecutor().execute(loop);
					loops.add(loop);
				} catch (URISyntaxException e) {
					LOG.error("Can't parse agent url: " + config.getTransportURI(), e);
				}
			}
		}
	}

	private ClientLoop createLoop(AgentConfig config) throws URISyntaxException {
		final String url = StringUtils.substringBefore(config.getTransportURI(), "/bin");
		final String username = config.getTransportUser();
		final String password = config.getTransportPassword();
		return new ClientLoop(receivers, url, username, password);
	}

	private void stopLoops() {
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
