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
import java.util.Iterator;
import java.util.Map;

import javax.jcr.query.Query;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
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
		final Calendar until = Calendar.getInstance();
		until.add(Calendar.HOUR, -ttl);

		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final String actionRootPath = actionRegistry.getActionRoot();
			final Resource actionRoot = resolver.getResource(actionRootPath);
			if (actionRoot != null) {
				final Resource yearNode = deleteChildrenUntil(actionRoot, until.get(Calendar.YEAR));
				if (yearNode == null) {
					return;
				}

				final Resource monthNode = deleteChildrenUntil(yearNode, until.get(Calendar.MONTH) + 1);
				if (monthNode == null) {
					return;
				}

				final Resource dayNode = deleteChildrenUntil(monthNode, until.get(Calendar.DAY_OF_MONTH));
				if (dayNode == null && ttl < 24) {
					return;
				}
				removeNodesFromDayFolder(dayNode, until);
			}
		} catch (PersistenceException e) {
			LOG.error("Can't clean UGC", e);
		} catch (LoginException e) {
			LOG.error("Can't get resolver", e);
		} finally {
			if (resolver != null && resolver.isLive()) {
				resolver.close();
			}
		}
	}

	private void removeNodesFromDayFolder(Resource dayResource, Calendar until) throws PersistenceException {
		final ResourceResolver resolver = dayResource.getResourceResolver();
		String oldActionsQuery = String.format(OLD_ACTIONS_QUERY, dayResource.getPath(),
				ISO8601.format(until));
		Iterator<Resource> oldActions = resolver.findResources(oldActionsQuery, Query.JCR_SQL2);
		while (oldActions.hasNext()) {
			resolver.delete(oldActions.next());
		}
		resolver.commit();
	}

	/**
	 * Get all children of given folder, check if child name can be parsed as Integer and remove if it name <
	 * until.
	 * 
	 * @param resource Parent resource.
	 * @param until Remove children < until.
	 * @return Child node with name == until.
	 * @throws PersistenceException
	 */
	private Resource deleteChildrenUntil(Resource resource, int until) throws PersistenceException {
		final ResourceResolver resolver = resource.getResourceResolver();

		boolean modified = false;
		Resource untilResource = null;

		final Iterator<Resource> iterator = resource.listChildren();
		while (iterator.hasNext()) {
			final Resource childResource = iterator.next();
			Integer name = parseIntegerOrNull(childResource.getName());
			if (name == null) {
				continue;
			} else if (name < until) {
				modified = true;
				resolver.delete(childResource);
			} else if (name == until) {
				untilResource = childResource;
			}
		}

		if (modified) {
			resolver.commit();
		}

		return untilResource;
	}

	private static Integer parseIntegerOrNull(String integer) {
		try {
			return Integer.parseInt(integer);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
