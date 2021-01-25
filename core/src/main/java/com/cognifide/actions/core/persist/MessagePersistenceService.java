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

package com.cognifide.actions.core.persist;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;

import com.cognifide.actions.core.serializer.MessageSerializer;

@Component
@Service(MessagePersistenceService.class)
public class MessagePersistenceService {
	static final String SERIALIZED_ACTIONS = "/var/cognifide/actions";

	@Reference
	private ResourceResolverFactory resolverFactory;

	public void persist(String type, Map<String, String> message) throws LoginException, PersistenceException {
		ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null);
		try {
			final Resource messageResource = createMessageResource(resolver);
			MessageSerializer.saveMessageToResource(messageResource, type, message);
			resolver.commit();
		} finally {
			resolver.close();
		}
	}

	private Resource createMessageResource(ResourceResolver resolver) throws PersistenceException {
		final Resource parent = ResourceUtil.getOrCreateResource(resolver, SERIALIZED_ACTIONS,
				Collections.singletonMap("jcr:primaryType", (Object) "sling:OrderedFolder"), null, false);

		return resolver.create(parent, UUID.randomUUID().toString(),
				Collections.singletonMap("jcr:primaryType", (Object) "nt:unstructured"));
	}

}
