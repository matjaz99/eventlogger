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


import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DFilter;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.model.DRequest;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.List;
import java.util.Map;

@ManagedBean
@SessionScoped
//@RequestScoped
public class BackendBean {

    @PostConstruct
    public void init() {
        LogFactory.getLogger().info("BackendBean: PostConstruct init");
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        String parameterOne = params.get("filter");
        LogFactory.getLogger().info("BackendBean: PostConstruct filter=" + parameterOne);
    }

    public List<DRequest> getRequests() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DRequest> list = iDataManager.getHttpRequests();
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return list;
    }

    public List<DEvent> getEvents() {

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = null;

        try {
            if (selectedHosts == null || selectedHosts.length == 0) {
                // no filter
                LogFactory.getLogger().info("BackendBean: getEvents: no filter");
                list = iDataManager.getEvents(null);
            } else {
                DFilter filter = new DFilter();
                filter.setHosts(selectedHosts);
                LogFactory.getLogger().info("BackendBean: getEvents: apply filter " + filter.toString());
                list = iDataManager.getEvents(filter);
            }
        } finally {
            DataManagerFactory.getInstance().returnClient(iDataManager);
        }
        return list;
    }

    public String getConcatenatedEvents() {

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = null;

        try {

//            if ((selectedHosts == null || selectedHosts.length == 0)
//                    && (selectedIdents == null || selectedIdents.length == 0)) {
//                // no filter
//                LogFactory.getLogger().info("BackendBean: getConcatenatedEvents: no filter");
//                list = iDataManager.getEvents(null);
//            } else {
//                DFilter filter = new DFilter();
//                filter.setHosts(selectedHosts);
//                filter.setIdents(selectedIdents);
//                LogFactory.getLogger().info("BackendBean: getConcatenatedEvents: apply filter " + filter.toString());
//                list = iDataManager.getEvents(filter);
//            }

            list = iDataManager.getEvents(composeFilter());

            if (list == null) return "no data";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).getHost()).append(" - ");
                sb.append(list.get(i).getIdent()).append("[").append(list.get(i).getPid()).append("] - ");
                sb.append(list.get(i).getMessage()).append("\n");
            }
            return sb.toString();

        } finally {
            DataManagerFactory.getInstance().returnClient(iDataManager);
        }

    }

    public DFilter composeFilter() {
        DFilter filter = new DFilter();
        boolean filterIsAltered = false;

        if (selectedHosts != null && selectedHosts.length > 0) {
            filter.setHosts(selectedHosts);
            filterIsAltered = true;
        }
        if (selectedIdents != null && selectedIdents.length > 0) {
            filter.setIdents(selectedIdents);
            filterIsAltered = true;
        }
        if (selectedSearchOption != null && selectedSearchOption.length() > 0) {
            filter.setSearchType(selectedSearchOption);
        }
        if (selectedSearchPattern != null && selectedSearchPattern.length() > 0) {
            filter.setSearchPattern(selectedSearchPattern);
            filterIsAltered = true;
        }
        filter.setLimit(limit);

        LogFactory.getLogger().info("BackendBean: filter is altered: " + filterIsAltered);

        // if nothing is set, return null
        if (!filterIsAltered) return null;

        LogFactory.getLogger().info("BackendBean: composeFilter: " + filter.toString());

        return filter;
    }

    public void doFinishCreateFilter(ActionEvent event) {
        LogFactory.getLogger().info(">>> BackendBean: doFinishCreateFilter: " + selectedSearchPattern);
    }

    public void createFilterAction() {
        LogFactory.getLogger().info(">>> BackendBean: createFilterAction: " + selectedSearchPattern);
    }





    /*****************************************************/
    /**                                                 **/
    /**            host and ident filter                **/
    /**                                                 **/
    /*****************************************************/


    private String[] selectedHosts;
    private List<String> availableHosts;
    private String[] selectedIdents;
    private List<String> availableIdents;

    public String[] getSelectedHosts() {
        if (selectedHosts != null && selectedHosts.length > 0) {
            for (int i = 0; i < selectedHosts.length; i++) {
                LogFactory.getLogger().info("getSelectedHosts[" + i + "]: " + selectedHosts[i]);
            }
        }
        return selectedHosts;
    }

    public void setSelectedHosts(String[] selectedHosts) {
        this.selectedHosts = selectedHosts;
    }

    public List<String> getAvailableHosts() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        availableHosts = iDataManager.getDistinctKeys("host");
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return availableHosts;
    }

    public void setAvailableHosts(List<String> availableHosts) {
        this.availableHosts = availableHosts;
    }

    public String[] getSelectedIdents() {
        if (selectedIdents != null && selectedIdents.length > 0) {
            for (int i = 0; i < selectedIdents.length; i++) {
                LogFactory.getLogger().info("getSelectedIdents[" + i + "]: " + selectedIdents[i]);
            }
        }
        return selectedIdents;
    }

    public void setSelectedIdents(String[] selectedIdents) {
        this.selectedIdents = selectedIdents;
    }

    public List<String> getAvailableIdents() {
        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        availableIdents = iDataManager.getDistinctKeys("ident");
        DataManagerFactory.getInstance().returnClient(iDataManager);
        return availableIdents;
    }

    public void setAvailableIdents(List<String> availableIdents) {
        this.availableIdents = availableIdents;
    }

    public void selectedHostsChangeEvent(ValueChangeEvent event) {
        LogFactory.getLogger().debug("selectedHostsChangeEvent: " + event.getNewValue().toString());
    }

    public void selectedIdentsChangeEvent(ValueChangeEvent event) {
        LogFactory.getLogger().debug("selectedIdentsChangeEvent: " + event.getNewValue().toString());
    }



    /*****************************************************/
    /**                                                 **/
    /**                  regex filter                   **/
    /**                                                 **/
    /*****************************************************/

    private String selectedSearchOption;
    private String selectedSearchPattern = "^[test]*$";

    public String getSelectedSearchOption() {
        return selectedSearchOption;
    }

    public void setSelectedSearchOption(String selectedSearchOption) {
        this.selectedSearchOption = selectedSearchOption;
    }

    public String getSelectedSearchPattern() {
        return selectedSearchPattern;
    }

    public void setSelectedSearchPattern(String selectedSearchPattern) {
        this.selectedSearchPattern = selectedSearchPattern;
    }



    /*****************************************************/
    /**                                                 **/
    /**                  time filter                    **/
    /**                                                 **/
    /*****************************************************/






    /*****************************************************/
    /**                                                 **/
    /**           the rest of the filter                **/
    /**                                                 **/
    /*****************************************************/



    private int limit = 1000;
    private boolean sortAscending;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
}
