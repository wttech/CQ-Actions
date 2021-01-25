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

package com.cognifide.actions.msg.replication;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;

@Component(metatype = true)
@Service(Configuration.class)
public class Configuration {

	@Property(value = Configuration.ROOT_DEFAULT)
	private static final String ROOT_NAME = "usermanagement.users.actions.registry.root";

	@Property(value = Configuration.RANDOM_PATTERN_DEFAULT)
	private static final String RANDOM_PATTERN_NAME = "actions.registry.random.pattern";

	private static final String ROOT_DEFAULT = "/content/usergenerated/actions/";

	private static final String RANDOM_PATTERN_DEFAULT = "**/**";

	private String randomPathPattern;

	private String actionRoot;

	@Activate
	void activate(Map<String, Object> config) {
		actionRoot = PropertiesUtil.toString(config.get(ROOT_NAME), ROOT_DEFAULT);
		if (actionRoot != null && !actionRoot.endsWith("/")) {
			actionRoot = actionRoot + "/";
		}
		randomPathPattern = PropertiesUtil.toString(config.get(RANDOM_PATTERN_NAME), RANDOM_PATTERN_DEFAULT);
	}

	public String getRandomPathPattern() {
		return randomPathPattern;
	}

	public String getActionRoot() {
		return actionRoot;
	}
}
