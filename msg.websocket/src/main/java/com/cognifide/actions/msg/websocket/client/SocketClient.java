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

package com.cognifide.actions.msg.websocket.client;

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
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.msg.websocket.api.SocketReceiver;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.ConfigManager;
import com.day.cq.replication.ConfigManager.ConfigEvent;
import com.day.cq.replication.ConfigManager.ConfigEventListener;

@Component
@References({ @Reference(name = "receiver", referenceInterface = SocketReceiver.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC) })
public class SocketClient implements ConfigEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SocketClient.class);

	private final Set<SocketReceiver> receivers = new CopyOnWriteArraySet<SocketReceiver>();

	private List<SocketClientRunnable> loops = new ArrayList<SocketClientRunnable>();

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
					final SocketClientRunnable loop = createLoop(config);
					Executors.newSingleThreadExecutor().execute(loop);
					loops.add(loop);
				} catch (URISyntaxException e) {
					LOG.error("Can't parse agent url: " + config.getTransportURI(), e);
				}
			}
		}
	}

	private SocketClientRunnable createLoop(AgentConfig config) throws URISyntaxException {
		final String url = StringUtils.substringBefore(config.getTransportURI(), "/bin");
		final String username = config.getTransportUser();
		final String password = config.getTransportPassword();
		return new SocketClientRunnable(receivers, url, username, password);
	}

	private void stopLoops() {
		for (SocketClientRunnable loop : loops) {
			loop.stop();
		}
		loops.clear();
	}

	protected void bindReceiver(SocketReceiver receiver) {
		receivers.add(receiver);
	}

	protected void unbindReceiver(SocketReceiver receiver) {
		receivers.remove(receiver);
	}

}
