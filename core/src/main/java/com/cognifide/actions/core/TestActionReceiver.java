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

package com.cognifide.actions.core;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.ActionReceiver;

@Component
@Service
public class TestActionReceiver implements ActionReceiver {

	private static final Logger LOG = LoggerFactory.getLogger(TestActionReceiver.class);

	@Override
	public String getType() {
		return "TEST";
	}

	@Override
	public void handleAction(ValueMap properties) {
		LOG.info("Received new TEST action: " + properties);
	}

}
