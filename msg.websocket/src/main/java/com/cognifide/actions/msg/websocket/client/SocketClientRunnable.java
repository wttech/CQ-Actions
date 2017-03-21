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

package com.cognifide.actions.msg.websocket.client;

import com.cognifide.actions.msg.websocket.api.SocketReceiver;
import com.cognifide.actions.msg.websocket.servlet.MessageWebsocketServlet;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.apache.commons.lang.StringUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClientRunnable extends Endpoint implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(SocketClientRunnable.class);

  private final Set<SocketReceiver> receivers;

  private final String serverUrl;

  private final ClientManager client;

  private Session session;

  private volatile boolean shouldStop = false;

  public SocketClientRunnable(Set<SocketReceiver> receivers, String serverUrl, String username,
      String password) throws URISyntaxException {
    this.receivers = receivers;
    this.serverUrl = serverUrl.replace("http://", "ws://") + MessageWebsocketServlet.PATH;
    client = ClientManager.createClient();
    String clientUsername = StringUtils.defaultIfEmpty(username, "admin");
    String clientPassword = StringUtils.defaultIfEmpty(password, "admin");
    client.getProperties()
        .put(ClientProperties.CREDENTIALS, new Credentials(clientUsername, clientPassword));
  }

  @Override
  public void onOpen(final Session session, EndpointConfig endpointConfig) {
    LOG.info("Session {} opened", session.getId());
    this.session = session;
    session.addMessageHandler(new MessageHandler.Whole<String>() {
      @Override
      public void onMessage(String message) {
        LOG.debug("Got message: " + message);
        gotMessage(message);
        try {
          session.getBasicRemote().sendText("OK");
        } catch (IOException e) {
          LOG.error("Can't respond to the message");
        }
      }
    });
  }

  @Override
  public void run() {
    reconnect();
    while (!shouldStop) {
      if (connectionBroken()) {
        String sessionId = session != null ? session.getId() : null;
        LOG.debug("Connection was lost... session: {} no longer active", sessionId);
        reconnect();
      }
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e1) {
        LOG.error("Interrupted", e1);
        return;
      }
    }
  }

  public void stop() {
    shouldStop = true;
    closeSession();
  }

  private boolean connectionBroken() {
    return session == null || !session.isOpen();
  }

  private void reconnect() {
    closeSession();
    try {
      LOG.debug("Connecting to server: `{}` (receivers no: {})", serverUrl, receivers.size());
      final Session currentSession = client.connectToServer(this, new URI(serverUrl));
      LOG.debug("New session created: ", currentSession.getId());
    } catch (IOException | DeploymentException | URISyntaxException e) {
      LOG.error("Can't connect to the server {}", serverUrl, e);
    }
  }

  private void closeSession() {
    String sessionId = session != null ? session.getId() : null;
    LOG.info("Closing session: {}", sessionId);
    if (session != null) {
      try {
        session.close();
      } catch (IOException e) {
        LOG.error("Can't close session", e);
      }
    }
  }

  protected void gotMessage(String message) {
    for (SocketReceiver receiver : receivers) {
      receiver.gotMessage(message);
    }
  }

}
