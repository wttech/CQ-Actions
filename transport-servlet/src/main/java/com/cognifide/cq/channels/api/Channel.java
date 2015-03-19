package com.cognifide.cq.channels.api;

import java.io.InputStream;
import java.io.OutputStream;

public class Channel {

	private final InputStream inputStream;

	private final OutputStream outputStream;

	private final String channelId;

	public Channel(InputStream inputStream, OutputStream outputStream, String channelId) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.channelId = channelId;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public String getChannelId() {
		return channelId;
	}
}
