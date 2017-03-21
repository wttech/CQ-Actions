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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.cognifide.actions.msg.websocket.api.SocketSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service({ ConnectedSockets.class, SocketSender.class })
public class ConnectedSockets implements SocketClosedListener, SocketSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedSockets.class);
	private final List<MessageSocket> sockets = new CopyOnWriteArrayList<MessageSocket>();

	public MessageSocket createSocket() {
		LOGGER.debug("Creating new socket");
		final MessageSocket newSocket = new MessageSocket(this);
		sockets.add(newSocket);
		return newSocket;
	}

	@Override
	public void socketClosed(MessageSocket messageSocket) {
		sockets.remove(messageSocket);
		LOGGER.debug("Socket closed (left {} sockets)", sockets.size());
	}

	public boolean sendMessage(String message) {
		LOGGER.debug("Got message `{}` - sending", message);
		boolean success = false;
		for (MessageSocket socket : sockets) {
			success = socket.sendMessage(message) || success;
		}
		return success;
	}
}
