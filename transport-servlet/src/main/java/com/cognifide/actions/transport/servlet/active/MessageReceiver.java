package com.cognifide.actions.transport.servlet.active;

public interface MessageReceiver {

	void gotMessage(String topic, String msg);

}
