/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2015 Wunderman Thompson Technology
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

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.core.api.MessageConsumer;
import com.cognifide.actions.core.serializer.MessageSerializer;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(immediate = true)
@Service
@Properties({ @Property(name = JobConsumer.PROPERTY_TOPICS, value = HandleMessageJob.TOPIC) })
public class HandleMessageJob implements JobConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(HandleMessageJob.class);

	final static String TOPIC = "com/cognifide/actions/repl/handleMessage";

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private MessageConsumer messageConsumer;

	@Override
	public JobResult process(Job job) {
		final String path = (String) job.getProperty(SlingConstants.PROPERTY_PATH);
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final PageManager pm = resolver.adaptTo(PageManager.class);
			final Page page = pm.getPage(path);
			final String actionType;
			if (page != null && page.getContentResource() != null) {
				actionType = page.getContentResource().getResourceType();
				consumeMessage(actionType, page.getContentResource());
			} else {
				LOG.debug("Empty resource type for action page: " + path);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
		return JobResult.OK;
	}

	private void consumeMessage(String actionType, Resource content) {
		final Map<String, String> immutableMap = MessageSerializer.getMessageFromResource(content);
		messageConsumer.consumeMessage(actionType, immutableMap);
	}
}