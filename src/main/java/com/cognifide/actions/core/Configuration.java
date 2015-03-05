package com.cognifide.actions.core;

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
