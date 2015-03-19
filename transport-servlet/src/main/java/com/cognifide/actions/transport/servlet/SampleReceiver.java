package com.cognifide.actions.transport.servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.transport.servlet.active.MessageReceiver;

@Component
@Service
public class SampleReceiver implements MessageReceiver {

	private static final Logger LOG = LoggerFactory.getLogger(SampleReceiver.class);

	@Override
	public void gotMessage(String line) {
		LOG.info("Got message " + line);
	}

}
