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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.jcr.observation.Event.NODE_ADDED;

@Component(metatype = true, immediate = true, label = "Cognifide Action Event Listener", description = "Cognifide Action Event Listener")
@Service
public class ActionEventListener implements EventListener {

	@Property(value = ActionEventListener.OBSERVED_PATH_DEFAULT, label = "Observed path", description = "The path in jcr tree to observe.")
	private static final String OBSERVED_PATH = "observed.path";

	private static final String OBSERVED_PATH_DEFAULT = "/content/usergenerated/actions";

	private static final String JCR_CONTENT_SUFFIX = "/jcr:content";

	private static final String[] TYPES = { "cq:Page" };

	private static Logger LOG = LoggerFactory.getLogger(ActionEventListener.class);

	@Reference
	private SlingRepository repository;

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private SlingSettingsService slingSettings;

	@Reference
	private JobManager jobManager;

	private Session session;

	private ObservationManager observationManager;

	@Activate
	protected void activate(Map<String, Object> config) {
		if (!isAuthor()) {
			LOG.info("Action Event Listener disabled.");
			return;
		}

		try {
			String observedPath = PropertiesUtil.toString(config.get(OBSERVED_PATH), OBSERVED_PATH_DEFAULT);
			this.session = repository.loginAdministrative(null);
			this.session.getWorkspace().getObservationManager()
					.addEventListener(this, NODE_ADDED, observedPath, true, null, TYPES, false);
			LOG.info(
					"Action Event Listener activated. Observing property changes to \"{}\" nodes under \"{}\"",
					TYPES != null ? Arrays.asList(TYPES) : "", observedPath);

		} catch (RepositoryException e) {
			LOG.error("Activating Action Event Listener failed:" + e);
		}
	}

	protected void deactivate(ComponentContext ctx) throws RepositoryException {

		if (observationManager != null) {
			observationManager.removeEventListener(this);

			LOG.info("Action Event Listener deactivated.");
		}
		if (session != null) {
			session.logout();
			session = null;
		}
	}

	@Override
	public void onEvent(EventIterator event) {
		LOG.debug("Handling events JCR");
		while (event.hasNext()) {
			try {
				convertEvent(event.nextEvent());
			} catch (RepositoryException e) {
				LOG.error("The problem appear during converting the event", e);
			}
		}

	}

	/**
	 * Converts the JCR tree change event (creating new cq:Page node) to the the OSGI event with topic
	 * com/cognifide/actions/defaultActionsTopic and sends it the queue.
	 * 
	 * @param event
	 * @throws RepositoryException
	 */
	public void convertEvent(Event event) throws RepositoryException {
		Map<String, Object> payload = new HashMap<String, Object>();
		String path = event.getPath();
		if (StringUtils.endsWith(path, JCR_CONTENT_SUFFIX)) {
			path = path.replace(JCR_CONTENT_SUFFIX, "");
		}
		payload.put(SlingConstants.PROPERTY_PATH, path);
		jobManager.addJob(ActionEventHandler.TOPIC, payload);
	}

	private boolean isAuthor() {
		return slingSettings.getRunModes().contains("author");
	}
}
