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

package com.cognifide.actions.transport.replication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.api.ActionSubmitter;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

@Component
@Service
public class ReplicationBasedSubmitterService implements ActionSubmitter {

	static final String ACTION_PROPERTIES = "_actionProperties";

	@Reference
	private Configuration config;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Override
	public void sendAction(String actionType, Map<String, String> properties) throws ActionSendException {
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final ModifiableValueMap map = createActionResource(resolver, actionType, "*");
			map.putAll(properties);
			map.put(ACTION_PROPERTIES, getArray(properties.keySet()));
			resolver.commit();
		} catch (LoginException e) {
			throw new ActionSendException(e);
		} catch (PersistenceException e) {
			throw new ActionSendException(e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}

	private static String[] getArray(Collection<String> collection) {
		final String[] arr = new String[collection.size()];
		int i = 0;
		for (String e : collection) {
			arr[i++] = e;
		}
		return arr;
	}

	private ModifiableValueMap createActionResource(ResourceResolver resolver, String actionType,
			String relPath) throws PersistenceException {
		final String path = createPath(relPath, config.getActionRoot(), config.getRandomPathPattern());
		final Resource page = ResourceUtil.getOrCreateResource(resolver, path,
				Collections.singletonMap(JcrConstants.JCR_PRIMARYTYPE, (Object) "cq:Page"), null, false);

		final Map<String, Object> contentMap = new LinkedHashMap<String, Object>();
		contentMap.put(JcrConstants.JCR_PRIMARYTYPE, "cq:PageContent");
		contentMap.put("sling:resourceType", actionType);
		contentMap.put("cq:distribute", false);
		final Resource content = resolver.create(page, JcrConstants.JCR_CONTENT, contentMap);
		resolver.commit();

		final ModifiableValueMap map = content.adaptTo(ModifiableValueMap.class);
		map.put(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
		map.put(NameConstants.PN_PAGE_LAST_MOD_BY, resolver.getUserID());
		map.put("cq:distribute", true);
		return map;
	}

	static String createPath(String relPath, String actionRoot, String randomPath) {
		final String path;
		if (StringUtils.startsWith(relPath, "/")) {
			path = relPath;
		} else {
			final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			path = String.format("%s%s/%s", actionRoot, dateFormat.format(new Date()), relPath);
		}

		if (path.endsWith("/*")) {
			long now = new Date().getTime();
			return String.format("%s%s/%s", StringUtils.removeEnd(path, "*"),
					generateRandomPathPart(randomPath), now);
		} else {
			return path;
		}
	}

	private static String generateRandomPathPart(String randomPath) {
		final StringBuilder builder = new StringBuilder();
		final Random random = new Random();
		for (char c : randomPath.toCharArray()) {
			if (c == '*') {
				builder.append(Integer.toHexString(random.nextInt(16)));
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

}
