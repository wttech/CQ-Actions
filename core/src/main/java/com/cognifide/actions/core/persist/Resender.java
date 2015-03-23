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

package com.cognifide.actions.core.persist;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.core.serializer.MessageSerializer;
import com.cognifide.actions.msg.api.MessageProducer;

@Component
@Service
@Properties({ @Property(name = "scheduler.concurrent", boolValue = false),
		@Property(name = "scheduler.period", longValue = 30) })
public class Resender implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Resender.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private MessageProducer sender;

	@Override
	public void run() {
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final Resource parent = resolver.getResource(MessagePersistenceService.SERIALIZED_ACTIONS);
			if (parent == null) {
				return;
			}
			for (final Resource child : parent.getChildren()) {
				resend(child);
			}
			resolver.commit();
		} catch (LoginException | PersistenceException e) {
			LOG.error("Can't resend", e);
		} catch (ActionSendException e) {
			LOG.error("Can't resend", e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}

	private void resend(Resource resource) throws PersistenceException, ActionSendException {
		final Map<String, String> message = MessageSerializer.getMessageFromResource(resource);
		if (sender.sendMessage(resource.getResourceType(), message)) {
			resource.getResourceResolver().delete(resource);
		}
	}

}
