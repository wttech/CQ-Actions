package com.cognifide.cq.channels.active;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.channels.api.Channel;
import com.cognifide.cq.channels.api.ChannelServer;

public class ChannelClient implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelClient.class);

	private final String channelId;

	private final ChannelServer channelServer;

	private final String handleUrl;

	public ChannelClient(String channelId, ChannelServer channelServer, String handleUrl) {
		this.channelId = channelId;
		this.channelServer = channelServer;
		this.handleUrl = handleUrl;
	}

	@Override
	public void run() {
		try {
			final HttpClient client = new HttpClient();
			final HttpMethod method = new GetMethod(handleUrl + "/bin/cognifide/channel/id/" + channelId);
			if (client.executeMethod(method) != 200) {
				return;
			}
			final InputStream is = method.getResponseBodyAsStream();
			final OutputStream os = new ActiveOutputStream(channelId, handleUrl);
			channelServer.handleNewConnection(new Channel(is, os, channelId));
		} catch (IOException e) {
			LOG.error("Can't handle the new connection", e);
		}
	}

}
