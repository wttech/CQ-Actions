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

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.core.api.MessageProducer;
import com.cognifide.actions.core.serializer.MessageSerializer;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
		Map<String, Object> authenticationInfo = new HashMap<>();
		authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, "com.cognifide.cq.actions.core");
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getServiceResourceResolver(authenticationInfo);
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
