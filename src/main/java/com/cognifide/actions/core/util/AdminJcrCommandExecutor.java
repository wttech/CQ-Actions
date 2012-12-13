package com.cognifide.actions.core.util;

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

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;

/**
 * This service executes commands providing {@link Session} and {@link ResourceResolver} from
 * {@link org.apache.sling.jcr.api.SlingRepository} using administrative login.
 * 
 */
// @formatter:off
@Component(immediate = true)
@Service(AdminJcrCommandExecutor.class)
@Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Admin Jcr Command Runner"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide")
})
// @formatter:on
public class AdminJcrCommandExecutor {

	@Reference
	private ResourceResolverFactory resolverFactory;

	public AdminJcrCommandExecutor() {
	}

	public synchronized void execute(JcrCommand cmd) throws Exception {
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			Session session = resourceResolver.adaptTo(Session.class);
			cmd.run(session, resourceResolver);
		} catch (Exception ex) {
			throw ex;
			// throw new RuntimeException("Error processing command.", ex);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}
}
