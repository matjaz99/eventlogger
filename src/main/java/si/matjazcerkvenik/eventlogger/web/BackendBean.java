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


import org.primefaces.event.SelectEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;
import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DFilter;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.Formatter;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.util.*;

@ManagedBean
@SessionScoped
//@RequestScoped
@SuppressWarnings("unused")
public class BackendBean {

    @PostConstruct
    public void init() {
        LogFactory.getLogger().info("BackendBean: PostConstruct init");
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        String parameterOne = params.get("filter");
        LogFactory.getLogger().info("BackendBean: PostConstruct filter=" + parameterOne);

//        confTimeRange("last1h");
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
     * @return whole text, new-line delimited
     */
    public String getConcatenatedEvents() {

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        List<DEvent> list = null;

        try {

            list = iDataManager.getEvents(composeDFilter());

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

    public DFilter composeDFilter() {

        DFilter filter = new DFilter();
        filter.setHosts(selectedHosts);
        filter.setIdents(selectedIdents);
        filter.setSearchPatternType(selectedSearchOption);
        filter.setSearchPattern(selectedSearchPattern);
        if (selectedPredefinedTimeRange != null
                && selectedPredefinedTimeRange.startsWith("last")) {
            Date startDateFromRange = new Date();
            Date endDateFromRange = new Date();
            long now = System.currentTimeMillis();
            if (selectedPredefinedTimeRange.equals("last1h")) {
                startDateFromRange.setTime(now - 3600 * 1000);
                endDateFromRange.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last4h")) {
                startDateFromRange.setTime(now - 4 * 3600 * 1000);
                endDateFromRange.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last24h")) {
                startDateFromRange.setTime(now - 24 * 3600 * 1000);
                endDateFromRange.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last7d")) {
                startDateFromRange.setTime(now - 7 * 24 * 3600 * 1000);
                endDateFromRange.setTime(now);
            } else if (selectedPredefinedTimeRange.equals("last30d")) {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) - 30);
                c1.set(Calendar.HOUR_OF_DAY, 0);
                c1.set(Calendar.MINUTE, 0);
                c1.set(Calendar.SECOND, 0);
                startDateFromRange.setTime(c1.getTimeInMillis());
                endDateFromRange.setTime(now);
            }
            filter.setFromTimestamp(startDateFromRange.getTime());
            filter.setToTimestamp(endDateFromRange.getTime());
        } else {
            filter.setFromTimestamp(startDate.getTime());
            filter.setToTimestamp(endDate.getTime());
        }

        filter.setLimit(limit);
        if (selectedSortType.equalsIgnoreCase("ascending")) {
            filter.setAscending(true);
        }

        LogFactory.getLogger().info("BackendBean: composeFilter: " + filter.toString());

        return filter;
    }

    public void resetFilter() {
        selectedIdents = null;
        selectedHosts = null;
        selectedSearchPattern = null;
        selectedSearchOption = "REGEX";
        selectedPredefinedTimeRange = "last1h";
        startDate = null;
        endDate = null;
        selectedSortType = "descending";
        limit = 1000;
    }

    public void applyFilterAction() {
        // nothing to do here, filter is actually applied in getConcatenatedEvents method
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

    public String selectedPredefinedTimeRange = "last1h";

    public String getSelectedPredefinedTimeRange() {
        return selectedPredefinedTimeRange;
    }

    public void setSelectedPredefinedTimeRange(String selectedPredefinedTimeRange) {
        this.selectedPredefinedTimeRange = selectedPredefinedTimeRange;
    }

    /**
     * Reset startDate and endDate if predefined time range is selected from drop-down menu
     */
    public void handlePredefinedTimeRangeChange() {
        startDate = null;
        endDate = null;
    }

    /**
     * Set predefined time range to 'custom' if startDate is changed
     */
    public void handleStartDateChange(SelectEvent<Date> event) {
        selectedPredefinedTimeRange = "custom";
    }

    /**
     * Set predefined time range to 'custom' if endDate is changed
     */
    public void handleEndDateChange(SelectEvent<Date> event) {
        selectedPredefinedTimeRange = "custom";
    }





    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*            the rest of the filter                 */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */



    private int limit = 1000;
    private String selectedSortType = "descending";

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSelectedSortType() {
        return selectedSortType;
    }

    public void setSelectedSortType(String selectedSortType) {
        this.selectedSortType = selectedSortType;
    }







    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*                 horizontal bar                    */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */



    private HorizontalBarChartModel hbarModel;

    public HorizontalBarChartModel getHbarModel() {
        createHorizontalBarModel();
        return hbarModel;
    }

    public void setHbarModel(HorizontalBarChartModel hbarModel) {
        this.hbarModel = hbarModel;
    }

    public void createHorizontalBarModel() {

        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) return;

        IDataManager idm = DataManagerFactory.getInstance().getClient();
        Map<String, Integer> map = idm.getTopEventsByHosts();

        hbarModel = new HorizontalBarChartModel();
        ChartData data = new ChartData();

        HorizontalBarChartDataSet hbarDataSet = new HorizontalBarChartDataSet();
        hbarDataSet.setLabel("Number of events");

        List<String> bgColor = new ArrayList<>();
        bgColor.add("rgba(255, 99, 132, 0.4)");
        bgColor.add("rgba(255, 159, 64, 0.4)");
        bgColor.add("rgba(255, 205, 86, 0.4)");
        bgColor.add("rgba(75, 192, 192, 0.4)");
        bgColor.add("rgba(54, 162, 235, 0.4)");
        bgColor.add("rgba(153, 102, 255, 0.4)");
        bgColor.add("rgba(101, 202, 255, 0.4)");
        hbarDataSet.setBackgroundColor(bgColor);

        List<String> borderColor = new ArrayList<>();
        borderColor.add("rgb(255, 99, 132)");
        borderColor.add("rgb(255, 159, 64)");
        borderColor.add("rgb(255, 205, 86)");
        borderColor.add("rgb(75, 192, 192)");
        borderColor.add("rgb(54, 162, 235)");
        borderColor.add("rgb(153, 102, 255)");
        borderColor.add("rgb(201, 203, 207)");
        hbarDataSet.setBorderColor(borderColor);
        hbarDataSet.setBorderWidth(1);

        data.addChartDataSet(hbarDataSet);

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (String s : map.keySet()) {
            values.add(map.getOrDefault(s, 0));
            labels.add(s);
        }
        hbarDataSet.setData(values);
        data.setLabels(labels);
        hbarModel.setData(data);

        //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addXAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Events by host in last 4 hours");
        options.setTitle(title);

        hbarModel.setOptions(options);
    }


}
