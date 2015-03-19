package com.cognifide.cq.channels.active;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.channels.api.ChannelServer;

public class ChannelDispatcherLoop implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelDispatcherLoop.class);

	private final Map<String, ChannelServer> servers;

	private final String serverUrl;

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private volatile boolean shouldStop = false;

	public ChannelDispatcherLoop(Map<String, ChannelServer> servers, String serverUrl) {
		this.servers = servers;
		this.serverUrl = serverUrl;
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
			}
		}
		executor.shutdownNow();
	}

	private void connect() throws HttpException, IOException {
		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod(serverUrl + "/bin/cognifide/channel/dispatcher.txt");
		if (client.executeMethod(method) != 200) {
			return;
		}
		final InputStreamReader reader = new InputStreamReader(method.getResponseBodyAsStream());
		final BufferedReader bufferedReader = new BufferedReader(reader);

		String type;
		while ((type = bufferedReader.readLine()) != null) {
			final String channelId = bufferedReader.readLine();
			final ChannelServer channelServer = servers.get(type);
			if (channelServer == null) {
				LOG.error("Can't find server for " + type);
			} else {
				handleNewConnection(channelId, channelServer);
			}
		}
	}

	private void handleNewConnection(String channelId, ChannelServer channelServer) {
		executor.submit(new ChannelClient(channelId, channelServer, serverUrl));
	}

	public void stop() {
		shouldStop = true;
	}
}