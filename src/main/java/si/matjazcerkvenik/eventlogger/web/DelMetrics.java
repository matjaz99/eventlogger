package si.matjazcerkvenik.eventlogger.web;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class DelMetrics {

    public static CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static final Gauge eventlogger_build_info = Gauge.build()
            .name("eventlogger_build_info")
            .help("Application meta data, value contains start time")
            .labelNames("app", "runtimeId", "version", "os")
            .register();

    public static final Counter eventlogger_webhook_messages_received_total = Counter.build()
            .name("eventlogger_webhook_messages_received_total")
            .help("Total number of received webhook messages.")
            .labelNames("remotehost", "method", "webhook")
            .register();

    public static final Counter eventlogger_webhook_messages_size_total = Counter.build()
            .name("eventlogger_webhook_messages_size_total")
            .help("Total size of received webhook messages.")
            .labelNames("remotehost", "method", "webhook")
            .register();

}
