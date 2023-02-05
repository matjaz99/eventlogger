/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.eventlogger.webhooks.http;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.webhooks.RequestProcessor;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class HttpWebhook extends HttpServlet {

	private static final long serialVersionUID = 4275913222328716391L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		DRequest m = RequestProcessor.processIncomingRequest(req, DProps.requestsReceivedCount++);

		IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
		iDataManager.addHttpRequest(m);
		DMetrics.eventlogger_http_requests_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc();
		DMetrics.eventlogger_http_requests_size_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc(m.getContentLength());

		try {

			// process body
			DEvent e = new DEvent();
			e.setId(DProps.eventsReceivedCount++);
			e.setRuntimeId(DProps.RUNTIME_ID);
			e.setTimestamp(System.currentTimeMillis());
			e.setHost(m.getRemoteHost());
			e.setIdent("eventlogger.http.get");
			e.setPid("0");
			if (m.getParameterMap().containsKey("ident")) {
				e.setIdent(m.getParameterMap().get("ident"));
			}
			if (m.getParameterMap().containsKey("pid")) {
				e.setPid(m.getParameterMap().get("pid"));
			}
			if (m.getParameterMap().containsKey("tag")) {
				e.setTag(m.getParameterMap().get("tag"));
			}
			if (m.getParameterMap().containsKey("msg")) {
				e.setMessage(m.getParameterMap().get("msg"));
			} else if (m.getParameterMap().containsKey("message")) {
				e.setMessage(m.getParameterMap().get("message"));
			} else {
				e.setMessage(null);
			}
			e.setEventSource("eventlogger.http.get");
			LogFactory.getLogger().trace(e.toString());
			DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), e.getHost(), e.getIdent()).inc();

			if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
				List<DEvent> eventList = new ArrayList<>();
				eventList.add(e);
				iDataManager.addEvents(eventList);
				return;
			}

			DMetrics.eventlogger_events_ignored_total.labels(m.getRemoteHost(), m.getMethod()).inc();
			LogFactory.getLogger().warn("HttpWebhook: doGet: message is empty; event will be ignored");

		} catch (Exception e) {
			LogFactory.getLogger().warn("HttpWebhook: doGet: Exception: " + e.getMessage());
		} finally {
			DataManagerFactory.getInstance().returnClient(iDataManager);
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response) throws IOException {

		DRequest m = RequestProcessor.processIncomingRequest(req, DProps.requestsReceivedCount++);

		IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
		iDataManager.addHttpRequest(m);
		DMetrics.eventlogger_http_requests_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc();
		DMetrics.eventlogger_http_requests_size_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc(m.getContentLength());

		try {

			if (req.getContentType().equalsIgnoreCase("application/json")) {
				processApplicationJson(m, iDataManager);
			}
			if (req.getContentType().equalsIgnoreCase("text/plain")) {
			}
			if (req.getContentType().equalsIgnoreCase("application/xml")) {
			}



		} catch (Exception e) {
			LogFactory.getLogger().warn("HttpWebhook: doPost: Exception: " + e.getMessage());
		} finally {
			DataManagerFactory.getInstance().returnClient(iDataManager);
		}

	}

	private void processApplicationJson(DRequest m, IDataManager iDataManager) {
		// process body
//		DEvent e = new DEvent();
//		e.setId(DProps.eventsReceivedCount++);
//		e.setRuntimeId(DProps.RUNTIME_ID);
//		e.setTimestamp(System.currentTimeMillis());
//		e.setHost(m.getRemoteHost());
//		e.setIdent("eventlogger.http.post");
//		e.setPid("0");
//		if (m.getParameterMap().containsKey("ident")) {
//			e.setIdent(m.getParameterMap().get("ident"));
//		}
//		if (m.getParameterMap().containsKey("pid")) {
//			e.setPid(m.getParameterMap().get("pid"));
//		}
//		if (m.getParameterMap().containsKey("tag")) {
//			e.setTag(m.getParameterMap().get("tag"));
//		}
//		e.setMessage(m.getBody());
//		e.setEventSource("eventlogger.http.post");
//		LogFactory.getLogger().trace(e.toString());
//		DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), m.getRemoteHost(), e.getIdent()).inc();

		// TODO check if it is an array or just a json object

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		List<DEvent> elist = gson.fromJson(m.getBody().trim(), new TypeToken<List<DEvent>>(){}.getType());

		String ident = "null";
		long now = System.currentTimeMillis();

		for (DEvent de : elist) {
			de.setId(DProps.eventsReceivedCount++);
			de.setRuntimeId(DProps.RUNTIME_ID);
			de.setTimestamp(now);
			de.setHost(m.getRemoteHost());
			de.setEventSource(m.getRemoteHost());
			de.setIdent("eventlogger.http.post");
			de.setPid("0");
			if (m.getParameterMap().containsKey("ident")) {
				ident = m.getParameterMap().get("ident");
				de.setIdent(ident);
			}
			if (m.getParameterMap().containsKey("pid")) {
				de.setPid(m.getParameterMap().get("pid"));
			}
			if (m.getParameterMap().containsKey("tag")) {
				de.setTag(m.getParameterMap().get("tag"));
			}
			LogFactory.getLogger().trace(de.toString());
//			System.out.println(de.toString());
		}

		if (elist != null && elist.size() > 0) {
			iDataManager.addEvents(elist);
			DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), m.getRemoteHost(), ident).inc(elist.size());
			return;
		}

		DMetrics.eventlogger_events_ignored_total.labels(m.getRemoteHost(), m.getMethod()).inc();
		LogFactory.getLogger().warn("HttpWebhook: doPost: message is empty; event will be ignored");
	}

}
