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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class MessageSocket {

	private static final Logger LOG = LoggerFactory.getLogger(MessageSocket.class);

	private final SocketClosedListener listener;

	private final AtomicBoolean confirmed = new AtomicBoolean(false);

	private Session session;

	public MessageSocket(SocketClosedListener listener) {
		this.listener = listener;
	}

	public synchronized boolean sendMessage(String msg) {
		confirmed.set(false);
		if (session == null) {
			return false;
		}
		try {
			RemoteEndpoint remote = session.getRemote();
			remote.sendString(msg);
			remote.flush();
			wait(5000);
			return confirmed.getAndSet(false);
		} catch (InterruptedException | IOException e) {
			LOG.error("Can't send message and receive confirmation", e);
			return false;
		}
	}

	@OnWebSocketClose
	public void onWebSocketClose(int i, String s) {
		LOG.info("Socket closed");
		closeConnection();
	}

	@OnWebSocketConnect
	public void onWebSocketConnect(Session session) {
		LOG.info("Socket opened");
		this.session = session;
		// Set no timeout for communication - wait forever
		this.session.setIdleTimeout(0);
	}

	@OnWebSocketError
	public void onWebSocketError(Throwable e) {
		LOG.debug("Socket Error occured", e);
		closeConnection();
	}

	private void closeConnection() {
		LOG.debug("Closing socket and session...");
		if (session != null) {
			session.close();
		}
		listener.socketClosed(this);
	}

	@OnWebSocketMessage
	public synchronized void OnWebSocketMessage(String msg) {
		LOG.debug("Incoming message " + msg);
		confirmed.set(true);
		notifyAll();
	}
}
