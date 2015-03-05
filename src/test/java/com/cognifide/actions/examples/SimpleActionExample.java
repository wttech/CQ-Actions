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

package com.cognifide.actions.examples;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.Action;
import com.day.cq.wcm.api.Page;

// @formatter:off
@Service
@Component(immediate = true)
@Properties({ @Property(name = Constants.SERVICE_DESCRIPTION, value = "Simple Action Example"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide") })
// @formatter:on
public class SimpleActionExample implements Action {

	public static final String ACTION_TYPE = "com/cognifide/actions/myExampleActionType";

	private static final Logger LOG = LoggerFactory.getLogger(SimpleActionExample.class);

	@Override
	public void perform(Page page) throws Exception {
		String currentThread = Thread.currentThread().getName();
		String pagePath = page.getPath();

		LOG.info("Performing Example Action for page: " + pagePath + " in thread: " + currentThread);

		// For parallel|sequence jobs queue testing
		Thread.sleep((int) (Math.random() * 3000));

		// Testing failed jobs for re-run.
		if (Math.random() > 0.3) {
			throw new Exception("Exeption occured!. for page: " + pagePath + " in thread:"
					+ currentThread);
		}
	}

	@Override
	public String getType() {
		return ACTION_TYPE;
	}

}
