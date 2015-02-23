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

import java.util.Calendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.commons.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.ActionRegistry;

/**
 * Remove old and unnecessary action entries.
 * 
 */
//@formatter:off
@Component(immediate = true, metatype = true)
@Properties({
	@Property(name = "scheduler.expression", value = "0 0 3 * * ?"),
	@Property(name = UserGeneratedContentCleaner.TTL_NAME, intValue = UserGeneratedContentCleaner.TTL_DEFAULT, description = "TTL in hours")
})
//@formatter:on
public class UserGeneratedContentCleaner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(UserGeneratedContentCleaner.class);

	private static final String OLD_ACTIONS_QUERY = "SELECT * FROM [cq:Page] AS s "
			+ "WHERE ISDESCENDANTNODE('%s') AND s.[jcr:created] < CAST('%s' AS DATE)";

	final static String TTL_NAME = "ttl";

	final static int TTL_DEFAULT = 48;

	private int ttl;

	@Reference
	private ActionRegistry actionRegistry;

	@Reference
	private Scheduler scheduler;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Activate
	void activate(Map<String, Object> config) throws Exception {
		ttl = PropertiesUtil.toInteger(config.get(TTL_NAME), TTL_DEFAULT);
	}

	public void run() {
		Calendar until = Calendar.getInstance();
		until.add(Calendar.HOUR, -ttl);

		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			Session session = resolver.adaptTo(Session.class);

			String actionRootPath = actionRegistry.getActionRoot();
			if (session.nodeExists(actionRootPath)) {
				Node actionRoot = session.getNode(actionRootPath);
				Node yearNode = null;
				Node monthNode = null;
				Node dayNode = null;

				yearNode = deleteChildrenUntil(session, actionRoot, until.get(Calendar.YEAR));
				if (yearNode != null) {
					monthNode = deleteChildrenUntil(session, yearNode, until.get(Calendar.MONTH) + 1);
				}
				if (monthNode != null) {
					dayNode = deleteChildrenUntil(session, monthNode, until.get(Calendar.DAY_OF_MONTH));
				}
				// don't remove entries from current day folder
				if (dayNode != null && ttl >= 24) {
					String oldActionsQuery = String.format(OLD_ACTIONS_QUERY, dayNode.getPath(),
							ISO8601.format(until));
					NodeIterator oldActions = UserGeneratedContentCleaner.executeSQL2Statement(
							oldActionsQuery, resolver);
					while (oldActions.hasNext()) {
						oldActions.nextNode().remove();
					}
					if (oldActions.getSize() > 0) {
						session.save();
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Can't clean UGC", e);
		} finally {
			if (resolver != null && resolver.isLive()) {
				resolver.close();
			}
		}
	}

	/**
	 * Get all children of given folder, check if child name can be parsed as Integer and remove if it name <
	 * until.
	 * 
	 * @param session Session to save after removal.
	 * @param node Parent node.
	 * @param until Remove children < until.
	 * @return Child node with name == until.
	 * @throws RepositoryException
	 */
	private Node deleteChildrenUntil(Session session, Node node, int until) throws RepositoryException {
		boolean modified = false;

		NodeIterator iterator = node.getNodes();
		Node untilNode = null;
		while (iterator.hasNext()) {
			Node childNode = iterator.nextNode();
			Integer name = parseIntegerOrNull(childNode.getName());
			if (name == null) {
				continue;
			} else if (name < until) {
				modified = true;
				childNode.remove();
			} else if (name == until) {
				untilNode = childNode;
			}
		}

		if (modified) {
			session.save();
		}

		return untilNode;
	}

	private static Integer parseIntegerOrNull(String integer) {
		try {
			return Integer.parseInt(integer);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static NodeIterator executeSQL2Statement(final String statement,
			final ResourceResolver resourceResolver) throws RepositoryException {
		final Session session = resourceResolver.adaptTo(Session.class);
		final QueryManager queryManager = session.getWorkspace().getQueryManager();

		final Query createQuery = queryManager.createQuery(statement, Query.JCR_SQL2);
		final QueryResult result = createQuery.execute();
		return result.getNodes();
	}
}
