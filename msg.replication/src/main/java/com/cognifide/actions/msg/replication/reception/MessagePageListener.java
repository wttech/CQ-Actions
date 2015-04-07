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

package com.cognifide.actions.msg.replication.reception;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.cognifide.actions.msg.replication.Configuration;

@Component(immediate = true)
@Service
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = SlingConstants.TOPIC_RESOURCE_ADDED),
		@Property(name = EventConstants.EVENT_FILTER, value = "(&(resourceType=cq:Page)(path=/content/usergenerated/*))") })
public class MessagePageListener implements EventHandler {

	private static final String JCR_CONTENT_SUFFIX = "/jcr:content";

	@Reference
	private SlingSettingsService slingSettings;

	@Reference
	private JobManager jobManager;

	@Reference
	private Configuration config;

	/**
	 * Converts the JCR tree change event (creating new cq:Page node) to the the OSGI event with topic
	 * com/cognifide/actions/defaultActionsTopic and sends it the queue.
	 * 
	 * @param event
	 * @throws RepositoryException
	 */
	@Override
	public void handleEvent(Event event) {
		if (!(isAuthor() && EventUtil.isLocal(event))) {
			return;
		}
		final String path = (String) event.getProperty("path");
		if (!StringUtils.startsWith(path, config.getActionRoot())) {
			return;
		}

		final Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(SlingConstants.PROPERTY_PATH, StringUtils.removeEnd(path, JCR_CONTENT_SUFFIX));
		jobManager.addJob(HandleMessageJob.TOPIC, null, payload);
	}

	private boolean isAuthor() {
		return slingSettings.getRunModes().contains("author");
	}
}
