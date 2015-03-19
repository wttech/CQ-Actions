package com.cognifide.cq.channels.api;

import java.io.IOException;

public interface ChannelServer {
	static String TYPE = "channel.type";

	void handleNewConnection(Channel newConnection) throws IOException;
}
