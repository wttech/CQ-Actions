package com.cognifide.actions.core;

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


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import com.cognifide.actions.api.Action;
import com.cognifide.actions.api.ActionRegistry;
import com.cognifide.actions.core.util.Utils;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.AgentConfig;
import com.day.cq.wcm.api.NameConstants;

//@formatter:off
@Service
@Component(immediate = true)
@Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Actions Registry Service"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
		@Property(name = ActionRegistryService.RANDOM_PATTERN_NAME, value = ActionRegistryService.RANDOM_PATTERN_DEFAULT),
		@Property(name = ActionRegistryService.ROOT_NAME, value = ActionRegistryService.ROOT_DEFAULT)
})
//@formatter:on
public class ActionRegistryService implements ActionRegistry {

	static final String ROOT_NAME = "usermanagement.users.actions.registry.root";

	static final String ROOT_DEFAULT = "/content/usergenerated/actions/";

	static final String RANDOM_PATTERN_NAME = "actions.registry.random.pattern";

	static final String RANDOM_PATTERN_DEFAULT = "**/**/";

	@Reference(referenceInterface = Action.class, policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private final Map<String, Action> actions = new ConcurrentHashMap<String, Action>();

	private final Random random = new Random();

	private String actionRoot;

	private String randomPathPattern;

	@Activate
	void activate(ComponentContext ctx) {
		actionRoot = Utils.slashEnd(Utils.propertyToString(ctx, ROOT_NAME, ROOT_DEFAULT));
		randomPathPattern = Utils.propertyToString(ctx, RANDOM_PATTERN_NAME, RANDOM_PATTERN_DEFAULT);
	}

	@Deactivate
	void deactivate() {
		actions.clear();
		actionRoot = null;
	}

	@Override
	public Action getAction(String type) {
		if (StringUtils.isNotBlank(type)) {
			return actions.get(type);
		} else {
			return null;
		}
	}

	// This code should be called from a synchronized method or method which acquires session by itself
	@Override
	public Node createActionNode(Session session, String relPath, String type) throws RepositoryException {
		String path = createPath(relPath);
		Node page = JcrUtil.createPath(path, true, "sling:Folder", "cq:Page", session, false);
		Node content = page.addNode(JcrConstants.JCR_CONTENT, "cq:PageContent");
		content.setProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, type);
		content.setProperty(AgentConfig.CQ_DISTRIBUTE, false);
		session.save();
		content.setProperty(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
		content.setProperty(NameConstants.PN_PAGE_LAST_MOD_BY, session.getUserID());
		content.setProperty(AgentConfig.CQ_DISTRIBUTE, true);
		return content;
	}

	@Override
	public String getActionRoot() {
		return actionRoot;
	}

	private String createPath(String relPath) {
		String path;
		if (StringUtils.startsWith(relPath, "/")) {
			path = relPath;
		} else {
			final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/");
			path = new StringBuilder(actionRoot).append(dateFormat.format(new Date())).append(relPath).toString();
		}

		if (path.endsWith("/*")) {
			path = new StringBuilder(StringUtils.removeEnd(path, "*")).append(generateRandomPathPart())
					.append(new Date().getTime()).toString();
		}
		return path;
	}

	private String generateRandomPathPart() {
		final StringBuilder builder = new StringBuilder();
		for (char c : randomPathPattern.toCharArray()) {
			if (c == '*') {
				builder.append(Integer.toHexString(random.nextInt(16)));
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	protected void bindActions(Action action) {
		if (action != null) {
			actions.put(action.getType(), action);
		}
	}

	protected void unbindActions(Action action) {
		if (actions != null && action != null) {
			actions.remove(action.getType());
		}
	}

}
