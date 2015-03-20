package com.cognifide.actions.transport.servlet.passive;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(methods = { "GET", "POST" }, paths = "/bin/cognifide/cq-actions", extensions = "txt", generateService = false)
@Service({ Servlet.class, MessageSender.class })
public class AcceptingServlet extends SlingAllMethodsServlet implements MessageSender {

	private static final Logger LOG = LoggerFactory.getLogger(AcceptingServlet.class);

	private static final long TIMEOUT = 5 * 1000;

	private static final long serialVersionUID = 661757446730532042L;

	private final Set<String> sentMessages = Collections.synchronizedSet(new HashSet<String>());

	private final Set<String> receivedConfirmations = Collections.synchronizedSet(new HashSet<String>());

	private Object connectionHold;

	private PrintWriter writer;

	@Reference
	private SlingSettingsService slingSettings;

	@Activate
	public void activate() {
		connectionHold = new Object();
	}

	@Deactivate
	public void deactivate() {
		synchronized (connectionHold) {
			connectionHold.notify();
		}
		connectionHold = null;
		writer = null;
		synchronized (receivedConfirmations) {
			receivedConfirmations.notifyAll();
		}
	}

	public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		if (!authenticate(request, response)) {
			return;
		}
		response.setContentType("text/plain; charset=utf-8");

		synchronized (connectionHold) {
			connectionHold.notifyAll();
		}
		synchronized (this) {
			writer = response.getWriter();
		}
		try {
			synchronized (connectionHold) {
				connectionHold.wait();
			}
		} catch (InterruptedException e) {
			throw new ServletException("Interrupted", e);
		}
	}

	public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		if (!authenticate(request, response)) {
			return;
		}

		final String msgId = StringUtils.removeStart(request.getRequestPathInfo().getSuffix(), "/");
		if (StringUtils.isBlank(msgId)) {
			throw new ServletException("No suffix found");
		}
		if (!sentMessages.contains(msgId)) {
			throw new ServletException("No one is waiting for confirmation for " + msgId);
		} else {
			receivedConfirmations.add(msgId);
			synchronized (receivedConfirmations) {
				receivedConfirmations.notifyAll();
			}
			response.getWriter().append("Message " + msgId + " confirmed");
			LOG.debug("Message " + msgId + " confirmed");
		}
	}

	@Override
	public boolean sendMessage(String topic, String msg) {
		final String msgId;
		synchronized (this) {
			if (msg.contains("\n")) {
				throw new IllegalArgumentException("Message can't contain new line character");
			}
			if (writer == null || writer.checkError()) {
				return false;
			}
			msgId = UUID.randomUUID().toString();
			sentMessages.add(msgId);
			writer.println(msgId);
			writer.println(topic);
			writer.println(msg);
			writer.flush();
			if (writer.checkError()) {
				sentMessages.remove(msgId);
				return false;
			}
		}

		LOG.debug("Waiting for confirmation for " + msgId);
		try {
			final long start = System.currentTimeMillis();
			while (!receivedConfirmations.remove(msgId)) {
				final long elapsed = System.currentTimeMillis() - start;
				if (elapsed > TIMEOUT || connectionHold == null) {
					return false;
				}
				synchronized (receivedConfirmations) {
					receivedConfirmations.wait(TIMEOUT - elapsed);
				}
			}
		} catch (InterruptedException e) {
			return false;
		} finally {
			sentMessages.remove(msgId);
		}
		return true;
	}

	private boolean isPublish() {
		return slingSettings.getRunModes().contains("publish");
	}

	private boolean authenticate(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws IOException {
		final String userId = request.getResourceResolver().getUserID();
		if (!isPublish()) {
			response.sendError(404);
			return false;
		} else if (!"admin".equals(userId)) {
			response.sendError(403);
			return false;
		} else {
			return true;
		}
	}
}
