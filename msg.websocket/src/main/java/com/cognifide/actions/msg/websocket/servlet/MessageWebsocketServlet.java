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

package com.cognifide.actions.msg.websocket.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.eclipse.jetty.websocket.WebSocketFactory.Acceptor;

@SlingServlet(paths = MessageWebsocketServlet.PATH, methods = "GET")
public class MessageWebsocketServlet extends SlingSafeMethodsServlet implements Acceptor {

	private static final long serialVersionUID = -3631947705678810095L;

	public static final String PATH = "/bin/cognifide/action/socket";

	@Reference
	private ConnectedSockets sockets;

	@Reference
	private SlingSettingsService slingSettings;

	private WebSocketFactory webSocketFactory;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return sockets.createSocket();
	}

	@Activate
	public void activate() throws Exception {
		webSocketFactory = new WebSocketFactory(this, 8192);
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

	@Override
	public boolean checkOrigin(HttpServletRequest paramHttpServletRequest, String paramString) {
		return true;
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
