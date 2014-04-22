package com.cognifide.actions.core.internal;

/*
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

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @formatter:off
@Component(immediate = true)
@Service
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Action handle event listener proxy."),
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
		@Property(name = "process.label", value = "[Cognifide] Action Handling proxy"),
		@Property(name = EventConstants.EVENT_TOPIC, value = SlingConstants.TOPIC_RESOURCE_ADDED),
		@Property(name = EventConstants.EVENT_FILTER, value = "(path=/content/usergenerated/actions/*)") })
// @formatter:on
public class ActionHandleEventListenerProxy implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ActionHandleEventListenerProxy.class);

	@Reference
	EventAdmin eventAdmin;

	@Override
	public void handleEvent(Event event) {
		LOG.debug("ActionHandler..." + event.getProperty(SlingConstants.PROPERTY_PATH).toString());

		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(SlingConstants.PROPERTY_PATH,
				event.getProperty(SlingConstants.PROPERTY_PATH));
		properties.put(JobUtil.PROPERTY_JOB_TOPIC, ActionHandleEventListener.TOPIC);

		Event mappedEvent = new Event(JobUtil.TOPIC_JOB, properties);
		eventAdmin.sendEvent(mappedEvent);
	}
}