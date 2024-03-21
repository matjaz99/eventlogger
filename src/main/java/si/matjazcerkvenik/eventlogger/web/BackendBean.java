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


import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.polar.PolarAreaChartDataSet;
import org.primefaces.model.charts.polar.PolarAreaChartModel;
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
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ManagedBean
@SessionScoped
@SuppressWarnings("unused")
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

                String displayPattern = selectedDisplayPattern;

                if (displayPattern.contains("%d")) {
                    displayPattern = displayPattern.replace("%d", Formatter.getFormatedTimestamp(list.get(i).getTimestamp()));
                }

                if (displayPattern.contains("%D")) {
                    long millisSince = now - list.get(i).getTimestamp();
                    if (millisSince < 4 * 3600 * 1000) {
                        displayPattern = displayPattern.replace("%D", Formatter.convertToDHMSFormat((int) millisSince / 1000) + " ago");
                    } else {
                        displayPattern = displayPattern.replace("%D", Formatter.getFormatedTimestamp(list.get(i).getTimestamp()));
                    }
                }

                if (displayPattern.contains("%h")) {
                    displayPattern = displayPattern.replace("%h", list.get(i).getHost());
                }

                if (displayPattern.contains("%r")) {
                    displayPattern = displayPattern.replace("%r", list.get(i).getRemoteAddress());
                }

                if (displayPattern.contains("%i") && !Formatter.isNullOrEmpty(list.get(i).getIdent())) {
                    displayPattern = displayPattern.replace("%i", list.get(i).getIdent());
                }

                if (displayPattern.contains("%p") && !Formatter.isNullOrEmpty(list.get(i).getPid())) {
                    displayPattern = displayPattern.replace("%p", list.get(i).getPid());
                }

                if (displayPattern.contains("%t") && !Formatter.isNullOrEmpty(list.get(i).getTag())) {
                    displayPattern = displayPattern.replace("%t", list.get(i).getTag());
                }

                if (displayPattern.contains("%f") && !Formatter.isNullOrEmpty(list.get(i).getLogfile())) {
                    displayPattern = displayPattern.replace("%f", list.get(i).getLogfile());
                }

                if (displayPattern.contains("%w") && !Formatter.isNullOrEmpty(list.get(i).getEndpoint())) {
                    displayPattern = displayPattern.replace("%w", list.get(i).getEndpoint());
                }

                if (displayPattern.contains("%m")) {
                    displayPattern = displayPattern.replace("%m", list.get(i).getMessage());
                }

                sb.append(displayPattern).append("\n");

