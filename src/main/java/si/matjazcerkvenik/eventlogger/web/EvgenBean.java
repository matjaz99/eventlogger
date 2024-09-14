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
package si.matjazcerkvenik.eventlogger.web;

import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.simplelogger.LEVEL;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
@SuppressWarnings("unused")
public class EvgenBean {

    private SimpleLogger evgenLog;
    private int numberOfNewLines = 10;

    private int dummyCount;

    @PostConstruct
    public void init() {
        evgenLog = LogFactory.getEvgenLog();
        LogFactory.getLogger().info("init evgenLog: " + evgenLog.getFilename());
    }


    public int getNumberOfNewLines() {
        return numberOfNewLines;
    }

    public void setNumberOfNewLines(int numberOfNewLines) {
        this.numberOfNewLines = numberOfNewLines;
    }

    public void applyDumpNewLinesAction() {
        LogFactory.getLogger().info("dumping " + numberOfNewLines + " lines");
        int i = 0;
        while (i < numberOfNewLines) {
            evgenLog.write(array[(int) (Math.random() * array.length)]);
            i++;
        }
    }

    public void applyLogAlarmAction1() {
        evgenLog.info("Special event to test raising alarm in eventlogger: Foobar does not work. Number " + dummyCount++);
    }

    public void applyLogAlarmAction2() {
        evgenLog.info("Special event to test raising alarm in eventlogger: Foobar has crashed. Number " + dummyCount++);
    }

    public void applyLogClearAction1() {
        evgenLog.info("Special event to test alarm clear in eventlogger: Foobar is working now. Number " + dummyCount++);
    }

    public void applyLogClearAction2() {
        evgenLog.info("Special event to test alarm clear in eventlogger: Foobar recovered from crash. Number " + dummyCount++);
    }

    public void applyLogEventAction() {
        evgenLog.info("Special event to test sending event in eventlogger. This is a foobar event. Number " + dummyCount++);
    }

    public String getEvgenLogFile() {
        return evgenLog.getFilename();
    }

    public void setEvgenLogFile(String evgenLogFile) {
        this.evgenLog.setFilename(evgenLogFile);
    }

    private String[] array = {
            "run-docker-runtime\\x2drunc-moby-28bec642baa6ff7cfa36ec149443a8c0c1bfbcc3b73971eb5752b324c175353b-runc.GIQl65.mount: Deactivated successfully.",
            "var-lib-docker-overlay2-36d969c2134bf4ede3ab961828b8b02b0c51a549e0f65a244a83d69b6f9c6c18\\x2dinit-merged.mount: Succeeded.",
            "starting signal loop\" namespace=moby path=/run/containerd/io.containerd.runtime.v2.task/moby/0b9992f1117100c7336c17212091ae6346ae46fa33f90b182311e2b89bf65324 pid=376328 runtime=io.containerd.runc.v2",
            "loading plugin \\\"io.containerd.ttrpc.v1.task\\\"...\" runtime=io.containerd.runc.v2 type=io.containerd.ttrpc.v1",
            "docker_gwbridge: port 6(vethe1fbcef) entered blocking state",
            "<info>  [1714334941.6620] device (vethdc43dba): carrier: link connected",
            "(root) CMDOUT (6426704 /data/prometheus/)",
            "(root) CMDOUT (0 /data/opensearch/)",
            "(root) CMD ([ -x /etc/init.d/anacron ] && if [ ! -d /run/systemd/system ]; then /usr/sbin/invoke-rc.d anacron start >/dev/null; fi)",
            "error: kex_exchange_identification: Connection closed by remote host",
            "pam_unix(sshd:session): session opened for user root by (uid=0)",
            "New session 157 of user root.",
            "Started Session 157 of user root.",
            "Accepted password for root from 192.168.1.100 port 54393 ssh2",
            "pam_unix(cron:session): session closed for user root",
            "msgrcv: type 2, code 6200010, obj_ident #1.1/-1.0/end/\\|219\\|STOPPED\\|dhcpd#",
            "msgrcv: type 2, code 6200010, obj_ident #1.1/-1.0/end/\\|219\\|STOPPED\\|nginx#",
            "msgrcv: type 2, code 6200010, obj_ident #1.1/-1.0/end/\\|219\\|STOPPED\\|vsftpd#",
            "[origin software=\"rsyslogd\" swVersion=\"8.2102.0-15.el8\" x-pid=\"949\" x-info=\"https://www.rsyslog.com\"] rsyslogd was HUPed",
            "ERROR SYNCTASK[ubuntu-vm]: failed to synchronize alarms; root cause: Unknown Host",
            "ERROR PrometheusHttpClient[elasticvm]: req[903] << UnknownHostException: elasticvm: Name does not resolve",
            "INFO  PrometheusHttpClient[promvm]: req[894] >> GET https://promvm/prometheus/api/v1/alerts",
            "ubuntu-vm dockerd[1176]: time=\"2024-04-28T22:03:38.274879219+02:00\" level=error msg=\"[resolver] failed to query DNS server: 8.8.8.8:53, query: ;pushgateway.\\tIN\\t AAAA\" error=\"read udp 172.18.0.10:58191->8.8.8.8:53: i/o timeout\"",
            "Selected source 46.54.225.12 (2.cloudlinux.pool.ntp.org)"
    };

}
