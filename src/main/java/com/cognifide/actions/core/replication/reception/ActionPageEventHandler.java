package com.cognifide.actions.core.replication.reception;

/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2012 Cognifide
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.Action;
import com.cognifide.actions.api.ActionRegistry;
import com.cognifide.actions.core.ActionWhiteboard;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

// @formatter:off
@SuppressWarnings("deprecation")
@Component(immediate = true)
@Service
@Properties({ @Property(name = EventConstants.EVENT_TOPIC, value = ActionPageEventHandler.TOPIC) })
// @formatter:on
public class ActionPageEventHandler implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ActionPageEventHandler.class);

	final static String TOPIC = "com/cognifide/actions/defaultActionsTopic";

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private ActionRegistry registry;

	@Reference
	private ActionWhiteboard whiteboard;

	@Override
	public void handleEvent(Event event) {
		final String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final PageManager pm = resolver.adaptTo(PageManager.class);
			final Page page = pm.getPage(path);
			final String actionType;
			if (page != null && page.getContentResource() != null) {
				actionType = page.getContentResource().getResourceType();
			} else {
				LOG.debug("Empty resource type for action page: " + path);
				return;
			}

			performLegacyAction(actionType, page);
			performAction(actionType, page.getProperties());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}

	private void performAction(String actionType, ValueMap properties) {
		final Map<String, String> map = new LinkedHashMap<String, String>();
		for (final String key : properties.get("_actionProperties", String[].class)) {
			map.put(key, properties.get(key, String.class));
		}
		final Map<String, String> immutableMap = Collections.unmodifiableMap(map);
		whiteboard.invokeAction(actionType, immutableMap);
	}

	private void performLegacyAction(final String actionType, final Page page) throws Exception {
		LOG.debug("Incoming action: " + actionType);
		final Action action = registry.getAction(actionType);
		if (action != null) {
			LOG.debug("Performing action: " + actionType);
			action.perform(page);
			LOG.info("Action " + actionType + " finished sucessfuly");
		} else {
			LOG.debug("No legacy action found for: " + actionType);
		}
	}
}