package com.cognifide.actions.transport.servlet;

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
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.transport.servlet.passive.MessageSender;

@Component
@Service
@Properties({ @Property(name = "scheduler.concurrent", boolValue = false),
		@Property(name = "scheduler.period", longValue = 30) })
public class Resender implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Resender.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private MessageSender sender;

	@Override
	public void run() {
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			final Resource parent = resolver.getResource(ActionMessageSubmitter.SERIALIZED_ACTIONS);
			if (parent == null) {
				return;
			}
			for (final Resource child : parent.getChildren()) {
				resend(child);
			}
			resolver.commit();
		} catch (LoginException | PersistenceException e) {
			LOG.error("Can't resend", e);
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
	}

	private void resend(Resource child) throws PersistenceException {
		final String message = child.adaptTo(ValueMap.class).get("message", String.class);
		if (message != null) {
			if (sender.sendMessage("ACTION", message)) {
				child.getResourceResolver().delete(child);
			}
		}
	}

}
