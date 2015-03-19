package com.cognifide.actions.core;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.actions.api.ActionReceiver;

@Component
@Service
public class TestActionReceiver implements ActionReceiver {

	private static final Logger LOG = LoggerFactory.getLogger(TestActionReceiver.class);

	@Override
	public String getType() {
		return "TEST";
	}

	@Override
	public void handleAction(Map<String, String> properties) {
		LOG.info("Received new TEST action: " + properties);
	}

}
