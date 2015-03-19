package com.cognifide.actions.transport.servlet.passive;

public interface MessageSender {
	boolean sendMessage(String topic, String msg);
}
