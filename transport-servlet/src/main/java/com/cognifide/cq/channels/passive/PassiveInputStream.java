package com.cognifide.cq.channels.passive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import com.cognifide.cq.channels.passive.api.PassiveInputStreamHandler;

public class PassiveInputStream extends InputStream {

	private final Deque<InputStream> streams = new ArrayDeque<InputStream>();

	private final PassiveInputStreamHandler handler;

	private final String channelId;

	private boolean closed;

	public PassiveInputStream(PassiveInputStreamHandler handler, String channelId) {
		this.channelId = channelId;
		this.handler = handler;
		handler.handleNewInputStream(channelId, this);
	}

	@Override
	public synchronized int read() throws IOException {
		while (!streams.isEmpty()) {
			final InputStream s = streams.peekFirst();
			final int b = s.read();
			if (b == -1) {
				streams.pollFirst();
				synchronized (s) {
					s.notifyAll();
				}
			} else {
				return b;
			}
		}
		return -1;
	}

	public synchronized boolean addInputStream(InputStream stream) throws IOException {
		if (closed) {
			return false;
		} else {
			streams.addLast(stream);
			return true;
		}
	}

	public synchronized void close() {
		closed = true;
		while (!streams.isEmpty()) {
			final InputStream is = streams.poll();
			synchronized (is) {
				is.notifyAll();
			}
		}
		handler.inputStreamClosed(channelId);
	}
}