//                long millisSince = now - list.get(i).getTimestamp();
//                if (millisSince < 4 * 3600 * 1000) {
//                    sb.append(Formatter.convertToDHMSFormat((int) millisSince / 1000)).append(" ago - ");
//                } else {
//                    sb.append(Formatter.getFormatedTimestamp(list.get(i).getTimestamp())).append(" - ");
//                }
//                sb.append(list.get(i).getHost()).append(" - ");
//                sb.append(list.get(i).getRemoteAddress()).append(" - ");
//                sb.append(list.get(i).getIdent()).append("[").append(list.get(i).getPid()).append("] - ");
//                sb.append(list.get(i).getMessage()).append("\n");
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

        if (selectedSearchOption != null) {
            filter.setSearchPatternType(selectedSearchOption);
            if (selectedSearchOption.equalsIgnoreCase("grok")) {
                GrokCompiler grokCompiler = GrokCompiler.newInstance();
                grokCompiler.registerDefaultPatterns();
                Grok grok = grokCompiler.compile(selectedSearchPattern);
                filter.setSearchPattern(grok.getNamedRegex());
            } else {
                filter.setSearchPattern(selectedSearchPattern);
            }
        }

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


    private StreamedContent downloadFile;

    public StreamedContent getDownloadFile() {
        String s = getConcatenatedEvents();
        downloadFile = DefaultStreamedContent.builder()
                .name("log_" + System.currentTimeMillis() + ".log")
                .contentType("text/plain")
                .stream(() -> new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)))
                .build();
        return downloadFile;
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
    /*                display pattern                    */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */



    private String selectedDisplayPattern = DProps.EVENTLOGGER_GUI_DISPLAY_PATTERN;

    public String getSelectedDisplayPattern() {
        return selectedDisplayPattern;
    }

    public void setSelectedDisplayPattern(String selectedDisplayPattern) {
        if (Formatter.isNullOrEmpty(selectedDisplayPattern)) selectedDisplayPattern = DProps.EVENTLOGGER_GUI_DISPLAY_PATTERN;
        this.selectedDisplayPattern = selectedDisplayPattern;
    }








    /* * * * * * * * * * * * * * * * * * * * * * * * * * */
    /*                                                   */
    /*                 horizontal bar                    */
    /*                                                   */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * */



    private HorizontalBarChartModel eventsByHostsHbarModel;

    public HorizontalBarChartModel getEventsByHostsHbarModel() {
        createHorizontalBarModel();
        return eventsByHostsHbarModel;
    }

    public void setEventsByHostsHbarModel(HorizontalBarChartModel eventsByHostsHbarModel) {
        this.eventsByHostsHbarModel = eventsByHostsHbarModel;
    }

    public void createHorizontalBarModel() {

        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) return;

        IDataManager idm = DataManagerFactory.getInstance().getClient();
        Map<String, Integer> map = idm.getTopEventsByHosts(7);
        DataManagerFactory.getInstance().returnClient(idm);

        eventsByHostsHbarModel = new HorizontalBarChartModel();
        ChartData data = new ChartData();

        HorizontalBarChartDataSet hbarDataSet = new HorizontalBarChartDataSet();
        hbarDataSet.setLabel("Number of events");

        List<String> bgColor = new ArrayList<>();
        bgColor.add("rgba(255, 99, 132, 0.6)");
        bgColor.add("rgba(255, 159, 64, 0.6)");
        bgColor.add("rgba(255, 205, 86, 0.6)");
        bgColor.add("rgba(75, 192, 192, 0.6)");
        bgColor.add("rgba(54, 162, 235, 0.6)");
        bgColor.add("rgba(153, 102, 255, 0.6)");
        bgColor.add("rgba(101, 202, 255, 0.6)");
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

        if (map != null) {
            for (String s : map.keySet()) {
                values.add(map.getOrDefault(s, 0));
                labels.add(s);
            }
        }

        hbarDataSet.setData(values);
        data.setLabels(labels);
        eventsByHostsHbarModel.setData(data);

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

        eventsByHostsHbarModel.setOptions(options);
    }


    private PolarAreaChartModel eventsByHostsPolarAreaModel;

    public PolarAreaChartModel getEventsByHostsPolarAreaModel() {
        createPolarAreaModel();
        return eventsByHostsPolarAreaModel;
    }

    public void setEventsByHostsPolarAreaModel(PolarAreaChartModel eventsByHostsPolarAreaModel) {
        this.eventsByHostsPolarAreaModel = eventsByHostsPolarAreaModel;
    }

    private void createPolarAreaModel() {

        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) return;

        IDataManager idm = DataManagerFactory.getInstance().getClient();
        Map<String, Integer> map = idm.getTopEventsByHosts(5);
        DataManagerFactory.getInstance().returnClient(idm);

        eventsByHostsPolarAreaModel = new PolarAreaChartModel();
        ChartData data = new ChartData();

        PolarAreaChartDataSet dataSet = new PolarAreaChartDataSet();

        List<String> bgColors = new ArrayList<>();
        bgColors.add("rgb(255, 99, 132)");
        bgColors.add("rgb(75, 192, 192)");
        bgColors.add("rgb(255, 205, 86)");
        bgColors.add("rgb(201, 203, 207)");
        bgColors.add("rgb(54, 162, 235)");
        dataSet.setBackgroundColor(bgColors);

        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        if (map != null) {
            for (String s : map.keySet()) {
                values.add(map.getOrDefault(s, 0));
                labels.add(s);
            }
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        eventsByHostsPolarAreaModel.setData(data);
    }



    private HorizontalBarChartModel eventsByIdentHbarModel;

    public HorizontalBarChartModel getEventsByIdentHbarModel() {
        createHorizontalBarModel2();
        return eventsByIdentHbarModel;
    }

    public void setEventsByIdentHbarModel(HorizontalBarChartModel eventsByIdentHbarModel) {
        this.eventsByIdentHbarModel = eventsByIdentHbarModel;
    }

    public void createHorizontalBarModel2() {

        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) return;

        IDataManager idm = DataManagerFactory.getInstance().getClient();
        Map<String, Integer> map = idm.getTopEventsByIdent(7);
        DataManagerFactory.getInstance().returnClient(idm);

        eventsByIdentHbarModel = new HorizontalBarChartModel();
        ChartData data = new ChartData();

        HorizontalBarChartDataSet hbarDataSet = new HorizontalBarChartDataSet();
        hbarDataSet.setLabel("Number of events");

        List<String> bgColor = new ArrayList<>();
        bgColor.add("rgba(255, 99, 132, 0.6)");
        bgColor.add("rgba(255, 159, 64, 0.6)");
        bgColor.add("rgba(255, 205, 86, 0.6)");
        bgColor.add("rgba(75, 192, 192, 0.6)");
        bgColor.add("rgba(54, 162, 235, 0.6)");
        bgColor.add("rgba(153, 102, 255, 0.6)");
        bgColor.add("rgba(101, 202, 255, 0.6)");
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

        if (map != null) {
            for (String s : map.keySet()) {
                values.add(map.getOrDefault(s, 0));
                labels.add(s);
            }
        }

        hbarDataSet.setData(values);
        data.setLabels(labels);
        eventsByIdentHbarModel.setData(data);

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
        title.setText("Events by ident in last 4 hours");
        options.setTitle(title);

        eventsByIdentHbarModel.setOptions(options);
    }
}
