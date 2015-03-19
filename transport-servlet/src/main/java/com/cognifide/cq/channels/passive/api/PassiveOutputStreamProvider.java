package com.cognifide.cq.channels.passive.api;

import java.io.OutputStream;

public interface PassiveOutputStreamProvider {

	OutputStream getOutputStream(String channelId, int timeout) throws InterruptedException;

}
