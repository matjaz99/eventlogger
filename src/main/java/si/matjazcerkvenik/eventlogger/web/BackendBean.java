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
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.Formatter;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.util.Calendar;
import java.util.Date;
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

    /**
     * This method actually prepares the whole output for the GUI.
     * @return
     */
    public String getConcatenatedEvents() {

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = null;

        try {

            list = iDataManager.getEvents(composeFilter());

            if (list == null) return "no data";

            long now = System.currentTimeMillis();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                long millisSince = now - list.get(i).getTimestamp();
                if (millisSince < 4 * 3600 * 1000) {
                    sb.append(Formatter.convertToDHMSFormat((int) millisSince / 1000)).append(" ago - ");
                } else {
                    sb.append(Formatter.getFormatedTimestamp(list.get(i).getTimestamp())).append(" - ");
                }
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

        LogFactory.getLogger().info("BackendBean: composeFilter: selectedHosts=" + selectedHosts);
        LogFactory.getLogger().info("BackendBean: composeFilter: selectedIdents=" + selectedIdents);
        LogFactory.getLogger().info("BackendBean: composeFilter: selectedSearchOption=" + selectedSearchOption);
        LogFactory.getLogger().info("BackendBean: composeFilter: selectedSearchPattern=" + selectedSearchPattern);
        LogFactory.getLogger().info("BackendBean: composeFilter: sortAscending=" + sortAscending);
        LogFactory.getLogger().info("BackendBean: composeFilter: limit=" + limit);
        LogFactory.getLogger().info("BackendBean: composeFilter: startDate=" + startDate);
        LogFactory.getLogger().info("BackendBean: composeFilter: endDate=" + endDate);

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
        if (sortAscending) {
            filter.setAscending(true);
            filterIsAltered = true;
        }
        if (startDate != null) filter.setFromTimestamp(startDate.getTime());
        if (endDate != null) filter.setToTimestamp(endDate.getTime());
        filter.setLimit(limit);

        LogFactory.getLogger().info("BackendBean: filter is altered: " + filterIsAltered);

        // if nothing is set, return null
        if (!filterIsAltered) return null;

        LogFactory.getLogger().info("BackendBean: composeFilter: " + filter.toString());

        return filter;
    }

    public void resetFilter() {
        LogFactory.getLogger().info(">>> BackendBean: resetFilter");
        selectedSearchPattern = null;
        selectedSearchOption = null;
        selectedPredefinedTimeRange = null;
        startDate = null;
        endDate = null;
        sortAscending = false;
        limit = 1000;
    }

    public void applyFilterAction() {
        LogFactory.getLogger().info(">>> BackendBean: applyFilterAction: " + selectedSearchPattern);
        LogFactory.getLogger().info(">>> BackendBean: applyFilterAction: " + selectedPredefinedTimeRange);

        if (!selectedPredefinedTimeRange.equals("no-value")) {
            if (startDate == null) startDate = new Date();
            if (endDate == null) endDate = new Date();
            long now = System.currentTimeMillis();
            if (selectedPredefinedTimeRange.equals("last1h")) {
                startDate.setTime(now - 3600 * 1000);
                endDate.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last4h")) {
                startDate.setTime(now - 4 * 3600 * 1000);
                endDate.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last24h")) {
                startDate.setTime(now - 24 * 3600 * 1000);
                endDate.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last7d")) {
                startDate.setTime(now - 7 * 24 * 3600 * 1000);
                endDate.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last30d")) {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) - 30);
                c1.set(Calendar.HOUR_OF_DAY, 0);
                c1.set(Calendar.MINUTE, 0);
                c1.set(Calendar.SECOND, 0);
                startDate.setTime(c1.getTimeInMillis());
                endDate.setTime(now);
            }
        } else {

        }

    }

    public String confTimeRange(String s) {
        startDate = new Date();
        endDate = new Date();
        long now = System.currentTimeMillis();
        if (selectedPredefinedTimeRange.equals("last1h")) {
            startDate.setTime(now - 3600 * 1000);
            endDate.setTime(now);
        } else if (selectedPredefinedTimeRange.equals("last4h")) {
            startDate.setTime(now - 4 * 3600 * 1000);
            endDate.setTime(now);
        } else if (selectedPredefinedTimeRange.equals("last24h")) {
            startDate.setTime(now - 24 * 3600 * 1000);
            endDate.setTime(now);
        } else if (selectedPredefinedTimeRange.equals("last7d")) {
            startDate.setTime(now - 7 * 24 * 3600 * 1000);
            endDate.setTime(now);
        } else if (selectedPredefinedTimeRange.equals("last30d")) {
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) - 30);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            startDate.setTime(c1.getTimeInMillis());
            endDate.setTime(now);
        }
        LogFactory.getLogger().info(">>> BackendBean: confTimeRange: " + s + " ---> startDate: " + startDate + " endDate: " + endDate);
        return "";
    }







    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*             host and ident filter                 */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */


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



    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*                   regex filter                    */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */

    private String selectedSearchOption;
    private String selectedSearchPattern;

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



    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*                   time filter                     */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */


    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String selectedPredefinedTimeRange;

    public String getSelectedPredefinedTimeRange() {
        return selectedPredefinedTimeRange;
    }

    public void setSelectedPredefinedTimeRange(String selectedPredefinedTimeRange) {
        this.selectedPredefinedTimeRange = selectedPredefinedTimeRange;
        confTimeRange(selectedPredefinedTimeRange);
    }





    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*            the rest of the filter                 */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */



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
