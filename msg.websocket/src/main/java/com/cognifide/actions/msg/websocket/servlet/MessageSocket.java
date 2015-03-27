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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageSocket implements WebSocket, OnTextMessage {

	private static final Logger LOG = LoggerFactory.getLogger(MessageSocket.class);

	private final SocketClosedListener listener;

	private final AtomicBoolean confirmed = new AtomicBoolean(false);

	private Connection connection;

	public MessageSocket(SocketClosedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onOpen(Connection connection) {
		LOG.info("Socket opened");
		this.connection = connection;
	}

	@Override
	public void onClose(int closeCode, String msg) {
		LOG.info("Socket closed");
		listener.socketClosed(this);
	}

	@Override
	public synchronized void onMessage(String msg) {
		LOG.info("Incoming message " + msg);
		confirmed.set(true);
		notifyAll();
	}

	public synchronized boolean sendMessage(String msg) {
		confirmed.set(false);
		if (connection == null) {
			return false;
		}
		try {
			connection.sendMessage(msg);
			wait(5000);
			return confirmed.getAndSet(false);
		} catch (InterruptedException | IOException e) {
			LOG.error("Can't send message and receive confirmation", e);
			return false;
		}
	}

}
