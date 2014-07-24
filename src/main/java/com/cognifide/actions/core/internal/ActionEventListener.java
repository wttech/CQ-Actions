package com.cognifide.actions.core.internal;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.core.util.Utils;

import static javax.jcr.observation.Event.NODE_ADDED;

/**
 * Just a simple DS Component
 */
@Component(metatype = true, immediate = true, label = "Cognifide Action Event Listener", description = "Cognifide Action Event Listener")
@Service
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, description = "Listen changes on chosen path on jcr changes."),
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
		@Property(name = "process.label", value = "[Cognifide] Action Event Listener", label = " ", description = " "),
		@Property(name = ActionEventListener.OBSERVED_PATH, value = ActionEventListener.OBSERVED_PATH_DEFAULT, label = ActionEventListener.OBSERVED_PATH_LABEL, description = ActionEventListener.OBSERVED_PATH_DESCRIPTION) })
public class ActionEventListener implements EventListener {

	static final String OBSERVED_PATH = "observed.path";

	static final String OBSERVED_PATH_DEFAULT = "/content/usergenerated/actions";

	static final String OBSERVED_PATH_LABEL = "Observed path";

	static final String OBSERVED_PATH_DESCRIPTION = "The path in jcr tree to observe.";

	private static final String JCR_CONTENT_SUFFIX = "/jcr:content";

	private static final String[] TYPES = { "cq:Page" };

	private static Logger LOG = LoggerFactory.getLogger(ActionEventListener.class);

	@Reference
	private SlingRepository repository;

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private SlingSettingsService slingSettings;

	private Session session;

	private ObservationManager observationManager;

	protected void activate(ComponentContext ctx) {
		if (!isAuthor()) {
			LOG.info("Action Event Listener disabled.");
			return;
		}

		try {
			String observedPath = Utils.propertyToString(ctx, OBSERVED_PATH, OBSERVED_PATH_DEFAULT);

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
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		String path = event.getPath();
		if (StringUtils.endsWith(path, JCR_CONTENT_SUFFIX)) {
			path = path.replace(JCR_CONTENT_SUFFIX, "");
		}
		properties.put(SlingConstants.PROPERTY_PATH, path);
		properties.put(JobUtil.PROPERTY_JOB_TOPIC, ActionEventHandler.TOPIC);

		org.osgi.service.event.Event mappedEvent = new org.osgi.service.event.Event(JobUtil.TOPIC_JOB,
				properties);
		LOG.debug("Handle JCR Tree change:" + path);
		eventAdmin.sendEvent(mappedEvent);
	}

	private boolean isAuthor() {
		return slingSettings.getRunModes().contains("author");
	}
}
