package com.cognifide.cq.channels.passive.api;

import com.cognifide.cq.channels.passive.PassiveInputStream;

public interface PassiveInputStreamHandler {
	void handleNewInputStream(String channelId, PassiveInputStream is);

	void inputStreamClosed(String channelId);
}
