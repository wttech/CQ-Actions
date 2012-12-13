package com.cognifide.actions.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

public class Utils {

	private Utils() {

	}

	public static String propertyToString(ComponentContext ctx, String name, String defaultValue) {
		Object object = ctx.getProperties().get(name);
		String value = PropertiesUtil.toString(object, defaultValue);
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	public static String slashEnd(String string) {
		if (string != null && !string.endsWith("/")) {
			return string + "/";
		}
		return string;
	}

}
