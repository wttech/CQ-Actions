package com.cognifide.cq.channels.active;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;

import com.cognifide.cq.channels.api.ChannelServer;

@Component
@Properties({ @Property(name = "serverUrl", value = { "http://localhost:6003" }) })
@References({ @Reference(name = "server", referenceInterface = ChannelServer.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC) })
public class DispatcherClient {

	private final Map<String, ChannelServer> servers = new HashMap<String, ChannelServer>();

	private List<ChannelDispatcherLoop> loops;

	@Reference
	private SlingSettingsService settings;

	@Activate
	public void activate(Map<String, Object> props) {
		if (!settings.getRunModes().contains("author")) {
			return;
		}
		for (String url : PropertiesUtil.toStringArray(props.get("serverUrl"))) {
			ChannelDispatcherLoop loop = new ChannelDispatcherLoop(servers, url);
			Executors.newSingleThreadExecutor().execute(loop);
		}
	}

	@Deactivate
	public void deactivate() {
		for (ChannelDispatcherLoop loop : loops) {
			loop.stop();
		}
	}

	protected void bindServer(ChannelServer server, Map<String, Object> properties) {
		final String id = getChannelId(properties);
		if (id != null) {
			servers.put(id, server);
		}
	}

	protected void unbindServer(ChannelServer server, Map<String, Object> properties) {
		final String id = getChannelId(properties);
		if (id != null) {
			servers.remove(id);
		}
	}

	private static String getChannelId(Map<String, Object> props) {
		return PropertiesUtil.toString(props.get(ChannelServer.TYPE), null);
	}
}
