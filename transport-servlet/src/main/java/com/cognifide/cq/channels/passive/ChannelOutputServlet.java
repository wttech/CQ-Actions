package com.cognifide.cq.channels.passive;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import com.cognifide.cq.channels.passive.api.PassiveOutputStreamProvider;

@SlingServlet(paths = "/bin/cognifide/channel/id", methods = "GET")
public class ChannelOutputServlet extends SlingSafeMethodsServlet implements PassiveOutputStreamProvider {

	private static final long serialVersionUID = -4526577354181234002L;

	private Map<String, OutputStream> streams = new HashMap<String, OutputStream>();

	@Deactivate
	public void deactivate() {
		for (OutputStream os : streams.values()) {
			synchronized (os) {
				os.notifyAll();
			}
		}
		streams.clear();
	}

	@Override
	public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException,
			ServletException {
		final String channelId = StringUtils.removeStart(request.getRequestPathInfo().getSuffix(), "/");
		if (StringUtils.isBlank(channelId)) {
			throw new ServletException("Empty connection name");
		}

		final OutputStream os = response.getOutputStream();
		synchronized (this) {
			streams.put(channelId, os);
			notifyAll();
		}

		synchronized (os) {
			try {
				os.wait();
				streams.remove(channelId);
			} catch (InterruptedException e) {
				throw new ServletException(e);
			}
		}
	}

	@Override
	public OutputStream getOutputStream(String channelId, int timeout) throws InterruptedException {
		long start = System.currentTimeMillis();

		synchronized (this) {
			while (!streams.containsKey(channelId)) {
				long elapsed = System.currentTimeMillis() - start;
				if (elapsed > timeout) {
					break;
				}
				wait(timeout - elapsed);
			}
		}

		return streams.get(channelId);
	}

}
