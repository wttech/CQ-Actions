package com.cognifide.actions.core.internal;

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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.Action;
import com.cognifide.actions.api.ActionRegistry;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

// @formatter:off
@Component(immediate = true)
@Service
@Properties({ @Property(name = EventConstants.EVENT_TOPIC, value = ActionEventHandler.TOPIC) })
// @formatter:on
public class ActionEventHandler implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ActionEventHandler.class);

	final static String TOPIC = "com/cognifide/actions/defaultActionsTopic";

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private ActionRegistry actionRegistry;

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

			LOG.debug("Incoming action: " + actionType);
			final Action action = actionRegistry.getAction(actionType);
			if (action != null) {
				LOG.debug("Performing action: " + actionType);
				action.perform(page);
				LOG.info("Action " + actionType + " finished sucessfuly");
			} else {
				LOG.info("No action found for: " + actionType);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}
}