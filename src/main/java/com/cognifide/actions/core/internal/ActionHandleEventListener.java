package com.cognifide.actions.core.internal;

/*
 * #%L Cognifide Actions %% Copyright (C) 2012 Cognifide %% Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License. #L%
 */

import javax.jcr.Session;

import org.apache.commons.lang.mutable.MutableObject;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.Action;
import com.cognifide.actions.api.ActionRegistry;
import com.cognifide.actions.core.util.AdminJcrCommandExecutor;
import com.cognifide.actions.core.util.JcrCommand;
import com.cognifide.actions.core.util.Utils;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

// @formatter:off
@Component(immediate = true)
@Service
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Action handle event listener."),
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
		@Property(name = "process.label", value = "[Cognifide] Action Handling"),
		@Property(name = ActionHandleEventListener.WORKING_PATH_NAME, value = ActionHandleEventListener.WORKING_PATH_DEFAULT),
		@Property(name = EventConstants.EVENT_FILTER, value = "(path=/content/usergenerated/actions/*)"),
		@Property(name = EventConstants.EVENT_TOPIC, value = ActionHandleEventListener.TOPIC) })
// @formatter:on
public class ActionHandleEventListener implements EventHandler, JobProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(ActionHandleEventListener.class);

	final static String WORKING_PATH_NAME = "working.path";

	final static String WORKING_PATH_DEFAULT = "/content/usergenerated";

	final static String TOPIC = "com/cognifide/actions/defaultActionsTopic";

	@Reference
	private AdminJcrCommandExecutor executor;

	@Reference
	private ActionRegistry actionRegistry;

	private String workingPath;

	@Activate
	void activate(ComponentContext ctx) throws Exception {
		workingPath = Utils.propertyToString(ctx, WORKING_PATH_NAME, WORKING_PATH_DEFAULT);
	}

	@Override
	public boolean process(Event job) {
		final String path = (String) job.getProperty(SlingConstants.PROPERTY_PATH);
		Boolean result = true;
		final MutableObject actionNameObject = new MutableObject();
		try {
			if (path.startsWith(workingPath)) {
				executor.execute(new JcrCommand() {
					@Override
					public void run(Session session, ResourceResolver resolver) throws Exception {
						PageManager pm = resolver.adaptTo(PageManager.class);
						Page page = pm.getPage(path);
						String actionType = null;
						if (page != null && page.getContentResource() != null) {
							actionType = page.getContentResource().getResourceType();
						}

						if (actionType != null) {
							LOG.debug("Incoming action: " + actionType);
							Action action = actionRegistry.getAction(actionType);
							if (action != null) {
								LOG.debug("Performing action: " + actionType);
								actionNameObject.setValue(actionType);
								action.perform(page);
								LOG.debug("Action " + actionType + " finished");
							} else {
								LOG.info("No action found for: " + actionType);
							}
						}
					}
				});

			}
		} catch (Exception ex) {
			result = false;
			LOG.error(ex.getMessage(), ex);
		}
		if (actionNameObject.getValue() != null) {
			LOG.info(String.format("Action %s succeeded", actionNameObject.getValue()));
		}
		return result;
	}

	@Override
	public void handleEvent(Event event) {
		if (EventUtil.isLocal(event)) {
			JobUtil.processJob(event, this);
		}
	}
}