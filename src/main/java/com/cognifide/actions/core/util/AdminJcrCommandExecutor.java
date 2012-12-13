package com.cognifide.actions.core.util;

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
 * @author witold_szczerba
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
