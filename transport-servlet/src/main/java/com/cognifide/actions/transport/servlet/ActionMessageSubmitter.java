package com.cognifide.actions.transport.servlet;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import com.cognifide.actions.api.ActionSendException;
import com.cognifide.actions.api.ActionSubmitter;
import com.cognifide.actions.transport.servlet.passive.MessageSender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
@Service
public class ActionMessageSubmitter implements ActionSubmitter {

	static final String SERIALIZED_ACTIONS = "/var/cognifide/actions";

	private static final Gson GSON = new Gson();

	@Reference
	private MessageSender sender;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Override
	public void sendAction(String actionType, Map<String, String> properties) throws ActionSendException {
		final JsonObject msg = new JsonObject();
		msg.addProperty("type", actionType);
		msg.add("payload", GSON.toJsonTree(properties));
		final String serializedMsg = GSON.toJson(msg);
		if (!sender.sendMessage("ACTION", serializedMsg)) {
			try {
				persist(actionType, serializedMsg);
			} catch (RepositoryException | LoginException e) {
				throw new ActionSendException(e);
			}
		}
	}

	private void persist(String type, String serializedMsg) throws RepositoryException, LoginException {
		ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null);
		try {
			final Session session = resolver.adaptTo(Session.class);
			final Node parent = JcrUtils.getOrCreateByPath(SERIALIZED_ACTIONS, "sling:Folder", session);
			final Node node = JcrUtils.getOrCreateUniqueByPath(parent, type, "nt:unstructured");
			node.setProperty("message", serializedMsg);
			session.save();
		} finally {
			resolver.close();
		}
	}
}
