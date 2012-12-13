package com.cognifide.actions.api;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface ActionRegistry {

	Action getAction(String type);

	Node createActionNode(Session session, String relPath, String type) throws RepositoryException;

	String getActionRoot();

}
