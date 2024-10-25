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
            .labelNames("remotehost", "webhook", "reason")
            .register();

    public static final Counter eventlogger_requests_ignored_total = Counter.build()
            .name("eventlogger_requests_ignored_total")
            .help("You are losing some events")
            .labelNames("remotehost", "webhook", "reason")
            .register();

    public static final Gauge eventlogger_requests_queue_size = Gauge.build()
            .name("eventlogger_requests_queue_size")
            .help("Requests waiting to be processed")
            .register();

    public static final Gauge eventlogger_request_processing_workers = Gauge.build()
            .name("eventlogger_request_processing_workers")
            .help("Number of worker threads for processing events")
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

    public static final Gauge eventlogger_db_buffer_size = Gauge.build()
            .name("eventlogger_db_buffer_size")
            .help("Data waiting to be inserted into DB")
            .register();

    public static final Gauge eventlogger_db_bulk_insert_size = Gauge.build()
            .name("eventlogger_db_bulk_insert_size")
            .help("Bulk insert size")
            .register();

    public static final Counter eventlogger_rule_actions_total = Counter.build()
            .name("eventlogger_rule_actions_total")
            .help("Total number of rule actions done.")
            .labelNames("action")
            .register();

    public static final Histogram eventlogger_rule_evaluation_seconds = Histogram.build()
            .buckets(0.01, 0.02, 0.03, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0)
            .name("eventlogger_rule_evaluation_seconds")
            .help("Histogram of rule evaluation duration")
            .labelNames("endpoint")
            .register();

    public static final Gauge eventlogger_memory_total_bytes = Gauge.build()
            .name("eventlogger_memory_total_bytes")
            .help("Total memory in bytes")
            .register();

    public static final Gauge eventlogger_memory_free_bytes = Gauge.build()
            .name("eventlogger_memory_free_bytes")
            .help("Free memory in bytes")
            .register();

    public static final Gauge eventlogger_memory_max_bytes = Gauge.build()
            .name("eventlogger_memory_max_bytes")
            .help("Max memory in bytes")
            .register();

    public static final Gauge eventlogger_available_processors = Gauge.build()
            .name("eventlogger_available_processors")
            .help("Number of available processors")
            .register();

}
