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
package si.matjazcerkvenik.eventlogger.webhooks;

import io.prometheus.client.exporter.common.TextFormat;
import si.matjazcerkvenik.eventlogger.db.EventQueue;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WebServlet(
        name = "PrometheusMetricsServlet",
        description = "Serving Prometheus metrics",
        urlPatterns = "/metrics"
)
public class PrometheusMetricsServlet extends HttpServlet {

    private static final long serialVersionUID = -5786248430627284392L;


    public PrometheusMetricsServlet() {
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        Runtime instance = Runtime.getRuntime();
        DMetrics.eventlogger_memory_total_bytes.set(instance.totalMemory());
        DMetrics.eventlogger_memory_free_bytes.set(instance.freeMemory());
        DMetrics.eventlogger_memory_max_bytes.set(instance.maxMemory());
        // used memory = total - free
        DMetrics.eventlogger_available_processors.set(instance.availableProcessors());

        DMetrics.eventlogger_db_buffer_size.set(EventQueue.getInstance().getQueueSize());
        DMetrics.eventlogger_db_bulk_insert_size.set(DProps.EVENTLOGGER_MONGODB_BULK_INSERT_MAX_SIZE);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(TextFormat.CONTENT_TYPE_004);

        Writer writer = resp.getWriter();
        try {
            TextFormat.write004(writer, DMetrics.registry.filteredMetricFamilySamples(parse(req)));
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private Set<String> parse(HttpServletRequest req) {
        String[] includedParam = req.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<String>(Arrays.asList(includedParam));
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }
}
