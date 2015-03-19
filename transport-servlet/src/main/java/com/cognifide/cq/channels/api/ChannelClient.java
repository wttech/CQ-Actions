package com.cognifide.cq.channels.api;

import java.io.IOException;

public interface ChannelClient {
	Channel connect(String type) throws IOException;
}
