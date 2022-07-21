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


import org.primefaces.event.UnselectEvent;
import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.webhooks.WebhookMessage;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class BackendBean {

    /* FOOTER */

    public String getVersion() {
        return DProps.VERSION;
    }
    public boolean isContainerized() { return DProps.IS_CONTAINERIZED; }
    public String getLocalIpAddress() {
        return DProps.LOCAL_IP;
    }
    public String getRuntimeId() {
        return DProps.RUNTIME_ID;
    }

    public List<WebhookMessage> getWebhookMessages() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<WebhookMessage> list = iDataManager.getWebhookMessages();
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return list;
    }

    public List<DEvent> getEvents() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = iDataManager.getEvents(null);
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return list;
    }

    public String getConcatenatedEvents() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = iDataManager.getEvents(null);
        DataManagerFactory.getInstance().returnClient(iDataManager);
        if (list == null) return "no data";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getHost()).append(" - ");
            sb.append(list.get(i).getIdent()).append("[").append(list.get(i).getPid()).append("] - ");
            sb.append(list.get(i).getMessage()).append("\n");
        }
        return sb.toString();
    }

    private String[] selectedHosts;
    private List<String> availableHosts;

    @PostConstruct
    public void init() {
        LogFactory.getLogger().info("PostConstruct init");
//        availableHosts = new ArrayList<>();
//        availableHosts.add("Miami");
//        availableHosts.add("London");
//        availableHosts.add("Paris");
//        availableHosts.add("Istanbul");
//        availableHosts.add("Berlin");
//        availableHosts.add("Barcelona");
//        availableHosts.add("Rome");
//        availableHosts.add("Brasilia");
//        availableHosts.add("Amsterdam");

    }

    public String[] getSelectedHosts() {
        LogFactory.getLogger().info("getSelectedHosts: " + selectedHosts);
        return selectedHosts;
    }

    public void setSelectedHosts(String[] selectedHosts) {
        this.selectedHosts = selectedHosts;
    }

    public List<String> getAvailableHosts() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        availableHosts = iDataManager.getAvailableHosts();
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return availableHosts;
    }

    public void setAvailableHosts(List<String> availableHosts) {
        this.availableHosts = availableHosts;
    }

    public void selectedHostsChangeEvent(ValueChangeEvent event) {
        LogFactory.getLogger().info("selectedHostsChangeEvent: " + event.getNewValue().toString());
    }

}
