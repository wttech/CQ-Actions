package com.cognifide.cq.actions.transport.channel;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.channels.api.Channel;
import com.cognifide.cq.channels.api.ChannelServer;

@Component
@Service
@Properties({ @Property(name = ChannelServer.TYPE, value = "echo") })
public class EchoServer implements ChannelServer {

	private static final Logger LOG = LoggerFactory.getLogger(EchoServer.class);

	@Override
	public void handleNewConnection(Channel newConnection) throws IOException {
		LOG.info("Got new connection: " + newConnection.getChannelId());
		IOUtils.copy(newConnection.getInputStream(), newConnection.getOutputStream());
		newConnection.getInputStream().close();
		newConnection.getOutputStream().close();
	}

}
