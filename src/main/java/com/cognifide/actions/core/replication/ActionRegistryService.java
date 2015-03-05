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

package com.cognifide.actions.core.replication;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.api.Action;
import com.cognifide.actions.api.ActionRegistry;
import com.cognifide.actions.core.Configuration;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.NameConstants;

@Component(immediate = true)
@Service
@Deprecated
public class ActionRegistryService implements ActionRegistry {

	@Reference(referenceInterface = Action.class, policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private final Set<Action> actions = new CopyOnWriteArraySet<Action>();

	@Reference
	private Configuration config;

	@Activate
	void activate() {
		actions.clear();
	}

	protected void bindActions(Action action) {
		actions.add(action);
	}

	protected void unbindActions(Action action) {
		actions.remove(action);
	}

	// Methods below are left for compatibility reasons, but shouldn't be used anymore.
	@Override
	@Deprecated
	public Node createActionNode(Session session, String relPath, String type) throws RepositoryException {
		final String path = ReplicationBasedSubmitterService.createPath(relPath, config.getActionRoot(),
				config.getRandomPathPattern());
		final Node page = JcrUtil.createPath(path, true, "sling:Folder", "cq:Page", session, false);
		final Node content = page.addNode(JcrConstants.JCR_CONTENT, "cq:PageContent");
		content.setProperty("sling:resourceType", type);
		content.setProperty("cq:distribute", false);
		session.save();

		content.setProperty(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
		content.setProperty(NameConstants.PN_PAGE_LAST_MOD_BY, session.getUserID());
		content.setProperty("cq:distribute", true);
		return content;
	}

	@Override
	@Deprecated
	public String getActionRoot() {
		return config.getActionRoot();
	}

	@Override
	public Action getAction(String type) {
		for (Action action : actions) {
			if (type.equals(action.getType())) {
				return action;
			}
		}
		return null;
	}
}
