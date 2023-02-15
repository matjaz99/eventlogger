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
package si.matjazcerkvenik.eventlogger.model;

import java.util.Arrays;

public class DFilter {

    private String sort;
    private int limit;
    private boolean ascending = false;
    private String[] hosts;
    private String[] idents;
    private String searchType;
    private String searchPattern;
    private long fromTimestamp;
    private long toTimestamp;


    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public String[] getIdents() {
        return idents;
    }

    public void setIdents(String[] idents) {
        this.idents = idents;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public long getFromTimestamp() {
        return fromTimestamp;
    }

    public void setFromTimestamp(long fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    public long getToTimestamp() {
        return toTimestamp;
    }

    public void setToTimestamp(long toTimestamp) {
        this.toTimestamp = toTimestamp;
    }

    @Override
    public String toString() {
        return "DFilter{" +
                "sort='" + sort + '\'' +
                ", limit=" + limit +
                ", hosts=[" + Arrays.toString(hosts) + "]" +
                ", ident=[" + Arrays.toString(idents) + "]" +
                ", searchType='" + searchType + '\'' +
                ", searchPattern='" + searchPattern + '\'' +
                ", fromTimestamp=" + fromTimestamp +
                ", toTimestamp=" + toTimestamp +
                '}';
    }
}
