package com.cognifide.actions.core.util;

/*
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
