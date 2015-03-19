package com.cognifide.cq.channels.active;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

public class ActiveOutputStream extends OutputStream {

	private final HttpClient client = new HttpClient();

	private final byte[] buffer = new byte[1024 * 16];

	private final String channelId;

	private final String handleUrl;

	private int offset;

	private boolean closed;

	public ActiveOutputStream(String channelId, String handleUrl) {
		this.channelId = channelId;
		this.handleUrl = handleUrl;
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Stream is closed");
		}

		buffer[offset++] = (byte) b;
		if (offset == buffer.length) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Stream is closed");
		}

		final byte[] content = new byte[offset];
		for (int i = 0; i < offset; i++) {
			content[i] = buffer[i];
		}
		final PostMethod method = new PostMethod(handleUrl + "/bin/cognifide/channel/id/" + channelId);
		final RequestEntity entity = new ByteArrayRequestEntity(content);
		method.setRequestEntity(entity);
		client.executeMethod(method);
		offset = 0;
	}

	@Override
	public void close() {
		closed = true;
	}
}
