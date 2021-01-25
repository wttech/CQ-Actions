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

package com.cognifide.actions.msg.push.active;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.msg.push.api.PushReceiver;

public class PushClientRunnable implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PushClientRunnable.class);

	private static final String SERVLET_PATH = "/bin/cognifide/cq-actions.txt";

	private final Set<PushReceiver> receivers;

	private final String serverUrl;

	private final HttpClient client;

	private volatile boolean shouldStop = false;

	public PushClientRunnable(Set<PushReceiver> receivers, String serverUrl, String username, String password)
			throws URISyntaxException {
		this.receivers = receivers;
		this.serverUrl = serverUrl;

		final HttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
		client = new HttpClient(connManager);
		client.getParams().setAuthenticationPreemptive(true);
		final Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		final URI serverUri = new URI(serverUrl);
		final int port = serverUri.getPort() == -1 ? 80 : serverUri.getPort();
		client.getState().setCredentials(new AuthScope(serverUri.getHost(), port, AuthScope.ANY_REALM),
				defaultcreds);
	}

	@Override
	public void run() {
		while (!shouldStop) {
			try {
				connect();
			} catch (IOException e) {
				LOG.error("Can't connect to the server " + serverUrl, e);
			}
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e1) {
				LOG.error("Interrupted", e1);
				return;
			}
		}
	}

	private void connect() throws IOException {
		final HttpMethod method = new GetMethod(serverUrl + SERVLET_PATH);
		if (client.executeMethod(method) != 200) {
			return;
		}
		final InputStreamReader reader = new InputStreamReader(method.getResponseBodyAsStream());
		final BufferedReader bufferedReader = new BufferedReader(reader);

		String msgId;
		while ((msgId = bufferedReader.readLine()) != null) {
			final String topic = bufferedReader.readLine();
			final String msg = bufferedReader.readLine();
			LOG.debug("Got message with id " + msgId);
			for (PushReceiver r : receivers) {
				r.gotMessage(topic, msg);
			}
			try {
				confirm(msgId);
				LOG.debug("Message " + msgId + " confirmed");
			} catch (IOException e) {
				LOG.error("Can't confirm message " + msgId, e);
			}
		}
		method.releaseConnection();
	}

	private void confirm(String msgId) throws IOException {
		final HttpMethod method = new PostMethod(serverUrl + SERVLET_PATH + "/" + msgId);
		client.executeMethod(method);
		method.releaseConnection();
	}

	public void stop() {
		shouldStop = true;
		((MultiThreadedHttpConnectionManager) client.getHttpConnectionManager()).shutdown();
	}

}
