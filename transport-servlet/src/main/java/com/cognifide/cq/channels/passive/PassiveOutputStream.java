package com.cognifide.cq.channels.passive;

import java.io.IOException;
import java.io.OutputStream;

import com.cognifide.cq.channels.passive.api.PassiveOutputStreamProvider;

public class PassiveOutputStream extends OutputStream {

	private final byte[] buffer = new byte[16 * 1024];

	private final PassiveOutputStreamProvider osProvider;

	private final String channelId;

	private int offset;

	private boolean closed;

	private OutputStream os;

	public PassiveOutputStream(PassiveOutputStreamProvider osProvider, String channelId) {
		this.osProvider = osProvider;
		this.channelId = channelId;
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
		if (os == null) {
			try {
				os = osProvider.getOutputStream(channelId, 5000);
			} catch (InterruptedException e) {
				throw new IOException("Can't get the output stream", e);
			}
			if (os == null) {
				throw new IOException("Can't get the output stream");
			}
		}
		os.write(buffer, 0, offset);
		offset = 0;
	}

	@Override
	public void close() {
		closed = true;
		if (os != null) {
			synchronized (os) {
				os.notifyAll();
			}
		}
	}
}
