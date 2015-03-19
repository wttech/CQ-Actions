package com.cognifide.actions.transport.servlet.active;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLoop implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ClientLoop.class);

	private static final String SERVLET_PATH = "/bin/cognifide/cq-actions.txt";

	private final Set<MessageReceiver> receivers;

	private final String serverUrl;

	private final HttpClient client;

	private volatile boolean shouldStop = false;

	public ClientLoop(Set<MessageReceiver> receivers, String serverUrl) {
		this.receivers = receivers;
		this.serverUrl = serverUrl;

		final HttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
		this.client = new HttpClient(connManager);
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
			for (MessageReceiver r : receivers) {
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
	}

}
