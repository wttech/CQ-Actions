package com.cognifide.actions.core.util;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * @see {@link AdminJcrCommandExecutor}
 * @author witold_szczerba
 */
public interface JcrCommand {

	void run(Session session, ResourceResolver resolver) throws Exception;

}
