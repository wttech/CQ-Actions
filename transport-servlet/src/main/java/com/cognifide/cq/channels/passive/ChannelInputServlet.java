package com.cognifide.cq.channels.passive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import com.cognifide.cq.channels.passive.api.PassiveInputStreamHandler;

@SlingServlet(paths = "/bin/cognifide/channel/id", methods = "POST")
public class ChannelInputServlet extends SlingAllMethodsServlet implements PassiveInputStreamHandler {

	private static final long TIMEOUT = 5000;

	private static final long serialVersionUID = -4526577354181234002L;

	private Map<String, PassiveInputStream> streams = new HashMap<String, PassiveInputStream>();

	@Deactivate
	public void deactivate() {
		for (PassiveInputStream os : streams.values()) {
			os.close();
		}
		streams.clear();
	}

	@Override
	public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws IOException, ServletException {
		final String channelId = StringUtils.removeStart(request.getRequestPathInfo().getSuffix(), "/");
		if (StringUtils.isBlank(channelId)) {
			throw new ServletException("Empty connection name");
		}

		long start = System.currentTimeMillis();
		final PassiveInputStream pis;
		synchronized (this) {
			while (!streams.containsKey(channelId)) {
				final long elapsed = System.currentTimeMillis() - start;
				if (elapsed > TIMEOUT) {
					break;
				}
				try {
					wait(TIMEOUT - elapsed);
				} catch (InterruptedException e) {
					throw new ServletException(e);
				}
			}
			pis = streams.get(channelId);
		}
		if (pis == null) {
			throw new ServletException("No channel with id " + channelId);
		}

		final InputStream is = request.getInputStream();
		if (!pis.addInputStream(is)) {
			return;
		}
		synchronized (is) {
			try {
				is.wait();
			} catch (InterruptedException e) {
				throw new ServletException(e);
			}
		}
		is.close();
	}

	@Override
	public synchronized void handleNewInputStream(String channelId, PassiveInputStream is) {
		streams.put(channelId, is);
		notify();
	}

	@Override
	public synchronized void inputStreamClosed(String channelId) {
		streams.remove(channelId);
	}
}
