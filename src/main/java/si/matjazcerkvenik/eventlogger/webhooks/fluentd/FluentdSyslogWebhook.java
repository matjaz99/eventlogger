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
package si.matjazcerkvenik.eventlogger.webhooks.fluentd;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.prometheus.client.Counter;
import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.eventlogger.model.DAlarmSeverity;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.config.DRule;
import si.matjazcerkvenik.eventlogger.util.AlarmMananger;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.webhooks.RequestProcessor;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class FluentdSyslogWebhook extends HttpServlet {

	private static final long serialVersionUID = 4275913222328716391L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not allowed");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws IOException {

		DRequest m = RequestProcessor.processIncomingRequest(req, DProps.requestsReceivedCount++);

		IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
		iDataManager.addHttpRequest(m);
		DMetrics.eventlogger_http_requests_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc();
		DMetrics.eventlogger_http_requests_size_total.labels(m.getRemoteHost(), m.getMethod(), "/*").inc(m.getContentLength());

		// process body
		List<DEvent> eventList = new ArrayList<>();

		if (req.getContentType().equalsIgnoreCase("application/x-ndjson")) {
			// this is a ndjson (objects separated by \n): {}{}{}
			String body = m.getBody().replace("}{", "}\n{");
			String[] msgArray = body.split("\n");

			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			long now = System.currentTimeMillis();

			for (int i = 0; i < msgArray.length; i++) {
				DEvent e = gson.fromJson(msgArray[i].trim(), DEvent.class);
				e.setId(DProps.eventsReceivedCount++);
				e.setRuntimeId(DProps.RUNTIME_ID);
				e.setTimestamp(now);
				e.setEventSource("fluentd.syslog");
				eventList.add(e);
				if (e.getHost() == null) e.setHost(m.getRemoteHost());
				if (e.getIdent() == null) e.setIdent("unknown");
				LogFactory.getLogger().trace(e.toString());
				DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), e.getHost(), e.getIdent()).inc();

				evaluateRules(e);
			}
		}
		if (req.getContentType().equalsIgnoreCase("application/json")) {
			// this is a json (array of objects): [{},{},{}]
		}



		iDataManager.addEvents(eventList);
		DataManagerFactory.getInstance().returnClient(iDataManager);

	}

	private void evaluateRules(DEvent event) {

		long before = System.nanoTime();

		// TODO check rules

		for (DRule rule : DProps.yamlConfig.getRules()) {

			if (!rule.getFilter().get("ident").equalsIgnoreCase(event.getIdent())) {
				continue;
			}

			if (!event.getMessage().contains(rule.getPattern().get("expr"))) {
				continue;
			}

			if (rule.getAction().get("type").equalsIgnoreCase("alarm")) {

				DAlarm a = new DAlarm(event.getHost(), rule.getName(),
						DAlarmSeverity.MAJOR, event.getIdent(), "addInfo");
				AlarmMananger.raiseAlarm(a);

			}

			if (rule.getAction().get("type").equalsIgnoreCase("count")) {

				Counter counter;
				if (!DMetrics.customCounterMetrics.containsKey(rule.getAction().get("metricName"))) {
					counter = DMetrics.customCounterMetrics.get(rule.getAction().get("metricName"));
				} else {
					counter = Counter.build()
							.name(rule.getAction().get("metricName"))
							.help(rule.getName())
							.labelNames("host", "ident")
							.register();
				}

				counter.labels(event.getHost(), event.getIdent()).inc();

			}

		}

		double duration = (System.nanoTime() - before) * 1.0 / 1000000000;
		DMetrics.eventlogger_rule_evaluation_seconds.observe(duration);

	}

}
