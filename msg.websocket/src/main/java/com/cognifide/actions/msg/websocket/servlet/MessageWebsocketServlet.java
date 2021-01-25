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

package com.cognifide.actions.msg.websocket.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

@SlingServlet(paths = MessageWebsocketServlet.PATH, methods = "GET")
public class MessageWebsocketServlet extends SlingSafeMethodsServlet implements WebSocketCreator {

	private static final long serialVersionUID = 3128232063640186422L;

	public static final String PATH = "/bin/cognifide/action/socket";

	@Reference
	private ConnectedSockets sockets;

	@Reference
	private SlingSettingsService slingSettings;

	private WebSocketServerFactory webSocketFactory;

	@Override
	public MessageSocket createWebSocket(ServletUpgradeRequest servletUpgradeRequest,
			ServletUpgradeResponse servletUpgradeResponse) {
		return sockets.createSocket();
	}

	@Activate
	public void activate() throws Exception {
		WebSocketBehavior behavior = WebSocketBehavior.SERVER;
		WebSocketPolicy policy = new WebSocketPolicy(behavior);
		webSocketFactory = new WebSocketServerFactory(policy);
		webSocketFactory.register(MessageSocket.class);
		webSocketFactory.setCreator(this);
		webSocketFactory.start();
	}

	@Deactivate
	public void deactivate() throws Exception {
		webSocketFactory.stop();
	}

	public void doGet(SlingHttpServletRequest slingRequest, SlingHttpServletResponse slingResponse)
			throws IOException {
		if (!authenticate(slingRequest, slingResponse)) {
			return;
		}

		final HttpServletResponse wrappedResponse = new WebsocketResponse(slingResponse);
		webSocketFactory.acceptWebSocket(slingRequest, wrappedResponse);
	}

	private boolean isPublish() {
		return slingSettings.getRunModes().contains("publish");
	}

	private boolean authenticate(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws IOException {
		final String userId = request.getResourceResolver().getUserID();
		if (!isPublish()) {
			response.sendError(404);
			return false;
		} else if (!"admin".equals(userId)) {
			response.addHeader("WWW-Authenticate", "Basic realm=\"Sling (Development)\"");
			response.sendError(401);
			return false;
		} else {
			return true;
		}
	}
}
