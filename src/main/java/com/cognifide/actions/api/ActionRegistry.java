package com.cognifide.actions.api;

/*--
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * This interface is an internal part of the CQ Actions implementation. Don't use its methods.
 * 
 * @author Tomasz Rękawek
 *
 */
public interface ActionRegistry {

	/**
	 * Returns action service for a given type.
	 */
	Action getAction(String type);

	/**
	 * Creates new action node.
	 * 
	 * @deprecated Use {@link ActionSubmitter#sendAction}.
	 */
	@Deprecated
	Node createActionNode(Session session, String relPath, String type) throws RepositoryException;

	/**
	 * Returns the action root folder.
	 * 
	 * @return
	 */
	String getActionRoot();

}
