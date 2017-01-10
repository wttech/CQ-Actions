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

import com.cognifide.actions.core.serializer.MessageSerializer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Service(MessagePersistenceService.class)
public class MessagePersistenceService {
	static final String SERIALIZED_ACTIONS = "/var/cognifide/actions";

	@Reference
	private ResourceResolverFactory resolverFactory;

	public void persist(String type, Map<String, String> message) throws LoginException, PersistenceException {
		Map<String, Object> authenticationInfo = new HashMap<>();
		authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, "com.cognifide.cq.actions.core");
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getServiceResourceResolver(authenticationInfo);
			final Resource messageResource = createMessageResource(resolver);
			MessageSerializer.saveMessageToResource(messageResource, type, message);
			resolver.commit();
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}

	private Resource createMessageResource(ResourceResolver resolver) throws PersistenceException {
		final Resource parent = ResourceUtil.getOrCreateResource(resolver, SERIALIZED_ACTIONS,
				Collections.singletonMap("jcr:primaryType", (Object) "sling:OrderedFolder"), null, false);

		return resolver.create(parent, UUID.randomUUID().toString(),
				Collections.singletonMap("jcr:primaryType", (Object) "nt:unstructured"));
	}

}
