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
package si.matjazcerkvenik.eventlogger.util;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

import java.util.HashMap;
import java.util.Map;

public class DMetrics {

    public static CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static Map<String, Counter> customCounterMetrics = new HashMap<>();

    public static final Gauge eventlogger_build_info = Gauge.build()
            .name("eventlogger_build_info")
            .help("Application meta data, value contains start time")
            .labelNames("app", "runtimeId", "version", "os")
            .register();

    public static final Counter eventlogger_http_requests_total = Counter.build()
            .name("eventlogger_http_requests_total")
            .help("Total number of received http requests.")
            .labelNames("remotehost", "method", "webhook")
            .register();

    public static final Counter eventlogger_http_requests_size_total = Counter.build()
            .name("eventlogger_http_requests_size_total")
            .help("Total size of received http requests; size in header")
            .labelNames("remotehost", "method", "webhook")
            .register();

    public static final Counter eventlogger_events_total = Counter.build()
            .name("eventlogger_events_total")
            .help("Total received events.")
            .labelNames("source", "host", "ident")
            .register();

    public static final Counter eventlogger_events_ignored_total = Counter.build()
            .name("eventlogger_events_ignored_total")
            .help("You are losing some events")
            .labelNames("source", "method")
            .register();

    public static final Histogram eventlogger_db_duration_seconds = Histogram.build()
            .buckets(0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2.0, 3.0, 5.0)
            .name("eventlogger_db_duration_seconds")
            .labelNames("database", "table", "action")
            .help("DB response time")
            .register();

    public static final Counter eventlogger_db_errors_total = Counter.build()
            .name("eventlogger_db_errors_total")
            .help("Total number of errors (exceptions) of DB operations.")
            .labelNames("database", "table", "action")
            .register();

    public static final Histogram eventlogger_rule_evaluation_seconds = Histogram.build()
            .buckets(0.01, 0.02, 0.03, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0)
            .name("eventlogger_rule_evaluation_seconds")
            .help("Histogram of rule evaluation duration")
            .register();

}
