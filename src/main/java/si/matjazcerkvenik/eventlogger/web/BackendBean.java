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
import si.matjazcerkvenik.eventlogger.webhooks.WebhookMessage;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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

    public List<DEvent> getEventMessages() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = iDataManager.getEventMessages();
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return list;
    }

    public String getConcatenatedEvents() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = iDataManager.getEventMessages();
        DataManagerFactory.getInstance().returnClient(iDataManager);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getHost()).append(" - ");
            sb.append(list.get(i).getIdent()).append("[").append(list.get(i).getPid()).append("] - ");
            sb.append(list.get(i).getMessage()).append("\n");
        }
        return sb.toString();
    }

    private String[] selectedCities2;
    private List<String> cities;

    @PostConstruct
    public void init() {
        cities = new ArrayList<>();
        cities.add("Miami");
        cities.add("London");
        cities.add("Paris");
        cities.add("Istanbul");
        cities.add("Berlin");
        cities.add("Barcelona");
        cities.add("Rome");
        cities.add("Brasilia");
        cities.add("Amsterdam");
    }

    public String[] getSelectedCities2() {
        return selectedCities2;
    }

    public void setSelectedCities2(String[] selectedCities2) {
        this.selectedCities2 = selectedCities2;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public void onItemUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage();
        msg.setSummary("Item unselected: " + event.getObject().toString());
        msg.setSeverity(FacesMessage.SEVERITY_INFO);

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
