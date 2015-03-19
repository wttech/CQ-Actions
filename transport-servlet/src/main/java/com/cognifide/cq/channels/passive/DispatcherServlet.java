package com.cognifide.cq.channels.passive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import com.cognifide.cq.channels.api.Channel;
import com.cognifide.cq.channels.api.ChannelClient;
import com.cognifide.cq.channels.passive.api.PassiveInputStreamHandler;
import com.cognifide.cq.channels.passive.api.PassiveOutputStreamProvider;

@SlingServlet(paths = "/bin/cognifide/channel/dispatcher", extensions = "txt")
public class DispatcherServlet extends SlingAllMethodsServlet implements ChannelClient {

	private static final long serialVersionUID = -4526577354181234002L;

	@Reference
	private PassiveOutputStreamProvider osProvider;

	@Reference
	private PassiveInputStreamHandler isHandler;

	private PrintWriter writer;

	@Deactivate
	public synchronized void deactivate() {
		notifyAll();
	}

	@Override
	public synchronized void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws IOException, ServletException {
		if (writer != null) {
			throw new ServletException("The dispatcher servlet is already connected");
		}
		writer = response.getWriter();
		try {
			wait();
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} finally {
			writer = null;
		}
	}

	@Override
	public Channel connect(String type) throws IOException {
		final String channelId = UUID.randomUUID().toString();
		writer.println(type);
		writer.println(channelId);
		writer.flush();

		final InputStream is = new PassiveInputStream(isHandler, channelId);
		final OutputStream os = new PassiveOutputStream(osProvider, channelId);

		return new Channel(is, os, channelId);
	}
}
